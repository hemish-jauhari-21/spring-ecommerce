import { useEffect, useState } from "react";
import ProductService from "../services/ProductService";
import type { Product } from "../types/Product";
import { useNavigate } from "react-router-dom";

function ManageProducts() {

    const [products, setProducts] = useState<Product[]>([]);

    const navigate = useNavigate();

    // Used when deleting a product
    const loadProducts = async () => {

        try {

            const data = await ProductService.getAllProducts();

            console.log("Products received:", data);

            setProducts(data);

        } catch (error) {

            console.error("Error fetching products:", error);

        }

    };

    // Fetch products when page loads
    useEffect(() => {

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

    const handleDelete = async (id: number) => {

        const confirmed = window.confirm(
            "Are you sure you want to delete this product?"
        );

        if (!confirmed) {
            return;
        }

        try {

            await ProductService.deleteProduct(id);

            alert("Product deleted successfully.");

            // Refresh the product list
            loadProducts();

        } catch (error) {

            console.error("Failed to delete product:", error);

            alert("Failed to delete product.");

        }

    };

    return (

        <div className="container mt-4">

            <div className="d-flex justify-content-between align-items-center">

                <h2>Manage Products</h2>

                <button
                    className="btn btn-success"
                    onClick={() => navigate("/admin/products/add")}
                >
                    Add Product
                </button>

            </div>

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

                    {products.map(product => (

                        <tr key={product.id}>

                            <td>{product.id}</td>

                            <td>{product.name}</td>

                            <td>₹ {product.price}</td>

                            <td>{product.stock}</td>

                            <td>{product.category}</td>

                            <td>

                                <button
                                    className="btn btn-warning btn-sm me-2"
                                    onClick={() =>
                                        navigate(
                                            `/admin/products/edit/${product.id}`
                                        )
                                    }
                                >
                                    Edit
                                </button>

                                <button
                                    className="btn btn-danger btn-sm"
                                    onClick={() =>
                                        handleDelete(product.id)
                                    }
                                >
                                    Delete
                                </button>

                            </td>

                        </tr>

                    ))}

                </tbody>

            </table>

        </div>

    );
}

export default ManageProducts;