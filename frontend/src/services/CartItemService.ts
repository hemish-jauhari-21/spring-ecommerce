import api from "./api";
import type { CartItem } from "../types/CartItem";
import type { CartItemDTO } from "../types/CartItemDTO";

class CartItemService {
    async addToCart(request: CartItemDTO): Promise<CartItem> {
        const response = await api.post<CartItem>("/cart-item/add", request);
        return response.data;
    }

    async getCartItems(cartId: number): Promise<CartItem[]> {
        const response = await api.get<CartItem[]>(`/cart-item/cart/${cartId}`);

        return response.data;
    }

    async updateCartItem(cartId: number, quantity: number): Promise<CartItem> {
        const response = await api.put<CartItem>(`/cart-item/update/${cartId}?quantity=${quantity}`);
        return response.data;
    }

    async deleteCartItem(cartId: number): Promise<void> {
        await api.delete(`/cart-item/${cartId}`);
    }
}

export default new CartItemService();