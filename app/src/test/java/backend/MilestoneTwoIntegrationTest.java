package backend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


class MilestoneTwoIntegrationTest {

    private ChatGPTMock chatGPTMock;
    private WhisperMock whisperMock;
    private DallEMock dallEMock;
    private RecipeBuilder recipeBuilder;
    private RecipeList recipeList;
    private AccountList accountList;
    private File recipeDatabaseFile;
    private File accountDatabaseFile;

    @BeforeEach
    public void setUp() {
        this.chatGPTMock = new ChatGPTMock();
        this.whisperMock = new WhisperMock();
        this.dallEMock = new DallEMock();
        recipeDatabaseFile = new File("test-recipe-database.json");
        if (recipeDatabaseFile.exists()) {
            recipeDatabaseFile.delete();
        }
        accountDatabaseFile = new File("test-account-database.json");
        if (accountDatabaseFile.exists()) {
            accountDatabaseFile.delete();
        }
        accountList = new AccountList(accountDatabaseFile);
        recipeList = new RecipeList(recipeDatabaseFile);
    }

    @AfterEach
    public void tearDown() {
        if (recipeDatabaseFile.exists()) {
            recipeDatabaseFile.delete();
        }
        if (accountDatabaseFile.exists()) {
            accountDatabaseFile.delete();
        }
    }

    /*
     * Integration test based on scenario-based system test
     * entitled "Caitlin starts using PantryPal 2" (in our
     * MS2 planning document)
     * 
     * Tests user stories 1, 2, 3
     */
    @Test
    void testCaitlinStartsUsingPantryPalTwo() throws IOException, InterruptedException, URISyntaxException {
        // user story 1 scenario 1
        assertTrue(accountList.addAccount("Caitlin", "password123"));
        assertEquals(0, recipeList.getRecipeIDs("Caitlin", "most-recent", "all").size());
        assertTrue(accountList.attemptLogin("Caitlin", "password123"));
        assertEquals(0, recipeList.getRecipeIDs("Caitlin", "most-recent", "all").size());

        // user story 3 scenario 1
        RecipeBuilder builder = new RecipeBuilder(chatGPTMock, whisperMock, dallEMock);
        whisperMock.setMockScenario("breakfast.wav", "breakfast");
        builder.getMealTypeElement().specify(new File("breakfast.wav"));
        whisperMock.setMockScenario("eggs-and-bacon.wav", "Eggs and bacon");
        builder.getIngredientsElement().specify(new File("eggs-and-bacon.wav"));
        chatGPTMock.setMockScenario("Please provide a recipe with a title denoted with \"Title:\", a new line, and then a detailed recipe. Create a breakfast recipe with the following ingredients: Eggs and bacon", "Title: Eggs and bacon\nFry bacon and add eggs");
        dallEMock.setMockScenario("Eggs and bacon", "hex of eggs and bacon");
        Recipe recipe = builder.returnRecipe("Caitlin");
        assertEquals("hex of eggs and bacon", recipe.getImageHex());
        recipeList.addRecipe(recipe);
    }
  
    /*
     * Integration test for Iteration 1 scenario-based system test
     * entitled "Our own test scenario"
     * 
     * Covers user stories 1, 2, 3
     */
    @Test
    void testOurOwnTestScenario() throws IOException, InterruptedException, URISyntaxException {
        // user story 2 scenario 1
        assertTrue(accountList.addAccount("Caitlin", "password123"));
        assertEquals(0, recipeList.getRecipeIDs("Caitlin", "most-recent", "all").size());

        // user story 2 scenario 3
        assertFalse(accountList.attemptLogin("Caitlin", "chefcaitlin"));

        // user story 1 scenario 3
        assertFalse(accountList.addAccount("Caitlin", "chefcaitlin123"));

        // user story 2 scenario 2
        assertTrue(accountList.attemptLogin("Caitlin", "password123"));
        RecipeBuilder builder = new RecipeBuilder(chatGPTMock, whisperMock, dallEMock);
        whisperMock.setMockScenario("dinner.wav", "dinner");
        builder.getMealTypeElement().specify(new File("dinner.wav"));
        whisperMock.setMockScenario("salmon-and-rice.wav", "salmon and rice");
        builder.getIngredientsElement().specify(new File("salmon-and-rice.wav"));
        chatGPTMock.setMockScenario("Please provide a recipe with a title denoted with \"Title:\", a new line, and then a detailed recipe. Create a dinner recipe with the following ingredients: salmon and rice", "Title: Salmon and rice\nCook salmon and rice");
        dallEMock.setMockScenario("Salmon and rice", "hex of salmon and rice");
        Recipe recipe = builder.returnRecipe("Caitlin");
        assertEquals("hex of salmon and rice", recipe.getImageHex());
        recipeList.addRecipe(recipe);

        // user story 3 scenario 2
        assertEquals(1, recipeList.getRecipeIDs("Caitlin", "most-recent", "all").size());
        assertEquals("hex of salmon and rice", recipeList.getRecipeByID(recipeList.getRecipeIDs("Caitlin", "most-recent", "all").get(0)).getImageHex());
    }
  
    /*
     * Integration test based on scenario-based system test
     * entitled "Our own test scenario" for Iteration 2
     *
     * Covers user stories 4, 5, 6, 7, 8, 9, 10
     */
    @Test
    void testOurOwnTestScenarioTwo() throws IOException, InterruptedException, URISyntaxException {
        assertTrue(accountList.addAccount("Caitlin", "password123"));
        assertEquals(0, recipeList.getRecipeIDs("Caitlin", "most-recent", "all").size());
        assertTrue(accountList.attemptLogin("Caitlin", "password123"));
        assertEquals(0, recipeList.getRecipeIDs("Caitlin", "most-recent", "all").size());

        // set up recipes for testing
        RecipeBuilder builder = new RecipeBuilder(chatGPTMock, whisperMock, dallEMock);
        whisperMock.setMockScenario("lunch.wav", "lunch");
        builder.getMealTypeElement().specify(new File("lunch.wav"));
        whisperMock.setMockScenario("lettuce-and-chicken.wav", "Lettuce and chicken");
        builder.getIngredientsElement().specify(new File("lettuce-and-chicken.wav"));
        chatGPTMock.setMockScenario("Please provide a recipe with a title denoted with \"Title:\", a new line, and then a detailed recipe. Create a lunch recipe with the following ingredients: Lettuce and chicken", "Title: Lettuce and chicken\nToss");
        dallEMock.setMockScenario("Lettuce and chicken", "hex of lettuce and chicken");
        Recipe oldRecipe = builder.returnRecipe("Caitlin");
        
        // regenerate recipe
        chatGPTMock.setMockScenario("Please provide a recipe with a title denoted with \"Title:\", a new line, and then a detailed recipe. Create a lunch recipe with the following ingredients: Lettuce and chicken", "Title: Lettuce and chicken 2\nToss but diferent");
        dallEMock.setMockScenario("Lettuce and chicken 2", "hex of lettuce and chicken but different");
        Recipe newRecipe = builder.returnRecipe("Caitlin");

        // user story 6 scenario 1
        assertNotEquals(oldRecipe.getTitle(), newRecipe.getTitle());
        assertNotEquals(oldRecipe.getInstructions(), newRecipe.getInstructions());
        assertNotEquals(oldRecipe.getImageHex(), newRecipe.getImageHex());
        assertEquals("hex of lettuce and chicken but different", newRecipe.getImageHex());

        // user story 6 scenario 2
        recipeList.addRecipe(newRecipe);
        assertEquals("Lettuce and chicken 2", recipeList.getRecipeByID(recipeList.getRecipeIDs("Caitlin", "most-recent", "all").get(0)).getTitle());
    }
  
  
    /*
     * Integration test based on scenario-based system test
     * entitled "Caitlin enjoys the new features of PantryPal 2"
     * 
     * Covers user stories 4, 5, 6, 7, 8, 9, 10
     */
    @Test
    void testCaitlinEnjoysTheNewFeaturesOfPantryPalTwo() throws IOException, InterruptedException, URISyntaxException {
        assertTrue(accountList.addAccount("Caitlin", "password123"));
        assertTrue(accountList.attemptLogin("Caitlin", "password123"));

        // set up recipes for testing
        RecipeBuilder builder = new RecipeBuilder(chatGPTMock, whisperMock, dallEMock);
        whisperMock.setMockScenario("breakfast.wav", "breakfast");
        builder.getMealTypeElement().specify(new File("breakfast.wav"));
        whisperMock.setMockScenario("eggs-and-cheese.wav", "Eggs and cheese");
        builder.getIngredientsElement().specify(new File("eggs-and-cheese.wav"));
        chatGPTMock.setMockScenario("Please provide a recipe with a title denoted with \"Title:\", a new line, and then a detailed recipe. Create a breakfast recipe with the following ingredients: Eggs and cheese", "Title: Eggs and cheese\nCook");
        dallEMock.setMockScenario("Eggs and cheese", "hex of eggs and cheese");
        Recipe recipe = builder.returnRecipe("Caitlin");
        assertEquals("hex of eggs and cheese", recipe.getImageHex());
        recipeList.addRecipe(recipe);

        // need this because sorting by most recent sometimes fails otherwise
        Thread.sleep(1);
        
        builder = new RecipeBuilder(chatGPTMock, whisperMock, dallEMock);
        whisperMock.setMockScenario("dinner.wav", "dinner");
        builder.getMealTypeElement().specify(new File("dinner.wav"));
        whisperMock.setMockScenario("pasta-and-tomato-sauce.wav", "Pasta and tomato sauce");
        builder.getIngredientsElement().specify(new File("pasta-and-tomato-sauce.wav"));
        chatGPTMock.setMockScenario("Please provide a recipe with a title denoted with \"Title:\", a new line, and then a detailed recipe. Create a dinner recipe with the following ingredients: Pasta and tomato sauce", "Title: Pasta Marinara\nCook");
        dallEMock.setMockScenario("Pasta Marinara", "hex of pasta marinara");
        recipe = builder.returnRecipe("Caitlin");
        assertEquals("hex of pasta marinara", recipe.getImageHex());
        recipeList.addRecipe(recipe);

        // user story 7 scenario 1
        assertEquals("dinner", recipeList.getRecipeByID(recipeList.getRecipeIDs("Caitlin", "most-recent", "all").get(0)).getMealType());
        assertEquals("breakfast", recipeList.getRecipeByID(recipeList.getRecipeIDs("Caitlin", "most-recent", "all").get(1)).getMealType());

        assertEquals("Pasta Marinara", recipeList.getRecipeByID(recipeList.getRecipeIDs("Caitlin", "most-recent", "all").get(0)).getTitle());

        assertEquals("Eggs and cheese", recipeList.getRecipeByID(recipeList.getRecipeIDs("Caitlin", "a-z", "all").get(0)).getTitle());

        assertEquals("Pasta Marinara", recipeList.getRecipeByID(recipeList.getRecipeIDs("Caitlin", "z-a", "all").get(0)).getTitle());

        assertEquals("Eggs and cheese", recipeList.getRecipeByID(recipeList.getRecipeIDs("Caitlin", "least-recent", "all").get(0)).getTitle());

        assertEquals(2, recipeList.getRecipeIDs("Caitlin", "most-recent", "all").size());

        assertEquals(1, recipeList.getRecipeIDs("Caitlin", "most-recent", "breakfast").size());
        assertEquals("Eggs and cheese", recipeList.getRecipeByID(recipeList.getRecipeIDs("Caitlin", "most-recent", "breakfast").get(0)).getTitle());

        assertEquals(2, recipeList.getRecipeIDs("Caitlin", "most-recent", "all").size());

        // user story 8 scenario 1
        assertEquals("Pasta Marinara", recipeList.getRecipeByID(recipeList.getRecipeIDs("Caitlin", "most-recent", "all").get(0)).getTitle());

        // user story 8 scenario 2
        assertEquals("Eggs and cheese", recipeList.getRecipeByID(recipeList.getRecipeIDs("Caitlin", "a-z", "all").get(0)).getTitle());

        // user story 8 scenario 3
        assertEquals("Pasta Marinara", recipeList.getRecipeByID(recipeList.getRecipeIDs("Caitlin", "z-a", "all").get(0)).getTitle());

        // user story 8 scenario 5
        assertEquals("Eggs and cheese", recipeList.getRecipeByID(recipeList.getRecipeIDs("Caitlin", "least-recent", "all").get(0)).getTitle());

        // user story 8 scenario 6
        assertEquals(2, recipeList.getRecipeIDs("Caitlin", "most-recent", "all").size());

        // user story 9 scenario 1
        assertEquals(1, recipeList.getRecipeIDs("Caitlin", "most-recent", "breakfast").size());
        assertEquals("Eggs and cheese", recipeList.getRecipeByID(recipeList.getRecipeIDs("Caitlin", "most-recent", "breakfast").get(0)).getTitle());

        // user story 9 scenario 2
        assertEquals(2, recipeList.getRecipeIDs("Caitlin", "most-recent", "all").size());

        // user story 5 scenario 1
        assertEquals("<html><body style=\"background-color: #e7ffe6; font-family: Arial;\"><h1>Eggs and cheese</h1><img src=\"data:image/png;base64,/u///u/vn8+/7v4=\" alt=\"Recipe Image\"><p>Cook</p></body></html>", recipeList.getRecipeByID(recipeList.getRecipeIDs("Caitlin", "a-z", "all").get(0)).toHTML());
    }
}

