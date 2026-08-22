import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { useAuth } from "../context/AuthContext";
import CartService from "../services/CartService";
import type { Cart as CartSummary } from "../services/CartService";
import CartItemService from "../services/CartItemService";
import { getErrorMessage, notifyError } from "../services/api";

import type { CartItem } from "../types/CartItem";

function Cart() {

    const { user } = useAuth();
    const navigate = useNavigate();

    const [cartItems, setCartItems] = useState<CartItem[]>([]);
    const [cart, setCart] = useState<CartSummary | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [busyItemId, setBusyItemId] = useState<number | null>(null);

    // Reload items + authoritative server total.
    // The displayed total always comes from the
    // backend CartResponseDTO, never from a
    // client-side calculation.
    const refreshCart = useCallback(async () => {
        const updatedItems =
            await CartItemService.getMyCartItems();

        setCartItems(updatedItems);

        if (updatedItems.length > 0) {
            const updatedCart =
                await CartService.getMyCart();

            setCart(updatedCart);
        }
    }, []);

    useEffect(() => {

        if (!user) {
            return;
        }

        const loadCart = async () => {

            try {

                const myCart =
                    await CartService.getMyCart();

                setCart(myCart);

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

        loadCart();

    }, [user]);

    // Update quantity through the backend.
    // The server validates ownership and stock
    // and recalculates the cart total.
    const handleUpdateQuantity = async (
        item: CartItem,
        newQuantity: number
    ) => {

        try {

            setBusyItemId(item.id);

            await CartItemService.updateCartItem(
                item.id,
                newQuantity
            );

            await refreshCart();

        } catch (error) {

            notifyError(error, "Unable to update cart.");

        } finally {

            setBusyItemId(null);

        }

    };

    // Delete through the backend, then reload so the
    // server total stays correct (no stale totals).
    const handleRemoveItem = async (item: CartItem) => {

        try {

            setBusyItemId(item.id);

            await CartItemService.deleteCartItem(item.id);

            await refreshCart();

        } catch (error) {

            notifyError(
                error,
                "Unable to remove item."
            );

        } finally {

            setBusyItemId(null);

        }

    };

    // User is not logged in
    if (!user) {

        return (

            <div className="container mt-5">

                <div className="alert alert-warning">

                    Please login to view your cart.

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
                    Loading cart...
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

            <div className="d-flex justify-content-between align-items-center">

                <h2>
                    My Cart
                </h2>

                {cart && (
                    <span className="text-muted">
                        Cart ID: {cart.id}
                    </span>
                )}

            </div>

            <hr />


            {cartItems.length === 0 ? (

                <div className="alert alert-info">

                    Your cart is empty.

                </div>

            ) : (

                <>

                    <table className="table table-bordered align-middle">

                        <thead>

                            <tr>

                                <th>
                                    Product
                                </th>

                                <th>
                                    Price
                                </th>

                                <th>
                                    Availability
                                </th>

                                <th>
                                    Quantity
                                </th>

                                <th>
                                    Subtotal
                                </th>

                                <th>
                                    Action
                                </th>

                            </tr>

                        </thead>


                        <tbody>

                            {cartItems.map(item => {

                                const maxQuantity =
                                    item.product.stock;

                                return (

                                    <tr key={item.id}>

                                        <td>

                                            {item.product.name}

                                        </td>

                                        <td>

                                            ₹ {item.product.price}

                                        </td>

                                        <td>

                                            {item.product.stock > 0 ? (

                                                <span className="text-success">
                                                    Available: {item.product.stock}
                                                </span>

                                            ) : (

                                                <span className="text-danger">
                                                    Out of Stock
                                                </span>

                                            )}

                                        </td>

                                        <td>

                                            <div className="d-flex align-items-center">

                                                {/* Decrease - never below 1 */}

                                                <button
                                                    type="button"
                                                    className="btn btn-outline-secondary btn-sm"
                                                    aria-label={`Decrease quantity of ${item.product.name}`}
                                                    onClick={() =>
                                                        handleUpdateQuantity(
                                                            item,
                                                            item.quantity - 1
                                                        )
                                                    }
                                                    disabled={
                                                        busyItemId === item.id ||
                                                        item.quantity <= 1
                                                    }
                                                >
                                                    -
                                                </button>


                                                {/* Quantity */}

                                                <span className="mx-3 fw-bold">

                                                    {item.quantity}

                                                </span>


                                                {/* Increase - never above available stock */}

                                                <button
                                                    type="button"
                                                    className="btn btn-outline-secondary btn-sm"
                                                    aria-label={`Increase quantity of ${item.product.name}`}
                                                    onClick={() =>
                                                        handleUpdateQuantity(
                                                            item,
                                                            item.quantity + 1
                                                        )
                                                    }
                                                    disabled={
                                                        busyItemId === item.id ||
                                                        maxQuantity === null ||
                                                        item.quantity >= maxQuantity
                                                    }
                                                >
                                                    +
                                                </button>

                                            </div>

                                        </td>

                                        <td>

                                            ₹ {
                                                item.product.price *
                                                item.quantity
                                            }

                                        </td>

                                        <td>

                                            <button
                                                className="btn btn-danger btn-sm"
                                                onClick={() => handleRemoveItem(item)}
                                                disabled={busyItemId === item.id}
                                            >

                                                Remove

                                            </button>

                                        </td>

                                    </tr>

                                );

                            })}

                        </tbody>

                    </table>


                    {/* Authoritative total from the backend */}

                    <div className="text-end">

                        <h4>

                            Total: ₹ {cart?.totalAmount ?? 0}

                        </h4>

                        <p className="text-muted small">
                            Total calculated by the server.
                        </p>

                        <button
                            className="btn btn-success mt-2"
                            onClick={() =>
                                navigate("/checkout")
                            }
                        >
                            Proceed to Checkout
                        </button>

                    </div>

                </>

            )}

        </div>

    );

}

export default Cart;
