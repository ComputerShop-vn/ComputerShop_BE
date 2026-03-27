package sp26.group3.computer.sba301_computershop.repository.projection;

public interface ProductRevenueProjection {
    String getProductName();
    String getVariantName();
    Long getTotalSold();
    Double getRevenue();
}
