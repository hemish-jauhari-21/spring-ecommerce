import { Outlet } from "react-router-dom";

import Footer from "../components/Footer";
import NavBar from "../components/NavBar";

function MainLayout() {
    return(
        <>
            <NavBar />

            <main className="flex-grow-1" style={{ paddingTop: "1rem" }}>
                <Outlet />
            </main>

            <Footer />
        </>
    );
}

export default MainLayout;
