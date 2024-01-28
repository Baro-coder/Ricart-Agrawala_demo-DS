package sr.wat.edu.pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;

public class StatusPanelController {
    private static StatusPanelController instance;

    public static StatusPanelController getInstance() {
        if (instance == null) {
            instance = new StatusPanelController();
        }
        return instance;
    }

    // Network config info
    @FXML
    private Label netInterfaceInfoLabel;
    @FXML
    private Label multicastGroupInfoLabel;

    // Node status
    @FXML
    private Label statusLabel;
    @FXML
    private Label stateLabel;
    @FXML
    private Circle stateDiode;


    public StatusPanelController() {
        System.out.println("StatusPanelController   [" + this + "]  : constructor");
    }

    @FXML
    private void initialize() {
        instance = this;

        // Node status
        statusLabel.setText("Init...");
        stateLabel.setText("Init");
    }


    public void setInterfaceInfo(String interfaceName, String interfaceAddress, int maskLength) {
        if (netInterfaceInfoLabel != null) {
            netInterfaceInfoLabel.setText(String.format("%s  %s /%d", interfaceName, interfaceAddress, maskLength));
        }
    }

    public void setMulticastGroupInfo(String groupAddress, int groupPort) {
        if (multicastGroupInfoLabel != null) {
            multicastGroupInfoLabel.setText(String.format("%s : %d", groupAddress, groupPort));
        }
    }


    public void setStatus(String text) {
        if (statusLabel != null) {
            statusLabel.setText(text);
        }
    }

    public void setState(String text) {
        if (stateLabel != null) {
            stateLabel.setText(text);
        }
    }
}
