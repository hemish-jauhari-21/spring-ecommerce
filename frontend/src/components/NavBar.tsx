import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";


function Navbar() {
    const { user, logout } = useAuth();
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

                    <Link className="nav-link" to="/cart">
                        Cart
                    </Link>

                    <Link className="nav-link" to="/orders">
                        Orders
                    </Link>

                    {/* <Link className="nav-link" to="/login">
                        Login
                    </Link> */}

                    {user ? (
                        <button className="btn btn-danger" onClick={logout}>
                            Logout
                        </button>
                    ) : (
                        <Link className="nav-link" to="/login">
                            Login
                        </Link>
                    )}

                    {
                        user && (
                            <span className="mt-2 me-3  text-white">
                                Welcome, {user.name}
                            </span>
                        )
                    }
                </div>

            </div>
        </nav>
    );
}

export default Navbar;