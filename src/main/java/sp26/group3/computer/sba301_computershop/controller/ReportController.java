package sp26.group3.computer.sba301_computershop.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.report.InstallmentRevenueResponse;
import sp26.group3.computer.sba301_computershop.dto.response.report.RevenueByProductResponse;
import sp26.group3.computer.sba301_computershop.dto.response.report.RevenueByTimeResponse;
import sp26.group3.computer.sba301_computershop.enums.GroupByType;
import sp26.group3.computer.sba301_computershop.service.ReportService;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class ReportController {

    ReportService reportService;

    // ===================== DOANH THU THEO THỜI GIAN =====================

    @GetMapping("/revenue/time")
    public ApiResponse<RevenueByTimeResponse> getRevenueByTime(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().minusMonths(1)}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now()}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "MONTH") GroupByType groupBy) {
        ApiResponse<RevenueByTimeResponse> response = new ApiResponse<>();
        response.setResult(reportService.getRevenueByTime(fromDate, toDate, groupBy));
        return response;
    }

    @GetMapping("/revenue/time/export")
    public ResponseEntity<byte[]> exportRevenueByTime(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "MONTH") GroupByType groupBy) {
        if (fromDate == null) fromDate = LocalDate.now().minusMonths(1);
        if (toDate == null) toDate = LocalDate.now();

        byte[] file = reportService.exportRevenueByTimeToExcel(fromDate, toDate, groupBy);
        String filename = "doanh-thu-theo-thoi-gian_" + fromDate + "_" + toDate + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    // ===================== DOANH THU THEO SẢN PHẨM =====================

    @GetMapping("/revenue/product")
    public ApiResponse<RevenueByProductResponse> getRevenueByProduct(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "10") int limit) {
        if (fromDate == null) fromDate = LocalDate.now().minusMonths(1);
        if (toDate == null) toDate = LocalDate.now();

        ApiResponse<RevenueByProductResponse> response = new ApiResponse<>();
        response.setResult(reportService.getRevenueByProduct(fromDate, toDate, limit));
        return response;
    }

    @GetMapping("/revenue/product/export")
    public ResponseEntity<byte[]> exportRevenueByProduct(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "10") int limit) {
        if (fromDate == null) fromDate = LocalDate.now().minusMonths(1);
        if (toDate == null) toDate = LocalDate.now();

        byte[] file = reportService.exportRevenueByProductToExcel(fromDate, toDate, limit);
        String filename = "doanh-thu-theo-san-pham_" + fromDate + "_" + toDate + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    // ===================== DOANH THU TRẢ GÓP =====================

    @GetMapping("/revenue/installment")
    public ApiResponse<InstallmentRevenueResponse> getInstallmentRevenue() {
        ApiResponse<InstallmentRevenueResponse> response = new ApiResponse<>();
        response.setResult(reportService.getInstallmentRevenue());
        return response;
    }

    @GetMapping("/revenue/installment/export")
    public ResponseEntity<byte[]> exportInstallmentRevenue() {
        byte[] file = reportService.exportInstallmentRevenueToExcel();
        String filename = "doanh-thu-tra-gop_" + LocalDate.now() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}
