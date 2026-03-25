package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import sp26.group3.computer.sba301_computershop.dto.response.report.*;
import sp26.group3.computer.sba301_computershop.enums.GroupByType;
import sp26.group3.computer.sba301_computershop.repository.OrderItemRepository;
import sp26.group3.computer.sba301_computershop.repository.OrderPaymentScheduleRepository;
import sp26.group3.computer.sba301_computershop.repository.OrderRepository;
import sp26.group3.computer.sba301_computershop.repository.projection.InstallmentOrderProjection;
import sp26.group3.computer.sba301_computershop.repository.projection.InstallmentSummaryProjection;
import sp26.group3.computer.sba301_computershop.repository.projection.ProductRevenueProjection;
import sp26.group3.computer.sba301_computershop.repository.projection.RevenuePeriodProjection;
import sp26.group3.computer.sba301_computershop.service.ReportService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportServiceImpl implements ReportService {

    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    OrderPaymentScheduleRepository orderPaymentScheduleRepository;

    // ===================== DOANH THU THEO THỜI GIAN =====================

    @Override
    public RevenueByTimeResponse getRevenueByTime(LocalDate fromDate, LocalDate toDate, GroupByType groupBy) {
        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime to = toDate.atTime(23, 59, 59);

        List<RevenuePeriodProjection> projections = switch (groupBy) {
            case DAY -> orderRepository.findRevenueGroupByDay(from, to);
            case MONTH -> orderRepository.findRevenueGroupByMonth(from, to);
            case YEAR -> orderRepository.findRevenueGroupByYear(from, to);
        };

        List<RevenuePeriodDto> breakdown = projections.stream()
                .map(p -> RevenuePeriodDto.builder()
                        .period(p.getPeriod())
                        .revenue(p.getRevenue() != null ? p.getRevenue() : 0)
                        .orderCount(p.getOrderCount() != null ? p.getOrderCount() : 0)
                        .build())
                .toList();

        double totalRevenue = breakdown.stream().mapToDouble(RevenuePeriodDto::getRevenue).sum();
        long totalOrders = breakdown.stream().mapToLong(RevenuePeriodDto::getOrderCount).sum();

        return RevenueByTimeResponse.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .groupBy(groupBy.name())
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .breakdown(breakdown)
                .build();
    }

    @Override
    public byte[] exportRevenueByTimeToExcel(LocalDate fromDate, LocalDate toDate, GroupByType groupBy) {
        RevenueByTimeResponse data = getRevenueByTime(fromDate, toDate, groupBy);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Doanh thu theo thời gian");
            sheet.setColumnWidth(0, 5000);
            sheet.setColumnWidth(1, 7000);
            sheet.setColumnWidth(2, 5000);

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle infoStyle = createInfoStyle(workbook);

            int rowIdx = 0;

            // Title
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BÁO CÁO DOANH THU THEO THỜI GIAN");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

            // Info rows
            rowIdx = writeInfoRow(sheet, rowIdx, infoStyle,
                    "Từ ngày: " + fromDate + "   Đến ngày: " + toDate + "   Nhóm theo: " + groupBy.name());
            rowIdx = writeInfoRow(sheet, rowIdx, infoStyle,
                    "Tổng doanh thu: " + formatVnd(data.getTotalRevenue()) + "   Tổng đơn hàng: " + data.getTotalOrders());
            rowIdx++; // empty row

            // Header
            Row header = sheet.createRow(rowIdx++);
            String[] headers = {"STT", "Kỳ", "Doanh thu (VNĐ)", "Số đơn hàng"};
            sheet.setColumnWidth(3, 4000);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int stt = 1;
            for (RevenuePeriodDto dto : data.getBreakdown()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue(dto.getPeriod());
                Cell revenueCell = row.createCell(2);
                revenueCell.setCellValue(dto.getRevenue());
                revenueCell.setCellStyle(currencyStyle);
                row.createCell(3).setCellValue(dto.getOrderCount());
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi tạo file Excel báo cáo doanh thu theo thời gian", e);
        }
    }

    // ===================== DOANH THU THEO SẢN PHẨM =====================

    @Override
    public RevenueByProductResponse getRevenueByProduct(LocalDate fromDate, LocalDate toDate, int limit) {
        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime to = toDate.atTime(23, 59, 59);

        List<ProductRevenueProjection> projections = orderItemRepository.findTopProducts(from, to);

        List<TopProductDto> products = projections.stream()
                .limit(limit)
                .map(p -> TopProductDto.builder()
                        .productName(p.getProductName())
                        .variantName(p.getVariantName())
                        .totalSold(p.getTotalSold() != null ? p.getTotalSold() : 0)
                        .revenue(p.getRevenue() != null ? p.getRevenue() : 0)
                        .build())
                .toList();

        double totalRevenue = products.stream().mapToDouble(TopProductDto::getRevenue).sum();

        return RevenueByProductResponse.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .totalRevenue(totalRevenue)
                .products(products)
                .build();
    }

    @Override
    public byte[] exportRevenueByProductToExcel(LocalDate fromDate, LocalDate toDate, int limit) {
        RevenueByProductResponse data = getRevenueByProduct(fromDate, toDate, limit);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Doanh thu theo sản phẩm");
            sheet.setColumnWidth(0, 3000);
            sheet.setColumnWidth(1, 10000);
            sheet.setColumnWidth(2, 7000);
            sheet.setColumnWidth(3, 5000);
            sheet.setColumnWidth(4, 7000);

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle infoStyle = createInfoStyle(workbook);

            int rowIdx = 0;

            // Title
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BÁO CÁO DOANH THU THEO SẢN PHẨM");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            // Info
            rowIdx = writeInfoRow(sheet, rowIdx, infoStyle,
                    "Từ ngày: " + fromDate + "   Đến ngày: " + toDate);
            rowIdx = writeInfoRow(sheet, rowIdx, infoStyle,
                    "Tổng doanh thu: " + formatVnd(data.getTotalRevenue()));
            rowIdx++;

            // Header
            Row header = sheet.createRow(rowIdx++);
            String[] headers = {"STT", "Tên sản phẩm", "Biến thể", "Số lượng bán", "Doanh thu (VNĐ)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data
            int stt = 1;
            for (TopProductDto dto : data.getProducts()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue(dto.getProductName());
                row.createCell(2).setCellValue(dto.getVariantName());
                row.createCell(3).setCellValue(dto.getTotalSold());
                Cell revenueCell = row.createCell(4);
                revenueCell.setCellValue(dto.getRevenue());
                revenueCell.setCellStyle(currencyStyle);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi tạo file Excel báo cáo doanh thu theo sản phẩm", e);
        }
    }

    // ===================== DOANH THU TRẢ GÓP =====================

    @Override
    public InstallmentRevenueResponse getInstallmentRevenue() {
        InstallmentSummaryProjection summaryProj = orderPaymentScheduleRepository.getInstallmentSummary();

        double totalPaid = summaryProj.getTotalPaid() != null ? summaryProj.getTotalPaid() : 0;
        double totalUnpaid = summaryProj.getTotalUnpaid() != null ? summaryProj.getTotalUnpaid() : 0;
        double totalOverdue = summaryProj.getTotalOverdue() != null ? summaryProj.getTotalOverdue() : 0;

        InstallmentSummaryDto summary = InstallmentSummaryDto.builder()
                .totalPaid(totalPaid)
                .totalUnpaid(totalUnpaid)
                .totalOverdue(totalOverdue)
                .total(totalPaid + totalUnpaid + totalOverdue)
                .build();

        List<InstallmentOrderProjection> orderProjs = orderPaymentScheduleRepository.findInstallmentOrderDetails();

        List<InstallmentOrderDetailDto> orders = orderProjs.stream()
                .map(p -> {
                    long total = p.getTotalInstallments() != null ? p.getTotalInstallments() : 0;
                    long paid = p.getPaidInstallments() != null ? p.getPaidInstallments() : 0;
                    return InstallmentOrderDetailDto.builder()
                            .orderId(p.getOrderId())
                            .customerUsername(p.getCustomerUsername())
                            .orderTotal(p.getOrderTotal() != null ? p.getOrderTotal() : 0)
                            .totalInstallments(total)
                            .paidInstallments(paid)
                            .remainingInstallments(total - paid)
                            .nextDueDate(p.getNextDueDate())
                            .build();
                })
                .toList();

        return InstallmentRevenueResponse.builder()
                .summary(summary)
                .orders(orders)
                .build();
    }

    @Override
    public byte[] exportInstallmentRevenueToExcel() {
        InstallmentRevenueResponse data = getInstallmentRevenue();

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle infoLabelStyle = createInfoLabelStyle(workbook);
            CellStyle infoValueStyle = createInfoValueStyle(workbook);

            // ---- Sheet 1: Tổng quan ----
            Sheet summarySheet = workbook.createSheet("Tổng quan trả góp");
            summarySheet.setColumnWidth(0, 8000);
            summarySheet.setColumnWidth(1, 7000);

            int rowIdx = 0;
            Row titleRow = summarySheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("TỔNG QUAN DOANH THU TRẢ GÓP");
            titleCell.setCellStyle(titleStyle);
            summarySheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

            rowIdx++;
            rowIdx = writeSummaryRow(summarySheet, rowIdx, infoLabelStyle, infoValueStyle,
                    "Tổng đã thu (PAID)", data.getSummary().getTotalPaid(), currencyStyle);
            rowIdx = writeSummaryRow(summarySheet, rowIdx, infoLabelStyle, infoValueStyle,
                    "Tổng chưa thu (UNPAID)", data.getSummary().getTotalUnpaid(), currencyStyle);
            rowIdx = writeSummaryRow(summarySheet, rowIdx, infoLabelStyle, infoValueStyle,
                    "Tổng quá hạn (OVERDUE)", data.getSummary().getTotalOverdue(), currencyStyle);
            rowIdx++;
            writeSummaryRow(summarySheet, rowIdx, infoLabelStyle, infoValueStyle,
                    "TỔNG CỘNG", data.getSummary().getTotal(), currencyStyle);

            // ---- Sheet 2: Chi tiết đơn trả góp ----
            Sheet detailSheet = workbook.createSheet("Chi tiết đơn trả góp");
            detailSheet.setColumnWidth(0, 3000);
            detailSheet.setColumnWidth(1, 4000);
            detailSheet.setColumnWidth(2, 6000);
            detailSheet.setColumnWidth(3, 7000);
            detailSheet.setColumnWidth(4, 5000);
            detailSheet.setColumnWidth(5, 5000);
            detailSheet.setColumnWidth(6, 5000);
            detailSheet.setColumnWidth(7, 6000);

            rowIdx = 0;
            Row detailTitle = detailSheet.createRow(rowIdx++);
            Cell detailTitleCell = detailTitle.createCell(0);
            detailTitleCell.setCellValue("CHI TIẾT ĐƠN HÀNG TRẢ GÓP");
            detailTitleCell.setCellStyle(titleStyle);
            detailSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
            rowIdx++;

            Row header = detailSheet.createRow(rowIdx++);
            String[] headers = {"STT", "Mã đơn", "Khách hàng", "Tổng đơn (VNĐ)",
                    "Tổng kỳ", "Đã trả", "Còn lại", "Ngày đến hạn"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int stt = 1;
            for (InstallmentOrderDetailDto dto : data.getOrders()) {
                Row row = detailSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue(dto.getOrderId());
                row.createCell(2).setCellValue(dto.getCustomerUsername());
                Cell totalCell = row.createCell(3);
                totalCell.setCellValue(dto.getOrderTotal());
                totalCell.setCellStyle(currencyStyle);
                row.createCell(4).setCellValue(dto.getTotalInstallments());
                row.createCell(5).setCellValue(dto.getPaidInstallments());
                row.createCell(6).setCellValue(dto.getRemainingInstallments());
                row.createCell(7).setCellValue(dto.getNextDueDate() != null ? dto.getNextDueDate() : "Đã hoàn tất");
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi tạo file Excel báo cáo doanh thu trả góp", e);
        }
    }

    // ===================== EXCEL STYLE HELPERS =====================

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        return style;
    }

    private CellStyle createInfoStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setItalic(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createInfoLabelStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createInfoValueStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        return style;
    }

    private int writeInfoRow(Sheet sheet, int rowIdx, CellStyle style, String text) {
        Row row = sheet.createRow(rowIdx);
        Cell cell = row.createCell(0);
        cell.setCellValue(text);
        cell.setCellStyle(style);
        return rowIdx + 1;
    }

    private int writeSummaryRow(Sheet sheet, int rowIdx, CellStyle labelStyle,
                                 CellStyle valueStyle, String label, double value,
                                 CellStyle currencyStyle) {
        Row row = sheet.createRow(rowIdx);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(currencyStyle);
        return rowIdx + 1;
    }

    private String formatVnd(double amount) {
        return String.format("%,.0f VNĐ", amount);
    }
}
