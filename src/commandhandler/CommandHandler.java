package commandhandler;

import javax.print.*;
import javax.print.attribute.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import cryptography.AES;
import dataformat.EncodeDecode;
import file.FileInfo;
import file.FileInfoList;
import file.ListFilesInDirectory;
import printerhelper.DocxPrinter;
import printerhelper.PptxPrinter;
import printerhelper.XlsxPrinter;
import utils.NotificationUtils;
import utils.XmlUtils;

import javax.jws.WebMethod;
import javax.jws.WebService;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


// Consumer side server
@WebService(name="EncodeFile",targetNamespace="http://encode/")
public class CommandHandler {
	
	 public static final Logger logger = Logger.getLogger(CommandHandler.class.getName());

	 static {
        try {
            // Create a file handler that writes log messages to a file
        	String propertyPath = "C:\\Users\\E14\\Desktop\\Properties\\info.properties";
        	String logFilePath = getLogPath(propertyPath);
            FileHandler fileHandler = new FileHandler(logFilePath,10*1024,1, true); // Append mode
            fileHandler.setFormatter(new SimpleFormatter()); // Use a simple text format
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	 // Method to send the file path to the application server
	@WebMethod(operationName = "sendFilePath")
	 public String receivePath(String fileInfosXml, String command, String printerName,String directory, String flag,String fileRetrieve,String message,String username,String encPassword) throws Exception{
		logger.info("\n\n\n");
		logger.info("command: "+command);
		logger.info("printer name: "+printerName);
		logger.info("directory: "+directory);
		logger.info("flag :"+flag);
		logger.info("file to retrieve: "+fileRetrieve);
		logger.info("message: "+message);
		logger.info("username: "+username);
		logger.info("encrypted password: "+encPassword);
		
		boolean validCredentials = ValidateCredentials(username,encPassword);
		if (validCredentials){
			
			// Handle retrieve case
			if (command.equals("retrieve")){
				return retrieveFile(command,directory,fileRetrieve);
			}
			// Handle notification case
			else if (command.equals("message")){
				return popNotification(message);
			}
			// Handle Print or Transfer since they are similar
			else{

			// System.out.println(fileInfosXml);
			
			// Parse the XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(fileInfosXml)));
            
            // Get all <fileInfos> elements
            NodeList fileInfosList = doc.getElementsByTagName("fileInfos");
            for (int i = 1; i < fileInfosList.getLength(); i++) {
                Element fileInfo = (Element) fileInfosList.item(i);
                String fileBase64 = fileInfo.getElementsByTagName("fileBase64").item(0).getTextContent();
                String fileName = fileInfo.getElementsByTagName("fileName").item(0).getTextContent();
                // System.out.println("fileBase64: " + fileBase64);
                // System.out.println("fileName: " + fileName);
                // System.out.println("---");
             if (ValidateExtension(fileName)){
		    try {
		    	String filePath = (directory.isEmpty()) ? "C:\\Users\\E14\\Desktop\\TestingPrint\\"+fileName : directory+"\\"+fileName;
		    	// Retrieve command requires the argument fileRetrieve
	
		        //Generate Directory if it does not exist
		        File file = new File(filePath);
		        File parentDir = file.getParentFile();
	
		        if (parentDir != null && !parentDir.exists()) {
		            if (parentDir.mkdirs()) {
		            	logger.info("directory created: "+parentDir.getAbsolutePath());
		                // System.out.println("Directory created: " + parentDir.getAbsolutePath());
		            } else {
		                // System.out.println("Failed to create directory: " + parentDir.getAbsolutePath());
		                logger.info("failed to create directory");
		               
		            }
		        }
		        
				 // Convert the base64 string to byte array
		        byte[] responseBytes = EncodeDecode.decodeBase64(fileBase64);
		        // Save the content of the received content into a file
		        EncodeDecode.saveBytesToFile(responseBytes, filePath);
		 	       
		          
		      
		        // System.out.println(fileName+" "+command+" "+" "+fileBase64);
		        logger.info("file name: "+fileName);
		        // Perform the print command or other operations as needed
		        if (command.equals("print")){
		        	if (filePath.endsWith(".pdf") || filePath.endsWith(".txt") || filePath.endsWith(".csv")||
		        		filePath.endsWith(".html") || filePath.endsWith(".htm")){
		                  printFileDefault(filePath, printerName,flag);
		        	}
		        	else if (filePath.endsWith(".docx")){
		        		DocxPrinter docxPrinter = new DocxPrinter();
		        		docxPrinter.printDocx(filePath, printerName);
		        	}
		        	else if (filePath.endsWith(".xlsx")){
		        		XlsxPrinter xlsxPrinter = new XlsxPrinter();
		        		xlsxPrinter.printXlsx(filePath, printerName);
		        	}
		        	else if (filePath.endsWith(".pptx")){
		        		PptxPrinter pptxPrinter = new PptxPrinter();
		        		pptxPrinter.printPptx(filePath, printerName);
		        	}
		        		
		        	}
		           
		        
		        else if (command.equals("transfer")){
		        	logger.info("file transfered");
		        }
		      
		        else{
		        	logger.info("unavailable command");
		        	return "unavailable command";
		        }
		
		     
	
		   }catch (Exception e) {
		        e.printStackTrace();
		        logger.info("Error occurred: " + e.getMessage());
		        return "Error occurred: " + e.getMessage();
		     }
		} 
             else {
            	 logger.info("invalid extension for file: "+fileName);
             }
            }
	    return command + " done successfully";
		  }
		}
	else{
		logger.info("Invalid credentials, unable to perform the requested action.");
		return "Invalid credentials, unable to perform the requested action.";
	}
	
	
}
	

	// Function that retrieves the file/folder
	 public String retrieveFile(String command, String directory, String fileRetrieve) throws Exception {
		 
		 	// Create the List of FileInfo and store this List in an object called retrieveList that has
			// As attribute a List of FileInfo
			FileInfoList retrieveList = new FileInfoList();
			List<FileInfo> retrieve = new ArrayList<>();
			
			// If fileRetrieve is Empty, then handle the case when the user wants a folder
			if (fileRetrieve.isEmpty()){
				
				// Function to retrieve all file names in the given directory
				List<String> fileNames = ListFilesInDirectory.getFileNames(directory);
				
				// Loop over the fileNames, generate the path and then appply base64 encoding and store
				// The enconding with the name of the file in the object FileInfo and add it to the list
				for (String fileName : fileNames){
					String filePath = (directory.isEmpty()) ? "C:\\Users\\E14\\Desktop\\TestingPrint\\"+fileName : directory+"\\"+fileName;
					logger.info("file name: "+fileName+"\ncommand: "+command);
					// System.out.println(filePath);
		        	String encodeBase64 = EncodeDecode.encode(filePath);
		        	logger.info("file "+filePath+" retrieved");
		        	Path path = Paths.get(filePath);
		            String fileNameRetrieve = path.getFileName().toString();
		        	FileInfo fileInfoRetrieve = new FileInfo();
		        	fileInfoRetrieve.setFileBase64(encodeBase64);fileInfoRetrieve.setFileName(fileNameRetrieve);
		        	if (ValidateExtension(fileNameRetrieve)) retrieve.add(fileInfoRetrieve);
		        	else logger.info("invalid extension for file: "+fileNameRetrieve);
		       
				}
				
				// Finally set the list and convert it to Xml then return it
	        	retrieveList.setFileInfos(retrieve);
	        	String fileInfosXmlRetrieve = XmlUtils.convertFileInfoListToXml(retrieveList);
	        	return fileInfosXmlRetrieve;
		}
			
		// Same case but taking one file which is fileRetrieve from the command
		else{
			
			String filePath = (directory.isEmpty()) ? "C:\\Users\\E14\\Desktop\\TestingPrint\\"+fileRetrieve : directory+"\\"+fileRetrieve;
			logger.info("file name: "+fileRetrieve+"\ncommand: "+command);
			// System.out.println(filePath);
	     	String encodeBase64 = EncodeDecode.encode(filePath);
	     	logger.info("file "+filePath+" retrieved");
	     	Path path = Paths.get(filePath);
	         String fileNameRetrieve = path.getFileName().toString();
	     	FileInfo fileInfoRetrieve = new FileInfo();
	     	fileInfoRetrieve.setFileBase64(encodeBase64);fileInfoRetrieve.setFileName(fileNameRetrieve);
	     	if (ValidateExtension(fileNameRetrieve)) retrieve.add(fileInfoRetrieve);
	     	else logger.info("invalid extension for file: "+fileNameRetrieve);
	     	retrieveList.setFileInfos(retrieve);
	     	String fileInfosXmlRetrieve = XmlUtils.convertFileInfoListToXml(retrieveList);
	     	return fileInfosXmlRetrieve;
		}
		
	}

	 // Prints the file and return to the user the what happened
	 public String printFileDefault(String filePath, String printerName,String flag) {
	    File file = new File(filePath);
	    if (!file.exists()) {
	    	logger.info("File not found");
	        return "File not found: " + filePath;
	    }

	    if (filePath == null || filePath.trim().isEmpty()) {
	    	logger.info("Error: File path is null or empty.");
            return "Error: File path is null or empty.";
        }

        try (InputStream inputStream = new FileInputStream(filePath)) {
            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            Doc document = new SimpleDoc(inputStream, flavor, null);

            PrintService printService;
            if (printerName == null || printerName.trim().isEmpty()) {
                printService = PrintServiceLookup.lookupDefaultPrintService();
                if (printService == null) {
                	logger.info("Error: No default printer found.");
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
                	logger.info("Error: Printer named \"" + printerName + "\" not found.");
                    return "Error: Printer named \"" + printerName + "\" not found.";
                }
            }

            DocPrintJob printJob = printService.createPrintJob();
            try {
                PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
                printJob.print(document, attributes);
                logger.info("Print submitted");
                return "Print job submitted successfully.";
               
            } catch (PrintException e) {
                return "Error: Failed to print the document. " + e.getMessage();
            }

        } catch (IOException e) {
        	logger.info("Error: Failed to read the file. ");
            return "Error: Failed to read the file. " + e.getMessage();
        } catch (Exception e) {
        	logger.info("Error: An unexpected error occurred. ");
            return "Error: An unexpected error occurred. " + e.getMessage();
        }
	    finally{
	    	switch (flag){
        	case "keep" : logger.info("file kept"); break; 
        	case "move": logger.info("file moved");transferFileToArchive(filePath);break;
        	case "delete": logger.info("file deleted");// System.out.println("deleting");
        	deleteFile(filePath);
        	break;
        	default: break;
        	
        }
	    }
	
	}

	 // Transfer the file to the same filePath but in an archive folder   
	 public void transferFileToArchive(String filePath) {
	        File file = new File(filePath);

	        // Check if the file exists
	        if (!file.exists()) {
	            // System.out.println("File not found: " + filePath);
	            return;
	        }

	        // Get the parent directory and define the archive subdirectory
	        File parentDir = file.getParentFile();
	        File archiveDir = new File(parentDir, "archive");

	        // Create the archive directory if it does not exist
	        if (!archiveDir.exists()) {
	            if (archiveDir.mkdirs()) {
	                // System.out.println("Archive directory created: " + archiveDir.getAbsolutePath());
	            } else {
	                // System.out.println("Failed to create archive directory: " + archiveDir.getAbsolutePath());
	                return;
	            }
	        }

	        // Define the target path and source path in the archive directory
	       Path sourcePath = file.toPath();
	       Path targetPath = archiveDir.toPath().resolve(file.getName());

	        try {
	            // Move the file to the archive directory
	            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
	            // System.out.println("File moved to archive: " + targetPath.toString());
	        } catch (IOException e) {
	            e.printStackTrace();
	            // System.out.println("An error occurred while moving the file.");
	        }
	 }
	 
	 // Delete the file
	 public static void deleteFile(String filePath) {
	        Path path = Paths.get(filePath);

	        // Print the absolute path of the file
	        // System.out.println("Attempting to delete file at: " + path.toAbsolutePath());

	        try {
	            if (Files.exists(path)) {
	                // Attempt to delete the file
	                Files.delete(path);
	                // System.out.println("File successfully deleted: " + filePath);
	            } else {
	                // System.out.println("File does not exist: " + filePath);
	            }
	        } catch (java.nio.file.NoSuchFileException e) {
	            // System.out.println("File not found: " + filePath);
	        } catch (java.io.IOException e) {
	            // System.out.println("IOException occurred while deleting the file: " + filePath);
	            e.printStackTrace();
	        } catch (Exception e) {
	            // System.out.println("Unexpected error occurred while deleting the file: " + filePath);
	            e.printStackTrace();
	        }
	 }
	 
	 // Function that calls AES class and validates user credentials
	 public boolean ValidateCredentials(String username, String encPassword) throws Exception{
		 return AES.Decrypt(username, encPassword);
	 }
	 
	 // Function to retrieve the properties from the given file path
	 public static String getLogPath(String filePath) {
	        Properties properties = new Properties();
	        try (FileInputStream fis = new FileInputStream(filePath)) {
	            // Load the properties file
	            properties.load(fis);
	            
	            // Get the key from the properties file
	            String logPath = properties.getProperty("log.path");
	            // Return the key
	            return logPath;
	        } catch (IOException e) {
	            e.printStackTrace();
	            return null;  // Handle this case as needed
	        }
	    }
	 
	 // Function to store allowed extensions in a List
	 public static List<String> getAllowedExtensions(String filePath) {
	        Properties properties = new Properties();
	        List<String> extensionsList = new ArrayList<>();
	        
	        try (FileInputStream fis = new FileInputStream(filePath)) {
	            // Load the properties file
	            properties.load(fis);
	            
	            // Get the value for the key 'allowedExtensions'
	            String allowedExtensions = properties.getProperty("allowedExtensions");
	            
	            if (allowedExtensions != null && !allowedExtensions.trim().isEmpty()) {
	                // split the string by spaces
	                String[] extensionsArray = allowedExtensions.split("\\s+");
	                
	                // Add each extension to the list
	                for (String extension : extensionsArray) {           
	                    extensionsList.add(extension);
	                }
	            }
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	            return null;  // Handle this case as needed
	        }
	        
	        return extensionsList;
	    }
	 
	 // Function to validate extensions
	 public boolean ValidateExtension(String fileName){
		 String propertyPath = "C:\\Users\\E14\\Desktop\\Properties\\info.properties";
		 List<String> allowedExtensions = getAllowedExtensions(propertyPath);
		 String extension = getFileExtension(fileName);
		 return allowedExtensions.contains(extension);
		 
		
	 }
	
	 // Helper method to get file extension with the dot
    public static String getFileExtension(String fileName) {
        // Find the last dot in the file name
        int dotIndex = fileName.lastIndexOf('.');
        
        // If there's no dot or the dot is the first character, return an empty string
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return "";
        }
        
        // Return the file extension including the dot
        return fileName.substring(dotIndex); // Includes the dot
    }
	 
	 // Method to display the notification
	 public String popNotification(String message) {
		
		logger.info("Notification called");
		// System.out.println(message);
		return NotificationUtils.popNotification(message);
	}
}
    

