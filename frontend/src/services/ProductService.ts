import type { Product } from "../types/Product";
import api from "./api";
import type { ProductRequest } from "../types/ProductRequest";


class ProductService {
    async getAllProducts(): Promise<Product[]> {

        console.log("Calling API...");

        const response = await api.get<Product[]>("/products/all");

        console.log(response.data);
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
