import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import ProductService from "../services/ProductService";
import { notifyError } from "../services/api";
import type { ProductRequest } from "../types/ProductRequest";
import { toast } from "react-toastify";

function EditProduct() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const [product, setProduct] = useState<ProductRequest>({
        name: "",
        description: "",
        price: 0,
        stock: 0,
        category: "",
        image_url: ""
    });

    useEffect(() => {
        if (id) {
            ProductService.getProductById(parseInt(id))
                .then((data) => {
                    setProduct(data);
                })
                .catch((error) => {
                    notifyError(error, "Unable to load product.");
                });
        }
    }, [id]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {        
        const { name, value } = e.target;
        setProduct((prevProduct) => ({
            ...prevProduct,
            [name]: name === "price" || name === "stock" ? parseFloat(value) : value
        }));
    };

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (id) {
            ProductService.updateProduct(parseInt(id), product)
                .then(() => {
                    toast.success("Product updated successfully.");
                    navigate("/products");
                })
                .catch((error) => {
                    notifyError(error, "Failed to update product.");
                });
        }
    };

    return (
        <div>
            <h1>Edit Product</h1>
            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="name">Name:</label>
                    <input
                        type="text"
                        id="name"
                        name="name"
                        value={product.name}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="description">Description:</label>
                    <textarea
                        id="description"
                        name="description"
                        value={product.description}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="price">Price:</label>
                    <input
                        type="number"
                        id="price"
                        name="price"
                        value={product.price}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="stock">Stock:</label>
                    <input
                        type="number"
                        id="stock"
                        name="stock"
                        value={product.stock}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="category">Category:</label>
                    <input
                        type="text"
                        id="category"
                        name="category"
                        value={product.category}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="image_url">Image URL:</label>
                    <input
                        type="text"
                        id="image_url"
                        name="image_url"
                        value={product.image_url}
                        onChange={handleChange}
                    />
                </div>
                <button type="submit">Update Product</button>
            </form>
        </div>
    );
}

export default EditProduct;