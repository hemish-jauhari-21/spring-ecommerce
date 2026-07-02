import type { Product } from "../types/Product";

type ProductCardProps = {
    product: Product;
};

function ProductCard({ product }: ProductCardProps) {
    return (
        <div className="card h-100 shadow-sm">

            <img
                src={product.image_url}
                className="card-img-top"
                alt={product.name}
                style={{ height: "220px", objectFit: "cover" }}
            />

            <div className="card-body d-flex flex-column">

                <h5 className="card-title">
                    {product.name}
                </h5>

                <p className="card-text text-muted">
                    {product.description}
                </p>

                <h4 className="text-success">
                    ₹ {product.price}
                </h4>

                <p>
                    Stock: {product.stock}
                </p>

                <button className="btn btn-primary mt-auto">
                    Add to Cart
                </button>

            </div>

        </div>
    );
}

export default ProductCard;