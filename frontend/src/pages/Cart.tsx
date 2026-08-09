import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { useAuth } from "../context/AuthContext";
import CartService from "../services/CartService";
import CartItemService from "../services/CartItemService";

import type { CartItem } from "../types/CartItem";

function Cart() {

    const { user } = useAuth();
    const navigate = useNavigate();

    const [cartItems, setCartItems] = useState<CartItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const [cartId, setCartId] = useState<number | null>(null);


    useEffect(() => {

    if (!user) {
        return;
    }

    const loadCart = async () => {

        try {

            const cart =
                await CartService.getCartByUserId(user.id);

            setCartId(cart.id);

            const items =
                await CartItemService.getCartItems(cart.id);

            setCartItems(items);

        } catch (error) {

            console.error(
                "Error loading cart:",
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

                <span>
                    Cart ID: {cartId}
                </span>

            </div>

            <hr />


            {cartItems.length === 0 ? (

                <div className="alert alert-info">

                    Your cart is empty.

                </div>

            ) : (

                <>

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

                                <th>
                                    Action
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

                                    <td>

                                        <button
                                            className="btn btn-danger btn-sm"
                                            onClick={async () => {

                                                try {

                                                    await CartItemService.deleteCartItem(
                                                        item.id
                                                    );

                                                    setCartItems(
                                                        prev =>
                                                            prev.filter(
                                                                cartItem =>
                                                                    cartItem.id !==
                                                                    item.id
                                                            )
                                                    );

                                                } catch (error) {

                                                    console.error(
                                                        "Error deleting cart item:",
                                                        error
                                                    );

                                                    alert(
                                                        "Unable to remove item."
                                                    );

                                                }

                                            }}
                                        >
                                            Remove
                                        </button>

                                    </td>

                                </tr>

                            ))}

                        </tbody>

                    </table>


                    {/* Cart total */}

                    <div className="text-end">

                        <h4>

                            Total: ₹ {
                                cartItems.reduce(
                                    (total, item) =>
                                        total +
                                        item.product.price *
                                        item.quantity,
                                    0
                                )
                            }

                        </h4>

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