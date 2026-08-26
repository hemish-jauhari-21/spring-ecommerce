import type { Product } from "../types/Product";
import type { ProductPageResponse, ProductSearchParams } from "../types/ProductSearch";
import api from "./api";
import type { ProductRequest } from "../types/ProductRequest";


class ProductService {
    async getAllProducts(): Promise<Product[]> {

        const response = await api.get<Product[]>("/products/all");

        return response.data;
    }

    async searchProducts(params: ProductSearchParams): Promise<ProductPageResponse> {
        const response = await api.get<ProductPageResponse>("/products/search", { params });
        return response.data;
    }

    async getCategories(): Promise<string[]> {
        const response = await api.get<string[]>("/products/categories");
        return response.data;
    }

    async addProduct(product: ProductRequest) {
        const response = await api.post("/products/add", product);
        return response.data;
    }

    async getProductById(id: number): Promise<Product> {
        const response = await api.get<Product>(`/products/${id}`);
        return response.data;
    }

    async updateProduct(id: number, product: ProductRequest) {
        const response = await api.put(`/products/${id}`, product);
        return response.data;
    }

    async deleteProduct(id: number): Promise<string> {
        const response = await api.delete(`/products/${id}`);
        return response.data;
    }
}

export default new ProductService;
