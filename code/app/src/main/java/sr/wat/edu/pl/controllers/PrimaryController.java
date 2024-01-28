package sr.wat.edu.pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;


public class PrimaryController {
    private static PrimaryController instance;

    public static PrimaryController getInstance() {
        if (instance == null) {
            instance = new PrimaryController();
        }
        return instance;
    }

    @FXML
    private TextArea logTextArea;


    public PrimaryController() {

    }

    @FXML
    private void initialize() {
        instance = this;
        
    }

    public void appendLog(String log) {
        if (logTextArea != null) {
            logTextArea.appendText(log + "\n");
        }
    }
}
