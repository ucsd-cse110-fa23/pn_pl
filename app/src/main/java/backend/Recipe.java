package backend;

import java.io.IOException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;

/**
 * Represents a recipe with a unique ID, title, instructions, and creation date.
 */
public class Recipe {

    private String recipeID;
    private String title;
    private String instructions;
    private Date dateCreated;
    private String accountUsername;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    private String imageHex;
    private String mealType;

    /**
     * Constructs a new Recipe with the provided ID, title, instructions, and
     * creation
     * date.
     * 
     * @param recipeID     Unique ID of the recipe.
     * @param title        Title of the recipe.
     * @param instructions Instructions for preparing the recipe.
     * @param dateCreated  Date when the recipe was generated.
     */
    public Recipe(String recipeID, String title, String instructions, Date dateCreated, String accountUsername,
            String imageHex, String mealType) {
        this.recipeID = recipeID;
        this.title = title;
        this.instructions = instructions;
        this.dateCreated = dateCreated;
        this.imageHex = imageHex;
        this.accountUsername = accountUsername;
        this.mealType = mealType;
    }

    /**
     * Constructs a new Recipe from a JSON object.
     * 
     * @param jsonRecipe JSON object representing the recipe.
     */
    public Recipe(JSONObject jsonRecipe) {
        String dateString = jsonRecipe.getString("dateCreated");
        try {
            this.dateCreated = this.formatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            this.dateCreated = new Date();
        }
        this.title = jsonRecipe.getString("title");
        this.instructions = jsonRecipe.getString("instructions");
        this.recipeID = jsonRecipe.getString("recipeID");
        this.imageHex = jsonRecipe.getString("imageHex");
        this.mealType = jsonRecipe.getString("mealType");
        this.accountUsername = jsonRecipe.getString("accountUsername");
    }

    /**
     * Returns the title of the recipe.
     * 
     * @return Title of the recipe.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Returns the instructions for preparing the recipe.
     * 
     * @return Instructions for preparing the recipe.
     */
    public String getInstructions() {
        return this.instructions;
    }

    /**
     * Returns the creation date of the recipe.
     * 
     * @return Creation date of the recipe.
     */
    public Date getDateCreated() {
        return this.dateCreated;
    }

    /**
     * Returns the unique ID of the recipe.
     * 
     * @return Unique ID of the recipe.
     */
    public String getRecipeID() {
        return this.recipeID;
    }

    /**
     * Returns the username of the account that created the recipe.
     * 
     * @return Username of the account that created the recipe.
     */
    public String getAccountUsername() {
        return this.accountUsername;
    }

    /**
     * Returns the meal type of the recipe.
     * 
     * @return Meal type of the recipe.
     */
    public String getMealType() {
        return this.mealType;
    }

    /**
     * Sets the instructions for preparing the recipe and updates the creation date.
     * 
     * @param instructions New instructions for preparing the recipe.
     */
    public void setInstructions(String instructions) {
        this.dateCreated = new Date();
        this.instructions = instructions;
    }

    /**
     * Returns the image of the recipe.
     * 
     * @return Image of the recipe.
     */
    public String getImageHex() {
        return this.imageHex;
    }

    /**
     * Returns a string representation of the recipe, or its title.
     * 
     * @return Title of the recipe.
     */
    @Override
    public String toString() {
        return this.title;
    }

    /**
     * Returns a JSON object representing the recipe.
     * 
     * @return JSON object representing the recipe.
     */
    public JSONObject toJSON() {
        JSONObject out = new JSONObject();
        out.put("recipeID", this.recipeID);
        out.put("title", this.title);
        out.put("instructions", this.instructions);
        out.put("imageHex", this.imageHex);
        out.put("mealType", this.mealType);
        out.put("dateCreated", this.formatter.format(this.dateCreated));
        out.put("accountUsername", this.accountUsername);
        return out;
    }

    /**
     * Returns an HTML representation of the recipe. Converts image to base64 to
     * follow HTML standards for images.
     * 
     * @return HTML representation of the recipe.
     * @throws IOException
     */
    public String toHTML() {
        String hexType = this.imageHex;
        String base64String = HexUtils.hexToBase64(hexType);

        String htmlInstructions = escapeHTML(this.instructions).replace("\n", "<br>");
        String imageTag = "<img src=\"data:image/png;base64," + base64String + "\" alt=\"Recipe Image\">";

        return "<html><body style=\"background-color: #e7ffe6; font-family: Arial;\"><h1>" + this.title
                + "</h1>"
                + imageTag + "<p>"
                + htmlInstructions
                + "</p></body></html>";
    }

    /**
     * Escapes HTML characters in a string.
     * 
     * @param s the string to escape
     * @return the escaped string
     * 
     * @see code from https://stackoverflow.com/a/25228492
     */
    private static String escapeHTML(String s) {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '\'' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}