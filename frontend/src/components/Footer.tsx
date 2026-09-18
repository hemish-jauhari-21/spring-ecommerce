import { Link } from "react-router-dom";

function Footer() {
    return (
        <footer
            className="mt-auto border-top"
            style={{
                backgroundColor: "var(--bg-white)",
                borderColor: "var(--border)",
            }}
        >
            <div className="container py-4">
                <div className="row g-4">
                    <div className="col-lg-4 col-md-6">
                        <h5
                            className="mb-3"
                            style={{ color: "var(--primary)", fontWeight: 600 }}
                        >
                            E-Commerce
                        </h5>
                        <p className="text-muted small mb-0">
                            Your trusted online store for quality products at great prices.
                        </p>
                    </div>

                    <div className="col-lg-2 col-md-6">
                        <h6 className="mb-3 fw-semibold">Shop</h6>
                        <ul className="list-unstyled mb-0" style={{ gap: "0.5rem", display: "flex", flexDirection: "column" }}>
                            <li>
                                <Link
                                    to="/products"
                                    className="text-muted small text-decoration-none"
                                    style={{ transition: "color 0.15s" }}
                                    onMouseEnter={(e) => (e.currentTarget.style.color = "var(--primary)")}
                                    onMouseLeave={(e) => (e.currentTarget.style.color = "")}
                                >
                                    All Products
                                </Link>
                            </li>
                        </ul>
                    </div>

                    <div className="col-lg-2 col-md-6">
                        <h6 className="mb-3 fw-semibold">Account</h6>
                        <ul className="list-unstyled mb-0" style={{ gap: "0.5rem", display: "flex", flexDirection: "column" }}>
                            <li>
                                <Link
                                    to="/account"
                                    className="text-muted small text-decoration-none"
                                    onMouseEnter={(e) => (e.currentTarget.style.color = "var(--primary)")}
                                    onMouseLeave={(e) => (e.currentTarget.style.color = "")}
                                >
                                    My Account
                                </Link>
                            </li>
                            <li>
                                <Link
                                    to="/orders"
                                    className="text-muted small text-decoration-none"
                                    onMouseEnter={(e) => (e.currentTarget.style.color = "var(--primary)")}
                                    onMouseLeave={(e) => (e.currentTarget.style.color = "")}
                                >
                                    My Orders
                                </Link>
                            </li>
                            <li>
                                <Link
                                    to="/cart"
                                    className="text-muted small text-decoration-none"
                                    onMouseEnter={(e) => (e.currentTarget.style.color = "var(--primary)")}
                                    onMouseLeave={(e) => (e.currentTarget.style.color = "")}
                                >
                                    Cart
                                </Link>
                            </li>
                        </ul>
                    </div>

                    <div className="col-lg-4 col-md-6">
                        <h6 className="mb-3 fw-semibold">Contact</h6>
                        <ul className="list-unstyled mb-0 text-muted small" style={{ gap: "0.5rem", display: "flex", flexDirection: "column" }}>
                            <li>support@ecommerce.com</li>
                        </ul>
                    </div>
                </div>

                <hr className="my-3" style={{ borderColor: "var(--border)" }} />

                <div className="d-flex flex-column flex-sm-row justify-content-between align-items-center gap-2">
                    <p className="text-muted small mb-0">
                        &copy; {new Date().getFullYear()} E-Commerce. All rights reserved.
                    </p>
                </div>
            </div>
        </footer>
    );
}

export default Footer;
