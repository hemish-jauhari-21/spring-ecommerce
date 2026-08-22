import { useState } from "react";
import { useNavigate } from "react-router-dom";
import ProductService from "../services/ProductService";
import { notifyError } from "../services/api";
import type { ProductRequest } from "../types/ProductRequest";
import { toast } from "react-toastify";

function AddProduct() {

    const navigate = useNavigate();

    const [product, setProduct] = useState<ProductRequest>({
        name: "",
        description: "",
        price: 0,
        stock: 0,
        category: "",
        image_url: ""
    });

    const handleChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
    ) => {

        const { name, value } = e.target;

        setProduct(prev => ({
            ...prev,
            [name]:
                name === "price" || name === "stock"
                    ? Number(value)
                    : value
        }));
    };

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {

        e.preventDefault();

        try {

            await ProductService.addProduct(product);

            toast.success("Product Added Successfully.");

            navigate("/admin/products");

        }

        catch (error) {

            notifyError(error, "Failed to add product.");

        }

    };

    return (

        <div className="container mt-5">

            <div className="row justify-content-center">

                <div className="col-md-8">

                    <div className="card shadow">

                        <div className="card-header bg-success text-white">

                            <h3>Add Product</h3>

                        </div>

                        <div className="card-body">

                            <form onSubmit={handleSubmit}>

                                <div className="mb-3">

                                    <label className="form-label">
                                        Product Name
                                    </label>

                                    <input
                                        type="text"
                                        className="form-control"
                                        name="name"
                                        value={product.name}
                                        onChange={handleChange}
                                        required
                                    />

                                </div>

                                <div className="mb-3">

                                    <label className="form-label">
                                        Description
                                    </label>

                                    <textarea
                                        className="form-control"
                                        rows={4}
                                        name="description"
                                        value={product.description}
                                        onChange={handleChange}
                                        required
                                    />

                                </div>

                                <div className="mb-3">

                                    <label className="form-label">
                                        Price
                                    </label>

                                    <input
                                        type="number"
                                        className="form-control"
                                        name="price"
                                        value={product.price}
                                        onChange={handleChange}
                                        required
                                    />

                                </div>

                                <div className="mb-3">

                                    <label className="form-label">
                                        Stock
                                    </label>

                                    <input
                                        type="number"
                                        className="form-control"
                                        name="stock"
                                        value={product.stock}
                                        onChange={handleChange}
                                        required
                                    />

                                </div>

                                <div className="mb-3">

                                    <label className="form-label">
                                        Category
                                    </label>

                                    <input
                                        type="text"
                                        className="form-control"
                                        name="category"
                                        value={product.category}
                                        onChange={handleChange}
                                        required
                                    />

                                </div>

                                <div className="mb-4">

                                    <label className="form-label">
                                        Image URL
                                    </label>

                                    <input
                                        type="text"
                                        className="form-control"
                                        name="image_url"
                                        value={product.image_url}
                                        onChange={handleChange}
                                        required
                                    />

                                </div>

                                <button
                                    type="submit"
                                    className="btn btn-success me-3"
                                >
                                    Save Product
                                </button>

                                <button
                                    type="button"
                                    className="btn btn-secondary"
                                    onClick={() => navigate("/admin/products")}
                                >
                                    Cancel
                                </button>

                            </form>

                        </div>

                    </div>

                </div>

            </div>

        </div>

    );
}

export default AddProduct;