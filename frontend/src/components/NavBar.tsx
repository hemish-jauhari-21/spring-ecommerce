import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";


function Navbar() {
    const { user, logout } = useAuth();
    
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate("/login");
    }

    return (
        <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
            <div className="container">

                <Link className="navbar-brand" to="/">
                    E-Commerce
                </Link>

                <div className="navbar-nav">

                    <Link className="nav-link" to="/">
                        Home
                    </Link>

                    <Link className="nav-link" to="/products">
                        Products
                    </Link>

                    {
                        user?.role === "USER" && (
                            <>
                                <Link className="nav-link" to="/cart">
                                    Cart
                                </Link>

                                <Link className="nav-link" to="/orders">
                                    Orders
                                </Link>
                            </>
                        )
                    }

                    {
                        user?.role === "ADMIN" && (
                            <>
                                <Link className="nav-link" to="/admin/products">
                                    Manage Products
                                </Link>

                                <Link className="nav-link" to="/admin/orders">
                                    Admin Orders
                                </Link>

                                <Link className="nav-link" to="/orders">
                                    Orders
                                </Link>
                            </>
                        )
                    }

                    {/* <Link className="nav-link" to="/login">
                        Login
                    </Link> */}

                    {
                        user && (
                            <span className="mt-2 me-3 pb-2 text-white">
                                Welcome, {user.name}
                            </span>
                        )
                    }

                    {
                        user && (
                            <Link className="nav-link" to="/account">
                                My Account
                            </Link>
                        )
                    }

                    {user ? (
                        <button className="btn btn-danger" onClick={handleLogout}>
                            Logout
                        </button>
                    ) : (
                        <>
                            <Link className="nav-link" to="/login">
                                Login
                            </Link>
                            <Link className="nav-link" to="/register">
                                Register
                            </Link>
                        </>
                    )}
                </div>

            </div>
        </nav>
    );
}

export default Navbar;