package backend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HexFormat;

/**
 * This class contains utility methods for converting between different
 * representations of data, specfically hexademical and base64 for image
 * presentation.
 */
public class HexUtils {

    /**
     * Converts a file to a hexadecimal string.
     * 
     * @param file the file to convert
     * @return the hexadecimal string
     */
    public static String fileToHex(File file) {
        try {
            byte[] fileBytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
            return HexFormat.of().formatHex(fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts a hexadecimal string to a file.
     * 
     * @param hex  the hexadecimal string
     * @param file the file to write to
     * @throws IOException
     */
    public static void hexToFile(String hex, File file) throws IOException {
        Files.write(Paths.get(file.getAbsolutePath()), HexFormat.of().parseHex(hex));
    }

    /**
     * Converts a hexadecimal string to a base64 string.
     * 
     * @param hexString the hexadecimal string
     * @return the base64 string
     */
    public static String hexToBase64(String hexString) {
        // Convert hexadecimal to bytes
        byte[] byteData = hexStringToByteArray(hexString);

        // Encode bytes in base64
        byte[] base64Data = Base64.getEncoder().encode(byteData);

        // Convert bytes to string
        String base64String = new String(base64Data);

        return base64String;
    }

    /**
     * Converts a base64 string to a hexadecimal string.
     * 
     * @param base64String the base64 string
     * @return the hexadecimal string
     */
    public static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
}
