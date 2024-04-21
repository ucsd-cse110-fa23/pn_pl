package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import middleware.Controller;
import middleware.ServerCommunicator;

import java.io.File;


/**
 * This class represents the main class that runs the application.
 */
public class Main extends Application {

    static final int WINDOW_WIDTH = 950;
    static final int WINDOW_HEIGHT = 600;

    /**
     * Starts the UI.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        File ingredientsAudioFile = new File("ingredients.wav");
        File mealTypeAudioFile = new File("mealType.wav");

        Controller controller = new Controller(new ServerCommunicator());
        SceneManager sceneManager = new SceneManager(controller, ingredientsAudioFile, mealTypeAudioFile);

        // this is a circular dependency, but the controller only
        // uses the scene manager to display the server error scene
        controller.setSceneManager(sceneManager);

        primaryStage.setTitle("PantryPal2");
        primaryStage.setScene(new Scene(sceneManager, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.show();

        // displays the login screen at startup
        sceneManager.displayLoginScene();
    }

    /**
     * Runs the application.
     * 
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
}