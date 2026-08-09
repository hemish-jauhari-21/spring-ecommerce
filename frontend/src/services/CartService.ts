import api from "./api";

export interface Cart {
    id: number;
    totalAmount: number;
    user?: {
        id: number;
        name: string;
        email: string;
    };
}

class CartService {

    async getCartByUserId(userId: number): Promise<Cart> {

        const response = await api.get<Cart>(
            `/cart/user/${userId}`
        );

        return response.data;
    }

}

export default new CartService();