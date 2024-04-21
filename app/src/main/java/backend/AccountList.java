package backend;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;

/**
 * This class represents a list of accounts that can be used to login to the
 * application.
 */
public class AccountList {

    /**
     * The file where the accounts are stored.
     */
    private File databaseFile;
    private List<Account> accounts;

    /**
     * This class represents an account with a username and password.
     */
    public class Account {
        private String username;
        private String password;

        /**
         * Constructs an account with the specified username and password.
         * 
         * @param username the username of the account
         * @param password the password of the account
         */
        public Account(String username, String password) {
            this.username = username;
            this.password = password;
        }

        /**
         * Constructs an account from a JSON object.
         * 
         * @param jsonAccount the JSON object representing the account
         */
        public Account(JSONObject jsonAccount) {
            this.username = jsonAccount.getString("username");
            this.password = jsonAccount.getString("password");
        }

        /**
         * Returns the JSON object representing the account.
         * 
         * @return the JSON object representing the account
         */
        public JSONObject toJSON() {
            JSONObject jsonAccount = new JSONObject();
            jsonAccount.put("username", this.username);
            jsonAccount.put("password", this.password);
            return jsonAccount;
        }

        /**
         * Returns the username of the account.
         * 
         * @return the username of the account
         */
        public String getUsername() {
            return this.username;
        }

        /**
         * Returns the password of the account.
         * 
         * @return the password of the account
         */
        public String getPassword() {
            return this.password;
        }

        // When I click save for the file it formats the comment like this, can't fix
        /**
         * Returns true if the specified username and password match the username and
         * password of the account.
         * 
         * @param username the username to check
         * @param password the password to check
         * @return true if the specified username and password match the username and
         *         password of the account
         */
        public boolean matchesCredentials(String username, String password) {
            return this.username.equals(username) && this.password.equals(password);
        }
    }

    /**
     * Constructs an account list with the specified database file.
     * 
     * @param databaseFile the file where the accounts are stored
     */
    public AccountList(File databaseFile) {
        this.databaseFile = databaseFile;
        this.accounts = new ArrayList<>();
        this.loadAccountsFromFile();
    }

    /**
     * Adds an account with the specified username and password to the account list.
     * 
     * @param username the username of the account to add
     * @param password the password of the account to add
     * @return true if the account was added, false if an account with the specified
     *         username already exists
     */
    public boolean addAccount(String username, String password) {
        for (Account account : this.accounts) {
            if (account.getUsername().equals(username)) {
                return false;
            }
        }
        this.accounts.add(new Account(username, password));
        this.updateDatabase();
        return true;
    }

    /**
     * Attempts to login with the specified username and password.
     * 
     * @param username the username to login with
     * @param password the password to login with
     * @return true if the login was successful, false otherwise
     */
    public boolean attemptLogin(String username, String password) {
        for (Account account : this.accounts) {
            if (account.matchesCredentials(username, password)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the specified username is valid.
     * 
     * @param username the username to check
     * @return true if the username is valid, false otherwise
     */
    public boolean validateUsername(String username) {
        // Check that username is not empty, contains no spaces, and contains only ASCII
        // characters

        if (username.length() == 0) {
            return false;
        }

        if (username.contains(" ")) {
            return false;
        }

        for (int i = 0; i < username.length(); i++) {
            if (username.charAt(i) > 127) {
                return false;
            }
        }

        return true;
    }

    public boolean validatePassword(String password) {
        // Check that username is not empty, contains no spaces, and contains only ASCII
        // characters

        if (password.length() == 0) {
            return false;
        }

        if (password.contains(" ")) {
            return false;
        }

        for (int i = 0; i < password.length(); i++) {
            if (password.charAt(i) > 127) {
                return false;
            }
        }

        return true;
    }

    public boolean passwordsMatch(String password, String reEnter) {
        return password.equals(reEnter);
    }

    /**
     * Gets the JSON for the account with the specified username and password
     * 
     * @param username
     * @param password
     * @return
     */
    public JSONObject getAccountJSON(String username, String password) {
        for (Account account : this.accounts) {
            if (account.matchesCredentials(username, password)) {
                return account.toJSON();
            }
        }
        return null;
    }

    /**
     * Returns the list of accounts.
     * 
     * @return the list of accounts
     */
    public List<Account> getAccounts() {
        return this.accounts;
    }

    /**
     * Loads the accounts from the database file.
     */
    public void loadAccountsFromFile() {
        if (this.databaseFile.exists()) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(this.databaseFile.getAbsolutePath())));
                JSONArray accountList = new JSONArray(content);
                for (int i = 0; i < accountList.length(); i++) {
                    JSONObject jsonAccount = accountList.getJSONObject(i);
                    Account account = new Account(jsonAccount);
                    this.accounts.add(account);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates the database file with the current list of accounts.
     */
    public void updateDatabase() {
        JSONArray jsonAccountList = new JSONArray();
        for (Account account : this.accounts) {
            jsonAccountList.put(account.toJSON());
        }
        try {
            FileWriter fw = new FileWriter(this.databaseFile);
            fw.write(jsonAccountList.toString());
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}