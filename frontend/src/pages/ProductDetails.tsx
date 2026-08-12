import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import ProductService from "../services/ProductService";
import type { Product } from "../types/Product";
import { useAuth } from "../context/AuthContext";
import CartItemService from "../services/CartItemService";

function ProductDetails() {

    const { id } = useParams();

    const navigate = useNavigate();

    const [product, setProduct] = useState<Product | null>(null);

    const [loading, setLoading] = useState(true);

    const [error, setError] = useState("");

    const { user } = useAuth();

    const [quantity, setQuantity] = useState(1);

    const [addingToCart, setAddingToCart] = useState(false);


    // Add product to cart
    const handleAddToCart = async () => {

        if (!user) {
            alert("Please login first.");
            navigate("/login");
            return;
        }

        if (!product) {
            return;
        }

        try {

            setAddingToCart(true);

            // Product goes into the logged-in user's cart
            await CartItemService.addToCart({
                productId: product.id,
                quantity: quantity
            });

            alert("Product added to cart successfully.");

        } catch (error) {

            console.error(
                "Failed to add product to cart:",
                error
            );

            alert("Failed to add product to cart.");

        } finally {

            setAddingToCart(false);

        }

    };


    // Load product
    useEffect(() => {

        const fetchProduct = async () => {

            try {

                const data =
                    await ProductService.getProductById(
                        Number(id)
                    );

                console.log(
                    "Product received:",
                    data
                );

                setProduct(data);

            } catch (error) {

                console.error(
                    "Error fetching product:",
                    error
                );

                setError(
                    "Unable to load product."
                );

            } finally {

                setLoading(false);

            }

        };

        fetchProduct();

    }, [id]);


    // Loading state
    if (loading) {

        return (
            <div className="container mt-5">

                <h3>
                    Loading product...
                </h3>

            </div>
        );

    }


    // Error state
    if (error || !product) {

        return (

            <div className="container mt-5">

                <div className="alert alert-danger">

                    {error || "Product not found."}

                </div>

                <button
                    className="btn btn-secondary"
                    onClick={() =>
                        navigate("/products")
                    }
                >
                    Back to Products
                </button>

            </div>

        );

    }


    return (

        <div className="container mt-5">

            <div className="row">

                {/* Product Image */}

                <div className="col-md-6">

                    <img
                        src={product.image_url}
                        alt={product.name}
                        className="img-fluid rounded"
                        style={{
                            maxHeight: "450px",
                            width: "100%",
                            objectFit: "contain"
                        }}
                    />

                </div>


                {/* Product Information */}

                <div className="col-md-6">

                    <h1>
                        {product.name}
                    </h1>

                    <hr />


                    {/* Price */}

                    <h3 className="text-success">

                        ₹ {product.price}

                    </h3>


                    {/* Description */}

                    <p className="mt-3">

                        {product.description}

                    </p>


                    {/* Category */}

                    <p>

                        <strong>
                            Category:
                        </strong>{" "}

                        {product.category}

                    </p>


                    {/* Stock */}

                    <p>

                        <strong>
                            Stock:
                        </strong>{" "}

                        {product.stock}

                    </p>


                    {/* Quantity + Add to Cart */}

                    {product.stock > 0 ? (

                        <>

                            <div className="d-flex align-items-center mb-3">

                                <label className="me-3 fw-bold">

                                    Quantity:

                                </label>


                                {/* Decrease */}

                                <button
                                    type="button"
                                    className="btn btn-outline-secondary"
                                    onClick={() =>
                                        setQuantity(
                                            prev =>
                                                Math.max(
                                                    1,
                                                    prev - 1
                                                )
                                        )
                                    }
                                    disabled={quantity <= 1}
                                >
                                    -
                                </button>


                                {/* Quantity */}

                                <span className="mx-3 fw-bold">

                                    {quantity}

                                </span>


                                {/* Increase */}

                                <button
                                    type="button"
                                    className="btn btn-outline-secondary"
                                    onClick={() =>
                                        setQuantity(
                                            prev =>
                                                Math.min(
                                                    product.stock,
                                                    prev + 1
                                                )
                                        )
                                    }
                                    disabled={
                                        quantity >=
                                        product.stock
                                    }
                                >
                                    +
                                </button>

                            </div>


                            {/* Add to Cart */}

                            <button
                                type="button"
                                className="btn btn-primary me-2"
                                onClick={handleAddToCart}
                                disabled={addingToCart}
                            >

                                {addingToCart
                                    ? "Adding..."
                                    : "Add to Cart"}

                            </button>

                        </>

                    ) : (

                        <button
                            className="btn btn-secondary"
                            disabled
                        >
                            Out of Stock
                        </button>

                    )}


                    {/* Back */}

                    <button
                        type="button"
                        className="btn btn-outline-secondary"
                        onClick={() =>
                            navigate("/products")
                        }
                    >
                        Back to Products
                    </button>

                </div>

            </div>

        </div>

    );

}

export default ProductDetails;