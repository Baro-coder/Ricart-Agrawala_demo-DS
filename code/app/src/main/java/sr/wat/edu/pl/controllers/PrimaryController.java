package sr.wat.edu.pl.controllers;

import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
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
    @FXML
    private ListView<String> requestsListView;


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

    public void addRequestToList(int index, String record) {
        requestsListView.getItems().add(index, record);
    }

    public void addAllRequests(ArrayList<String> records) {
        requestsListView.getItems().addAll(records);
    }

    public void removeRequestByIndex(int index) {
        if (requestsListView.getItems().size() > index) {
            requestsListView.getItems().remove(index);
        }
    }
}
