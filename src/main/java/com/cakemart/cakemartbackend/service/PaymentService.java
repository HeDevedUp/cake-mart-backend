package com.cakemart.cakemartbackend.service;

import com.cakemart.cakemartbackend.model.Order;
import com.cakemart.cakemartbackend.model.OrderItem;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PaymentService {

    private final String stripeSecretKey;

    public PaymentService(@Value("${stripe.secret.key}") String stripeSecretKey) {
        this.stripeSecretKey = stripeSecretKey;
    }

    public String createCheckoutSession(Order order) {
        if (stripeSecretKey == null || stripeSecretKey.isBlank() || "YOUR_SECRET_KEY".equals(stripeSecretKey)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Stripe secret key not configured");
        }
        if (order == null || order.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is required");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order has no items");
        }

        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams.Builder params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                // For now: simple placeholders; replace with real frontend URLs later.
                .setSuccessUrl("http://localhost:3000/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:3000/cancel");

        for (OrderItem item : order.getItems()) {
            String name = item.getProduct() == null ? "Product" : item.getProduct().getName();
            long quantity = Math.max(1, item.getQuantity());

            // Stripe expects smallest currency unit (e.g., cents).
            // We store `price` as double in OrderItem; convert with BigDecimal to reduce rounding issues.
            BigDecimal unitAmountDecimal = BigDecimal.valueOf(item.getPrice()).multiply(BigDecimal.valueOf(100));
            long unitAmount = unitAmountDecimal.setScale(0, RoundingMode.HALF_UP).longValue();

            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setQuantity(quantity)
                    .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("usd")
                                    .setUnitAmount(unitAmount)
                                    .setProductData(
                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                    .setName(name)
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            params.addLineItem(lineItem);
        }

        try {
            Session session = Session.create(params.build());
            return session.getUrl();
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Stripe error: " + e.getMessage());
        }
    }
}

