package sr.wat.edu.pl.controllers;

import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import sr.wat.edu.pl.core.sys.Node;


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
    @FXML
    private ListView<Node> nodesListView;


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

    public void clearRequestsList() {
        requestsListView.getItems().clear();
    }

    public void addNodeToList(Node node) {
        nodesListView.getItems().add(node);
    }

    public void removeNode(Node node) {
        nodesListView.getItems().remove(node);
    }

    public void clearNodes() {
        nodesListView.getItems().clear();
    }
}
