package ui;

import javafx.scene.layout.BorderPane;

import java.io.File;

import middleware.Controller;

/**
 * This class represents the scene manager that manages the scenes of the
 * application.
 */
public class SceneManager extends BorderPane implements ISceneManager {

    public static final String FONT = "Arial";

    ListScene listScene;
    Controller controller;
    RecipeScene recipeScene;
    RecipeCreationScene recipeCreationScene;
    AccountCreationScene accountCreationScene;
    LoginScene loginScene;
    ServerErrorScene serverErrorScene;
    boolean isPaused = false;

    /**
     * Constructs a new SceneManager with the provided controller and audio files.
     * 
     * @param ingredientsAudioFile
     * @param mealTypeAudioFile
     */
    public SceneManager(Controller controller, File ingredientsAudioFile, File mealTypeAudioFile) {
        this.controller = controller;

        this.setStyle("-fx-background-color: #e7ffe6;");
        this.listScene = new ListScene(this, controller);
        this.recipeScene = new RecipeScene(this, controller);
        this.recipeCreationScene = new RecipeCreationScene(this, controller, ingredientsAudioFile, mealTypeAudioFile);
        this.accountCreationScene = new AccountCreationScene(this, controller);
        this.loginScene = new LoginScene(this, controller);
        this.serverErrorScene = new ServerErrorScene(this);
    }

    public LoginScene getLoginScene() {
        return loginScene;
    }

    /**
     * Displays the recipe details.
     * 
     * @param recipeID
     */
    public void displayRecipeDetails(String recipeID) {
        if (isPaused) {
            return;
        }
        recipeScene.displayRecipeDetails(recipeID);
        if (isPaused) {
            this.setTop(null);
            this.setBottom(null);
        }
    }

    /**
     * Displays the new recipe.
     * 
     * @param recipeID
     */
    public void displayNewlyCreatedRecipe(String recipeID) {
        if (isPaused) {
            return;
        }
        recipeScene.displayNewlyCreatedRecipe(recipeID);
        if (isPaused) {
            this.setTop(null);
            this.setBottom(null);
        }
    }

    /**
     * Displays the recipe list.
     */
    public void displayRecipeList() {
        if (isPaused) {
            return;
        }
        listScene.displayRecipeList();
        if (isPaused) {
            this.setTop(null);
            this.setBottom(null);
        }
    }

    /**
     * Displays the recipe creation scene.
     */
    public void displayRecipeCreationScene() {
        if (isPaused) {
            return;
        }
        recipeCreationScene.displayRecipeCreationScene(controller.generateNewRecipeBuilder());
        if (isPaused) {
            this.setTop(null);
            this.setBottom(null);
        }
    }

    /**
     * Displays the login scene.
     * 
     * @param recipeID
     */
    public void displayLoginScene() {
        if (isPaused) {
            return;
        }
        loginScene.displayLoginScene();
        if (isPaused) {
            this.setTop(null);
            this.setBottom(null);
        }
    }

    /**
     * Displays the account creation scene.
     */
    public void displayAccountCreationScene() {
        if (isPaused) {
            return;
        }
        accountCreationScene.displayAccountCreationScene();
        if (isPaused) {
            this.setTop(null);
            this.setBottom(null);
        }
    }

    /**
     * Displays the server error scene.
     */
    public void displayServerErrorScene() {
        serverErrorScene.displayServerErrorScene();
        isPaused = true;
    }
}