package printerhelper;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;


import javax.print.*;
import java.awt.*;
import java.awt.print.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;


public class DocxPrinter {

    public static void main(String[] args) {
        DocxPrinter printer = new DocxPrinter();
        String filePath = "C:\\Users\\E14\\Desktop\\test.docx";
        String printerName = ""; // Leave empty for default printer
        try {
            printer.printDocx(filePath, printerName);
        } catch (IOException | PrinterException e) {
            e.printStackTrace();
        }
    }

    public void printDocx(String filePath, String printerName) throws IOException, PrinterException {
        XWPFDocument document = new XWPFDocument(new FileInputStream(filePath));
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintable(new DocxPrintable(document));

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

    static class DocxPrintable implements Printable {
        private final XWPFDocument document;

        public DocxPrintable(XWPFDocument document) {
            this.document = document;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            List<XWPFParagraph> paragraphs = document.getParagraphs();
			int yPosition = 20; // Start position from top
			int pageHeight = (int) pageFormat.getImageableHeight();

			// Draw paragraphs
			for (XWPFParagraph paragraph : paragraphs) {
			    g2d.drawString(paragraph.getText(), 20, yPosition);
			    yPosition += 15; // Adjust line height

			   

            

			    // Stop drawing if yPosition exceeds page height
			    if (yPosition > pageHeight - 20) {
			        break;
			    }
			}

            return PAGE_EXISTS;
        }
    }
}
