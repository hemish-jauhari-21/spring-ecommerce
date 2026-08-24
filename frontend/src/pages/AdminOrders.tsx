import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";

import OrderService from "../services/OrderService";
import { notifyError } from "../services/api";
import type { OrderResponse } from "../services/OrderService";
import {
    ALLOWED_TRANSITIONS,
    type OrderStatus
} from "../types/OrderStatus";
import OrderStatusBadge from "../components/OrderStatusBadge";

function AdminOrders() {

    const navigate = useNavigate();

    const [orders, setOrders] =
        useState<OrderResponse[]>([]);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState("");

    // Which order is currently being updated
    const [updatingOrderId, setUpdatingOrderId] =
        useState<number | null>(null);


    useEffect(() => {

        const loadOrders = async () => {

            try {

                const data =
                    await OrderService.getAllOrders();

                setOrders(data);

            } catch (err) {

                setError(
                    notifyError(
                        err,
                        "Unable to load orders."
                    )
                );

            } finally {

                setLoading(false);

            }

        };

        loadOrders();

    }, []);


    const handleStatusChange = async (
        orderId: number,
        newStatus: OrderStatus
    ) => {

        try {

            setUpdatingOrderId(orderId);

            const updated =
                await OrderService.updateOrderStatus(
                    orderId,
                    newStatus
                );

            setOrders(prev =>
                prev.map(order =>
                    order.id === updated.id
                        ? updated
                        : order
                )
            );

            toast.success(
                `Order #${orderId} marked as ${newStatus}.`
            );

        } catch (err) {

            notifyError(
                err,
                `Failed to update order #${orderId}.`
            );

        } finally {

            setUpdatingOrderId(null);

        }
    };


    if (loading) {

        return (

            <div className="container mt-5">

                <h3>
                    Loading orders...
                </h3>

            </div>

        );

    }


    if (error) {

        return (

            <div className="container mt-5">

                <div className="alert alert-danger">
                    {error}
                </div>

            </div>

        );

    }


    return (

        <div className="container mt-4">

            <h2>
                Manage Orders
            </h2>

            <hr />


            {orders.length === 0 ? (

                <div className="alert alert-info">
                    No orders have been placed yet.
                </div>

            ) : (

                <div className="table-responsive">

                    <table className="table table-bordered table-hover align-middle">

                        <thead className="table-dark">

                            <tr>

                                <th>
                                    Order ID
                                </th>

                                <th>
                                    Customer
                                </th>

                                <th>
                                    Date
                                </th>

                                <th>
                                    Total Amount
                                </th>

                                <th>
                                    Status
                                </th>

                                <th>
                                    Change Status
                                </th>

                                <th>
                                    Actions
                                </th>

                            </tr>

                        </thead>


                        <tbody>

                            {orders.map(order => {

                                const nextStatuses =
                                    ALLOWED_TRANSITIONS[order.status] ?? [];

                                return (

                                    <tr key={order.id}>

                                        <td>
                                            #{order.id}
                                        </td>

                                        <td>
                                            {order.user.name}
                                            <br />
                                            <small className="text-muted">
                                                {order.user.email}
                                            </small>
                                        </td>

                                        <td>
                                            {new Date(
                                                order.createdAt
                                            ).toLocaleString()}
                                        </td>

                                        <td>
                                            ₹ {order.totalAmount}
                                        </td>

                                        <td>
                                            <OrderStatusBadge
                                                status={order.status}
                                            />
                                        </td>

                                        <td>
                                            {nextStatuses.length > 0 ? (
                                                nextStatuses.map(next => (

                                                    <button
                                                        key={next}
                                                        className="btn btn-outline-primary btn-sm me-1 mb-1"
                                                        disabled={
                                                            updatingOrderId ===
                                                            order.id
                                                        }
                                                        onClick={() =>
                                                            handleStatusChange(
                                                                order.id,
                                                                next
                                                            )
                                                        }
                                                    >
                                                        {next}
                                                    </button>

                                                ))
                                            ) : (
                                                <small className="text-muted">
                                                    No further changes
                                                </small>
                                            )}
                                        </td>

                                        <td>
                                            <button
                                                className="btn btn-primary btn-sm"
                                                onClick={() =>
                                                    navigate(
                                                        `/orders/${order.id}`
                                                    )
                                                }
                                            >
                                                View Details
                                            </button>
                                        </td>

                                    </tr>

                                );

                            })}

                        </tbody>

                    </table>

                </div>

            )}

        </div>

    );

}

export default AdminOrders;
