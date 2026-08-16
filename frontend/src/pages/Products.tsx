import { useEffect, useState } from "react";

import ProductService from "../services/ProductService";

import type { Product } from "../types/Product";
import ProductCard from "../components/ProductCard";

function Products() {

    const [products, setProducts] = useState<Product[]>([]);

    useEffect(() => {
        const fetchProducts = async () => {
            try {
                const data = await ProductService.getAllProducts();
                setProducts(data);
            } catch (error) {
                console.error(error);
            }
        };

        fetchProducts();
    }, []);

    return (
        <div className="container mt-4">

            <h2 className="mb-4">
                Products
            </h2>

            <div className="row">

                {products.map((product) => (

                    <div
                        key={product.id}
                        className="col-lg-3 col-md-4 col-sm-6 mb-4"
                    >
                        <ProductCard product={product} />
                    </div>

                ))}

            </div>

        </div>
    );
}

export default Products;