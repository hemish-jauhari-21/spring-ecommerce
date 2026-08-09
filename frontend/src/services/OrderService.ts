import api from "./api";

export interface OrderResponse {
    id: number;

    user: {
        id: number;
        name: string;
        email: string;
    };

    totalAmount: number;
    status: string;
    createdAt: string;
}

class OrderService {

    async placeOrder(
        userId: number
    ): Promise<OrderResponse> {

        const response =
            await api.post<OrderResponse>(
                "/order/place",
                {
                    userId: userId
                }
            );

        return response.data;
    }


    async getOrdersByUser(
        userId: number
    ): Promise<OrderResponse[]> {

        const response =
            await api.get<OrderResponse[]>(
                `/order/user/${userId}`
            );

        return response.data;
    }
}

export default new OrderService();