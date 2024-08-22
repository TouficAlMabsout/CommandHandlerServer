package cryptography;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import commandhandler.CommandHandler;
import utils.HexUtils;

import javax.crypto.spec.IvParameterSpec;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class AES {
	 // Defining the algorithm
	 private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
	 
	// Function to Decrypt the password and decrypt the username and password
	public static boolean Decrypt(String username,String encPassword) throws Exception{
		String filePath = "C:\\Users\\E14\\Desktop\\Properties\\info.properties";
		String[] properties = getProperties(filePath);
		String key = properties[0];String encUsername = properties[1]; String encPasswordFile = properties[2];String IV = properties[3];
		
		byte[] keyBytes = HexUtils.hexStringToByteArray(key);
	    SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

	    // Static IV
	    byte[] ivBytes = HexUtils.hexStringToByteArray(IV);
	    IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

	    // Decrypt the values
	    String decryptedUsernameHex = decryptFromHex(encUsername, secretKey, ivSpec);
	    String decryptedPasswordFileHex = decryptFromHex(encPasswordFile, secretKey, ivSpec);
	    String decryptedPasswordHex = decryptFromHex(encPassword,secretKey,ivSpec);
	    
	    //Get the username in ASCII
	    // Convert hexadecimal string to bytes
	    byte[] decryptedUsernameBytes = HexUtils.hexStringToByteArray(decryptedUsernameHex);
	    byte[] decryptedPasswordFileBytes = HexUtils.hexStringToByteArray(decryptedPasswordFileHex);
	    byte[] decryptedPasswordBytes = HexUtils.hexStringToByteArray(decryptedPasswordHex);
	    
	    // Convert bytes to a string
	    String decryptedUsernameString = new String(decryptedUsernameBytes, StandardCharsets.UTF_8);
	    String decryptedPasswordFileString = new String(decryptedPasswordFileBytes, StandardCharsets.UTF_8);
	    String decryptedPasswordString = new String(decryptedPasswordBytes, StandardCharsets.UTF_8);

	    // System.out.println("Decrypted Username: " + decryptedUsernameString);
	    // System.out.println("Decrypted Password From File: " + decryptedPasswordFileString);
	    // System.out.println("Dcerypted Password From User: " + decryptedPasswordString);
		
	    if (decryptedUsernameString.equals(username) && decryptedPasswordFileString.equals(decryptedPasswordString)){
	    	CommandHandler.logger.info("user authenticated");
	    	return true;
	    }
	    CommandHandler.logger.info("user not authenticated");
		return false;
	}
	
	 // Decrypt from Hex and return it as Hex
	 public static String decryptFromHex(String encryptedValueHex, SecretKeySpec key, IvParameterSpec iv) throws Exception {
	        Cipher cipher = Cipher.getInstance(ALGORITHM);
	        cipher.init(Cipher.DECRYPT_MODE, key, iv);
	        byte[] encryptedBytes = HexUtils.hexStringToByteArray(encryptedValueHex);
	        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
	        return HexUtils.bytesToHex(decryptedBytes);
	    }
	
	// Function to retrieve the properties from the given file path
	public static String[] getProperties(String filePath) {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            // Load the properties file
            properties.load(fis);
            
            // Get the key from the properties file
            String key = properties.getProperty("aes.key");
            String encUsername = properties.getProperty("aes.username");
            String encPassword = properties.getProperty("aes.password");
            String IV = properties.getProperty("aes.IV");
            // Return the key (already in hexadecimal format)
            return new String [] {key,encUsername,encPassword,IV};
        } catch (IOException e) {
            e.printStackTrace();
            return null;  // Handle this case as needed
        }
    }
	
	
	
}
