import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { useAuth } from "../context/AuthContext";
import CartService from "../services/CartService";
import CartItemService from "../services/CartItemService";
import OrderService from "../services/OrderService";

import type { CartItem } from "../types/CartItem";

function Checkout() {

    const { user } = useAuth();

    const navigate = useNavigate();

    const [cartItems, setCartItems] =
        useState<CartItem[]>([]);

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

    const loadCart = async () => {

        try {

            const cart =
                await CartService.getCartByUserId(user.id);

            const items =
                await CartItemService.getCartItems(cart.id);

            setCartItems(items);

        } catch (error) {

            console.error(
                "Error loading checkout:",
                error
            );

            setError(
                "Unable to load your cart."
            );

        } finally {

            setLoading(false);

        }

    };

    loadCart();

}, [user]);


    const totalAmount =
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
                await OrderService.placeOrder(
                    user.id
                );

            alert(
                `Order placed successfully! Order ID: ${order.id}`
            );

            navigate("/orders");

        } catch (error) {

            console.error(
                "Failed to place order:",
                error
            );

            alert(
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
                            Product
                        </th>

                        <th>
                            Price
                        </th>

                        <th>
                            Quantity
                        </th>

                        <th>
                            Total
                        </th>

                    </tr>

                </thead>


                <tbody>

                    {cartItems.map(item => (

                        <tr key={item.id}>

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

                <h3>
                    Total: ₹ {totalAmount}
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