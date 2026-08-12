import api from "./api";
import type { CartItem } from "../types/CartItem";
import type { CartItemDTO } from "../types/CartItemDTO";

class CartItemService {

    async addToCart(request: CartItemDTO): Promise<CartItem> {

        const response = await api.post<CartItem>(
            "/cart-item/add",
            request
        );

        return response.data;
    }

    async getMyCartItems(): Promise<CartItem[]> {

        const response = await api.get<CartItem[]>(
            "/cart-item/me"
        );

        return response.data;
    }

    async updateCartItem(
        cartItemId: number,
        quantity: number
    ): Promise<CartItem> {

        const response = await api.put<CartItem>(
            `/cart-item/update/${cartItemId}?quantity=${quantity}`
        );

        return response.data;
    }

    async deleteCartItem(cartItemId: number): Promise<void> {

        await api.delete(`/cart-item/${cartItemId}`);

    }

}

export default new CartItemService();