package ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.control.Label;

import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.control.TextField;

import middleware.Controller;

/**
 * This class represents the scene that displays the list of recipes.
 */
public class AccountCreationScene extends VBox {

    // The SceneMangager that displays the scene
    SceneManager sceneManager;

    // The Controller that communicates with the backend
    Controller controller;

    /**
     * This class represents the top bar of the account creation scene.
     */
    public class AccountCreationTopBar extends HBox {

        private Button cancelButton;

        /**
         * Constructs a new AccountCreationTopBar.
         */
        AccountCreationTopBar() {
            this.setAlignment(Pos.CENTER_LEFT);
            this.setPadding(new Insets(10, 10, 10, 10));
            this.setSpacing(10);
            this.setStyle("-fx-background-color: #c6ecc6;");

            cancelButton = createStyledButton("Cancel");
            cancelButton.setOnAction(e -> sceneManager.displayLoginScene());

            this.getChildren().add(cancelButton);

            Label title = new Label("Register for account");
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 20;");
            this.getChildren().add(title);
        }
    }

    /**
     * Constructs a new AccountCreationScene with the provided scene manager.
     * 
     * @param sceneManager
     */
    AccountCreationScene(SceneManager sceneManager, Controller controller) {
        this.sceneManager = sceneManager;
        this.controller = controller;

        this.setSpacing(5);
        this.setPadding(new Insets(10, 10, 10, 10));
        this.setAlignment(Pos.TOP_CENTER);
        this.setPrefSize(500, 560);
        this.setStyle("-fx-background-color: #e7ffe6;");
    }

    /**
     * Displays the account creation scene
     */
    public void displayAccountCreationScene() {
        this.getChildren().clear();

        // Username and password fields
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);
        TextField passwordField = new TextField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);

        TextField reEnterPassword = new TextField("");
        reEnterPassword.setPromptText("Re-enter Password");
        reEnterPassword.setMaxWidth(300);

        /**
         * Create account button executes functionality
         */
        Button createAccountButton = createStyledButton("Create Account");
        createAccountButton.setDisable(true);

        createAccountButton.setOnAction(e -> {
            if (!controller.passwordsMatch(passwordField.getText(), reEnterPassword.getText())) {
                this.getChildren().add(new Label("Passwords do not match"));
                return;
            }

            if (!controller.validateUsername(usernameField.getText())) {
                this.getChildren().add(new Label("Invalid username"));
                return;
            }

            if (!controller.validatePassword(passwordField.getText()) || !controller.validatePassword(reEnterPassword.getText())) {
                this.getChildren().add(new Label("Invalid password"));
                return;
            }
            
            // Checks that username is not already in use [username uniqueness]
            String response = controller.addAccount(usernameField.getText(), passwordField.getText());
            if (response == null) {
                this.getChildren().add(new Label("Username already in use, try another"));
            } else {
                sceneManager.getLoginScene().resetLabel();
                sceneManager.displayRecipeList();
            }
        });

        setTextFieldTriggers(usernameField, createAccountButton, passwordField);
        setTextFieldTriggers(passwordField, createAccountButton, usernameField);

        this.getChildren().addAll(usernameField, passwordField, reEnterPassword, createAccountButton);

        sceneManager.setCenter(this);
        sceneManager.setTop(new AccountCreationTopBar());
    }

    /**
     * Sets the triggers for the provided text field. The create account button will
     * be disabled if either the provided text field or the other text field is
     * empty.
     * 
     * @param textField
     * @param createAccountButton
     * @param otherTextField
     */
    private void setTextFieldTriggers(TextField textField, Button createAccountButton, TextField otherTextField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            createAccountButton.setDisable(textField.getText().length() == 0 || otherTextField.getText().length() == 0);
        });
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