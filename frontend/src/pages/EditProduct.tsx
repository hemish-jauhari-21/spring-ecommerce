import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import ProductService from "../services/ProductService";
import { notifyError } from "../services/api";
import type { ProductRequest } from "../types/ProductRequest";
import { toast } from "react-toastify";

type FormErrors = Partial<Record<keyof ProductRequest, string>>;

function validate(product: ProductRequest): FormErrors {
    const errors: FormErrors = {};

    if (!product.name.trim()) {
        errors.name = "Product name is required.";
    } else if (product.name.trim().length > 255) {
        errors.name = "Product name must not exceed 255 characters.";
    }

    if (!product.description.trim()) {
        errors.description = "Description is required.";
    } else if (product.description.trim().length > 1000) {
        errors.description = "Description must not exceed 1000 characters.";
    }

    if (product.price === null || product.price === undefined || product.price === 0) {
        errors.price = "Price is required.";
    } else if (product.price <= 0) {
        errors.price = "Price must be greater than 0.";
    }

    if (product.stock === null || product.stock === undefined) {
        errors.stock = "Stock is required.";
    } else if (product.stock < 0) {
        errors.stock = "Stock cannot be less than 0.";
    }

    if (!product.category.trim()) {
        errors.category = "Category is required.";
    } else if (product.category.trim().length > 100) {
        errors.category = "Category must not exceed 100 characters.";
    }

    return errors;
}

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

    const [errors, setErrors] = useState<FormErrors>({});
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        if (id) {
            ProductService.getProductById(parseInt(id))
                .then((data) => {
                    setProduct(data);
                })
                .catch((error) => {
                    notifyError(error, "Unable to load product.");
                })
                .finally(() => {
                    setLoading(false);
                });
        }
    }, [id]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setProduct((prevProduct) => ({
            ...prevProduct,
            [name]: name === "price" || name === "stock" ? parseFloat(value) : value
        }));

        if (errors[name as keyof ProductRequest]) {
            setErrors(prev => {
                const next = { ...prev };
                delete next[name as keyof ProductRequest];
                return next;
            });
        }
    };

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        const validationErrors = validate(product);
        if (Object.keys(validationErrors).length > 0) {
            setErrors(validationErrors);
            return;
        }

        if (!id) return;

        setSubmitting(true);

        try {
            await ProductService.updateProduct(parseInt(id), product);
            toast.success("Product updated successfully.");
            navigate("/admin/products");
        } catch (error) {
            notifyError(error, "Failed to update product.");
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) {
        return (
            <div className="container mt-5">
                <div className="row justify-content-center">
                    <div className="col-md-8">
                        <p>Loading product...</p>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="container mt-5">
            <div className="row justify-content-center">
                <div className="col-md-8">
                    <div className="card shadow">
                        <div className="card-header bg-success text-white">
                            <h3>Edit Product</h3>
                        </div>
                        <div className="card-body">
                            <form onSubmit={handleSubmit}>
                                <div className="mb-3">
                                    <label className="form-label">Product Name</label>
                                    <input
                                        type="text"
                                        className={`form-control${errors.name ? " is-invalid" : ""}`}
                                        name="name"
                                        value={product.name}
                                        onChange={handleChange}
                                    />
                                    {errors.name && (
                                        <div className="invalid-feedback">{errors.name}</div>
                                    )}
                                </div>

                                <div className="mb-3">
                                    <label className="form-label">Description</label>
                                    <textarea
                                        className={`form-control${errors.description ? " is-invalid" : ""}`}
                                        rows={4}
                                        name="description"
                                        value={product.description}
                                        onChange={handleChange}
                                    />
                                    {errors.description && (
                                        <div className="invalid-feedback">{errors.description}</div>
                                    )}
                                </div>

                                <div className="mb-3">
                                    <label className="form-label">Price</label>
                                    <input
                                        type="number"
                                        className={`form-control${errors.price ? " is-invalid" : ""}`}
                                        name="price"
                                        value={product.price}
                                        onChange={handleChange}
                                        min="0.01"
                                        step="0.01"
                                    />
                                    {errors.price && (
                                        <div className="invalid-feedback">{errors.price}</div>
                                    )}
                                </div>

                                <div className="mb-3">
                                    <label className="form-label">Stock</label>
                                    <input
                                        type="number"
                                        className={`form-control${errors.stock ? " is-invalid" : ""}`}
                                        name="stock"
                                        value={product.stock}
                                        onChange={handleChange}
                                        min="0"
                                    />
                                    {errors.stock && (
                                        <div className="invalid-feedback">{errors.stock}</div>
                                    )}
                                </div>

                                <div className="mb-3">
                                    <label className="form-label">Category</label>
                                    <input
                                        type="text"
                                        className={`form-control${errors.category ? " is-invalid" : ""}`}
                                        name="category"
                                        value={product.category}
                                        onChange={handleChange}
                                    />
                                    {errors.category && (
                                        <div className="invalid-feedback">{errors.category}</div>
                                    )}
                                </div>

                                <div className="mb-4">
                                    <label className="form-label">Image URL</label>
                                    <input
                                        type="text"
                                        className="form-control"
                                        name="image_url"
                                        value={product.image_url}
                                        onChange={handleChange}
                                    />
                                </div>

                                <button
                                    type="submit"
                                    className="btn btn-success me-3"
                                    disabled={submitting}
                                >
                                    {submitting ? "Updating..." : "Update Product"}
                                </button>

                                <button
                                    type="button"
                                    className="btn btn-secondary"
                                    onClick={() => navigate("/admin/products")}
                                    disabled={submitting}
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

export default EditProduct;
