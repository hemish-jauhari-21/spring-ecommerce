import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import OrderService from "../services/OrderService";
import type { OrderDetailsResponse } from "../services/OrderService";
import { getErrorMessage } from "../services/api";
import { useAuth } from "../context/AuthContext";

function OrderDetails() {

    const { id } = useParams();

    const navigate = useNavigate();

    const { user } = useAuth();

    const [order, setOrder] =
        useState<OrderDetailsResponse | null>(null);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState("");


    useEffect(() => {

        if (!user || !id) {
            return;
        }

        const loadOrder = async () => {

            try {

                const data =
                    await OrderService.getOrderDetails(
                        Number(id)
                    );

                setOrder(data);

            } catch (error) {

                setError(
                    getErrorMessage(
                        error,
                        "Unable to load order details."
                    )
                );

            } finally {

                setLoading(false);

            }

        };

        loadOrder();

    }, [user, id]);


    // User is not logged in
    if (!user) {

        return (

            <div className="container mt-5">

                <div className="alert alert-warning">

                    Please login to view your order.

                </div>

                <button
                    className="btn btn-primary"
                    onClick={() => navigate("/login")}
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
                    Loading order...
                </h3>

            </div>

        );

    }


    // Error
    if (error || !order) {

        return (

            <div className="container mt-5">

                <div className="alert alert-danger">

                    {error || "Order not found."}

                </div>

                <button
                    className="btn btn-secondary"
                    onClick={() => navigate("/orders")}
                >
                    Back to Orders
                </button>

            </div>

        );

    }


    return (

        <div className="container mt-5">

            {/* Header */}

            <div className="d-flex justify-content-between align-items-center">

                <h2>
                    Order #{order.id}
                </h2>

                <button
                    className="btn btn-outline-secondary"
                    onClick={() => navigate("/orders")}
                >
                    Back to Orders
                </button>

            </div>

            <hr />


            {/* Order Information */}

            <div className="card mb-4">

                <div className="card-body">

                    <div className="row">

                        <div className="col-md-4">

                            <strong>
                                Order Date
                            </strong>

                            <p>
                                {new Date(
                                    order.createdAt
                                ).toLocaleString()}
                            </p>

                        </div>


                        <div className="col-md-4">

                            <strong>
                                Status
                            </strong>

                            <p>

                                <span className="badge bg-warning text-dark">

                                    {order.status}

                                </span>

                            </p>

                        </div>


                        <div className="col-md-4">

                            <strong>
                                Customer
                            </strong>

                            <p>
                                {order.user.name}
                                <br />
                                {order.user.email}
                            </p>

                        </div>

                    </div>

                </div>

            </div>


            {/* Products */}

            <h4>
                Ordered Products
            </h4>

            <div className="table-responsive">

                <table className="table table-bordered">

                    <thead className="table-dark">

                        <tr>

                            <th>
                                Product
                            </th>

                            <th>
                                Price
                            </th>

                            <th>
                                Quantity
                            </th>

                            <th>
                                Subtotal
                            </th>

                        </tr>

                    </thead>


                    <tbody>

                        {order.items.map(item => (

                            <tr key={item.id}>

                                <td>

                                    <div className="d-flex align-items-center">

                                        {item.product.imageUrl && (

                                            <img
                                                src={
                                                    item.product.imageUrl
                                                }
                                                alt={
                                                    item.product.name
                                                }
                                                style={{
                                                    width: "70px",
                                                    height: "70px",
                                                    objectFit: "contain",
                                                    marginRight: "15px"
                                                }}
                                            />

                                        )}

                                        <div>

                                            <strong>
                                                {item.product.name}
                                            </strong>

                                            <br />

                                            <small className="text-muted">

                                                {item.product.category}

                                            </small>

                                        </div>

                                    </div>

                                </td>


                                <td>

                                    ₹ {item.price}

                                </td>


                                <td>

                                    {item.quantity}

                                </td>


                                <td>

                                    ₹ {
                                        item.price *
                                        item.quantity
                                    }

                                </td>

                            </tr>

                        ))}

                    </tbody>


                    <tfoot>

                        <tr>

                            <th
                                colSpan={3}
                                className="text-end"
                            >
                                Total:
                            </th>

                            <th>

                                ₹ {order.totalAmount}

                            </th>

                        </tr>

                    </tfoot>

                </table>

            </div>

        </div>

    );

}

export default OrderDetails;