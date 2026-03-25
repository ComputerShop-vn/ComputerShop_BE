package sp26.group3.computer.sba301_computershop.repository.projection;

public interface InstallmentSummaryProjection {
    Double getTotalPaid();
    Double getTotalUnpaid();
    Double getTotalOverdue();
}
