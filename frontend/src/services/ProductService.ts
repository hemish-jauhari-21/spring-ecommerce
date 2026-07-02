import type { Product } from "../types/Product";
import api from "./api";


class ProductService {
    async getAllProducts(): Promise<Product[]> {

        console.log("Calling API...");

        const response = await api.get<Product[]>("/products/all");

        console.log(response.data);
        return response.data;
    }
}

export default new ProductService;
