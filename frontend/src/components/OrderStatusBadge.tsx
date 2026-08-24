import type { OrderStatus } from "../types/OrderStatus";

const STATUS_BADGE_CLASSES: Record<OrderStatus, string> = {
    PENDING: "bg-warning text-dark",
    CONFIRMED: "bg-info text-dark",
    SHIPPED: "bg-primary",
    DELIVERED: "bg-success",
    CANCELLED: "bg-danger"
};

function OrderStatusBadge({ status }: { status: OrderStatus | string }) {
    const badgeClass =
        STATUS_BADGE_CLASSES[status as OrderStatus] ?? "bg-secondary";

    return <span className={`badge ${badgeClass}`}>{status}</span>;
}

export default OrderStatusBadge;
