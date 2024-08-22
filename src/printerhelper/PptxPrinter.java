package printerhelper;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import javax.print.*;
import java.awt.*;
import java.awt.print.*;
import java.io.FileInputStream;
import java.io.IOException;

public class PptxPrinter {

    public static void main(String[] args) {
        PptxPrinter printer = new PptxPrinter();
        String filePath = "C:\\Users\\E14\\Desktop\\test.pptx";
        String printerName = ""; // Leave empty for default printer
        try {
            printer.printPptx(filePath, printerName);
        } catch (IOException | PrinterException e) {
            e.printStackTrace();
        }
    }

    public void printPptx(String filePath, String printerName) throws IOException, PrinterException {
        try (XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(filePath))) {
            XSLFSlide slide = ppt.getSlides().get(0); // Get the first slide
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setPrintable(new PptxTextPrintable(slide));

            PrintService printService = getPrintService(printerName);
            if (printService != null) {
                printerJob.setPrintService(printService);
                printerJob.print();
                System.out.println("Slide sent to printer.");
            } else {
                System.out.println("Printer not found.");
            }
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

    static class PptxTextPrintable implements Printable {
        private final XSLFSlide slide;

        public PptxTextPrintable(XSLFSlide slide) {
            this.slide = slide;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            // Clear the graphics
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // Set the starting position
            double xPosition = 20;
            double yPosition = 50;

            // Get slide text shapes
            XSLFTextShape[] textShapes = slide.getPlaceholders();

            for (XSLFTextShape textShape : textShapes) {
                for (XSLFTextParagraph paragraph : textShape.getTextParagraphs()) {
                    for (XSLFTextRun textRun : paragraph.getTextRuns()) {
                        // Extract font properties
                        String fontFamily = textRun.getFontFamily() != null ? textRun.getFontFamily() : "Arial";
                        int fontSize = textRun.getFontSize() != null ? textRun.getFontSize().intValue() : 12;
                        int fontStyle = Font.PLAIN;
                        if (textRun.isBold()) {
                            fontStyle |= Font.BOLD;
                        }
                        if (textRun.isItalic()) {
                            fontStyle |= Font.ITALIC;
                        }
                        Font font = new Font(fontFamily, fontStyle, fontSize);
                        g2d.setFont(font);

                        // Extract text and draw
                        String text = textRun.getRawText();
                        g2d.drawString(text, (int) xPosition, (int) yPosition);

                        // Update yPosition for next line
                        yPosition += g2d.getFontMetrics().getHeight();
                    }
                    // Add space between paragraphs
                    yPosition += 10;
                }
            }

            return PAGE_EXISTS;
        }
    }
}
