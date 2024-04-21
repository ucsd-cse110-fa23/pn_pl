package middleware;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.net.URLDecoder;
import java.net.URLEncoder;

import org.json.JSONObject;

import backend.HexUtils;
import ui.ISceneManager;

/**
 * This class represents a controller that handles requests from the frontend
 * and
 * sends them to the backend.
 */
public class Controller {

    private String accountUsername;
    private String sortBy;
    private String filterBy;
    private ISceneManager sceneManager;
    private ServerCommunicator serverCommunicator;

    /**
     * Constructs a new Controller.
     */
    public Controller(ServerCommunicator serverCommunicator) {
        this.accountUsername = null;
        this.sortBy = "most-recent";
        this.filterBy = "all";
        this.serverCommunicator = serverCommunicator;
    }

    public ServerCommunicator getServerCommunicator() {
        return this.serverCommunicator;
    }

    /**
     * Sets the scene manager.
     * 
     * @param sceneManager
     */
    public void setSceneManager(ISceneManager sceneManager) {
        this.sceneManager = sceneManager;
        sendRequestWithCheck("/status", null, "GET"); // makes sure server is up
    }

    /**
     * Generates a new recipe builder and returns the ID of the recipe builder.
     * 
     * @return
     */
    public String generateNewRecipeBuilder() {
        return sendRequestWithCheck("/generate-new-recipe-builder", null, "GET");
    }

    /**
     * Returns the meal type of the recipe with the specified ID.
     * 
     * @param recipeID
     * @return the meal type
     */
    public String getRecipeMealType(String recipeID) {
        return sendRequestWithCheck("/get-recipe-meal-type", "recipeID=" + recipeID, "GET");
    }

    /**
     * Returns the recipe title.
     * 
     * @param recipeID
     * @return the recipe title
     */
    public String getRecipeTitle(String recipeID) {
        return sendRequestWithCheck("/get-recipe-title", "recipeID=" + recipeID, "GET");
    }

    /**
     * Get recipe date
     * 
     * @param recipeID
     * @return
     */

    public String getRecipeDate(String recipeID) {
        return sendRequestWithCheck("/get-recipe-date", "recipeID=" + recipeID, "GET");
    }

    /**
     * Returns the detailed instructions for a given recipe.
     * 
     * @param recipeID
     * @return the instructions
     */
    public String getRecipeInstructions(String recipeID) {
        try {
            return URLDecoder.decode(
                    sendRequestWithCheck("/get-recipe-instructions", "recipeID=" + recipeID, "GET").substring(1),
                    "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the image for a given recipe.
     * 
     * @param recipeID
     * @return the image
     */
    public File getRecipeImage(String recipeID) {
        String response = sendRequestWithCheck("/get-recipe-image", "recipeID=" + recipeID, "GET");
        File imageFile = new File("generated-image.png");
        try {
            HexUtils.hexToFile(response, imageFile);
        } catch (Exception e) {
            System.out.println("Error getting image");
        }
        return imageFile;
    }

    /**
     * Returns all the recipe IDs.
     * 
     * @return list of recipe IDs
     */
    public List<String> getRecipeIDs() {
        String response = sendRequestWithCheck("/get-recipe-ids",
                "accountUsername=" + accountUsername + "&sortBy=" + sortBy + "&filterBy=" + filterBy, "GET");
        if (response.equals(".")) {
            return new ArrayList<>();
        }
        return Arrays.asList(response.split(","));
    }

    /**
     * Resets an element in the recipe builder back to null.
     * 
     * @param recipeID
     * @param elementName
     */
    public void resetRecipeCreatorElement(String recipeID, String elementName) {
        sendRequestWithCheck("/reset-recipe-creator-element", "recipeID=" + recipeID + "&elementName=" + elementName,
                "PUT");
    }

    /**
     * Sets a recipe builder element by providing an audio file.
     * 
     * @param recipeID
     * @param elementName
     * @param audioFile
     * @return the new value of the element if it was set, otherwise null
     */
    public String specifyRecipeCreatorElement(String recipeID, String elementName, File audioFile) {
        String hex = HexUtils.fileToHex(audioFile);
        String response = sendRequestWithCheck("/specify-recipe-creator-element",
                "recipeID=" + recipeID + "&elementName=" + elementName + "&hex=" + hex, "POST").substring(1);
        if (response.equals("invalid")) {
            return null;
        }
        return response;
    }

    /**
     * Returns true if the recipe is ready to be created, false otherwise.
     * 
     * @param recipeID
     * @return true if the recipe builder is completed, false otherwise
     */
    public boolean isRecipeCreatorCompleted(String recipeID) {
        String response = sendRequestWithCheck("/is-recipe-creator-completed", "recipeID=" + recipeID, "GET");
        return response.equals("true");
    }

    /**
     * Generates a recipe from the recipe builder.
     * 
     * @param recipeID
     */
    public void generateRecipe(String recipeID) {
        sendRequestWithCheck("/generate-recipe", "recipeID=" + recipeID + "&accountUsername=" + this.accountUsername,
                "PUT");
    }

    /**
     * Removes the recipe with the specified ID.
     * 
     * @param recipeID
     */
    public void removeRecipe(String recipeID) {
        sendRequestWithCheck("/remove-recipe", "recipeID=" + recipeID, "DELETE");
    }

    /**
     * Saves the recipe with the specified ID to the server's filesystem.
     * 
     * @param recipeID
     */
    public void saveRecipe(String recipeID) {
        sendRequestWithCheck("/save-recipe", "recipeID=" + recipeID, "GET");
    }

    /**
     * Edits the instructions of the recipe with the specified ID.
     * 
     * @param recipeID
     * @param newInstructions
     */
    public void editRecipe(String recipeID, String newInstructions) {
        try {
            // we add the dot to make sure the query string is not empty
            sendRequestWithCheck("/edit-recipe",
                    "recipeID=" + recipeID + "&newInstructions=." + URLEncoder.encode(newInstructions, "UTF-8"), "PUT");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds an account with the specified username and password.
     * 
     * @param username
     * @param password
     * @return the username of the account if it was created, otherwise null
     */
    public String addAccount(String username, String password) {
        try {
            String response = sendRequestWithCheck("/add-account", "username=" + URLEncoder.encode(username, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8"),
                    "POST");
            if (response.equals("created")) {
                this.accountUsername = username;
                return username;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns true if the specified username and password match the username and
     * password of the account. Furthermore, sets sort and filter back to default.
     * 
     * @param username
     * @param password
     * @return true if the specified username and password match the username and
     *         password of the account
     */
    public boolean login(String username, String password) {
        try {
            String response = sendRequestWithCheck("/login", "username=" + URLEncoder.encode(username, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8"), "GET");
            if (response.equals("success")) {
                this.accountUsername = username;
                this.sortBy = "most-recent";
                this.filterBy = "all";
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean passwordsMatch(String password1, String password2) {
        try {
            String response = sendRequestWithCheck("/passwords-match", "password1=" + URLEncoder.encode(password1, "UTF-8") + "&password2=" + URLEncoder.encode(password2, "UTF-8"),
                    "GET");
            return response.equals("true");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean validateUsername(String username) {
        try {
            String response = sendRequestWithCheck("/valid-username", "username=" + URLEncoder.encode(username, "UTF-8"), "GET");
            return response.equals("true");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean validatePassword(String password) {
        try {
            String response = sendRequestWithCheck("/valid-password", "password=" + URLEncoder.encode(password, "UTF-8"), "GET");
            return response.equals("true");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns the JSON for the account with the specified username and password.
     * 
     * @param username
     * @param password
     * @return the JSON for the account with the specified username and password
     */
    public JSONObject getAccountJSON(String username, String password) {
        String response = sendRequestWithCheck("/get-account-json", "username=" + username + "&password=" + password,
                "GET");
        return new JSONObject(response);
    }

    /**
     * Returns true if the user is logged in, false otherwise. Also resets the sort
     * and filter back to default.
     * 
     * @return true if the user is logged in, false otherwise
     */
    public void logout() {
        this.accountUsername = null;
        File automaticLoginFile = new File("automaticLogin.json");
        if (automaticLoginFile.exists()) {
            automaticLoginFile.delete();
        }
        this.sortBy = "most-recent";
        this.filterBy = "all";
    }

    /**
     * Sets the sort type
     * 
     * @param sortBy
     */
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    /**
     * Returns the sort type
     * 
     * @return sortBy
     */
    public String getSortBy() {
        return this.sortBy;
    }

    /**
     * Sets the filter type
     * 
     * @param filterBy
     */
    public void setFilterBy(String filterBy) {
        this.filterBy = filterBy;
    }

    /**
     * Returns the filter type
     * 
     * @return filterBy
     */
    public String getFilterBy() {
        return this.filterBy;
    }

    /**
     * Sends a request to the server and returns the response. If the server is
     * down, displays the server error scene.
     * 
     * @param path
     * @param query
     * @param method
     * @return the response from the server
     */
    public String sendRequestWithCheck(String path, String query, String method) {
        try {
            return serverCommunicator.sendRequest(path, query, method);
        } catch (Exception e) {
            sceneManager.displayServerErrorScene();
            return null;
        }
    }
}
