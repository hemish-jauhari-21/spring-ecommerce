import { useEffect, useState } from "react";
import ProductService from "../services/ProductService";
import ProductCard from "../components/ProductCard";
import type { Product } from "../types/Product";


function Home() {
    const [products, setProducts] = useState<Product[]>([]);

    useEffect(() => {
        const fetchProducts = async () => {
            try {
                const data = await ProductService.getAllProducts();
                setProducts(data);
            } 
            catch (error) {
                console.error(error);
            }
        };

        fetchProducts();
    }, [])

    return (
        <>
            <div className="container mt-5">
                <h1>Welcome to My E-Commerce Store</h1>
                <p>This frontend is connected to our Spring Boot backend.</p>

                <div className="row">

                    {products.map((product) => (

                        <div key={product.id}
                            className="col-lg-3 col-md-4 col-sm-6 mb-4"
                        >
                            <ProductCard product={product}/>
                        </div>
                    ))}
                </div>
            </div>
        </>
    );
}

export default Home;