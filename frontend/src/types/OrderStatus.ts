export const ORDER_STATUSES = [
    "PENDING",
    "CONFIRMED",
    "SHIPPED",
    "DELIVERED",
    "CANCELLED"
] as const;

export type OrderStatus = (typeof ORDER_STATUSES)[number];

// Allowed lifecycle, mirrors the backend rules.
// PENDING   -> CONFIRMED | CANCELLED
// CONFIRMED -> SHIPPED   | CANCELLED
// SHIPPED   -> DELIVERED
// DELIVERED / CANCELLED are terminal.
export const ALLOWED_TRANSITIONS: Record<OrderStatus, OrderStatus[]> = {
    PENDING: ["CONFIRMED", "CANCELLED"],
    CONFIRMED: ["SHIPPED", "CANCELLED"],
    SHIPPED: ["DELIVERED"],
    DELIVERED: [],
    CANCELLED: []
};

export function canTransitionTo(
    current: OrderStatus,
    next: OrderStatus
): boolean {
    return ALLOWED_TRANSITIONS[current]?.includes(next) ?? false;
}

export function isOrderStatus(value: unknown): value is OrderStatus {
    return (
        typeof value === "string" &&
        (ORDER_STATUSES as readonly string[]).includes(value)
    );
}
