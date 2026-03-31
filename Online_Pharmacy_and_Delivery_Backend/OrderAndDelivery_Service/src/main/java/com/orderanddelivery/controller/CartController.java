package com.orderanddelivery.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.orderanddelivery.config.JwtUserPrincipal;
import com.orderanddelivery.requestDTO.AddToCartRequest;
import com.orderanddelivery.responseDTO.CartResponse;
import com.orderanddelivery.service.CartService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders/cart")
@RequiredArgsConstructor
@Validated
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal JwtUserPrincipal principal) {
        return ResponseEntity.ok(cartService.getCart(getUserId(principal)));
    }

    @PostMapping
    public ResponseEntity<?> addToCart(@Valid @RequestBody AddToCartRequest request,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addToCart(getUserId(principal), request));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<Void> updateQuantity(
            @PathVariable Long itemId,
            @RequestParam @Min(value = 0, message = "Quantity cannot be negative") @Max(value = 10, message = "Max quantity is 10") int quantity,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        cartService.updateQuantity(getUserId(principal), itemId, quantity);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        cartService.removeFromCart(getUserId(principal), itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal JwtUserPrincipal principal) {
        cartService.clearCart(getUserId(principal));
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(JwtUserPrincipal principal) {
        if (principal == null || principal.userId() == null) {
            throw new IllegalArgumentException("Authenticated user information is missing");
        }
        return principal.userId();
    }
}
