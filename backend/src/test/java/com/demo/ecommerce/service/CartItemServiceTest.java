package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.CartItemDTO;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CartItemServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartItemService cartItemService;

    private User buildUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setName("User " + id);
        user.setEmail(email);
        user.setRole(Role.USER);
        return user;
    }

    private Product buildProduct(Long id, String name, double price, int stock) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setPrice(price);
        p.setStock(stock);
        p.setCategory("cat");
        p.setDescription("desc");
        return p;
    }

    private Cart buildCart(Long id, User user) {
        Cart cart = new Cart();
        cart.setId(id);
        cart.setUser(user);
        cart.setTotalAmount(0.0);
        return cart;
    }

    private Authentication buildAuth(User user, Role role) {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role.name()));
        doReturn(authorities).when(auth).getAuthorities();

        return auth;
    }

    // --- addItem ---

    @Test
    void addItem_newProduct_addsToCart() {
        User user = buildUser(1L, "user@test.com");
        Authentication auth = buildAuth(user, Role.USER);

        Product product = buildProduct(10L, "Widget", 9.99, 20);
        Cart cart = buildCart(100L, user);

        when(cartService.getOrCreateCart(auth)).thenReturn(cart);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(100L, 10L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> {
            CartItem ci = inv.getArgument(0);
            ci.setId(200L);
            return ci;
        });

        CartItemDTO dto = new CartItemDTO(10L, 3);

        var result = cartItemService.addItem(dto, auth);

        assertNotNull(result);
        assertEquals(3, result.getQuantity());
        verify(cartItemRepository).save(any(CartItem.class));
        verify(cartService).recalculateTotal(cart);
    }

    @Test
    void addItem_existingProductInCart_increasesQuantity() {
        User user = buildUser(1L, "user@test.com");
        Authentication auth = buildAuth(user, Role.USER);

        Product product = buildProduct(10L, "Widget", 9.99, 20);
        Cart cart = buildCart(100L, user);

        CartItem existingItem = new CartItem();
        existingItem.setId(200L);
        existingItem.setCart(cart);
        existingItem.setProduct(product);
        existingItem.setQuantity(2);

        when(cartService.getOrCreateCart(auth)).thenReturn(cart);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(100L, 10L))
                .thenReturn(Optional.of(existingItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        CartItemDTO dto = new CartItemDTO(10L, 3);

        var result = cartItemService.addItem(dto, auth);

        assertEquals(5, result.getQuantity());
        verify(cartService).recalculateTotal(cart);
    }

    @Test
    void addItem_exceedsStock_throwsBusinessException() {
        User user = buildUser(1L, "user@test.com");
        Authentication auth = buildAuth(user, Role.USER);

        Product product = buildProduct(10L, "Widget", 9.99, 2);
        Cart cart = buildCart(100L, user);

        when(cartService.getOrCreateCart(auth)).thenReturn(cart);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(100L, 10L)).thenReturn(Optional.empty());

        CartItemDTO dto = new CartItemDTO(10L, 5);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> cartItemService.addItem(dto, auth)
        );

        assertTrue(ex.getMessage().contains("2"));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addItem_outOfStock_throwsBusinessException() {
        User user = buildUser(1L, "user@test.com");
        Authentication auth = buildAuth(user, Role.USER);

        Product product = buildProduct(10L, "Widget", 9.99, 0);
        Cart cart = buildCart(100L, user);

        when(cartService.getOrCreateCart(auth)).thenReturn(cart);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(100L, 10L)).thenReturn(Optional.empty());

        CartItemDTO dto = new CartItemDTO(10L, 1);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> cartItemService.addItem(dto, auth)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("out of stock"));
    }

    @Test
    void addItem_productNotFound_throwsResourceNotFoundException() {
        User user = buildUser(1L, "user@test.com");
        Authentication auth = buildAuth(user, Role.USER);

        Cart cart = buildCart(100L, user);
        when(cartService.getOrCreateCart(auth)).thenReturn(cart);
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        CartItemDTO dto = new CartItemDTO(999L, 1);

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartItemService.addItem(dto, auth)
        );
    }

    // --- deleteItem ---

    @Test
    void deleteItem_notOwner_throwsAccessDeniedException() {
        User owner = buildUser(2L, "owner@test.com");
        User otherUser = buildUser(3L, "other@test.com");
        Authentication auth = buildAuth(otherUser, Role.USER);

        Product product = buildProduct(10L, "Widget", 9.99, 20);
        Cart ownerCart = buildCart(100L, owner);

        CartItem item = new CartItem();
        item.setId(200L);
        item.setCart(ownerCart);
        item.setProduct(product);
        item.setQuantity(1);

        when(cartItemRepository.findById(200L)).thenReturn(Optional.of(item));

        assertThrows(
                AccessDeniedException.class,
                () -> cartItemService.deleteItem(200L, auth)
        );

        verify(cartItemRepository, never()).deleteById(any());
    }

    @Test
    void deleteItem_ownerItem_deletesSuccessfully() {
        User user = buildUser(1L, "user@test.com");
        Authentication auth = buildAuth(user, Role.USER);

        Product product = buildProduct(10L, "Widget", 9.99, 20);
        Cart cart = buildCart(100L, user);

        CartItem item = new CartItem();
        item.setId(200L);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(1);

        when(cartItemRepository.findById(200L)).thenReturn(Optional.of(item));

        String result = cartItemService.deleteItem(200L, auth);

        assertEquals("Item deleted successfully", result);
        verify(cartItemRepository).deleteById(200L);
        verify(cartService).recalculateTotal(cart);
    }

    @Test
    void deleteItem_notFound_throwsResourceNotFoundException() {
        User user = buildUser(1L, "user@test.com");
        Authentication auth = buildAuth(user, Role.USER);

        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartItemService.deleteItem(999L, auth)
        );
    }

    // --- updateQuantity ---

    @Test
    void updateQuantity_notOwner_throwsAccessDeniedException() {
        User owner = buildUser(2L, "owner@test.com");
        User otherUser = buildUser(3L, "other@test.com");
        Authentication auth = buildAuth(otherUser, Role.USER);

        Product product = buildProduct(10L, "Widget", 9.99, 20);
        Cart ownerCart = buildCart(100L, owner);

        CartItem item = new CartItem();
        item.setId(200L);
        item.setCart(ownerCart);
        item.setProduct(product);
        item.setQuantity(1);

        when(cartItemRepository.findById(200L)).thenReturn(Optional.of(item));

        assertThrows(
                AccessDeniedException.class,
                () -> cartItemService.updateQuantity(200L, 5, auth)
        );
    }

    @Test
    void updateQuantity_exceedsStock_throwsBusinessException() {
        User user = buildUser(1L, "user@test.com");
        Authentication auth = buildAuth(user, Role.USER);

        Product product = buildProduct(10L, "Widget", 9.99, 2);
        Cart cart = buildCart(100L, user);

        CartItem item = new CartItem();
        item.setId(200L);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(1);

        when(cartItemRepository.findById(200L)).thenReturn(Optional.of(item));

        assertThrows(
                BusinessException.class,
                () -> cartItemService.updateQuantity(200L, 10, auth)
        );
    }

    @Test
    void updateQuantity_validQuantity_updatesSuccessfully() {
        User user = buildUser(1L, "user@test.com");
        Authentication auth = buildAuth(user, Role.USER);

        Product product = buildProduct(10L, "Widget", 9.99, 20);
        Cart cart = buildCart(100L, user);

        CartItem item = new CartItem();
        item.setId(200L);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(1);

        when(cartItemRepository.findById(200L)).thenReturn(Optional.of(item));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = cartItemService.updateQuantity(200L, 5, auth);

        assertEquals(5, result.getQuantity());
        verify(cartService).recalculateTotal(cart);
    }

    @Test
    void updateQuantity_invalidQuantity_throwsBusinessException() {
        User user = buildUser(1L, "user@test.com");
        Authentication auth = buildAuth(user, Role.USER);

        assertThrows(
                BusinessException.class,
                () -> cartItemService.updateQuantity(200L, 0, auth)
        );
    }

    @Test
    void updateQuantity_nullQuantity_throwsBusinessException() {
        User user = buildUser(1L, "user@test.com");
        Authentication auth = buildAuth(user, Role.USER);

        assertThrows(
                BusinessException.class,
                () -> cartItemService.updateQuantity(200L, null, auth)
        );
    }
}
