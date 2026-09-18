import { Link, useLocation, useNavigate } from "react-router-dom";
import { Navbar, Nav, Container, Dropdown } from "react-bootstrap";
import { useAuth } from "../hooks/useAuth";


function NavBar() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = () => {
        logout();
        navigate("/login");
    }

    const isActive = (path: string) => location.pathname === path;

    return (
        <Navbar expand="lg" className="navbar-light border-bottom" style={{ backgroundColor: "var(--bg-white)" }} sticky="top">
            <Container>
                <Navbar.Brand as={Link} to="/" className="fw-semibold" style={{ color: "var(--primary)", fontSize: "1.25rem" }}>
                    E-Commerce
                </Navbar.Brand>

                <Navbar.Toggle aria-label="Toggle navigation" />

                <Navbar.Collapse>
                    <Nav className="me-auto" style={{ gap: "0.25rem" }}>
                        <Nav.Link
                            as={Link}
                            to="/"
                            active={isActive("/")}
                            style={{
                                color: isActive("/") ? "var(--primary)" : undefined,
                                fontWeight: isActive("/") ? 600 : undefined,
                            }}
                        >
                            Home
                        </Nav.Link>

                        <Nav.Link
                            as={Link}
                            to="/products"
                            active={isActive("/products")}
                            style={{
                                color: isActive("/products") ? "var(--primary)" : undefined,
                                fontWeight: isActive("/products") ? 600 : undefined,
                            }}
                        >
                            Products
                        </Nav.Link>

                        {user?.role === "USER" && (
                            <>
                                <Nav.Link
                                    as={Link}
                                    to="/cart"
                                    active={isActive("/cart")}
                                    style={{
                                        color: isActive("/cart") ? "var(--primary)" : undefined,
                                        fontWeight: isActive("/cart") ? 600 : undefined,
                                    }}
                                >
                                    Cart
                                </Nav.Link>

                                <Nav.Link
                                    as={Link}
                                    to="/orders"
                                    active={isActive("/orders")}
                                    style={{
                                        color: isActive("/orders") ? "var(--primary)" : undefined,
                                        fontWeight: isActive("/orders") ? 600 : undefined,
                                    }}
                                >
                                    Orders
                                </Nav.Link>
                            </>
                        )}

                        {user?.role === "ADMIN" && (
                            <>
                                <Nav.Link
                                    as={Link}
                                    to="/admin/products"
                                    active={location.pathname.startsWith("/admin/products")}
                                    style={{
                                        color: location.pathname.startsWith("/admin/products") ? "var(--primary)" : undefined,
                                        fontWeight: location.pathname.startsWith("/admin/products") ? 600 : undefined,
                                    }}
                                >
                                    Manage Products
                                </Nav.Link>

                                <Nav.Link
                                    as={Link}
                                    to="/admin/orders"
                                    active={isActive("/admin/orders")}
                                    style={{
                                        color: isActive("/admin/orders") ? "var(--primary)" : undefined,
                                        fontWeight: isActive("/admin/orders") ? 600 : undefined,
                                    }}
                                >
                                    Admin Orders
                                </Nav.Link>

                                <Nav.Link
                                    as={Link}
                                    to="/orders"
                                    active={isActive("/orders")}
                                    style={{
                                        color: isActive("/orders") ? "var(--primary)" : undefined,
                                        fontWeight: isActive("/orders") ? 600 : undefined,
                                    }}
                                >
                                    Orders
                                </Nav.Link>
                            </>
                        )}
                    </Nav>

                    <Nav className="align-items-lg-center" style={{ gap: "0.5rem" }}>
                        {user ? (
                            <>
                                <Dropdown align="end">
                                    <Dropdown.Toggle
                                        variant="outline-secondary"
                                        id="user-dropdown"
                                        className="d-flex align-items-center gap-2"
                                        style={{ borderRadius: "var(--radius-sm)" }}
                                    >
                                        <span
                                            className="d-inline-flex align-items-center justify-content-center rounded-circle"
                                            style={{
                                                width: "32px",
                                                height: "32px",
                                                backgroundColor: "var(--primary-light)",
                                                color: "var(--primary)",
                                                fontSize: "0.875rem",
                                                fontWeight: 600,
                                            }}
                                        >
                                            {user.name?.charAt(0).toUpperCase()}
                                        </span>
                                        <span className="d-none d-lg-inline">{user.name}</span>
                                    </Dropdown.Toggle>

                                    <Dropdown.Menu style={{ minWidth: "180px" }}>
                                        <Dropdown.Item as={Link} to="/account">
                                            My Account
                                        </Dropdown.Item>
                                        <Dropdown.Divider />
                                        <Dropdown.Item onClick={handleLogout} className="text-danger">
                                            Logout
                                        </Dropdown.Item>
                                    </Dropdown.Menu>
                                </Dropdown>
                            </>
                        ) : (
                            <>
                                <Nav.Link
                                    as={Link}
                                    to="/login"
                                    active={isActive("/login")}
                                    style={{
                                        color: isActive("/login") ? "var(--primary)" : undefined,
                                        fontWeight: isActive("/login") ? 600 : undefined,
                                    }}
                                >
                                    Login
                                </Nav.Link>
                                <Link to="/register">
                                    <button
                                        className="btn btn-primary btn-sm"
                                        style={{ borderRadius: "var(--radius-sm)" }}
                                    >
                                        Register
                                    </button>
                                </Link>
                            </>
                        )}
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
}

export default NavBar;
