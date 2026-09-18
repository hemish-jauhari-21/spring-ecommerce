import { useCallback, useEffect, useState } from "react";

import ProductService from "../services/ProductService";
import { notifyError } from "../services/api";

import type { Product } from "../types/Product";
import type { ProductSearchParams } from "../types/ProductSearch";
import ProductCard from "../components/ProductCard";

const SORT_OPTIONS: { label: string; value: string }[] = [
    { label: "Default", value: "default" },
    { label: "Name A-Z", value: "name_asc" },
    { label: "Name Z-A", value: "name_desc" },
    { label: "Price Low to High", value: "price_asc" },
    { label: "Price High to Low", value: "price_desc" },
    { label: "Stock Low to High", value: "stock_asc" },
    { label: "Stock High to Low", value: "stock_desc" },
];

function parseSortOption(value: string): { sortBy: string; direction: string } {
    if (value === "default") {
        return { sortBy: "id", direction: "asc" };
    }
    const parts = value.split("_");
    const direction = parts.pop() ?? "asc";
    const sortBy = parts.join("_");
    return { sortBy, direction };
}

function Products() {
    const [products, setProducts] = useState<Product[]>([]);
    const [loading, setLoading] = useState(true);
    const [categories, setCategories] = useState<string[]>([]);

    const [keyword, setKeyword] = useState("");
    const [category, setCategory] = useState("");
    const [minPrice, setMinPrice] = useState("");
    const [maxPrice, setMaxPrice] = useState("");
    const [sortOption, setSortOption] = useState("default");

    const [page, setPage] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [first, setFirst] = useState(true);
    const [last, setLast] = useState(true);

    const fetchProducts = useCallback(async (params: ProductSearchParams) => {
        try {
            setLoading(true);
            const data = await ProductService.searchProducts(params);
            setProducts(data.content);
            setPage(data.page);
            setTotalElements(data.totalElements);
            setTotalPages(data.totalPages);
            setFirst(data.first);
            setLast(data.last);
        } catch (error) {
            notifyError(error, "Unable to load products.");
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        const loadCategories = async () => {
            try {
                const data = await ProductService.getCategories();
                setCategories(data);
            } catch (error) {
                notifyError(error, "Unable to load categories.");
            }
        };
        loadCategories();
    }, []);

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        fetchProducts({ page: 0, size: 8 });
    }, [fetchProducts]);

    const buildParams = useCallback((pageNum: number): ProductSearchParams => {
        const params: ProductSearchParams = {
            page: pageNum,
            size: 8,
        };

        if (keyword.trim()) {
            params.keyword = keyword.trim();
        }

        if (category) {
            params.category = category;
        }

        const parsedMin = minPrice !== "" ? parseFloat(minPrice) : undefined;
        const parsedMax = maxPrice !== "" ? parseFloat(maxPrice) : undefined;

        if (parsedMin !== undefined) {
            if (isNaN(parsedMin) || parsedMin < 0) {
                return params;
            }
            params.minPrice = parsedMin;
        }

        if (parsedMax !== undefined) {
            if (isNaN(parsedMax) || parsedMax < 0) {
                return params;
            }
            params.maxPrice = parsedMax;
        }

        if (parsedMin !== undefined && parsedMax !== undefined && parsedMin > parsedMax) {
            return params;
        }

        const { sortBy, direction } = parseSortOption(sortOption);
        params.sortBy = sortBy;
        params.direction = direction;

        return params;
    }, [keyword, category, minPrice, maxPrice, sortOption]);

    const handleSearch = useCallback(() => {
        const parsedMin = minPrice !== "" ? parseFloat(minPrice) : undefined;
        const parsedMax = maxPrice !== "" ? parseFloat(maxPrice) : undefined;

        if (parsedMin !== undefined && (isNaN(parsedMin) || parsedMin < 0)) {
            notifyError(new Error("Min price must be >= 0"), "Min price must be >= 0");
            return;
        }
        if (parsedMax !== undefined && (isNaN(parsedMax) || parsedMax < 0)) {
            notifyError(new Error("Max price must be >= 0"), "Max price must be >= 0");
            return;
        }
        if (parsedMin !== undefined && parsedMax !== undefined && parsedMin > parsedMax) {
            notifyError(new Error("Min price cannot exceed max price"), "Min price cannot exceed max price");
            return;
        }

        fetchProducts(buildParams(0));
    }, [buildParams, fetchProducts, minPrice, maxPrice]);

    const handleKeyDown = useCallback((e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Enter") {
            handleSearch();
        }
    }, [handleSearch]);

    const handleCategoryChange = useCallback((e: React.ChangeEvent<HTMLSelectElement>) => {
        setCategory(e.target.value);
        setPage(0);
        const newParams: ProductSearchParams = {
            page: 0,
            size: 8,
        };
        if (keyword.trim()) {
            newParams.keyword = keyword.trim();
        }
        if (e.target.value) {
            newParams.category = e.target.value;
        }
        const { sortBy, direction } = parseSortOption(sortOption);
        newParams.sortBy = sortBy;
        newParams.direction = direction;
        fetchProducts(newParams);
    }, [fetchProducts, keyword, sortOption]);

    const handleSortChange = useCallback((e: React.ChangeEvent<HTMLSelectElement>) => {
        const newSortOption = e.target.value;
        setSortOption(newSortOption);
        const newParams: ProductSearchParams = {
            page: 0,
            size: 8,
        };
        if (keyword.trim()) {
            newParams.keyword = keyword.trim();
        }
        if (category) {
            newParams.category = category;
        }
        const parsedMin = minPrice !== "" ? parseFloat(minPrice) : undefined;
        const parsedMax = maxPrice !== "" ? parseFloat(maxPrice) : undefined;
        if (parsedMin !== undefined && !isNaN(parsedMin) && parsedMin >= 0) {
            newParams.minPrice = parsedMin;
        }
        if (parsedMax !== undefined && !isNaN(parsedMax) && parsedMax >= 0) {
            newParams.maxPrice = parsedMax;
        }
        if (parsedMin !== undefined && parsedMax !== undefined && parsedMin > parsedMax) {
            fetchProducts(newParams);
            return;
        }
        const { sortBy, direction } = parseSortOption(newSortOption);
        newParams.sortBy = sortBy;
        newParams.direction = direction;
        fetchProducts(newParams);
    }, [fetchProducts, keyword, category, minPrice, maxPrice]);

    const handleReset = useCallback(() => {
        setKeyword("");
        setCategory("");
        setMinPrice("");
        setMaxPrice("");
        setSortOption("default");
        fetchProducts({ page: 0, size: 8 });
    }, [fetchProducts]);

    const handlePageChange = useCallback((newPage: number) => {
        fetchProducts(buildParams(newPage));
    }, [buildParams, fetchProducts]);

    const pageNumbers = (): number[] => {
        const pages: number[] = [];
        const maxVisible = 5;
        let start = Math.max(0, page - Math.floor(maxVisible / 2));
        const end = Math.min(totalPages, start + maxVisible);
        if (end - start < maxVisible) {
            start = Math.max(0, end - maxVisible);
        }
        for (let i = start; i < end; i++) {
            pages.push(i);
        }
        return pages;
    };

    return (
        <div className="container mt-4">

            <div className="products-header">
                <h2>Products</h2>
                {totalElements > 0 && (
                    <span className="products-result-count">
                        {totalElements} product{totalElements !== 1 ? "s" : ""} found
                    </span>
                )}
            </div>

            <div className="products-filter-section">
                <div className="row g-3 align-items-end">

                    <div className="col-lg-3 col-md-4 col-sm-6">
                        <label className="form-label">Search</label>
                        <input
                            type="text"
                            className="form-control"
                            placeholder="Search products..."
                            value={keyword}
                            onChange={(e) => setKeyword(e.target.value)}
                            onKeyDown={handleKeyDown}
                        />
                    </div>

                    <div className="col-lg-2 col-md-3 col-sm-6">
                        <label className="form-label">Category</label>
                        <select
                            className="form-select"
                            value={category}
                            onChange={handleCategoryChange}
                        >
                            <option value="">All Categories</option>
                            {categories.map((cat) => (
                                <option key={cat} value={cat}>
                                    {cat}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="col-lg-2 col-md-2 col-sm-4">
                        <label className="form-label">Min Price</label>
                        <input
                            type="number"
                            className="form-control"
                            placeholder="0"
                            min="0"
                            value={minPrice}
                            onChange={(e) => setMinPrice(e.target.value)}
                        />
                    </div>

                    <div className="col-lg-2 col-md-2 col-sm-4">
                        <label className="form-label">Max Price</label>
                        <input
                            type="number"
                            className="form-control"
                            placeholder="Any"
                            min="0"
                            value={maxPrice}
                            onChange={(e) => setMaxPrice(e.target.value)}
                        />
                    </div>

                    <div className="col-lg-2 col-md-3 col-sm-4">
                        <label className="form-label">Sort By</label>
                        <select
                            className="form-select"
                            value={sortOption}
                            onChange={handleSortChange}
                        >
                            {SORT_OPTIONS.map((opt) => (
                                <option key={opt.value} value={opt.value}>
                                    {opt.label}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="col-lg-1 col-md-1 col-sm-12">
                        <div className="products-filter-actions">
                            <button
                                className="btn btn-primary btn-sm"
                                onClick={handleSearch}
                            >
                                Search
                            </button>
                            <button
                                className="btn btn-outline-secondary btn-sm"
                                onClick={handleReset}
                            >
                                Reset
                            </button>
                        </div>
                    </div>

                </div>
            </div>

            {loading ? (
                <div className="products-loading">
                    <div className="spinner-border" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </div>
                    <span>Loading products...</span>
                </div>
            ) : products.length === 0 ? (
                <div className="products-empty">
                    <p>No products match your current filters.</p>
                    <button
                        className="btn btn-primary btn-sm"
                        onClick={handleReset}
                    >
                        Reset Filters
                    </button>
                </div>
            ) : (
                <>
                    <div className="row">
                        {products.map((product) => (
                            <div
                                key={product.id}
                                className="col-lg-3 col-md-4 col-sm-6 mb-4"
                            >
                                <ProductCard product={product} />
                            </div>
                        ))}
                    </div>

                    {totalPages > 1 && (
                        <nav className="products-pagination" aria-label="Product pagination">
                            <ul className="pagination justify-content-center">
                                <li className={`page-item ${first ? "disabled" : ""}`}>
                                    <button
                                        className="page-link"
                                        onClick={() => handlePageChange(page - 1)}
                                        disabled={first}
                                    >
                                        Previous
                                    </button>
                                </li>

                                {pageNumbers().map((p) => (
                                    <li
                                        key={p}
                                        className={`page-item ${p === page ? "active" : ""}`}
                                    >
                                        <button
                                            className="page-link"
                                            onClick={() => handlePageChange(p)}
                                        >
                                            {p + 1}
                                        </button>
                                    </li>
                                ))}

                                <li className={`page-item ${last ? "disabled" : ""}`}>
                                    <button
                                        className="page-link"
                                        onClick={() => handlePageChange(page + 1)}
                                        disabled={last}
                                    >
                                        Next
                                    </button>
                                </li>
                            </ul>
                        </nav>
                    )}
                </>
            )}

        </div>
    );
}

export default Products;
