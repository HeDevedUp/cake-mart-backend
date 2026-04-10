package com.cakemart.cakemartbackend.service;

import com.cakemart.cakemartbackend.dto.OrderDto;
import com.cakemart.cakemartbackend.dto.OrderItemDto;
import com.cakemart.cakemartbackend.model.Cart;
import com.cakemart.cakemartbackend.model.CartItem;
import com.cakemart.cakemartbackend.model.Order;
import com.cakemart.cakemartbackend.model.OrderItem;
import com.cakemart.cakemartbackend.model.OrderStatus;
import com.cakemart.cakemartbackend.model.Product;
import com.cakemart.cakemartbackend.model.User;
import com.cakemart.cakemartbackend.repository.CartRepository;
import com.cakemart.cakemartbackend.repository.OrderRepository;
import com.cakemart.cakemartbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
public class OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderService(CartRepository cartRepository,
                        OrderRepository orderRepository,
                        UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderDto createOrderFromCart(String userEmail) {
        User user = requireUser(userEmail);

        Cart cart = cartRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(0.0)
                .build();

        double total = 0.0;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            double price = product.getPrice() == null ? 0.0 : product.getPrice().doubleValue();
            int qty = cartItem.getQuantity();

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(qty)
                    .price(price)
                    .build();

            order.getItems().add(orderItem);
            total += price * qty;
        }

        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        // Clear cart after order creation
        cart.getItems().clear(); // orphanRemoval=true deletes CartItem rows
        cartRepository.save(cart);

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getUserOrders(String userEmail) {
        requireUser(userEmail);
        return orderRepository.findByUserEmail(userEmail).stream()
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toDto)
                .toList();
    }

    private User requireUser(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return user;
    }

    private OrderDto toDto(Order order) {
        List<OrderItemDto> items = order.getItems().stream()
                .sorted(Comparator.comparing(OrderItem::getId, Comparator.nullsLast(Long::compareTo)))
                .map(i -> new OrderItemDto(
                        i.getId(),
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getPrice()
                ))
                .toList();

        return new OrderDto(
                order.getId(),
                order.getUser().getEmail(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                items
        );
    }
}

