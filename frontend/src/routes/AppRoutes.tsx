import { BrowserRouter, Routes, Route } from "react-router-dom";

import MainLayout from "../layouts/MainLayout";
import ProtectedRoute from "../components/ProtectedRoute";

import Home from "../pages/Home";
import Cart from "../pages/Cart";
import Orders from "../pages/Orders";
import Login from "../pages/Login";
import Products from "../pages/Products";
import ManageProducts from "../pages/ManageProducts";
import AdminRoute from "../components/AdminRoute";
import AddProduct from "../pages/AddProduct";

function AppRoutes() {
    return (
        <BrowserRouter>
            <Routes>
                <Route element={<MainLayout />}>

                    <Route path="/" element={<Home />} />
                    <Route path="/products" element={<Products />} />

                    <Route path="/cart" element={<ProtectedRoute>
                        <Cart />
                    </ProtectedRoute>} />
                    <Route path="/orders" element={<ProtectedRoute>
                        <Orders />
                    </ProtectedRoute>} />

                    <Route path="/login" element={<Login />} />

                    <Route 
                        path="/admin/products"
                        element={
                            <AdminRoute>
                                <ManageProducts />
                            </AdminRoute>
                        }
                    />

                    <Route path="/admin/products/add"
                        element={
                            <AdminRoute>
                                <AddProduct />
                            </AdminRoute>
                        }
                    />

                </Route>
            </Routes>
        </BrowserRouter>
    );
}

export default AppRoutes;