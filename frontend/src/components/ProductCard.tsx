import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";

import type { Product } from "../types/Product";
import { useAuth } from "../context/AuthContext";
import CartItemService from "../services/CartItemService";
import { notifyError } from "../services/api";

type ProductCardProps = {
    product: Product;
};

function ProductCard({ product }: ProductCardProps) {

    const navigate = useNavigate();
    const { user } = useAuth();
    const [addingToCart, setAddingToCart] = useState(false);
    const [imgError, setImgError] = useState(false);

    const handleCardClick = () => {
        navigate(`/products/${product.id}`);
    };

    const handleAddToCart = async (e: React.MouseEvent) => {
        e.stopPropagation();

        if (!user) {
            toast.warning("Please login first.");
            navigate("/login");
            return;
        }

        try {
            setAddingToCart(true);
            await CartItemService.addToCart({
                productId: product.id,
                quantity: 1,
            });
            toast.success("Product added to cart successfully.");
        } catch (error) {
            notifyError(error, "Failed to add product to cart.");
        } finally {
            setAddingToCart(false);
        }
    };

    const outOfStock = product.stock <= 0;

    return (
        <div
            className="card h-100 shadow-sm"
            style={{ cursor: "pointer" }}
            onClick={handleCardClick}
        >
            {product.image_url && !imgError ? (
                <img
                    src={product.image_url}
                    className="card-img-top"
                    alt={product.name}
                    style={{ height: "220px", objectFit: "cover" }}
                    onError={() => setImgError(true)}
                />
            ) : (
                <div
                    className="card-img-top d-flex align-items-center justify-content-center bg-light text-muted"
                    style={{ height: "220px" }}
                >
                    No Image
                </div>
            )}

            <div className="card-body d-flex flex-column">
                <h5 className="card-title">{product.name}</h5>
                <p className="card-text text-muted">{product.description}</p>
                <h4 className="text-success">₹ {product.price}</h4>
                <p>
                    {outOfStock ? (
                        <span className="text-danger fw-bold">Out of Stock</span>
                    ) : (
                        <>Stock: {product.stock}</>
                    )}
                </p>
                <button
                    className="btn btn-primary mt-auto"
                    disabled={outOfStock || addingToCart}
                    onClick={handleAddToCart}
                >
                    {addingToCart ? "Adding..." : outOfStock ? "Out of Stock" : "Add to Cart"}
                </button>
            </div>
        </div>
    );
}

export default ProductCard;
