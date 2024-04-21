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

/*
 * Integration tests for the backend.
 * 
 * These are based on the end-to-end scenarios we wrote
 * in our Milestone 1 document, which are based on the
 * BDD Scenarios in each user story.
 * 
 * We also make sure to cover all the features in the MS1
 * document, which are marked with comments
 * (except Feature 8, which we can't test on the backend)
 */
class MilestoneOneIntegrationTest {

    private ChatGPTMock chatGPTMock;
    private WhisperMock whisperMock;
    private DallEMock dallEMock;
    private RecipeBuilder recipeBuilder;
    private RecipeList recipeList;
    private File databaseFile;

    /**
     * Sets up the RecipeBuilder for testing.
     */
    @BeforeEach
    public void setUp() {
        this.chatGPTMock = new ChatGPTMock();
        this.whisperMock = new WhisperMock();
        this.dallEMock = new DallEMock();
        this.recipeBuilder = new RecipeBuilder(this.chatGPTMock, this.whisperMock, this.dallEMock);

        databaseFile = new File("test-database.json");
        if (databaseFile.exists()) {
            databaseFile.delete();
        }
    }

    /**
     * Tears down the RecipeListTest database file.
     */
    @AfterEach
    public void tearDown() {
        if (databaseFile.exists()) {
            databaseFile.delete();
        }
    }

    /*
     * Tests Scenario-based Milestone Test 1.1 (user stories 1, 2, 5)
     */
    @Test
    void testOurOwnTestCaseOne() throws IOException, InterruptedException, URISyntaxException {
        // preset the database contents
        String databaseContents = "[{\"instructions\":\"Fry the egg and fry the bacon\",\"dateCreated\":\"2023-11-12T15:57:23-08:00\",\"title\":\"Eggs and bacon\",\"recipeID\":\"id 1\",\"accountUsername\":\"\",\"mealType\":\"breakfast\",\"imageHex\":\"hex1\"},{\"instructions\":\"Cook pasta then add pesto\",\"dateCreated\":\"2023-11-12T15:57:24-08:00\",\"title\":\"Pesto pasta\",\"recipeID\":\"id 2\",\"accountUsername\":\"\",\"mealType\":\"dinner\",\"imageHex\":\"hex2\"}]";
        FileWriter fw = new FileWriter(this.databaseFile);
        fw.write(databaseContents);
        fw.flush();
        fw.close();

        // load the recipes from the database file and view (user story 1)
        // also Features 3 and 5 in MS1 document
        recipeList = new RecipeList(databaseFile);
        List<String> recipeIDs = recipeList.getRecipeIDs("", "most-recent", "all"); // we do this because the UI
                                                                                    // also
                                                                                    // pulls the IDs
        // first
        assertEquals(2, recipeIDs.size());
        assertEquals("Pesto pasta", recipeList.getRecipeByID(recipeIDs.get(0)).getTitle());
        assertEquals("Eggs and bacon", recipeList.getRecipeByID(recipeIDs.get(1)).getTitle());

        // view recipe instructions (user story 1)
        assertEquals("Fry the egg and fry the bacon",
                        recipeList.getRecipeByID(recipeIDs.get(1)).getInstructions());

        // this part of the scenario involved navigating back to the home page, but
        // can't really test that

        // create a new recipe (user story 2)
        // also Feature 1 in MS1 document
        recipeBuilder = new RecipeBuilder(chatGPTMock, whisperMock, dallEMock);
        String recipeID = recipeBuilder.getRecipeID();
        assertTrue(recipeID.length() > 10); // make sure it has an ID
        assertFalse(recipeBuilder.isCompleted());

        // view recipe meal type (user story 7 [milestone 2])
        assertEquals("breakfast", recipeList.getRecipeByID(recipeIDs.get(1)).getMealType());
        assertEquals("dinner", recipeList.getRecipeByID(recipeIDs.get(0)).getMealType());

        // we had something here to test if whisper didn't recognize any audio, but
        // turns out
        // whisper always returns some text even if you say nothing

        whisperMock.setMockScenario("ingredients.wav", "I have eggs, cheese, and bread.");
        recipeBuilder.getIngredientsElement().specify(new File("ingredients.wav"));
        assertFalse(recipeBuilder.isCompleted());

        // this was not part of the original Iteration 1 test because meal type wasn't
        // included
        whisperMock.setMockScenario("mealType.wav", "breakfast");
        recipeBuilder.getMealTypeElement().specify(new File("mealType.wav"));

        chatGPTMock.setMockScenario(
                        "Please provide a recipe with a title denoted with \"Title:\", a new line, and then a detailed recipe. Create a breakfast recipe with the following ingredients: I have eggs, cheese, and bread.",
                        "Title: Cheesy Egg Bread\n\n2 eggs, 3 cheese, 1 bread");
        dallEMock.setMockScenario("Cheesy Egg Bread", "hex 1"); // random URL of a pasta image
        assertTrue(recipeBuilder.isCompleted());
        Recipe recipe = recipeBuilder.returnRecipe("");
        assertEquals(recipeID, recipe.getRecipeID()); // make sure recipeID persists across builder and recipe
        assertEquals("Cheesy Egg Bread", recipe.getTitle());
        assertEquals("2 eggs, 3 cheese, 1 bread", recipe.getInstructions());

        // make sure recipe isn't saved yet
        assertEquals(2, recipeList.getRecipeIDs("", "most-recent", "all").size());

        // save recipe (user story 5)
        // also Feature 4 in MS1 document
        recipeList.addRecipe(recipe);
        assertEquals(3, recipeList.getRecipeIDs("", "most-recent", "all").size());
        assertEquals("Cheesy Egg Bread", recipeList.getRecipeByID(recipeID).getTitle());
        assertEquals("Cheesy Egg Bread",
                        recipeList.getRecipeByID(recipeList.getRecipeIDs("", "most-recent", "all").get(0))
                                        .getTitle());
        assertEquals("2 eggs, 3 cheese, 1 bread",
                        recipeList.getRecipeByID(recipeList.getRecipeIDs("", "most-recent", "all").get(0))
                                        .getInstructions());
    }

    /*
     * Tests Scenario-based Milestone Test 1.2 (user stories 3, 4, 5)
     * This is also Chef Caitlin's test
     */
    @Test
    void testChefCaitlinScenario() throws IOException, InterruptedException, URISyntaxException {
        // this test is very similar to the one above, so I'll omit the comments
        String databaseContents = "[{\"instructions\":\"Cook spaghetti and then add the tomato sauce and meatballs.\",\"dateCreated\":\"2023-11-12T15:57:24-08:00\",\"title\":\"Spaghetti with Tomato Sauce and Meatballs\",\"recipeID\":\"id 1\",\"accountUsername\":\"\",\"mealType\":\"dinner\",\"imageHex\":\"hex1\"}]";
        FileWriter fw = new FileWriter(this.databaseFile);
        fw.write(databaseContents);
        fw.flush();
        fw.close();

        recipeList = new RecipeList(databaseFile);
        List<String> recipeIDs = recipeList.getRecipeIDs("", "most-recent", "all");
        assertEquals(1, recipeIDs.size());
        assertEquals("Spaghetti with Tomato Sauce and Meatballs",
                        recipeList.getRecipeByID(recipeIDs.get(0)).getTitle());

        assertEquals("Cook spaghetti and then add the tomato sauce and meatballs.",
                        recipeList.getRecipeByID(recipeIDs.get(0)).getInstructions());

        whisperMock.setMockScenario("chicken-broccoli.wav", "I have chicken, broccoli, garlic, and rice.");
        recipeBuilder = new RecipeBuilder(chatGPTMock, whisperMock, dallEMock);
        String recipeID = recipeBuilder.getRecipeID();
        assertTrue(recipeID.length() > 10);
        assertFalse(recipeBuilder.isCompleted());
        assertEquals("I have chicken, broccoli, garlic, and rice.",
                        recipeBuilder.getIngredientsElement().specify(new File("chicken-broccoli.wav")));
        assertFalse(recipeBuilder.isCompleted());
        whisperMock.setMockScenario("dinner.wav", "Dinner.");
        assertEquals("dinner", recipeBuilder.getMealTypeElement().specify(new File("dinner.wav")));

        chatGPTMock.setMockScenario(
                        "Please provide a recipe with a title denoted with \"Title:\", a new line, and then a detailed recipe. Create a dinner recipe with the following ingredients: I have chicken, broccoli, garlic, and rice.",
                        "Title: Chicken Broccoli Stir-Fry\n\nGood instructions");
        dallEMock.setMockScenario("Chicken Broccoli Stir-Fry", "hex 1");
        assertTrue(recipeBuilder.isCompleted());
        Recipe recipe = recipeBuilder.returnRecipe("");
        assertEquals(recipeID, recipe.getRecipeID());
        assertEquals("Chicken Broccoli Stir-Fry", recipe.getTitle());
        assertEquals("Good instructions", recipe.getInstructions());

        recipeList.addRecipe(recipe);
        assertEquals(2, recipeList.getRecipeIDs("", "most-recent", "all").size());
        assertEquals("Chicken Broccoli Stir-Fry",
                        recipeList.getRecipeByID(recipeList.getRecipeIDs("", "most-recent", "all").get(0))
                                        .getTitle());
        assertEquals("Good instructions",
                        recipeList.getRecipeByID(recipeList.getRecipeIDs("", "most-recent", "all").get(0))
                                        .getInstructions());

        // this test also makes sure recipes persist across restarts (user story 5)
        // also Feature 4 in MS1 document
        recipeList = new RecipeList(databaseFile);
        assertEquals(2, recipeList.getRecipeIDs("", "most-recent", "all").size());
        assertEquals("Chicken Broccoli Stir-Fry",
                        recipeList.getRecipeByID(recipeList.getRecipeIDs("", "most-recent", "all").get(0))
                                        .getTitle());
        assertEquals("Good instructions",
                        recipeList.getRecipeByID(recipeList.getRecipeIDs("", "most-recent", "all").get(0))
                                        .getInstructions());
    }

    /*
     * Tests Scenario-based Milestone Test 2.1 (user stories 3, 4, 6)
    */
    @Test
    void testOurOwnTestCaseTwo() throws IOException, InterruptedException, URISyntaxException {
        // start with empty database
        recipeList = new RecipeList(databaseFile);
        assertEquals(0, recipeList.getRecipeIDs("", "most-recent", "all").size());

        // create a new recipe
        recipeBuilder = new RecipeBuilder(chatGPTMock, whisperMock, dallEMock);
        String recipeID = recipeBuilder.getRecipeID();

        // specify invalid meal type (user story 3)
        whisperMock.setMockScenario("invalid-meal.wav", "Brunch.");
        assertNull(recipeBuilder.getMealTypeElement().specify(new File("invalid-meal.wav")));
        assertFalse(recipeBuilder.getMealTypeElement().isSet());
        assertFalse(recipeBuilder.isCompleted());

        // now make it valid (user story 3)
        // also Feature 2 in MS1 document
        whisperMock.setMockScenario("breakfast.wav", "BREAKFAST!!!");
        assertEquals("breakfast", recipeBuilder.getMealTypeElement().specify(new File("breakfast.wav")));
        assertTrue(recipeBuilder.getMealTypeElement().isSet());
        assertFalse(recipeBuilder.isCompleted());

        whisperMock.setMockScenario("eggs-and-cheese.wav", "Eggs and cheese.");
        assertEquals("Eggs and cheese.",
                        recipeBuilder.getIngredientsElement().specify(new File("eggs-and-cheese.wav")));
        assertTrue(recipeBuilder.isCompleted());

        // generate recipe
        chatGPTMock.setMockScenario(
                        "Please provide a recipe with a title denoted with \"Title:\", a new line, and then a detailed recipe. Create a breakfast recipe with the following ingredients: Eggs and cheese.",
                        "Title: Cheesy Eggs\n\n2 eggs, 3 cheese");
        dallEMock.setMockScenario("Cheesy Eggs", "hex 1");
        Recipe recipe = recipeBuilder.returnRecipe("");
        assertEquals("Cheesy Eggs", recipe.getTitle());
        assertEquals("2 eggs, 3 cheese", recipe.getInstructions());
        recipeList.addRecipe(recipe);

        // edit recipe (user story 4)
        // also Feature 6 in MS1 document
        recipeList.getRecipeByID(recipeID).setInstructions("3 eggs, 2 cheese");
        assertEquals("3 eggs, 2 cheese", recipeList.getRecipeByID(recipeID).getInstructions());
        assertEquals("3 eggs, 2 cheese",
                        recipeList.getRecipeByID(recipeList.getRecipeIDs("", "most-recent", "all").get(0))
                                        .getInstructions());

        // delete recipe (user story 6)
        // also Feature 7 in MS1 document
        recipeList.removeRecipe(
                        recipeList.getRecipeByID(recipeList.getRecipeIDs("", "most-recent", "all").get(0)));
        assertEquals(0, recipeList.getRecipeIDs("", "most-recent", "all").size());

        // make sure recipe is deleted
        recipeList = new RecipeList(databaseFile);
        assertEquals(0, recipeList.getRecipeIDs("", "most-recent", "all").size());
    }

    /*
     * Tests Scenario-based Milestone Test 2.2 (user stories 3, 4, 6)
     * This is also Chef Caitlin's test
     */
    @Test
    void testChefCaitlinScenarioTwo() throws IOException, InterruptedException, URISyntaxException {
        recipeList = new RecipeList(databaseFile);
        RecipeBuilder recipeBuilder = new RecipeBuilder(chatGPTMock, whisperMock, dallEMock);
        whisperMock.setMockScenario("dinner.wav", "Dinner.");
        assertEquals("dinner", recipeBuilder.getMealTypeElement().specify(new File("dinner.wav")));
        whisperMock.setMockScenario("chicken-broccoli.wav", "I have chicken, broccoli, garlic, and rice.");
        assertEquals("I have chicken, broccoli, garlic, and rice.",
                        recipeBuilder.getIngredientsElement().specify(new File("chicken-broccoli.wav")));

        chatGPTMock.setMockScenario(
                        "Please provide a recipe with a title denoted with \"Title:\", a new line, and then a detailed recipe. Create a dinner recipe with the following ingredients: I have chicken, broccoli, garlic, and rice.",
                        "Title: Chicken Broccoli Stir-Fry\n\nGood instructions");
        dallEMock.setMockScenario("Chicken Broccoli Stir-Fry", "hex 1");
        Recipe recipe = recipeBuilder.returnRecipe("");
        assertEquals("Chicken Broccoli Stir-Fry", recipe.getTitle());
        assertEquals("Good instructions", recipe.getInstructions());
        recipeList.addRecipe(recipe);

        assertEquals(1, recipeList.getRecipeIDs("", "most-recent", "all").size());

        recipeList.removeRecipe(recipe);
        assertEquals(0, recipeList.getRecipeIDs("", "most-recent", "all").size());
    }
}