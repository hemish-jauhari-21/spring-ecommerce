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
}

export default new ProductService;
