package utils;

public class HexUtils {

    // Convert a byte array to a hexadecimal string
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Convert a hexadecimal string to a byte array
    public static byte[] hexStringToByteArray(String s) {
        if (s == null || s.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must be non-null and have an even length.");
        }

        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            try {
                int high = Character.digit(s.charAt(i), 16);
                int low = Character.digit(s.charAt(i + 1), 16);

                if (high == -1 || low == -1) {
                    throw new IllegalArgumentException("Invalid hex character.");
                }

                data[i / 2] = (byte) ((high << 4) + low);
            } catch (IndexOutOfBoundsException e) {
               // System.err.println("Error processing hex string at index: " + i);
                throw e;
            }
        }
        return data;
    }
}
