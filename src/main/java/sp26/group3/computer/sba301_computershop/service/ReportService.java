package sp26.group3.computer.sba301_computershop.service;

import sp26.group3.computer.sba301_computershop.dto.response.report.InstallmentRevenueResponse;
import sp26.group3.computer.sba301_computershop.dto.response.report.RevenueByProductResponse;
import sp26.group3.computer.sba301_computershop.dto.response.report.RevenueByTimeResponse;
import sp26.group3.computer.sba301_computershop.enums.GroupByType;

import java.time.LocalDate;

public interface ReportService {

    RevenueByTimeResponse getRevenueByTime(LocalDate fromDate, LocalDate toDate, GroupByType groupBy);

    byte[] exportRevenueByTimeToExcel(LocalDate fromDate, LocalDate toDate, GroupByType groupBy);

    RevenueByProductResponse getRevenueByProduct(LocalDate fromDate, LocalDate toDate, int limit);

    byte[] exportRevenueByProductToExcel(LocalDate fromDate, LocalDate toDate, int limit);

    InstallmentRevenueResponse getInstallmentRevenue();

    byte[] exportInstallmentRevenueToExcel();
}
