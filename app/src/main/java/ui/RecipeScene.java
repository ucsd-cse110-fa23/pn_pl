package ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.animation.PauseTransition;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import middleware.Controller;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

/**
 * This class represents the scene that displays the details of a recipe.
 */
class RecipeScene extends ScrollPane {

    SceneManager sceneManager;
    Controller controller;
    Label instructionsLabel;
    TextArea instructionsTextArea;
    ImageView imageView;
    VBox content;

    /**
     * This class represents the top bar of the recipe scene.
     */
    public class RecipeSceneTopBar extends HBox {

        /**
         * Constructs a new RecipeSceneTopBar with the provided recipe ID.
         * 
         * @param recipeID
         */
        RecipeSceneTopBar(String recipeID) {
            this.setAlignment(Pos.CENTER_LEFT);
            this.setPadding(new Insets(10, 10, 10, 10));
            this.setSpacing(10);
            this.setStyle("-fx-background-color: #c6ecc6;");

            Label title = new Label(controller.getRecipeTitle(recipeID));
            title.setFont(new Font(SceneManager.FONT, 20));

            Button backButton = createStyledButton("Back");
            backButton.setOnAction(e -> sceneManager.displayRecipeList());

            Button editButton = createStyledButton("Edit");
            editButton.setOnAction(e -> displayRecipeEditScene(recipeID));

            /**
             * This button copies the URL of the recipe to the user's clipboard for easy
             * sharing and viewing.
             */
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent clipboardContent = new ClipboardContent();
            Button shareButton = createStyledButton("Share");
            PauseTransition pause = new PauseTransition(javafx.util.Duration.seconds(2));
            pause.setOnFinished(e -> {
                shareButton.setText("Share");
                shareButton.setDisable(false);
            });

            shareButton.setOnAction(e -> {
                shareButton.setDisable(true);
                shareButton.setText("Copied to clipboard!");
                clipboardContent.putString(controller.getServerCommunicator().getURL() + "/recipe?recipeID=" + recipeID);
                clipboard.setContent(clipboardContent);
                pause.playFromStart();
            });

            Button deleteButton = createStyledButton("Delete");
            deleteButton.setOnAction(e -> {
                controller.removeRecipe(recipeID);
                sceneManager.displayRecipeList();
            });

            this.getChildren().addAll(backButton, title, editButton, shareButton, deleteButton);
        }
    }

    /**
     * This class represents the top bar of the newly created recipe scene.
     */
    public class NewlyCreatedRecipeSceneTopBar extends HBox {
        NewlyCreatedRecipeSceneTopBar(String recipeID) {
            this.setAlignment(Pos.CENTER_LEFT);
            this.setPadding(new Insets(10, 10, 10, 10));
            this.setSpacing(10);
            this.setStyle("-fx-background-color: #c6ecc6;");

            Label title = new Label(controller.getRecipeTitle(recipeID));
            title.setFont(new Font(SceneManager.FONT, 20));

            Button cancelButton = createStyledButton("Cancel");
            cancelButton.setOnAction(e -> sceneManager.displayRecipeList());

            Button saveButton = createStyledButton("Save");
            saveButton.setOnAction(e -> {
                controller.saveRecipe(recipeID);
                sceneManager.displayRecipeList();
            });

            // Refresh button creates a new recipe with meal type and ingredients data
            // preserved
            Button refreshButton = createStyledButton("Refresh");
            refreshButton.setOnAction(e -> {
                controller.generateRecipe(recipeID);
                sceneManager.displayNewlyCreatedRecipe(recipeID);
            });
            this.getChildren().addAll(cancelButton, title, saveButton, refreshButton);
        }
    }

    /**
     * This class represents the top bar of the recipe edit scene.
     */
    public class RecipeEditTopBar extends HBox {
        RecipeEditTopBar(String recipeID) {
            this.setAlignment(Pos.CENTER_LEFT);
            this.setPadding(new Insets(10, 10, 10, 10));
            this.setSpacing(10);
            this.setStyle("-fx-background-color: #c6ecc6;");

            Label title = new Label(controller.getRecipeTitle(recipeID));
            title.setFont(new Font(SceneManager.FONT, 20));

            Image image = new Image(new File("generated_image.png").toURI().toString());
            ImageView imageView = new ImageView(image);

            Button cancelButton = createStyledButton("Cancel");
            cancelButton.setOnAction(e -> sceneManager.displayRecipeList());

            Button saveButton = createStyledButton("Save Edits");
            saveButton.setOnAction(e -> {
                controller.editRecipe(recipeID, instructionsTextArea.getText());
                sceneManager.displayRecipeDetails(recipeID);
            });

            this.getChildren().addAll(cancelButton, title, imageView, saveButton);
        }
    }

    /**
     * Constructs a new RecipeScene with the provided scene manager and controller.
     * 
     * @param sceneManager
     */
    RecipeScene(SceneManager sceneManager, Controller controller) {
        this.sceneManager = sceneManager;
        this.controller = controller;

        instructionsLabel = new Label();
        instructionsLabel.setWrapText(true);
        instructionsLabel.setFont(new Font(SceneManager.FONT, 14));
        instructionsLabel.setStyle("-fx-background-color: #e7ffe6;");

        this.imageView = new ImageView();
        this.imageView.setFitWidth(200);
        this.imageView.setFitHeight(200);

        this.content = new VBox();
        this.content.setAlignment(Pos.CENTER);
        this.content.setSpacing(20);
        this.content.getChildren().addAll(imageView, instructionsLabel);

        this.setFitToWidth(true);
        this.setContent(this.content);
        this.setPadding(new Insets(30, 30, 30, 30));
        this.setStyle("-fx-background: #e7ffe6;");

        instructionsTextArea = new TextArea();
        instructionsTextArea.setPrefHeight(450);
    }

    /**
     * Displays the recipe edit scene.
     * 
     * @param recipeID
     */
    public void displayRecipeEditScene(String recipeID) {
        this.displayRecipeDetails(recipeID);
        this.instructionsTextArea.setText(controller.getRecipeInstructions(recipeID));
        this.instructionsTextArea.setWrapText(true);
        this.setContent(instructionsTextArea);
        sceneManager.setCenter(this);
        sceneManager.setTop(new RecipeEditTopBar(recipeID));
    }

    /**
     * Displays the recipe details.
     * 
     * @param recipeID
     */
    public void displayRecipeDetails(String recipeID) {
        instructionsLabel.setText(controller.getRecipeInstructions(recipeID));
        File imageFile = controller.getRecipeImage(recipeID);
        Image image = new Image(imageFile.toURI().toString());
        this.imageView.setImage(image);
        this.setContent(this.content);
        sceneManager.setCenter(this);
        sceneManager.setTop(new RecipeSceneTopBar(recipeID));
    }

    /**
     * Displays the newly created recipe.
     * 
     * @param recipeID
     */
    public void displayNewlyCreatedRecipe(String recipeID) {
        instructionsLabel.setText(controller.getRecipeInstructions(recipeID));
        File imageFile = controller.getRecipeImage(recipeID);
        Image image = new Image(imageFile.toURI().toString());
        this.imageView.setImage(image);
        this.setContent(this.content);
        sceneManager.setCenter(this);
        sceneManager.setTop(new NewlyCreatedRecipeSceneTopBar(recipeID));
    }

    /**
     * Creates a styled button with the provided text.
     * 
     * @param text
     * @return Button with the provided text
     */
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #a3d9a5; -fx-text-fill: #000000;");
        button.setFont(new Font(SceneManager.FONT, 14));
        button.setPadding(new Insets(5, 15, 5, 15));

        // Hover effect
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #8cc68c; -fx-text-fill: #000000;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #a3d9a5; -fx-text-fill: #000000;"));

        return button;
    }
}
