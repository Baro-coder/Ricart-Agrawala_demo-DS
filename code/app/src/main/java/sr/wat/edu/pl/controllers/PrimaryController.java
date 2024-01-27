package sr.wat.edu.pl.controllers;


import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;


public class PrimaryController {

    @FXML
    private NetSettingsController netSettingsController;
    @FXML
    private SysSettingsController sysSettingsController;
    @FXML
    private CtlPanelController ctlPanelController;

    @FXML
    private StackPane netSettings;
    @FXML
    private StackPane sysSettings;
    @FXML
    private StackPane ctlPanel;


    public PrimaryController() {
        System.out.println("NetSettingsController   [" + netSettingsController + "] : from primary");
        System.out.println("SysSettingsController   [" + sysSettingsController + "] : from primary");
        System.out.println("CtlPanelController      [" + ctlPanelController + "] : from primary");
    }

    @FXML
    private void initialize() {

    }
}
