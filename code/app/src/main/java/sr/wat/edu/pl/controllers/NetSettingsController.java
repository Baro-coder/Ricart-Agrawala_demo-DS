package sr.wat.edu.pl.controllers;

import javafx.beans.value.ObservableValue;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import sr.wat.edu.pl.core.net.NetInterface;
import sr.wat.edu.pl.core.net.NetManager;


public class NetSettingsController {
    private static NetSettingsController instance;

    public static NetSettingsController getInstance() {
        if (instance == null) {
            instance = new NetSettingsController();
        }
        return instance;
    }
    
    // Network Interface
    @FXML
    private ComboBox<String> interfaceComboBox;
    @FXML
    private Label interfaceErrorLabel;

    // Multicast Group IPv4 Address
    @FXML
    private TextField aAddressTextField;    // First Octet
    @FXML
    private TextField bAddressTextField;    // Second Octet
    @FXML
    private TextField cAddressTextField;    // Third Octet
    @FXML
    private TextField dAddressTextField;    // Fourth Octet
    @FXML
    private Label addressErrorLabel;

    // Port Address
    @FXML
    private TextField portTextField;
    @FXML
    private Label portErrorLabel;

    // Healthcheck
    @FXML
    private Label healthcheckPeriodLabel;
    @FXML
    private Slider healthcheckPeriodSlider;
    @FXML
    private ToggleGroup healthcheckModeToggleGroup;
    @FXML
    private RadioButton healthcheckAutoRadioButton;
    @FXML
    private RadioButton healthcheckManualRadioButton;

    // Buttons
    @FXML
    private Button applyButton;

    // Controllers
    CtlPanelController ctlPanelController;

    // Core
    NetManager netManager;


    // Constructor
    public NetSettingsController() {
        netManager = NetManager.getInstance();
    }

    // FXML Controls init
    @FXML
    private void initialize() {
        instance = this;

        // Network Interface ComboBox init
        netManager.updateInterfacesList();
        interfaceComboBox.getItems().addAll(netManager.getInterfacesNames());
        interfaceComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (oldValue != null && !oldValue.equals(newValue)) {
                    handleInterfaceComboBoxChange(newValue);
                }
            }
        });
        if (interfaceComboBox.getItems().isEmpty()) {
            interfaceErrorLabel.setText("No available interface!");
        }
        

        // Address / Port TextFields init
        addNumericFilter(aAddressTextField, 3);
        addNumericFilter(bAddressTextField, 3);
        addNumericFilter(cAddressTextField, 3);
        addNumericFilter(dAddressTextField, 3);
        addNumericFilter(portTextField, 5);
        addressErrorLabel.setText("");
        portErrorLabel.setText("");

        // Healthcheck init
        healthcheckPeriodLabel.setText(String.valueOf(healthcheckPeriodSlider.getValue()));
        healthcheckPeriodSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                healthcheckPeriodLabel.setText(String.valueOf(newValue.intValue()));
            }
        });

        healthcheckModeToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldState, Toggle newState) {
                RadioButton selectedRadioButton = (RadioButton) healthcheckModeToggleGroup.getSelectedToggle();
                if (selectedRadioButton.equals(healthcheckAutoRadioButton)) {
                    netManager.setAutoHealthcheck(true);
                } else {
                    netManager.setAutoHealthcheck(false);
                }
            }
        });
    }

    // TextField numeric filter
    private void addNumericFilter(TextField textField, int maxLength) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                textField.setText(newValue.replaceAll("[^\\d]", ""));
            }
    
            if (newValue.length() > maxLength) {
                textField.setText(oldValue);
            }
        });
    }

    // Interface change
    private void handleInterfaceComboBoxChange(String selectedOption) {
        NetInterface selectedInterface = netManager.getInterfaceByName(selectedOption);
        if (!selectedInterface.supportsMulticast()) {
            // Error
            interfaceErrorLabel.setText("Multicast not supported!");
        } else {
            interfaceErrorLabel.setText("");
            netManager.setActiveInterface(selectedInterface);
        }

        StatusPanelController.getInstance().setInterfaceInfo(selectedInterface.getName(), selectedInterface.getAddress(), selectedInterface.getMaskLength());
    }

    @FXML
    private void applySettings() {
        boolean settingsValid = true;

        String multicastGroupAddress = "-";
        int multicastGroupPort = -1;

        if (aAddressTextField.getText().isBlank() || bAddressTextField.getText().isBlank() || cAddressTextField.getText().isBlank() || dAddressTextField.getText().isBlank()) {
            addressErrorLabel.setText("Required fields!");
            settingsValid = false;
        } else {
            try {
                netManager.setMulticastGroupAddress(aAddressTextField.getText(), bAddressTextField.getText(), cAddressTextField.getText(), dAddressTextField.getText());
                addressErrorLabel.setText("");
                multicastGroupAddress = String.format("%s.%s.%s.%s", aAddressTextField.getText(), bAddressTextField.getText(), cAddressTextField.getText(), dAddressTextField.getText());
            } catch (Exception e) {
                addressErrorLabel.setText(e.getMessage());
                settingsValid = false;
            }
        }
        
        if (portTextField.getText().isBlank()) {
            portErrorLabel.setText("Required field!");
            settingsValid = false;
        } else {
            int port = Integer.parseInt(portTextField.getText());
            try {
                netManager.setMulticastGroupPort(port);
                portErrorLabel.setText("");
                multicastGroupPort = port;
            } catch (Exception e) {
                portErrorLabel.setText(e.getMessage());
                settingsValid = false;
            }
        }

        if (!interfaceErrorLabel.getText().isBlank()) {
            interfaceErrorLabel.setText("Required selection!");
            settingsValid = false;
        }
        
        netManager.setHealthcheckPeriod(healthcheckPeriodSlider.valueProperty().getValue().intValue());

        StatusPanelController.getInstance().setMulticastGroupInfo(multicastGroupAddress, multicastGroupPort);

        if (!portTextField.isDisable()) {
            if (settingsValid) {
                CtlPanelController.getInstance().enableToJoin();
            } else {
                CtlPanelController.getInstance().disableToJoin();
            }
        }
    }


    public void enableNetworkControls() {
        interfaceComboBox.setDisable(false);
        aAddressTextField.setDisable(false);
        bAddressTextField.setDisable(false);
        cAddressTextField.setDisable(false);
        dAddressTextField.setDisable(false);
        portTextField.setDisable(false);
    }

    public void disableNetworkControls() {
        interfaceComboBox.setDisable(true);
        aAddressTextField.setDisable(true);
        bAddressTextField.setDisable(true);
        cAddressTextField.setDisable(true);
        dAddressTextField.setDisable(true);
        portTextField.setDisable(true);
    }
}
