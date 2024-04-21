package middleware;

import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.io.File;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.util.List;

import backend.Recipe;
import backend.RecipeBuilder;
import backend.RecipeList;
import backend.AccountList;
import backend.ChatGPT;
import backend.Whisper;
import backend.DallE;
import backend.HexUtils;

/**
 * This class represents a server that handles requests from the frontend.
 */
public class Server {

    private static final int SERVER_PORT = 8100;
    private static final String SERVER_HOSTNAME = "0.0.0.0";
    private static final String RECIPE_DATABASE_FILENAME = "database.json";
    private static final String ACCOUNT_DATABASE_FILENAME = "accounts.json";

    /**
     * Starts the server.
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        HttpServer server = HttpServer.create(
                new InetSocketAddress(SERVER_HOSTNAME, SERVER_PORT),
                0);

        server.createContext("/",
                new RequestHandler(new File(RECIPE_DATABASE_FILENAME), new File(ACCOUNT_DATABASE_FILENAME)));

        server.setExecutor(threadPoolExecutor);
        server.start();
    }
}

/**
 * This class represents a request handler that handles requests from the
 * frontend.
 */
class RequestHandler implements HttpHandler {

    private static final String OPENAI_API_KEY = "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";
    private static final String SUCCESS_MESSAGE = "success";
    private static final String FAILURE_MESSAGE = "failure";

    RecipeList recipeList;
    AccountList accountList;
    Map<String, RecipeBuilder> recipeBuilders;
    Map<String, Recipe> temporaryRecipes;
    File audioFile;
    ChatGPT chatGPT;
    Whisper whisper;
    DallE dallE;

    /**
     * Constructs a new RequestHandler with the provided database file.
     * 
     * @param databaseFile
     */
    public RequestHandler(File recipeDatabaseFile, File accountDatabaseFile) {
        this.recipeList = new RecipeList(recipeDatabaseFile);
        this.accountList = new AccountList(accountDatabaseFile);
        this.recipeBuilders = new HashMap<>();
        this.temporaryRecipes = new HashMap<>();
        this.audioFile = new File("audio.wav");
        this.chatGPT = new ChatGPT(OPENAI_API_KEY);
        this.whisper = new Whisper(OPENAI_API_KEY);
        this.dallE = new DallE(OPENAI_API_KEY);
    }

    /**
     * Handles the request and sends it to the appropriate method.
     */
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            URI uri = httpExchange.getRequestURI();
            String path = uri.getPath();
            Map<String, String> query = this.parseQuery(uri.getQuery());

            String response = "";

            // handle request based on path
            switch (path) {
                case "/status":
                    response = "available";
                    break;
                case "/generate-new-recipe-builder":
                    response = this.handleGenerateNewRecipeBuilder();
                    break;
                case "/get-recipe-meal-type":
                    response = this.handleGetRecipeMealType(query);
                    break;
                case "/get-recipe-title":
                    response = this.handleGetRecipeTitle(query);
                    break;
                case "/get-recipe-instructions":
                    response = this.handleGetRecipeInstructions(query);
                    break;
                case "/get-recipe-ids":
                    response = this.handleGetRecipeIDs(query);
                    break;
                case "/reset-recipe-creator-element":
                    response = this.handleResetRecipeCreatorElement(query);
                    break;
                case "/specify-recipe-creator-element":
                    response = this.handleSpecifyRecipeCreatorElement(query);
                    break;
                case "/is-recipe-creator-completed":
                    response = this.handleIsRecipeCreatorCompleted(query);
                    break;
                case "/generate-recipe":
                    response = this.handleGenerateRecipe(query);
                    break;
                case "/remove-recipe":
                    response = this.handleRemoveRecipe(query);
                    break;
                case "/save-recipe":
                    response = this.handleSaveRecipe(query);
                    break;
                case "/edit-recipe":
                    response = this.handleEditRecipe(query);
                    break;
                case "/add-account":
                    response = this.handleAddAccount(query);
                    break;
                case "/get-recipe-image":
                    response = this.handleGetImage(query);
                    break;
                case "/get-recipe-date":
                    response = this.handleGetDate(query);
                    break;
                case "/login":
                    response = this.handleLogin(query);
                    break;
                case "/get-account-json":
                    response = this.handleGetAccountJSON(query);
                    break;
                case "/recipe":
                    response = this.handleGetRecipeHTML(query);
                    break;
                case "/passwords-match":
                    response = this.handlePasswordsMatch(query);
                    break;
                case "/valid-username":
                    response =this.handleValidUsername(query);
                    break;
                case "/valid-password":
                    response = this.handleValidPassword(query);
                    break;
                default:
                    response = "Invalid path";
                    break;
            }

            // send response
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream outStream = httpExchange.getResponseBody();
            outStream.write(response.getBytes());
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // these are the same methods as in Controller.java, just on the server side
    private Map<String, String> parseQuery(String queryString) {
        Map<String, String> queryMap = new HashMap<>();
        if (queryString != null) {
            String[] queries = queryString.split("&");
            for (String query : queries) {
                queryMap.put(query.split("=")[0], query.split("=")[1]);
            }
        }
        return queryMap;
    }

    /**
     * Creates a RecipeBuilder object and adds it to the recipeBuilders map.
     * 
     * @return the recipeID of the RecipeBuilder object
     */
    private String handleGenerateNewRecipeBuilder() {
        RecipeBuilder recipeBuilder = new RecipeBuilder(chatGPT, whisper, dallE);
        this.recipeBuilders.put(recipeBuilder.getRecipeID(), recipeBuilder);
        return recipeBuilder.getRecipeID();
    }

    /**
     * Returns the meal type of the recipe with the specified recipeID.
     * 
     * @param query
     * @return meal type of the recipe
     */
    private String handleGetRecipeMealType(Map<String, String> query) {
        String recipeID = query.get("recipeID");
        if (temporaryRecipes.containsKey(recipeID))
            return this.temporaryRecipes.get(recipeID).getMealType();
        return this.recipeList.getRecipeByID(recipeID).getMealType();
    }

    /**
     * Returns the title of the recipe with the specified recipeID.
     * 
     * @param query
     * @return title of the recipe
     */
    private String handleGetRecipeTitle(Map<String, String> query) {
        String recipeID = query.get("recipeID");
        if (temporaryRecipes.containsKey(recipeID))
            return this.temporaryRecipes.get(recipeID).getTitle();
        return this.recipeList.getRecipeByID(recipeID).getTitle();
    }

    /**
     * Returns the image of the recipe with the specified recipeID.
     * 
     * @param query
     * @return hex string of the image associated with the recipe
     */
    private String handleGetImage(Map<String, String> query) {
        String recipeID = query.get("recipeID");
        if (temporaryRecipes.containsKey(recipeID)) {
            return this.temporaryRecipes.get(recipeID).getImageHex();
        }
        return this.recipeList.getRecipeByID(recipeID).getImageHex();
    }

    /**
     * Returns the date of the recipe with the specified recipeID.
     * 
     * @param query
     * @return date of the recipe
     */
    private String handleGetDate(Map<String, String> query) {
        String recipeID = query.get("recipeID");
        if (temporaryRecipes.containsKey(recipeID)) {
            return this.temporaryRecipes.get(recipeID).getDateCreated().toString();
        }
        return this.recipeList.getRecipeByID(recipeID).getDateCreated().toString();
    }

    /**
     * Returns the instructions of the recipe with the specified recipeID.
     * 
     * @param query
     * @return recipe instructions
     */
    private String handleGetRecipeInstructions(Map<String, String> query) {
        String recipeID = query.get("recipeID");
        try {
            // we need to encode the instructions because they may contain special
            // characters like newline
            if (temporaryRecipes.containsKey(recipeID))
                // we need to encode the instructions because they contain special characters
                // like newlines
                return "." + URLEncoder.encode(this.temporaryRecipes.get(recipeID).getInstructions(), "UTF-8");
            return "." + URLEncoder.encode(this.recipeList.getRecipeByID(recipeID).getInstructions(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns a list of recipe IDs associated with account
     * 
     * @param query
     * @return list of recipe IDs associated with account
     */
    private String handleGetRecipeIDs(Map<String, String> query) {
        String accountUsername = query.get("accountUsername");
        String sortBy = query.get("sortBy");
        String filterBy = query.get("filterBy");
        List<String> ids = this.recipeList.getRecipeIDs(accountUsername, sortBy, filterBy);
        if (ids.isEmpty()) {
            return ".";
        }
        return String.join(",", ids);
    }

    /**
     * Resets the specified element of the recipe builder.
     * 
     * @param query
     * @return success message if reset was successful
     */
    private String handleResetRecipeCreatorElement(Map<String, String> query) {
        String recipeID = query.get("recipeID");
        String elementName = query.get("elementName");
        if (elementName.equals("mealType")) {
            this.recipeBuilders.get(recipeID).getMealTypeElement().reset();
        } else if (elementName.equals("ingredients")) {
            this.recipeBuilders.get(recipeID).getIngredientsElement().reset();
        } else {
            throw new IllegalArgumentException("Invalid element name");
        }
        return SUCCESS_MESSAGE;
    }

    /**
     * Specifies the specified element of the recipe builder.
     * 
     * @param query
     * @return the specified value if the element is specified successfully,
     *         otherwise return FAILURE_MESSAGE
     */
    private String handleSpecifyRecipeCreatorElement(Map<String, String> query) {
        try {
            String hex = query.get("hex");
            HexUtils.hexToFile(hex, this.audioFile);
            String recipeID = query.get("recipeID");
            String elementName = query.get("elementName");
            String out = FAILURE_MESSAGE;
            if (elementName.equals("mealType")) {
                out = this.recipeBuilders.get(recipeID).getMealTypeElement().specify(this.audioFile);
            } else if (elementName.equals("ingredients")) {
                out = this.recipeBuilders.get(recipeID).getIngredientsElement().specify(this.audioFile);
            }
            if (out == null) {
                out = "invalid";
            }
            return "." + out;
        } catch (Exception e) {
            e.printStackTrace();
            return FAILURE_MESSAGE;
        }
    }

    /**
     * Returns whether the recipe builder is completed.
     * 
     * @param query
     * @return true if the recipe builder is completed, false otherwise
     */
    private String handleIsRecipeCreatorCompleted(Map<String, String> query) {
        String recipeID = query.get("recipeID");
        return Boolean.toString(this.recipeBuilders.get(recipeID).isCompleted());
    }

    /**
     * Generates a recipe from the recipe builder and adds it to the temporary list
     * of recipes.
     * 
     * @param query
     * @return success message if the recipe was generated successfully, otherwise
     *         failure message
     * @throws InterruptedException
     * @throws URISyntaxException
     */
    private String handleGenerateRecipe(Map<String, String> query) throws InterruptedException, URISyntaxException {
        String recipeID = query.get("recipeID");
        String accountUsername = query.get("accountUsername");
        try {
            Recipe recipe = this.recipeBuilders.get(recipeID).returnRecipe(accountUsername);
            this.temporaryRecipes.put(recipe.getRecipeID(), recipe);
        } catch (IOException e) {
            e.printStackTrace();
            return FAILURE_MESSAGE;
        }
        return SUCCESS_MESSAGE;
    }

    /**
     * Removes the recipe with the specified recipeID from the recipe list.
     * 
     * @param query
     * @return success message if the recipe was removed successfully
     */
    private String handleRemoveRecipe(Map<String, String> query) {
        String recipeID = query.get("recipeID");
        this.recipeList.removeRecipe(this.recipeList.getRecipeByID(recipeID));
        return SUCCESS_MESSAGE;
    }

    /**
     * Saves the recipe with the specified recipeID from temporary list to the
     * recipe list.
     * 
     * @param query
     * @return success message if the recipe was saved successfully
     */
    private String handleSaveRecipe(Map<String, String> query) {
        String recipeID = query.get("recipeID");
        this.recipeList.addRecipe(this.temporaryRecipes.remove(recipeID));
        return SUCCESS_MESSAGE;
    }

    /**
     * Edits the instructions of the recipe with the specified recipeID.
     * 
     * @param query
     * @return success message if the recipe was edited successfully, otherwise
     *         failure message
     */
    private String handleEditRecipe(Map<String, String> query) {
        try {
            String recipeID = query.get("recipeID");
            String newInstructions = URLDecoder.decode(query.get("newInstructions"), "UTF-8").substring(1);
            this.recipeList.getRecipeByID(recipeID).setInstructions(newInstructions);
            this.recipeList.updateDatabase();
            this.recipeList.sortRecipesByDate();
            return SUCCESS_MESSAGE;
        } catch (Exception e) {
            e.printStackTrace();
            return FAILURE_MESSAGE;
        }
    }

    /**
     * Adds an account with the specified username and password.
     * 
     * @param query
     * @return "created" if account was created successfully, "in use" if username
     *         is already in use
     */
    private String handleAddAccount(Map<String, String> query) {
        try {
            String username = URLDecoder.decode(query.get("username"), "UTF-8");
            String password = URLDecoder.decode(query.get("password"), "UTF-8");
            if (this.accountList.addAccount(username, password)) {
                return "created";
            }
            return "in use";
        } catch (Exception e) {
            e.printStackTrace();
            return "in use";
        }
    }

    /**
     * Attempts to login with the specified username and password.
     * 
     * @param query
     * @return success message if the login was successful, otherwise failure
     */
    private String handleLogin(Map<String, String> query) {
        String username = query.get("username");
        String password = query.get("password");
        if (this.accountList.attemptLogin(username, password)) {
            return SUCCESS_MESSAGE;
        }
        return FAILURE_MESSAGE;
    }

    /**
     * Returns the HTML representation of the recipe with the specified recipeID.
     * 
     * @param query
     * @return HTML representation of the recipe if successful, otherwise failure
     *         message
     */
    private String handleGetRecipeHTML(Map<String, String> query) {
        String recipeID = query.get("recipeID");
        try {
            return this.recipeList.getRecipeByID(recipeID).toHTML();
        } catch (Exception e) {
            e.printStackTrace();
            return FAILURE_MESSAGE;
        }
    }

    /**
     * Gets the JSON for the account with the specified username and password
     * 
     * @param query
     * @return JSON for the account if successful, otherwise failure message
     */
    private String handleGetAccountJSON(Map<String, String> query) {
        String username = query.get("username");
        String password = query.get("password");
        return this.accountList.getAccountJSON(username, password).toString();
    }

    /**
     * Validate that passwords match
     * @param query
     * @return true if passwords match, false otherwise
     */
    private String handlePasswordsMatch(Map<String, String> query) {
        String password1 = query.get("password1");
        String password2 = query.get("password2");
        return Boolean.toString(this.accountList.passwordsMatch(password1, password2));
    }

    /**
     * Validate the username
     * @param query
     * @return true if the username is valid, false otherwise
     */
    private String handleValidUsername(Map<String, String> query) {
        try {
            String username = URLDecoder.decode(query.get("username"), "UTF-8");
            return Boolean.toString(this.accountList.validateUsername(username));
        } catch (Exception e) {
            e.printStackTrace();
            return "FALSE";
        }
    }

    /**
     * Validate the password
     * @param query
     * @return true if the password is valid, false otherwise
     */
    private String handleValidPassword(Map<String, String> query) {
        try {
            String password = URLDecoder.decode(query.get("password"), "UTF-8");
            return Boolean.toString(this.accountList.validatePassword(password));
        } catch (Exception e) {
            e.printStackTrace();
            return "FALSE";
        }
    }
}