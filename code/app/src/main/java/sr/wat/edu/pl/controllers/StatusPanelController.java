package sr.wat.edu.pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import sr.wat.edu.pl.core.sys.Node.NodeState;

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

        setState(NodeState.NOT_READY);
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

    public void setState(NodeState state) {
        if (stateLabel != null) {
            stateLabel.setText(state.name());
            
            switch (state) {
                case NOT_READY:
                    setStatus("Node has to be configured.");
                    stateDiode.setFill(Color.GRAY);
                    break;
                case READY:
                    setStatus("Ready to join the system.");
                    stateDiode.setFill(Color.RED);
                    break;
                case IDLE:
                    setStatus("No requested tasks.");
                    stateDiode.setFill(Color.BLUE);
                    break;
                case WAITING:
                    setStatus("Waiting for access.");
                    stateDiode.setFill(Color.YELLOW);
                    break;
                case WORKING:
                    setStatus("Access Granted! Working...");
                    stateDiode.setFill(Color.GREEN);
                    break;
            
                default:
                    break;
            }
        }
    }
}
