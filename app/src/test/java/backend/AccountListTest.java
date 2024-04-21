package backend;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.io.IOException;

class AccountListTest {

    private AccountList accountList;
    private File databaseFile;

    @BeforeEach
    public void setUp() {
        databaseFile = new File("test-account-database.json");
        if (databaseFile.exists()) {
            databaseFile.delete();
        }
        accountList = new AccountList(databaseFile);
    }

    @AfterEach
    public void tearDown() {
        if (databaseFile.exists()) {
            databaseFile.delete();
        }
    }

    @Test
    void testAddAccountValid() {
        assertTrue(accountList.addAccount("test username", "test password"));
        assertEquals(1, accountList.getAccounts().size());
        assertEquals("test username", accountList.getAccounts().get(0).getUsername());
        assertTrue(accountList.addAccount("test username 2", "test password 2"));
        assertEquals(2, accountList.getAccounts().size());
        assertEquals("test username 2", accountList.getAccounts().get(1).getUsername());
    }

    @Test
    void testAddAccountInvalid() {
        assertTrue(accountList.addAccount("test username", "test password"));
        assertFalse(accountList.addAccount("test username", "test password 2"));
        assertEquals(1, accountList.getAccounts().size());
        assertEquals("test username", accountList.getAccounts().get(0).getUsername());
    }

    @Test
    void testAttemptLoginSuccess() {
        accountList.getAccounts().add(accountList.new Account("test username", "test password"));
        assertTrue(accountList.attemptLogin("test username", "test password"));
    }

    @Test
    void testAttemptLoginFail() {
        accountList.getAccounts().add(accountList.new Account("test username", "test password"));
        assertFalse(accountList.attemptLogin("test username", "test password 2"));
        assertFalse(accountList.attemptLogin("test username 2", "test password"));
    }

    @Test
    void testLoadAccountsFromFile() throws IOException {
        String fileContents = "[{\"password\":\"p1\",\"username\":\"abc\"},{\"password\":\"p2\",\"username\":\"abcd\"}]";
        FileWriter fileWriter = new FileWriter(databaseFile);
        fileWriter.write(fileContents);
        fileWriter.close();
        accountList.loadAccountsFromFile();
        assertEquals(2, accountList.getAccounts().size());
        assertEquals("abc", accountList.getAccounts().get(0).getUsername());
        assertEquals("abcd", accountList.getAccounts().get(1).getUsername());
    }

    @Test
    void testUpdateDatabase() throws IOException {
        accountList.addAccount("test username", "test password");
        accountList.addAccount("test username 2", "test password 2");
        accountList.updateDatabase();
        String fileContents = new String(Files.readAllBytes(Paths.get(databaseFile.getPath())));
        JSONArray jsonArray = new JSONArray(fileContents);
        assertEquals(2, jsonArray.length());
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        assertEquals("test username", jsonObject.getString("username"));
        assertEquals("test password", jsonObject.getString("password"));
        jsonObject = jsonArray.getJSONObject(1);
        assertEquals("test username 2", jsonObject.getString("username"));
        assertEquals("test password 2", jsonObject.getString("password"));
    }

    @Test
    void testGetAccountJSON() {
        accountList.addAccount("test username", "test password");
        accountList.addAccount("test username 2", "test password 2");
        JSONObject jsonAccount = accountList.getAccountJSON("test username", "test password");
        assertEquals("test username", jsonAccount.getString("username"));
        assertEquals("test password", jsonAccount.getString("password"));
        jsonAccount = accountList.getAccountJSON("test username 2", "test password 2");
        assertEquals("test username 2", jsonAccount.getString("username"));
        assertEquals("test password 2", jsonAccount.getString("password"));
        jsonAccount = accountList.getAccountJSON("test username", "test password 2");
        assertNull(jsonAccount);
    }

    @Test
    public void testValidateUsername() {
        // Test empty username
        assertFalse(accountList.validateUsername(""));

        // Test username with space
        assertFalse(accountList.validateUsername("my username"));

        // Test username with non-ASCII character
        assertFalse(accountList.validateUsername("usernåme"));

        // Test valid username
        assertTrue(accountList.validateUsername("username"));
    }

    @Test
    public void testValidatePassword() {
        // Test empty password
        assertFalse(accountList.validatePassword(""));

        // Test password with space
        assertFalse(accountList.validatePassword("my password"));

        // Test password with non-ASCII character
        assertFalse(accountList.validatePassword("påssword"));

        // Test valid password
        assertTrue(accountList.validatePassword("password"));
    }

    @Test
    public void testPasswordsMatch() {
        // Test matching passwords
        assertTrue(accountList.passwordsMatch("password", "password"));

        // Test non-matching passwords
        assertFalse(accountList.passwordsMatch("password", "password123"));
    }

}
