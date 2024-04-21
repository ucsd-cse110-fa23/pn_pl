package backend;

import java.util.Collections;
import java.util.Comparator;
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
 * This class represents a list of Recipe objects. It provides methods for
 * adding and removing recipes,
 * getting a list of recipe IDs, getting a list of all recipes, getting a recipe
 * by ID, sorting the recipes
 * by date, and updating the recipe list in the database file.
 */
public class RecipeList {

    private List<Recipe> recipes;
    private File databaseFile;

    /**
     * Constructs a new RecipeList with the provided database file.
     * 
     * @param databaseFile
     */
    public RecipeList(File databaseFile) {
        this.recipes = new ArrayList<>();
        this.databaseFile = databaseFile;
        this.loadRecipesFromFile();
        this.sortRecipesByDate();
    }

    /**
     * Adds the provided recipe to the list of recipes and updates the database.
     * 
     * @param recipe
     */
    public void addRecipe(Recipe recipe) {
        this.recipes.add(recipe);
        this.updateDatabase();
        this.sortRecipesByDate();
    }

    /**
     * Removes the provided recipe from the list of recipes and updates the
     * database.
     * 
     * @param recipe
     */
    public void removeRecipe(Recipe recipe) {
        this.recipes.remove(recipe);
        this.updateDatabase();
    }

    /**
     * Returns a list of all recipe IDs.
     * 
     * @return list of recipe IDs
     */
    public List<String> getRecipeIDs(String accountUsername, String sortBy, String filterBy) {
        List<String> recipeIDs = new ArrayList<>();
        for (Recipe recipe : this.recipes) {
            if (recipe.getAccountUsername().equals(accountUsername)) {
                recipeIDs.add(recipe.getRecipeID());
            }
        }
        // Sort by "most-recent", "least-recent", "a-z", "z-a"
        switch (sortBy) {
            case "most-recent":
                recipeIDs.sort((a, b) -> this.getRecipeByID(b).getDateCreated()
                        .compareTo(this.getRecipeByID(a).getDateCreated()));
                break;
            case "least-recent":
                recipeIDs.sort((a, b) -> this.getRecipeByID(a).getDateCreated()
                        .compareTo(this.getRecipeByID(b).getDateCreated()));
                break;
            case "a-z":
                recipeIDs.sort((a, b) -> this.getRecipeByID(a).getTitle()
                        .compareTo(this.getRecipeByID(b).getTitle()));
                break;
            case "z-a":
                recipeIDs.sort((a, b) -> this.getRecipeByID(b).getTitle()
                        .compareTo(this.getRecipeByID(a).getTitle()));
                break;
        }
        // Filter by "all", "breakfast", "lunch", "dinner"
        switch (filterBy) {
            case "all":
                break;
            case "breakfast":
                recipeIDs.removeIf(recipeID -> !this.getRecipeByID(recipeID).getMealType().equals("breakfast"));
                break;
            case "lunch":
                recipeIDs.removeIf(recipeID -> !this.getRecipeByID(recipeID).getMealType().equals("lunch"));
                break;
            case "dinner":
                recipeIDs.removeIf(recipeID -> !this.getRecipeByID(recipeID).getMealType().equals("dinner"));
                break;
            default:
                break;

        }
        return recipeIDs;
    }

    /**
     * Returns a list of all recipes.
     * 
     * @return list of all recipes
     */
    public List<Recipe> getRecipes() {
        return this.recipes;
    }

    /**
     * Returns the recipe with the provided ID.
     * 
     * @param recipeID
     * @return specific recipe
     */
    public Recipe getRecipeByID(String recipeID) {
        for (Recipe recipe : this.recipes) {
            if (recipe.getRecipeID().equals(recipeID)) {
                return recipe;
            }
        }
        return null;
    }

    /**
     * Sorts the list of recipes by date.
     */
    public void sortRecipesByDate() {
        // Sort the list with a custom comparator that compares the dates
        Collections.sort(this.recipes, new Comparator<Recipe>() {
            @Override
            public int compare(Recipe r1, Recipe r2) {
                // Sort in descending order so the most recent dates come first
                return r2.getDateCreated().compareTo(r1.getDateCreated());
            }
        });
    }

    /**
     * Updates the database file with the current list of recipes.
     */
    public void updateDatabase() {
        JSONArray jsonRecipeList = new JSONArray();
        for (Recipe recipe : this.recipes) {
            jsonRecipeList.put(recipe.toJSON());
        }
        try {
            FileWriter fw = new FileWriter(this.databaseFile);
            fw.write(jsonRecipeList.toString());
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the recipes from the database file into the list of recipes.
     */
    public void loadRecipesFromFile() {
        if (this.databaseFile.exists()) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(this.databaseFile.getAbsolutePath())));
                JSONArray jsonRecipeList = new JSONArray(content);
                for (int i = 0; i < jsonRecipeList.length(); i++) {
                    JSONObject jsonRecipe = jsonRecipeList.getJSONObject(i);
                    Recipe recipe = new Recipe(jsonRecipe);
                    this.recipes.add(recipe);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
