package ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;

import javax.sound.sampled.*;
import java.io.File;

import middleware.Controller;

/**
 * This class represents the scene that allows the user to create a recipe.
 */
class RecipeCreationScene extends VBox {

    SceneManager sceneManager;
    Controller controller;
    Button completedButton;
    File ingredientsAudioFile;
    File mealTypeAudioFile;

    /**
     * This class represents the top bar of the recipe creation scene.
     */
    public class RecipeCreationTopBar extends HBox {

        private Button cancelButton;

        /**
         * Constructor for the RecipeCreationTopBar class.
         */
        RecipeCreationTopBar() {
            this.setAlignment(Pos.CENTER_LEFT);
            this.setPadding(new Insets(10, 10, 10, 10));
            this.setSpacing(10);
            this.setStyle("-fx-background-color: #c6ecc6;");

            cancelButton = createStyledButton("Cancel");
            cancelButton.setOnAction(e -> sceneManager.displayRecipeList());
            this.getChildren().add(cancelButton);

            Label title = new Label("Create a Recipe");
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 20;");
            this.getChildren().add(title);
        }

        /**
         * Returns the cancel button.
         * 
         * @return cancel button
         */
        public Button getCancelButton() {
            return cancelButton;
        }
    }

    /**
     * This class represents an audio recorder that allows the user to record audio
     * from the microphone.
     */
    public class AudioRecorder {

        File audioFile;
        ToggleButton recordButton;
        AudioFormat audioFormat;
        TargetDataLine targetDataLine;

        /**
         * Constructor for the AudioRecorder class.
         * 
         * @param audioFile
         * @param recordButton
         */
        AudioRecorder(File audioFile, ToggleButton recordButton) {
            this.audioFile = audioFile;
            this.recordButton = recordButton;
            this.audioFormat = new AudioFormat(
                    44100,
                    16,
                    1,
                    true,
                    false);
        }

        /**
         * Allows the user to record audio when the "Record ..." button is pressed.
         */
        public void recordAudio() {
            Thread recordingThread = new Thread(this::startRecording);
            recordingThread.start();
        }

        /**
         * Stops the audio recording process when the "Stop Recording" button is
         * pressed.
         */
        public void stopRecordingAudio() {
            stopRecording();
        }

        /**
         * This method is used to start the audio recording process.
         */
        private void startRecording() {
            try {
                DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
                targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
                targetDataLine.open(audioFormat);
                targetDataLine.start();
                AudioInputStream audioInputStream = new AudioInputStream(targetDataLine);
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, audioFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * This method is used to stop the audio recording process.
         */
        private void stopRecording() {
            targetDataLine.stop();
            targetDataLine.close();
        }
    }

    /**
     * Constructor for the RecipeCreationScene class.
     * 
     * @param sceneManager
     * @param ingredientsAudioFile
     * @param mealTypeAudioFile
     */
    RecipeCreationScene(SceneManager sceneManager, Controller controller, File ingredientsAudioFile,
            File mealTypeAudioFile) {
        this.sceneManager = sceneManager;
        this.controller = controller;
        this.setSpacing(10);
        this.setPadding(new Insets(20, 20, 20, 20));
        this.setAlignment(Pos.TOP_CENTER);
        this.setStyle("-fx-background-color: #e7ffe6;");

        this.ingredientsAudioFile = ingredientsAudioFile;
        this.mealTypeAudioFile = mealTypeAudioFile;
    }

    /**
     * Sets up a recording button with all the appropriate triggers.
     * 
     * @param button
     * @param label
     * @param successMessage
     * @param invalidTypeMessage
     * @param recipeID
     * @param elementName
     * @param audioFile
     * @param completeButton
     * @param cancelButton
     * @param otherButton
     */
    private void setRecordingButtonTriggers(ToggleButton button,
            Label label,
            String successMessage,
            String invalidTypeMessage,
            String recipeID,
            String elementName,
            File audioFile,
            Button completeButton,
            Button cancelButton,
            ToggleButton otherButton) {
        AudioRecorder audioRecorder = new AudioRecorder(audioFile, button);
        String originalText = button.getText();

        // this button starts/stops the audio recording
        button.setOnAction(e -> {
            if (button.isSelected()) {
                // disables all other buttons when recording
                controller.resetRecipeCreatorElement(recipeID, elementName);
                completeButton.setDisable(true);
                otherButton.setDisable(true);
                cancelButton.setDisable(true);
                audioRecorder.recordAudio();
                button.setText("Stop Recording");
            } else {
                // send recording to the server and display the response
                audioRecorder.stopRecordingAudio();
                String result = controller.specifyRecipeCreatorElement(recipeID, elementName, audioFile);
                // System.out.println(result);
                if (result == null) {
                    label.setText(invalidTypeMessage);
                } else {
                    if (result.contains("error")) {
                        label.setText("Error, button pressed too early. Please try again.");
                        button.setText("Record " + elementName);
                        return;
                    } else if (result.contains("Audio file is too short")) { // Switch this to no audio found
                        label.setText("Error, button pressed too early. Please try again.");
                        button.setText("Record " + elementName);
                        return;
                    } else if (result.contains("No ingredients specified")) {
                        label.setText("No ingredients specified. Please try again.");
                        button.setText("Record " + elementName);
                        return;
                    } else {
                        label.setText(String.format(successMessage, result));
                    }
                }
                button.setDisable(false);
                button.setText(originalText);
                otherButton.setDisable(false);
                cancelButton.setDisable(false);
                if (controller.isRecipeCreatorCompleted(recipeID)) {
                    completeButton.setDisable(false);
                }
            }
        });
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
        button.setFont(new Font(sceneManager.FONT, 14));
        button.setPadding(new Insets(10, 20, 10, 20));
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #8cc68c; -fx-text-fill: #000000;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #a3d9a5; -fx-text-fill: #000000;"));
        return button;
    }

    /**
     * Creates a styled toggle button.
     * 
     * @param toggleButton
     * @return
     */
    private ToggleButton createStyledToggleButton(ToggleButton toggleButton) {
        toggleButton.setStyle("-fx-background-color: #a3d9a5; -fx-text-fill: #000000;");
        toggleButton.setFont(new Font(SceneManager.FONT, 14));
        toggleButton.setPadding(new Insets(10, 20, 10, 20));

        // Hover effect
        toggleButton.setOnMouseEntered(e -> {
            if (!toggleButton.isSelected())
                toggleButton.setStyle("-fx-background-color: #8cc68c; -fx-text-fill: #000000;");
        });

        // Hover effect
        toggleButton.setOnMouseExited(e -> {
            if (!toggleButton.isSelected())
                toggleButton.setStyle("-fx-background-color: #a3d9a5; -fx-text-fill: #000000;");
        });

        // Selected effect
        toggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                toggleButton.setStyle("-fx-background-color: #6fa06b; -fx-text-fill: #000000;");
            } else {
                toggleButton.setStyle("-fx-background-color: #a3d9a5; -fx-text-fill: #000000;");
            }
        });
        return toggleButton;
    }

    /**
     * Displays the recipe creation scene.
     * 
     * @param recipeID
     */
    public void displayRecipeCreationScene(String recipeID) {
        this.getChildren().clear();
        Button completeButton = createStyledButton("Generate Recipe");
        RecipeCreationTopBar topBar = new RecipeCreationTopBar();

        Label recordMealTypeLabel = new Label("Please select either breakfast, lunch, or dinner.");
        Label recordIngredientsLabel = new Label("Recorded ingredients will appear here...");
        ToggleButton recordIngredientsButton = new ToggleButton("Record Ingredients");
        ToggleButton recordMealTypeButton = new ToggleButton("Record Meal Type");

        // Set triggers for the meal type button
        setRecordingButtonTriggers(recordMealTypeButton,
                recordMealTypeLabel,
                "You selected %s",
                "Please select either breakfast, lunch, or dinner.",
                recipeID,
                "mealType",
                mealTypeAudioFile,
                completeButton,
                topBar.getCancelButton(),
                recordIngredientsButton);
        this.getChildren().add(createStyledToggleButton(recordMealTypeButton));
        this.getChildren().add(recordMealTypeLabel);

        // Set triggers for the ingredients button
        setRecordingButtonTriggers(recordIngredientsButton,
                recordIngredientsLabel,
                "You said: %s",
                null,
                recipeID,
                "ingredients",
                ingredientsAudioFile,
                completeButton,
                topBar.getCancelButton(),
                recordMealTypeButton);
        this.getChildren().add(createStyledToggleButton(recordIngredientsButton));
        this.getChildren().add(recordIngredientsLabel);

        // Set the complete button trigger
        completeButton.setOnAction(e -> {
            controller.generateRecipe(recipeID);
            sceneManager.displayNewlyCreatedRecipe(recipeID);
        });
        completeButton.setDisable(true);
        this.getChildren().add(completeButton);
        sceneManager.setTop(topBar);
        sceneManager.setCenter(this);
    }
}
