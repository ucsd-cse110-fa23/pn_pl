package backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;

import java.io.File;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;

/**
 * Test for HexUtils.java
 * Justin
 */
public class HexUtilsTest {

    private File testFile;
    private String testHexString;
    private String testBase64String;

    /**
     * Sets up HexUtils for testing.
     */
    @BeforeEach
    public void setUp() throws IOException {
        // Create a temporary test file
        testFile = File.createTempFile("test", ".txt");
        Files.write(testFile.toPath(), "Hello, World!".getBytes());

        // Hexadecimal representation of "Hello, World!"
        testHexString = "48656c6c6f2c20576f726c6421";

        // Base64 representation of "Hello, World!"
        testBase64String = "SGVsbG8sIFdvcmxkIQ==";
    }

    /**
     * Deletes test files after done with testing
     */
    @AfterEach
    public void tearDown() {
        // Delete the temporary test file
        if (testFile.exists())
            testFile.delete();
    }

    /**
     * Tests the fileToHex() function
     */
    @Test
    public void testFileToHex() {
        String hexResult = HexUtils.fileToHex(testFile);
        assertEquals(testHexString, hexResult);
    }

    /**
     * Tests the hexToFile() function
     */
    @Test
    public void testHexToFile() throws IOException {
        File tempFile = File.createTempFile("testHexToFile", ".txt");
        HexUtils.hexToFile(testHexString, tempFile);

        byte[] originalBytes = Files.readAllBytes(testFile.toPath());
        byte[] resultBytes = Files.readAllBytes(tempFile.toPath());

        assertArrayEquals(originalBytes, resultBytes);
    }

    /**
     * Tests the hexToBase64() function
     */
    @Test
    public void testHexToBase64() {
        String base64Result = HexUtils.hexToBase64(testHexString);
        assertEquals(testBase64String, base64Result);
    }

    /**
     * Tests the hexStringToByteArray() function
     */
    @Test
    public void testHexStringToByteArray() {
        byte[] resultBytes = HexUtils.hexStringToByteArray(testHexString);
        byte[] expectedBytes = "Hello, World!".getBytes();
        assertArrayEquals(expectedBytes, resultBytes);
    }
}
