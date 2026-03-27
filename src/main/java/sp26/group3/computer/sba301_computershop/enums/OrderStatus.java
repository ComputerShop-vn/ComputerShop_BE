package sp26.group3.computer.sba301_computershop.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING(0),
    CONFIRMED(1),
    PROCESSING(2),
    SHIPPED(3),
    DELIVERED(4),
    CANCELLED(-1);

    private final int rank;

    OrderStatus(int rank) {
        this.rank = rank;
    }

    public boolean canTransitionTo(OrderStatus nextStatus) {
        // Terminal states cannot transition out (rank -1, 4)
        if (this == DELIVERED || this == CANCELLED) {
            return false;
        }

        // Standard forward progression
        if (nextStatus.rank > this.rank && nextStatus.rank >= 0) {
            return true;
        }

        // Reaching terminal error state (CANCELLED)
        if (nextStatus == CANCELLED) {
            // Can only cancel if not yet in processing/shipping
            return this == PENDING || this == CONFIRMED;
        }

        return false;
    }
}
