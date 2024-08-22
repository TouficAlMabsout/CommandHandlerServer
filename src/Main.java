import javax.print.*;
import javax.print.attribute.*;

import commandhandler.CommandHandler;
import utils.NotificationUtils;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Main {

    public static String printFile(String filePath, String printerName) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return "Error: File path is null or empty.";
        }

        try (InputStream inputStream = new FileInputStream(filePath)) {
            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            Doc document = new SimpleDoc(inputStream, flavor, null);

            PrintService printService;
            if (printerName == null || printerName.trim().isEmpty()) {
                printService = PrintServiceLookup.lookupDefaultPrintService();
                if (printService == null) {
                    return "Error: No default printer found.";
                }
            } else {
                PrintService[] printServices = PrintServiceLookup.lookupPrintServices(flavor, null);
                printService = null;
                for (PrintService service : printServices) {
                    if (printerName.equalsIgnoreCase(service.getName())) {
                        printService = service;
                        break;
                    }
                }
                if (printService == null) {
                    return "Error: Printer named \"" + printerName + "\" not found.";
                }
            }

          /*  DocPrintJob printJob = printService.createPrintJob();
            try {
                PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
                printJob.print(document, attributes);
                return "Print job submitted successfully.";
            } catch (PrintException e) {
                return "Error: Failed to print the document. " + e.getMessage();
            }
            */ PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setPrintService(printService);

            // Display print dialog
            if (!printerJob.printDialog()) {
                return "Print job cancelled by the user.";
            }

            // Print the document
            try {
                printerJob.print();
                return "Print job submitted successfully.";
            } catch (PrinterException e) {
                return "Error: Failed to print the document. " + e.getMessage();
            }

        } catch (IOException e) {
            return "Error: Failed to read the file. " + e.getMessage();
        } catch (Exception e) {
            return "Error: An unexpected error occurred. " + e.getMessage();
        }
    }

    public static void main(String[] args) {
         printFile("C:\\Users\\E14\\Desktop\\allowextensions.txt","");
      }
    }

