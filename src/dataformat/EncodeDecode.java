package dataformat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.file.Files;

import java.util.Base64;



public class EncodeDecode {
	// Convert base64 string to bytes array
	public static byte[] decodeBase64(String base64String) {
	    return Base64.getDecoder().decode(base64String);
	}
	
	// Main function to encode in base64
    public static String encode(String filePath) {
        File inputFile = new File(filePath);

        try {
            if (!inputFile.exists()) {
                return "Input file does not exist.";
            }

            // Read file to byte array
            byte[] fileBytes = readFileToByteArray(inputFile);

            // Convert byte array to Base64
            String base64Encoded = encodeToBase64(fileBytes);

            return base64Encoded;

        } catch (IOException e) {
            e.printStackTrace();
            return "Error encoding file: " + e.getMessage();
        }
    }
		 
		
	// Convert the file to a byte array
	public static byte[] readFileToByteArray(File file) throws IOException {
		        //to read the file
		        return Files.readAllBytes(file.toPath());
		    }
		  
	// Convert from bytes to Base64
	public static String encodeToBase64(byte[] data) {
		        // convert from bytes to Base64
		        return Base64.getEncoder().encodeToString(data);
	}
	
	// Save our bytes in a file path
	public static File saveBytesToFile(byte[] bytes, String filePath) throws IOException {
	    File file = new File(filePath);
	    try (FileOutputStream foutput = new FileOutputStream(file)) {
	        foutput.write(bytes);
	    }
	    return file;
	}
}
