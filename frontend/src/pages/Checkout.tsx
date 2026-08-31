import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { useAuth } from "../context/AuthContext";
import CartService from "../services/CartService";
import CartItemService from "../services/CartItemService";
import OrderService from "../services/OrderService";
import { getErrorMessage, notifyError } from "../services/api";
import { toast } from "react-toastify";

import type { CartItem } from "../types/CartItem";

function Checkout() {

    const { user } = useAuth();

    const navigate = useNavigate();

    const [cartItems, setCartItems] =
        useState<CartItem[]>([]);

    // Authoritative total loaded from the
    // backend CartResponseDTO.
    const [serverTotal, setServerTotal] =
        useState<number | null>(null);

    const [loading, setLoading] =
        useState(true);

    const [placingOrder, setPlacingOrder] =
        useState(false);

    const [error, setError] =
        useState("");


    useEffect(() => {

        if (!user) {
            return;
        }

        const loadCheckout = async () => {

            try {

                const cart =
                    await CartService.getMyCart();

                setServerTotal(cart.totalAmount);

                const items =
                    await CartItemService.getMyCartItems();

                setCartItems(items);

            } catch (error) {

                setError(
                    getErrorMessage(
                        error,
                        "Unable to load your cart."
                    )
                );

            } finally {

                setLoading(false);

            }

        };

        loadCheckout();

    }, [user]);


    // Client-side subtotal is display-only.
    // The order total is always calculated by the backend.
    const displayTotal =
        serverTotal ??
        cartItems.reduce(
            (total, item) =>
                total +
                item.product.price *
                item.quantity,
            0
        );


    const handlePlaceOrder = async () => {

        if (!user) {

            navigate("/login");

            return;
        }

        try {

            setPlacingOrder(true);

            const order =
                await OrderService.placeOrder();

            toast.success(
                `Order placed successfully! Order ID: ${order.id}`
            );

            navigate(`/orders/${order.id}`);

        } catch (error) {

            notifyError(
                error,
                "Failed to place order. Please check stock and try again."
            );

        } finally {

            setPlacingOrder(false);

        }
    };


    if (!user) {

        return (

            <div className="container mt-5">

                <div className="alert alert-warning">

                    Please login to continue.

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


    if (loading) {

        return (

            <div className="container mt-5">

                <h3>
                    Loading checkout...
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


    if (cartItems.length === 0) {

        return (

            <div className="container mt-5">

                <div className="alert alert-info">

                    Your cart is empty.

                </div>

                <button
                    className="btn btn-primary"
                    onClick={() =>
                        navigate("/products")
                    }
                >
                    Continue Shopping
                </button>

            </div>

        );

    }


    return (

        <div className="container mt-5">

            <h2>
                Checkout
            </h2>

            <hr />


            <h4 className="mb-3">
                Order Summary
            </h4>


            <table className="table table-bordered">

                <thead>

                    <tr>

                        <th>
                            Image
                        </th>

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

                    {cartItems.map(item => (

                        <tr key={item.id}>

                            <td>

                                {item.product.image_url && (

                                    <img
                                        src={item.product.image_url}
                                        alt={item.product.name}
                                        style={{
                                            width: "50px",
                                            height: "50px",
                                            objectFit: "contain"
                                        }}
                                    />

                                )}

                            </td>

                            <td>
                                {item.product.name}
                            </td>

                            <td>
                                ₹ {item.product.price}
                            </td>

                            <td>
                                {item.quantity}
                            </td>

                            <td>
                                ₹ {
                                    item.product.price *
                                    item.quantity
                                }
                            </td>

                        </tr>

                    ))}

                </tbody>

            </table>


            <div className="text-end">

                {/* Server-provided authoritative total */}

                <h3>
                    Total: ₹ {displayTotal}
                </h3>


                <button
                    className="btn btn-success btn-lg mt-3"
                    onClick={handlePlaceOrder}
                    disabled={placingOrder}
                >

                    {placingOrder
                        ? "Placing Order..."
                        : "Place Order"}

                </button>

            </div>

        </div>
    );
}

export default Checkout;
