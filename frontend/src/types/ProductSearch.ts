import type { Product } from "./Product";

export interface ProductSearchParams {
    keyword?: string;
    category?: string;
    minPrice?: number;
    maxPrice?: number;
    sortBy?: string;
    direction?: string;
    page?: number;
    size?: number;
}

export interface ProductPageResponse {
    content: Product[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
}
