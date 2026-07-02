import { Link } from "react-router-dom";

function Navbar() {
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

                    <Link className="nav-link" to="/login">
                        Login
                    </Link>

                </div>

            </div>
        </nav>
    );
}

export default Navbar;