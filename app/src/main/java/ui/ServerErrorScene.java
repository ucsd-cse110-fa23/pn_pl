package ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;

import javafx.scene.layout.*;
import javafx.scene.control.Label;
import javafx.geometry.Insets;

/**
 * This class represents the scene that displays the server status if server is
 * offline.
 */
public class ServerErrorScene extends VBox {

    SceneManager sceneManager;

    /**
     * Constructs a new ServerErrorScene with the provided scene manager.
     * 
     * @param sceneManager
     */
    ServerErrorScene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        this.setSpacing(5);
        this.setPadding(new Insets(10, 10, 10, 10));
        this.setAlignment(Pos.TOP_CENTER);
        this.setPrefSize(500, 560);
        this.setStyle("-fx-background-color: #e7ffe6;");
    }

    /**
     * Displays the server error scene.
     */
    public void displayServerErrorScene() {
        this.getChildren().clear();

        Label title = new Label("Server Error");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 20;");

        Label message = new Label("There was an error connecting to the server. Please try again later.");
        message.setStyle("-fx-font-size: 16;");

        Button quitButton = new Button("Quit");
        quitButton.setOnAction(e -> System.exit(0));

        this.getChildren().addAll(title, message, quitButton);
        this.setAlignment(Pos.CENTER);

        sceneManager.setCenter(this);
        sceneManager.setTop(null);
    }
}
