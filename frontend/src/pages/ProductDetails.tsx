import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import ProductService from "../services/ProductService";
import { getErrorMessage, notifyError } from "../services/api";
import type { Product } from "../types/Product";
import { useAuth } from "../hooks/useAuth";
import CartItemService from "../services/CartItemService";
import { toast } from "react-toastify";

function ProductDetails() {

    const { id } = useParams();

    const navigate = useNavigate();

    const [product, setProduct] = useState<Product | null>(null);

    const [loading, setLoading] = useState(true);

    const [error, setError] = useState("");

    const { user } = useAuth();

    const [quantity, setQuantity] = useState(1);

    const [addingToCart, setAddingToCart] = useState(false);

    const [imgError, setImgError] = useState(false);


    // Add product to cart
    const handleAddToCart = async () => {

        if (!user) {
            toast.warning("Please login first.");
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

            toast.success("Product added to cart successfully.");

        } catch (error) {

            notifyError(error, "Failed to add product to cart.");

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

                setProduct(data);

                // Quantity always starts at 1
                setQuantity(1);

            } catch (error) {

                setError(
                    getErrorMessage(
                        error,
                        "Unable to load product."
                    )
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
            <div className="container mt-4">
                <div className="pd-loading">
                    <div className="spinner-border" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </div>
                    <span>Loading product...</span>
                </div>
            </div>
        );

    }


    // Error state
    if (error || !product) {

        return (
            <div className="container mt-4">
                <div className="pd-error">
                    <p>{error || "Product not found."}</p>
                    <button
                        className="btn btn-primary"
                        onClick={() => navigate("/products")}
                    >
                        Back to Products
                    </button>
                </div>
            </div>
        );

    }


    return (

        <div className="container mt-4">

            <div className="row g-4">

                {/* Product Image */}

                <div className="col-md-6">

                    <div className="pd-image-wrap">
                        {product.image_url && !imgError ? (
                            <img
                                src={product.image_url}
                                alt={product.name}
                                onError={() => setImgError(true)}
                            />
                        ) : (
                            <span className="pd-no-image">No Image Available</span>
                        )}
                    </div>

                </div>


                {/* Product Information */}

                <div className="col-md-6">

                    {/* Category */}
                    <span className="pd-category">{product.category}</span>

                    {/* Name */}
                    <h1 className="pd-name">{product.name}</h1>

                    {/* Price */}
                    <div className="pd-price">₹ {product.price}</div>

                    {/* Description */}
                    <p className="pd-description">{product.description}</p>

                    {/* Stock availability */}
                    <p className={`pd-stock ${product.stock > 0 ? "in-stock" : "out-of-stock"}`}>
                        {product.stock > 0
                            ? `In Stock — ${product.stock} available`
                            : "Out of Stock"
                        }
                    </p>

                    {/* Quantity + Add to Cart */}
                    {product.stock > 0 ? (

                        <div className="pd-qty">
                            <span className="pd-qty-label">Qty</span>
                            <div className="pd-qty-controls">
                                <button
                                    type="button"
                                    onClick={() =>
                                        setQuantity(prev =>
                                            Math.max(1, prev - 1)
                                        )
                                    }
                                    disabled={quantity <= 1}
                                    aria-label="Decrease quantity"
                                >
                                    −
                                </button>
                                <span className="pd-qty-display">{quantity}</span>
                                <button
                                    type="button"
                                    onClick={() =>
                                        setQuantity(prev =>
                                            Math.min(product.stock, prev + 1)
                                        )
                                    }
                                    disabled={quantity >= product.stock}
                                    aria-label="Increase quantity"
                                >
                                    +
                                </button>
                            </div>
                        </div>

                    ) : null}


                    <div className="pd-actions">

                        {product.stock > 0 ? (
                            <button
                                type="button"
                                className="btn btn-primary"
                                onClick={handleAddToCart}
                                disabled={addingToCart}
                            >
                                {addingToCart ? "Adding..." : "Add to Cart"}
                            </button>
                        ) : (
                            <button
                                className="btn btn-primary"
                                disabled
                            >
                                Out of Stock
                            </button>
                        )}

                        <button
                            type="button"
                            className="btn btn-outline-secondary"
                            onClick={() => navigate("/products")}
                        >
                            Back to Products
                        </button>

                    </div>

                </div>

            </div>

        </div>

    );

}

export default ProductDetails;
