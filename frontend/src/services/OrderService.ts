import api from "./api";
import type { OrderStatus } from "../types/OrderStatus";

export interface OrderResponse {
    id: number;

    user: {
        id: number;
        name: string;
        email: string;
    };

    totalAmount: number;
    status: OrderStatus;
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
    status: OrderStatus;
    createdAt: string;

    items: OrderItemResponse[];
}


class OrderService {

    async placeOrder(): Promise<OrderResponse> {

        const response =
            await api.post<OrderResponse>(
                "/order/place"
            );

        return response.data;
    }


    async getMyOrders(): Promise<OrderResponse[]> {

        const response =
            await api.get<OrderResponse[]>(
                "/order/me"
            );

        return response.data;
    }


    async getAllOrders(): Promise<OrderResponse[]> {

        const response =
            await api.get<OrderResponse[]>(
                "/order/all"
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


    async updateOrderStatus(
        orderId: number,
        status: OrderStatus
    ): Promise<OrderResponse> {

        const response =
            await api.put<OrderResponse>(
                `/order/${orderId}/status`,
                { status }
            );

        return response.data;
    }
}


export default new OrderService();
