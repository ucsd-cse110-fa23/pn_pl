package backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Unit testing for RecipeBuilder class
 */
class RecipeBuilderTest {

    private RecipeBuilder recipeBuilder;
    private ChatGPTMock chatGPTMock;
    private WhisperMock whisperMock;
    private DallEMock dallEMock;

    /**
     * Sets up the RecipeBuilder for testing.
     */
    @BeforeEach
    public void setUp() {
        this.chatGPTMock = new ChatGPTMock();
        this.whisperMock = new WhisperMock();
        this.dallEMock = new DallEMock();
        recipeBuilder = new RecipeBuilder(this.chatGPTMock, this.whisperMock, this.dallEMock);
    }

    /**
     * Tests the isCompleted method.
     */
    @Test
    void testIsCompleted() {
        assertFalse(recipeBuilder.isCompleted());
        recipeBuilder.getIngredientsElement().setValue("Ingredient 1 and ingredient 2");
        assertFalse(recipeBuilder.isCompleted());
        recipeBuilder.getMealTypeElement().setValue("breakfast");
        assertTrue(recipeBuilder.isCompleted());
        recipeBuilder.getMealTypeElement().reset();
        assertFalse(recipeBuilder.isCompleted());
    }

    /**
     * Tests specifying a valid meal type
     *
     * @throws IOException
     */
    @Test
    void testSpecifyOne() throws IOException {
        whisperMock.setMockScenario("breakfast-meal-type.wav", "BREAKFAST");
        assertEquals("breakfast", recipeBuilder.getMealTypeElement().specify(new File("breakfast-meal-type.wav")));
        assertEquals("breakfast", recipeBuilder.getMealTypeElement().getValue());
    }

    /**
     * Tests that specifying throws an exception when there is a network error
     */
    @Test
    void testSpecifyTwo() {
        try {
            recipeBuilder.getMealTypeElement().specify(new File("throw-exception.wav"));
            fail();
        } catch (IOException e) {
            // expected
        }
    }

    /**
     * Tests that null is returned when specifying an invalid meal type
     *
     * @throws IOException
     */
    @Test
    void testSpecifyThree() throws IOException {
        whisperMock.setMockScenario("invalid-meal-type.wav", "Brunch");
        assertNull(recipeBuilder.getMealTypeElement().specify(new File("invalid-meal-type.wav")));
        assertFalse(recipeBuilder.getMealTypeElement().isSet());
    }

    /**
     * Tests specifying an ingredient list
     *
     * @throws IOException
     */
    @Test
    void testSpecifyFour() throws IOException {
        whisperMock.setMockScenario("ingredients.wav", "Ingredient 1 and ingredient 2");
        assertEquals("Ingredient 1 and ingredient 2",
                recipeBuilder.getIngredientsElement().specify(new File("ingredients.wav")));
        assertEquals("Ingredient 1 and ingredient 2",
                recipeBuilder.getIngredientsElement().getValue());
    }

    /**
     * Tests the recipe that the builder returns
     * 
     * @throws IOException
     */
    @Test
    void testReturnRecipe() throws IOException, InterruptedException, URISyntaxException {
        chatGPTMock.setMockScenario(
                "Please provide a recipe with a title denoted with \"Title:\", a new line, and then a detailed recipe. Create a breakfast recipe with the following ingredients: Ingredient 1 and ingredient 2",
                "Title: Test Title\n\nIngredient 1 and ingredient 2");
        dallEMock.setMockScenario("Test Title", "hex 1");
        String recipeID = recipeBuilder.getRecipeID();
        recipeBuilder.getMealTypeElement().setValue("breakfast");
        recipeBuilder.getIngredientsElement().setValue("Ingredient 1 and ingredient 2");
        Recipe recipe = recipeBuilder.returnRecipe("");
        assertNotNull(recipe);
        assertEquals("Test Title", recipe.getTitle());
        assertEquals("Ingredient 1 and ingredient 2", recipe.getInstructions());
        assertEquals(recipeID, recipe.getRecipeID());
    }

    /**
     * Tests that the recipe is refreshed when the user provides a new recipe
     * 
     * @throws IOException
     */
    @Test
    void testRefreshRecipe() throws IOException, InterruptedException, URISyntaxException {
        chatGPTMock.setMockScenario(
                "Please provide a recipe with a title denoted with \"Title:\", a new line, and then a detailed recipe. Create a breakfast recipe with the following ingredients: Ingredient 1 and ingredient 2",
                "Title: Test Title\n\nIngredient 1 and ingredient 2");
        dallEMock.setMockScenario("Test Title", "hex 1");
        recipeBuilder.getMealTypeElement().setValue("breakfast");
        recipeBuilder.getIngredientsElement().setValue("Ingredient 1 and ingredient 2");
        Recipe oldRecipe = recipeBuilder.returnRecipe("");
        
        chatGPTMock.setMockScenario(
                "Please provide a recipe with a title denoted with \"Title:\", a new line, and then a detailed recipe. Create a breakfast recipe with the following ingredients: Ingredient 1 and ingredient 2",
                "Title: Test Title 2\n\nIngredient 1 and ingredient 2 but different");
        dallEMock.setMockScenario("Test Title 2", "hex 2");
        recipeBuilder.getMealTypeElement().setValue("breakfast");
        recipeBuilder.getIngredientsElement().setValue("Ingredient 1 and ingredient 2");
        Recipe newRecipe = recipeBuilder.returnRecipe("");

        assertEquals(oldRecipe.getRecipeID(), newRecipe.getRecipeID());
        assertNotEquals(oldRecipe.getTitle(), newRecipe.getTitle());
        assertNotEquals(oldRecipe.getInstructions(), newRecipe.getInstructions());
        assertEquals("Test Title 2", newRecipe.getTitle());
        assertEquals("Ingredient 1 and ingredient 2 but different", newRecipe.getInstructions());
    }
}
