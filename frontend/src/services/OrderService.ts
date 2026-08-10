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


export interface OrderItemResponse {
    id: number;

    product: {
        id: number;
        name: string;
        price: number;
        description: string;
        stock: number;
        category: string;
        imageUrl: string;
    };

    quantity: number;

    // Price at the time the order was placed
    price: number;
}


export interface OrderDetailsResponse {
    id: number;

    user: {
        id: number;
        name: string;
        email: string;
    };

    totalAmount: number;
    status: string;
    createdAt: string;

    items: OrderItemResponse[];
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


    async getOrderDetails(
        orderId: number
    ): Promise<OrderDetailsResponse> {

        const response =
            await api.get<OrderDetailsResponse>(
                `/order/${orderId}`
            );

        return response.data;
    }
}


export default new OrderService();