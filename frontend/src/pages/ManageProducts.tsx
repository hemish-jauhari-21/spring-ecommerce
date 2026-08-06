import { useEffect, useState } from "react";
import ProductService from "../services/ProductService";
import type { Product } from "../types/Product";
import { useNavigate } from "react-router-dom";


function ManageProducts() {
    const [products, setProducts] = useState<Product[]>([]);

    const navigate = useNavigate();

    useEffect(() => {
        // const fetchProducts = async () => {
        //     try {
        //         const data = await ProductService.getAllProducts();
        //         setProducts(data);
        //     } catch (error) {
        //         console.error(error);
        //     }
        // };

        // fetchProducts();

        const fetchProducts = async () => {

            try {

                const data = await ProductService.getAllProducts();

                console.log("Products received:", data);

                setProducts(data);

            } catch (error) {

                console.error("Error fetching products:", error);

            }

        };
        fetchProducts();
    }, []);

    return (

        <div className="container mt-4">

            <h2>Manage Products</h2>

            <button
                className="btn btn-success"
                onClick={() => navigate("/admin/products/add")}
            >
                Add Product
            </button>

            <table className="table table-bordered mt-4">

                <thead>

                    <tr>

                        <th>ID</th>
                        <th>Name</th>
                        <th>Price</th>
                        <th>Stock</th>
                        <th>Category</th>
                        <th>Actions</th>

                    </tr>

                </thead>

                <tbody>

                    {

                        products.map(product => (

                            <tr key={product.id}>

                                <td>{product.id}</td>

                                <td>{product.name}</td>

                                <td>₹ {product.price}</td>

                                <td>{product.stock}</td>

                                <td>{product.category}</td>

                                <td>

                                    <button className="btn btn-warning btn-sm me-2">
                                        Edit
                                    </button>

                                    <button className="btn btn-danger btn-sm">
                                        Delete
                                    </button>

                                </td>

                            </tr>

                        ))

                    }

                </tbody>

            </table>

        </div>

    );

}

export default ManageProducts;