import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { useAuth } from "../context/AuthContext";
import OrderService from "../services/OrderService";
import type { OrderResponse } from "../services/OrderService";

function Orders() {

    const { user } = useAuth();

    const navigate = useNavigate();

    const [orders, setOrders] =
        useState<OrderResponse[]>([]);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState("");


    useEffect(() => {

        if (!user) {
            return;
        }

        const loadOrders = async () => {

            try {

                const data =
                    await OrderService.getOrdersByUser(
                        user.id
                    );

                console.log(
                    "Orders received:",
                    data
                );

                setOrders(data);

            } catch (error) {

                console.error(
                    "Error loading orders:",
                    error
                );

                setError(
                    "Unable to load orders."
                );

            } finally {

                setLoading(false);

            }

        };

        loadOrders();

    }, [user]);


    // Not logged in
    if (!user) {

        return (

            <div className="container mt-5">

                <div className="alert alert-warning">

                    Please login to view your orders.

                </div>

                <button
                    className="btn btn-primary"
                    onClick={() =>
                        navigate("/login")
                    }
                >
                    Login
                </button>

            </div>

        );

    }


    // Loading
    if (loading) {

        return (

            <div className="container mt-5">

                <h3>
                    Loading orders...
                </h3>

            </div>

        );

    }


    // Error
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

        <div className="container mt-5">

            <h2>
                My Orders
            </h2>

            <hr />


            {orders.length === 0 ? (

                <div className="alert alert-info">

                    You haven't placed any orders yet.

                </div>

            ) : (

                <div className="table-responsive">

                    <table className="table table-bordered table-hover">

                        <thead className="table-dark">

                            <tr>

                                <th>
                                    Order ID
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

                            </tr>

                        </thead>


                        <tbody>

                            {orders.map(order => (

                                <tr key={order.id}>

                                    <td>
                                        #{order.id}
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

                                        <span className="badge bg-warning text-dark">

                                            {order.status}

                                        </span>

                                    </td>

                                </tr>

                            ))}

                        </tbody>

                    </table>

                </div>

            )}

        </div>

    );

}

export default Orders;