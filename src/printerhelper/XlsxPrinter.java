package printerhelper;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.print.*;
import java.awt.*;
import java.awt.print.*;
import java.io.FileInputStream;
import java.io.IOException;

public class XlsxPrinter {

    public static void main(String[] args) {
        XlsxPrinter printer = new XlsxPrinter();
        String filePath = "C:\\Users\\E14\\Desktop\\test.xlsx";
        String printerName = ""; // Leave empty for default printer
        try {
            printer.printXlsx(filePath, printerName);
        } catch (IOException | PrinterException e) {
            e.printStackTrace();
        }
    }

    public void printXlsx(String filePath, String printerName) throws IOException, PrinterException {
        @SuppressWarnings("resource")
		Workbook workbook = new XSSFWorkbook(new FileInputStream(filePath));
        Sheet sheet = workbook.getSheetAt(0); // Get the first sheet
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintable(new XlsxPrintable(sheet));

        PrintService printService = getPrintService(printerName);
        if (printService != null) {
            printerJob.setPrintService(printService);
            printerJob.print();
            System.out.println("Document sent to printer.");
        } else {
            System.out.println("Printer not found.");
        }
    }

    private PrintService getPrintService(String printerName) {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        if (printerName.isEmpty()) {
            return PrintServiceLookup.lookupDefaultPrintService();
        }
        for (PrintService service : services) {
            if (service.getName().equalsIgnoreCase(printerName)) {
                return service;
            }
        }
        return null;
    }

    static class XlsxPrintable implements Printable {
        private final Sheet sheet;

        public XlsxPrintable(Sheet sheet) {
            this.sheet = sheet;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            int yPosition = 20; // Start position from top
            int pageHeight = (int) pageFormat.getImageableHeight();
            int lineHeight = 15; // Height of each line
            int columnWidth = 100; // Width of each column

            // Draw rows
            for (Row row : sheet) {
                int xPosition = 20; // Start position from left
                int rowHeight = lineHeight;
                boolean firstCell = true;

                for (Cell cell : row) {
                    if (!firstCell) {
                        xPosition += columnWidth; // Move to the next column
                    }
                    firstCell = false;

                    // Draw cell text
                    String text = getCellText(cell);
                    g2d.drawString(text, xPosition, yPosition + rowHeight);

                    // Draw cell border
                    g2d.drawRect(xPosition - 5, yPosition, columnWidth, rowHeight);

                    // Adjust column width based on cell content
                    int textWidth = g2d.getFontMetrics().stringWidth(text);
                    columnWidth = Math.max(columnWidth, textWidth + 10);
                }
                yPosition += rowHeight; // Move to the next row

                // Stop drawing if yPosition exceeds page height
                if (yPosition > pageHeight - 20) {
                    break;
                }
            }

            return PAGE_EXISTS;
        }

        private String getCellText(Cell cell) {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    return String.valueOf(cell.getNumericCellValue());
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    return cell.getCellFormula();
                default:
                    return "";
            }
        }
    }
}
