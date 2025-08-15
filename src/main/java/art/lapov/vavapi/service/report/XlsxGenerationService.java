package art.lapov.vavapi.service.report;

import art.lapov.vavapi.model.Reservation;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class XlsxGenerationService {

    private final ReservationRepository reservationRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Generate Excel report for completed reservations as a CLIENT
     * Shows all reservations where the user was the client
     */
    public byte[] generateClientReservationsReport(User client) throws IOException {
        List<Reservation> reservations = reservationRepository.findCompletedReservationsForClient(client);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Mes réservations");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Create header row with French labels
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "N° Réservation",
                    "Date début",
                    "Date fin",
                    "Durée (heures)",
                    "Station",
                    "Adresse",
                    "Ville",
                    "Propriétaire",
                    "Montant (€)",
                    "Date paiement"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 1;
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle euroStyle = createEuroStyle(workbook);

            for (Reservation reservation : reservations) {
                Row row = sheet.createRow(rowNum++);

                // Reservation ID
                row.createCell(0).setCellValue(reservation.getId());

                // Start date
                Cell startDateCell = row.createCell(1);
                startDateCell.setCellValue(reservation.getStartDate().format(DATE_FORMATTER));
                startDateCell.setCellStyle(dateStyle);

                // End date
                Cell endDateCell = row.createCell(2);
                endDateCell.setCellValue(reservation.getEndDate().format(DATE_FORMATTER));
                endDateCell.setCellStyle(dateStyle);

                // Duration in hours
                double duration = java.time.Duration.between(
                        reservation.getStartDate(),
                        reservation.getEndDate()
                ).toMinutes() / 60.0;
                row.createCell(3).setCellValue(String.format("%.2f", duration));

                // Station name
                row.createCell(4).setCellValue(reservation.getStation().getLocation().getName());

                // Address
                row.createCell(5).setCellValue(reservation.getStation().getLocation().getAddress());

                // City
                row.createCell(6).setCellValue(reservation.getStation().getLocation().getCity());

                // Owner name
                User owner = reservation.getStation().getLocation().getOwner();
                row.createCell(7).setCellValue(owner.getFullName());

                // Amount in euros
                Cell amountCell = row.createCell(8);
                double amountEuros = reservation.getTotalCostInCents() / 100.0;
                amountCell.setCellValue(amountEuros);
                amountCell.setCellStyle(euroStyle);

                // Payment date
                if (reservation.getPayment() != null) {
                    Cell paidAtCell = row.createCell(9);
                    paidAtCell.setCellValue(reservation.getPayment().getPaidAt().format(DATE_FORMATTER));
                    paidAtCell.setCellStyle(dateStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add summary row
            addSummaryRow(sheet, rowNum + 1, reservations, true);

            // Convert to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Generate Excel report for completed reservations as an OWNER
     * Shows all reservations for stations owned by the user
     */
    public byte[] generateOwnerReservationsReport(User owner) throws IOException {
        List<Reservation> reservations = reservationRepository.findCompletedReservationsForOwner(owner);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Réservations de mes stations");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Create header row with French labels
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "N° Réservation",
                    "Date début",
                    "Date fin",
                    "Durée (heures)",
                    "Station",
                    "Adresse",
                    "Client",
                    "Email client",
                    "Téléphone client",
                    "Montant (€)",
                    "Date paiement"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 1;
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle euroStyle = createEuroStyle(workbook);

            for (Reservation reservation : reservations) {
                Row row = sheet.createRow(rowNum++);

                // Reservation ID
                row.createCell(0).setCellValue(reservation.getId());

                // Start date
                Cell startDateCell = row.createCell(1);
                startDateCell.setCellValue(reservation.getStartDate().format(DATE_FORMATTER));
                startDateCell.setCellStyle(dateStyle);

                // End date
                Cell endDateCell = row.createCell(2);
                endDateCell.setCellValue(reservation.getEndDate().format(DATE_FORMATTER));
                endDateCell.setCellStyle(dateStyle);

                // Duration in hours
                double duration = java.time.Duration.between(
                        reservation.getStartDate(),
                        reservation.getEndDate()
                ).toMinutes() / 60.0;
                row.createCell(3).setCellValue(String.format("%.2f", duration));

                // Station name
                row.createCell(4).setCellValue(reservation.getStation().getLocation().getName());

                // Station address
                row.createCell(5).setCellValue(
                        reservation.getStation().getLocation().getAddress() != null ?
                                reservation.getStation().getLocation().getAddress() + " " + reservation.getStation().getLocation().getCity() : ""
                );

                // Client name
                User client = reservation.getClient();
                row.createCell(6).setCellValue(client.getFirstName() + " " + client.getLastName());

                // Client email
                row.createCell(7).setCellValue(client.getEmail());

                // Client phone
                row.createCell(8).setCellValue(client.getPhone());

                // Amount in euros
                Cell amountCell = row.createCell(9);
                double amountEuros = reservation.getTotalCostInCents() / 100.0;
                amountCell.setCellValue(amountEuros);
                amountCell.setCellStyle(euroStyle);

                // Payment date
                if (reservation.getPaidAt() != null) {
                    Cell paidAtCell = row.createCell(10);
                    paidAtCell.setCellValue(reservation.getPaidAt().format(DATE_FORMATTER));
                    paidAtCell.setCellStyle(dateStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add summary row
            addSummaryRow(sheet, rowNum + 1, reservations, false);

            // Convert to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Create header style for Excel
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
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

    /**
     * Create date style for Excel cells
     */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Create euro currency style for Excel cells
     */
    private CellStyle createEuroStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00 €"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    /**
     * Add summary row with totals
     */
    private void addSummaryRow(Sheet sheet, int rowNum, List<Reservation> reservations, boolean isClientReport) {
        if (reservations.isEmpty()) return;

        // Skip one row for spacing
        rowNum++;

        Row summaryRow = sheet.createRow(rowNum);
        CellStyle boldStyle = sheet.getWorkbook().createCellStyle();
        Font boldFont = sheet.getWorkbook().createFont();
        boldFont.setBold(true);
        boldStyle.setFont(boldFont);

        // Add "TOTAL" label
        Cell labelCell = summaryRow.createCell(0);
        labelCell.setCellValue("TOTAL");
        labelCell.setCellStyle(boldStyle);

        // Calculate total amount
        double totalEuros = reservations.stream()
                .mapToInt(Reservation::getTotalCostInCents)
                .sum() / 100.0;

        // Position of amount column differs between client and owner reports
        int amountColumn = isClientReport ? 8 : 9;
        Cell totalCell = summaryRow.createCell(amountColumn);
        totalCell.setCellValue(totalEuros);

        CellStyle euroStyle = sheet.getWorkbook().createCellStyle();
        DataFormat format = sheet.getWorkbook().createDataFormat();
        euroStyle.setDataFormat(format.getFormat("#,##0.00 €"));
        euroStyle.setAlignment(HorizontalAlignment.RIGHT);
        euroStyle.setFont(boldFont);
        totalCell.setCellStyle(euroStyle);

        // Add reservation count
        Row countRow = sheet.createRow(rowNum + 1);
        Cell countLabelCell = countRow.createCell(0);
        countLabelCell.setCellValue("Nombre de réservations:");
        countLabelCell.setCellStyle(boldStyle);

        Cell countValueCell = countRow.createCell(1);
        countValueCell.setCellValue(reservations.size());
    }
}