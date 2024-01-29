package sr.wat.edu.pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import sr.wat.edu.pl.core.sys.RaSystem;
import sr.wat.edu.pl.core.sys.Node.NodeState;

public class CtlPanelController {

    private static CtlPanelController instance;

    public static CtlPanelController getInstance() {
        if (instance == null) {
            instance = new CtlPanelController();
        }
        return instance;
    }

    @FXML
    private Button joinSystemButton;
    @FXML
    private Button leaveSystemButton;
    @FXML
    private Button requestButton;
    @FXML
    private Button responseButton;
    @FXML
    private Button healthcheckButton;


    // Core
    RaSystem raSystem;
    

    public CtlPanelController() {
        raSystem = RaSystem.getInstance();
    }

    @FXML
    private void initialize() {
        instance = this;
    }

    

    public void enableToJoin() {
        setButtonsDisable(true);
        joinSystemButton.setDisable(false);
        RaSystem.getInstance().getLocalNode().setState(NodeState.READY);
        StatusPanelController.getInstance().setState(NodeState.READY);
    }

    public void disableToJoin() {
        setButtonsDisable(true);
        RaSystem.getInstance().getLocalNode().setState(NodeState.NOT_READY);
        StatusPanelController.getInstance().setState(NodeState.NOT_READY);
    }

    private void setButtonsDisable(boolean value) {
        joinSystemButton.setDisable(value);
        leaveSystemButton.setDisable(value);
        requestButton.setDisable(value);
        responseButton.setDisable(value);
        healthcheckButton.setDisable(value);
    }

    @FXML
    public void joinSystem() {
        raSystem.joinSystem();

        setButtonsDisable(false);
        joinSystemButton.setDisable(true);
    
        NetSettingsController.getInstance().disableNetworkControls();
    }

    @FXML
    public void leaveSystem() {
        raSystem.leaveSystem();

        enableToJoin();

        NetSettingsController.getInstance().enableNetworkControls();
    }

    @FXML
    public void performRequest() {
        raSystem.sendRequest();
    }

    @FXML
    public void resendResponse() {
        raSystem.sendResponse();
    }

    @FXML
    public void performHealthcheckPing() {
        raSystem.sendHealthcheck();
    }
}
