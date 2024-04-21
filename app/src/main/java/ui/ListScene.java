package ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

import java.util.List;
import middleware.Controller;

/**
 * This class represents the scene that displays the list of recipes.
 */
public class ListScene extends VBox {

    SceneManager sceneManager;
    ScrollPane scroller;
    Controller controller;

    /**
     * This class represents a recipe entry in the list of recipes.
     */
    public class RecipeInListUI extends HBox {
        /**
         * Constructs a new RecipeInListUI with the provided recipe ID and scene
         * 
         * @param recipeID
         * @param sceneManager
         */
        RecipeInListUI(String recipeID, SceneManager sceneManager) {
            this.setSpacing(10);
            this.setAlignment(Pos.CENTER_LEFT);
            this.setPadding(new Insets(10, 10, 10, 10));

            Label mealType = new Label(controller.getRecipeMealType(recipeID).toUpperCase());
            mealType.setFont(Font.font(SceneManager.FONT, FontPosture.ITALIC, 16));
            mealType.setWrapText(true);

            Label title = new Label(controller.getRecipeTitle(recipeID));
            title.setFont(new Font(SceneManager.FONT, 16));
            title.setWrapText(true);

            Button detailButton = createStyledButton("View Details");
            detailButton.setOnAction(e -> {
                sceneManager.displayRecipeDetails(recipeID);
            });

            this.getChildren().addAll(mealType, title, detailButton);
            this.setStyle("-fx-background-color: #e7ffe6; -fx-border-color: #a3d9a5; -fx-border-width: 0.5;");
        }
    }

    /**
     * This class represents the top bar of the list scene.
     */
    public class ListSceneTopBar extends HBox {
        /**
         * Constructs a new ListSceneTopBar with the provided scene manager.
         * 
         * @param sceneManager
         */
        ListSceneTopBar(SceneManager sceneManager) {
            this.setAlignment(Pos.CENTER);
            this.setPadding(new Insets(10, 10, 10, 10));
            this.setSpacing(10);

            /**
             * This label displays the text "Recipes" in bold font.
             */
            Label recipesLabel = new Label("Recipes");
            recipesLabel.setFont(new Font("Arial", 20));
            recipesLabel.setStyle("-fx-font-weight: bold;");

            /**
             * This button takes the user to the recipe creation scene.
             */
            Button newRecipeButton = createStyledButton("New Recipe");
            newRecipeButton.setOnAction(e -> sceneManager.displayRecipeCreationScene());

            /**
             * This label displays the text "Sort by:" in regular font.
             */
            Label sortByLabel = new Label("Sort by:");
            sortByLabel.setFont(new Font("Arial", 14));

            /**
             * This choice box allows the user to select how to sort the recipes.
             */
            ChoiceBox<String> sortChoiceBox = new ChoiceBox<>();
            sortChoiceBox.getItems().addAll("most-recent", "least-recent", "a-z", "z-a");
            sortChoiceBox.setValue(controller.getSortBy());
            sortChoiceBox.setOnAction(e -> {
                controller.setSortBy(sortChoiceBox.getValue());
                sceneManager.displayRecipeList();
            });

            /**
             * This label displays the text "Filter by:" in regular font.
             */
            Label filterByLabel = new Label("Filter by:");
            filterByLabel.setFont(new Font("Arial", 14));

            /**
             * This choice box allows the user to select how to filter the recipes.
             */
            ChoiceBox<String> filterChoiceBox = new ChoiceBox<>();
            filterChoiceBox.getItems().addAll("all", "breakfast", "lunch", "dinner");
            filterChoiceBox.setValue(controller.getFilterBy());
            filterChoiceBox.setOnAction(e -> {
                controller.setFilterBy(filterChoiceBox.getValue());
                sceneManager.displayRecipeList();
            });

            /**
             * This button logs the user out and takes them to the login scene.
             */
            Button logoutButton = createStyledButton("Logout");
            logoutButton.setOnAction(e -> {
                controller.logout();
                sceneManager.displayLoginScene();
            });

            // Containers for styling
            HBox rightContainer = new HBox(logoutButton);
            rightContainer.setAlignment(Pos.CENTER_RIGHT);
            HBox.setHgrow(rightContainer, Priority.ALWAYS);

            HBox leftContainer = new HBox(recipesLabel);
            leftContainer.setAlignment(Pos.CENTER_LEFT);

            this.getChildren().addAll(leftContainer, newRecipeButton, sortByLabel, sortChoiceBox, filterByLabel,
                    filterChoiceBox, rightContainer);
            this.setStyle("-fx-background-color: #c6ecc6;");
        }
    }

    /**
     * Constructs a new ListScene with the provided scene manager.
     * 
     * @param sceneManager
     */
    ListScene(SceneManager sceneManager, Controller controller) {
        this.sceneManager = sceneManager;
        this.controller = controller;
        this.setSpacing(5);
        this.setPadding(new Insets(10, 10, 10, 10));
        this.setPrefSize(500, 560);
        scroller = new ScrollPane(this);
        scroller.setFitToWidth(true);
        scroller.setFitToHeight(true);
        this.setStyle("-fx-background-color: #e7ffe6;");
    }

    /**
     * Displays the list of recipes.
     */
    public void displayRecipeList() {
        this.getChildren().clear();
        List<String> recipeIDs = controller.getRecipeIDs();
        for (String recipeID : recipeIDs) {
            RecipeInListUI recipeEntry = new RecipeInListUI(recipeID, this.sceneManager);
            this.getChildren().add(recipeEntry);
        }
        sceneManager.setCenter(scroller);
        sceneManager.setTop(new ListSceneTopBar(this.sceneManager));
    }

    /**
     * Creates a styled button with the provided text.
     * 
     * @param text
     * @return Button with the provided text.
     */
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #a3d9a5; -fx-text-fill: #000000;");
        button.setFont(new Font("Arial", 14));
        button.setPadding(new Insets(5, 15, 5, 15));

        // Hover effect
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #8cc68c; -fx-text-fill: #000000;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #a3d9a5; -fx-text-fill: #000000;"));

        return button;
    }
}