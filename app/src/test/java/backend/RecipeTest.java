package backend;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit testing for Recipe class
 */
class RecipeTest {

    private Recipe recipe;
    private Date dateCreated;

    /**
     * Sets up the RecipeTest.
     */
    @BeforeEach
    public void setUp() {
        dateCreated = new Date(0);
        recipe = new Recipe("id 1", "Chocolate Cake", "Mix ingredients and bake for 30 minutes.", dateCreated, "Caitlin", "image hex test",
                "dinner");
    }

    /**
     * Tests that the Recipe properly sets the instructions.
     */
    @Test
    void testSetInstructions() {
        String newInstructions = "Mix ingredients and bake for 45 minutes.";
        Date oldDate = recipe.getDateCreated();
        recipe.setInstructions(newInstructions);
        assertEquals(newInstructions, recipe.getInstructions());
        assertTrue(recipe.getDateCreated().after(oldDate));
    }

    /**
     * Tests that the Recipe properly returns the title.
     */
    @Test
    void testToString() {
        assertEquals("Chocolate Cake", recipe.toString());
    }

    /**
     * Tests Recipe properly reads JSON Object.
     */
    @Test
    void testFromJSON() {
        JSONObject json = new JSONObject(
                "{\"title\":\"abc\",\"instructions\":\"ab\",\"dateCreated\":\"1970-01-01T00:00:00-00:00\",\"recipeID\":\"id 1\",\"accountUsername\":\"username 1\",\"mealType\":\"breakfast\",\"imageHex\":\"hex 1\"}");
        Recipe recipe = new Recipe(json);
        assertEquals("id 1", recipe.getRecipeID());
        assertEquals("abc", recipe.getTitle());
        assertEquals("ab", recipe.getInstructions());
        assertEquals(new Date(0), recipe.getDateCreated());
        assertEquals("username 1", recipe.getAccountUsername());
        assertEquals("hex 1", recipe.getImageHex());
        assertEquals("breakfast", recipe.getMealType());
    }

    /**
     * Tests to JSON file.
     */
    @Test
    void testToJSON() {
        JSONObject json = recipe.toJSON();
        assertEquals("Chocolate Cake", json.getString("title"));
        assertEquals("Mix ingredients and bake for 30 minutes.",
                json.getString("instructions"));

        // this one seems to depend on the system
        assertTrue(
                json.getString("dateCreated").startsWith("1969") ||
                        json.getString("dateCreated").startsWith("1970"));
    }

    /*
     * Tests editing a recipe
     */
    @Test
    void testEditRecipe() {
        long currentTime = System.currentTimeMillis();
        Recipe recipe = new Recipe("", "Muffins", "Add 1 cup of sugar and flour.", new Date(currentTime - 1000), "", "",
                "");
        recipe.setInstructions("Add 1/2 cup of sugar and flour.");
        assertEquals("Add 1/2 cup of sugar and flour.", recipe.getInstructions());
        assertTrue(recipe.getDateCreated().after(new Date(currentTime - 1000)));
    }

    /**
     * Tests retrieving recipe details
     */
    @Test
    void testGetRecipeDetails() {
        assertEquals("Chocolate Cake", recipe.getTitle());
        assertEquals("Mix ingredients and bake for 30 minutes.",
                recipe.getInstructions());
        assertEquals("id 1", recipe.getRecipeID());
        assertEquals(dateCreated, recipe.getDateCreated());
    }

    /**
     * Tests retrieving recipe HTML
     */
    @Test
    void testToHTML() {
        assertEquals("<html><body style=\"background-color: #e7ffe6; font-family: Arial;\"><h1>Chocolate Cake</h1><img src=\"data:image/png;base64,75/f/u/+7w==\" alt=\"Recipe Image\"><p>Mix ingredients and bake for 30 minutes.</p></body></html>", recipe.toHTML());
    }
}
