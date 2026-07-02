import { Outlet } from "react-router-dom";

import Footer from "../components/Footer";
import NavBar from "../components/NavBar";

function MainLayout() {
    return(
        <>
            <NavBar />

            <main className="container mt-4">
                <Outlet />
            </main>

            <Footer />
        </>
    );
}

export default MainLayout;