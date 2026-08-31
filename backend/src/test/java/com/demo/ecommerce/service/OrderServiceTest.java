package com.demo.ecommerce.service;

import com.demo.ecommerce.exception.BusinessException;
import com.demo.ecommerce.exception.ResourceNotFoundException;
import com.demo.ecommerce.model.*;
import com.demo.ecommerce.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderService orderService;

    private User buildUser(Long id, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setName("User " + id);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    private Product buildProduct(Long id, String name, double price, int stock) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setPrice(price);
        p.setStock(stock);
        return p;
    }

    private Cart buildCart(Long id, User user) {
        Cart cart = new Cart();
        cart.setId(id);
        cart.setUser(user);
        cart.setTotalAmount(0.0);
        return cart;
    }

    private CartItem buildCartItem(Long id, Cart cart, Product product, int quantity) {
        CartItem item = new CartItem();
        item.setId(id);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    private Order buildOrder(Long id, User user, OrderStatus status, double total) {
        Order order = new Order();
        order.setId(id);
        order.setUser(user);
        order.setStatus(status);
        order.setTotalAmount(total);
        return order;
    }

    private Authentication buildAuth(User user) {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        doReturn(authorities).when(auth).getAuthorities();
        return auth;
    }

    private ProductRepository.ProductStockRow buildProductStockRow(Long id, String name, int stock, double price) {
        ProductRepository.ProductStockRow row = mock(ProductRepository.ProductStockRow.class);
        when(row.getId()).thenReturn(id);
        when(row.getName()).thenReturn(name);
        when(row.getStock()).thenReturn(stock);
        when(row.getPrice()).thenReturn(price);
        return row;
    }

    // =====================
    // PLACE ORDER
    // =====================

    @Test
    void placeOrder_success_createsOrderAndReducesStock() {
        User user = buildUser(1L, "user@test.com", Role.USER);
        Authentication auth = buildAuth(user);

        Product product = buildProduct(10L, "Widget", 25.0, 20);
        Cart cart = buildCart(100L, user);
        CartItem cartItem = buildCartItem(200L, cart, product, 3);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(100L)).thenReturn(List.of(cartItem));

        ProductRepository.ProductStockRow stockRow = buildProductStockRow(10L, "Widget", 20, 25.0);
        when(productRepository.findStockForUpdate(anyCollection())).thenReturn(List.of(stockRow));

        Order savedOrder = buildOrder(300L, user, OrderStatus.PENDING, 75.0);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(productRepository.getReferenceById(10L)).thenReturn(product);
        when(productRepository.reduceStock(10L, 3)).thenReturn(1);

        var result = orderService.placeOrder(auth);

        assertNotNull(result);
        assertEquals(75.0, result.getTotalAmount());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(productRepository).reduceStock(10L, 3);
        verify(cartItemRepository).deleteAllInBatch(any());
        verify(cartRepository).save(cart);
    }

    @Test
    void placeOrder_emptyCart_throwsBusinessException() {
        User user = buildUser(1L, "user@test.com", Role.USER);
        Authentication auth = buildAuth(user);

        Cart cart = buildCart(100L, user);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(100L)).thenReturn(Collections.emptyList());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> orderService.placeOrder(auth)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("cart is empty"));
    }

    @Test
    void placeOrder_cartNotFound_throwsResourceNotFoundException() {
        User user = buildUser(1L, "user@test.com", Role.USER);
        Authentication auth = buildAuth(user);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.placeOrder(auth)
        );
    }

    @Test
    void placeOrder_insufficientStock_throwsBusinessException() {
        User user = buildUser(1L, "user@test.com", Role.USER);
        Authentication auth = buildAuth(user);

        Product product = buildProduct(10L, "Widget", 25.0, 2);
        Cart cart = buildCart(100L, user);
        CartItem cartItem = buildCartItem(200L, cart, product, 5);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(100L)).thenReturn(List.of(cartItem));

        ProductRepository.ProductStockRow stockRow = buildProductStockRow(10L, "Widget", 2, 25.0);
        when(productRepository.findStockForUpdate(anyCollection())).thenReturn(List.of(stockRow));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> orderService.placeOrder(auth)
        );

        assertTrue(ex.getMessage().contains("2"));
    }

    @Test
    void placeOrder_outOfStock_throwsBusinessException() {
        User user = buildUser(1L, "user@test.com", Role.USER);
        Authentication auth = buildAuth(user);

        Product product = buildProduct(10L, "Widget", 25.0, 0);
        Cart cart = buildCart(100L, user);
        CartItem cartItem = buildCartItem(200L, cart, product, 1);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(100L)).thenReturn(List.of(cartItem));

        ProductRepository.ProductStockRow stockRow = buildProductStockRow(10L, "Widget", 0, 25.0);
        when(productRepository.findStockForUpdate(anyCollection())).thenReturn(List.of(stockRow));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> orderService.placeOrder(auth)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("out of stock"));
    }

    // =====================
    // ORDER STATUS TRANSITIONS
    // =====================

    @Test
    void updateOrderStatus_pendingToConfirmed_updatesStatus() {
        User admin = buildUser(1L, "admin@test.com", Role.ADMIN);
        Authentication auth = buildAuth(admin);

        Order order = buildOrder(100L, admin, OrderStatus.PENDING, 50.0);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.updateStatusIfCurrent(100L, OrderStatus.PENDING, OrderStatus.CONFIRMED)).thenReturn(1);

        var result = orderService.updateOrderStatus(100L, OrderStatus.CONFIRMED, auth);

        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        verify(orderRepository).updateStatusIfCurrent(100L, OrderStatus.PENDING, OrderStatus.CONFIRMED);
        verify(productRepository, never()).restoreStock(anyLong(), anyInt());
    }

    @Test
    void updateOrderStatus_pendingToCancelled_restoresStock() {
        User admin = buildUser(1L, "admin@test.com", Role.ADMIN);
        Authentication auth = buildAuth(admin);

        Order order = buildOrder(100L, admin, OrderStatus.PENDING, 50.0);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.updateStatusIfCurrent(100L, OrderStatus.PENDING, OrderStatus.CANCELLED)).thenReturn(1);

        Product product = buildProduct(10L, "Widget", 25.0, 20);
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(orderItem));
        when(productRepository.restoreStock(10L, 2)).thenReturn(1);

        var result = orderService.updateOrderStatus(100L, OrderStatus.CANCELLED, auth);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(productRepository).restoreStock(10L, 2);
    }

    @Test
    void updateOrderStatus_invalidTransition_throwsBusinessException() {
        User admin = buildUser(1L, "admin@test.com", Role.ADMIN);
        Authentication auth = buildAuth(admin);

        Order order = buildOrder(100L, admin, OrderStatus.PENDING, 50.0);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> orderService.updateOrderStatus(100L, OrderStatus.DELIVERED, auth)
        );

        assertTrue(ex.getMessage().contains("Cannot change"));
    }

    @Test
    void updateOrderStatus_terminalDelivered_throwsBusinessException() {
        User admin = buildUser(1L, "admin@test.com", Role.ADMIN);
        Authentication auth = buildAuth(admin);

        Order order = buildOrder(100L, admin, OrderStatus.DELIVERED, 50.0);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> orderService.updateOrderStatus(100L, OrderStatus.CANCELLED, auth)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("delivered"));
    }

    @Test
    void updateOrderStatus_terminalCancelled_throwsBusinessException() {
        User admin = buildUser(1L, "admin@test.com", Role.ADMIN);
        Authentication auth = buildAuth(admin);

        Order order = buildOrder(100L, admin, OrderStatus.CANCELLED, 50.0);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> orderService.updateOrderStatus(100L, OrderStatus.CONFIRMED, auth)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("cancelled"));
    }

    @Test
    void updateOrderStatus_notAdmin_throwsAccessDeniedException() {
        User user = buildUser(1L, "user@test.com", Role.USER);
        Authentication auth = buildAuth(user);

        assertThrows(
                AccessDeniedException.class,
                () -> orderService.updateOrderStatus(100L, OrderStatus.CONFIRMED, auth)
        );
    }

    @Test
    void updateOrderStatus_orderNotFound_throwsResourceNotFoundException() {
        User admin = buildUser(1L, "admin@test.com", Role.ADMIN);
        Authentication auth = buildAuth(admin);

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.updateOrderStatus(999L, OrderStatus.CONFIRMED, auth)
        );
    }

    // =====================
    // ORDER ACCESS CONTROL
    // =====================

    @Test
    void getOrderDetails_otherUserOrder_throwsAccessDeniedException() {
        User owner = buildUser(1L, "owner@test.com", Role.USER);
        User otherUser = buildUser(2L, "other@test.com", Role.USER);
        Authentication auth = buildAuth(otherUser);

        Order order = buildOrder(100L, owner, OrderStatus.PENDING, 50.0);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThrows(
                AccessDeniedException.class,
                () -> orderService.getOrderDetails(100L, auth)
        );
    }

    @Test
    void getOrderDetails_ownerCanAccess() {
        User user = buildUser(1L, "user@test.com", Role.USER);
        Authentication auth = buildAuth(user);

        Order order = buildOrder(100L, user, OrderStatus.PENDING, 50.0);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(100L)).thenReturn(Collections.emptyList());

        var result = orderService.getOrderDetails(100L, auth);

        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

    @Test
    void getOrderDetails_adminCanAccessAnyOrder() {
        User owner = buildUser(1L, "owner@test.com", Role.USER);
        User admin = buildUser(2L, "admin@test.com", Role.ADMIN);
        Authentication auth = buildAuth(admin);

        Order order = buildOrder(100L, owner, OrderStatus.PENDING, 50.0);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(100L)).thenReturn(Collections.emptyList());

        var result = orderService.getOrderDetails(100L, auth);

        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

    @Test
    void getAllOrders_notAdmin_throwsAccessDeniedException() {
        User user = buildUser(1L, "user@test.com", Role.USER);
        Authentication auth = buildAuth(user);

        assertThrows(
                AccessDeniedException.class,
                () -> orderService.getAllOrders(auth)
        );
    }

    @Test
    void placeOrder_concurrentStockChange_throwsBusinessException() {
        User user = buildUser(1L, "user@test.com", Role.USER);
        Authentication auth = buildAuth(user);

        Product product = buildProduct(10L, "Widget", 25.0, 5);
        Cart cart = buildCart(100L, user);
        CartItem cartItem = buildCartItem(200L, cart, product, 3);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(100L)).thenReturn(List.of(cartItem));

        ProductRepository.ProductStockRow stockRow = buildProductStockRow(10L, "Widget", 5, 25.0);
        when(productRepository.findStockForUpdate(anyCollection())).thenReturn(List.of(stockRow));

        Order savedOrder = buildOrder(300L, user, OrderStatus.PENDING, 75.0);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(productRepository.getReferenceById(10L)).thenReturn(product);
        when(productRepository.reduceStock(10L, 3)).thenReturn(0);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> orderService.placeOrder(auth)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("insufficient stock"));
    }
}
