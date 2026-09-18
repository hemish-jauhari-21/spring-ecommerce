import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";

import type { Product } from "../types/Product";
import { useAuth } from "../hooks/useAuth";
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
            className="product-card"
            onClick={handleCardClick}
        >
            <div className="product-card-image-wrap">
                {product.image_url && !imgError ? (
                    <img
                        src={product.image_url}
                        alt={product.name}
                        onError={() => setImgError(true)}
                    />
                ) : (
                    <span className="product-card-no-image">No Image</span>
                )}
                {product.category && (
                    <span className="product-card-category">{product.category}</span>
                )}
            </div>

            <div className="product-card-body">
                <h5 className="product-card-name">{product.name}</h5>
                <p className="product-card-description">{product.description}</p>
                <div className="product-card-price">₹ {product.price}</div>
                <p className={`product-card-stock ${outOfStock ? "out-of-stock" : "in-stock"}`}>
                    {outOfStock ? "Out of Stock" : `In Stock: ${product.stock}`}
                </p>
                <button
                    className="btn btn-primary product-card-btn"
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
