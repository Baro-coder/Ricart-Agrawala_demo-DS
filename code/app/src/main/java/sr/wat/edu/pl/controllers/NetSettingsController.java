package sr.wat.edu.pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;


public class NetSettingsController {

    // Multicast Group IPv4 Address
    @FXML
    private TextField aAddressTextField;    // First Octet
    @FXML
    private TextField bAddressTextField;    // Second Octet
    @FXML
    private TextField cAddressTextField;    // Third Octet
    @FXML
    private TextField dAddressTextField;    // Fourth Octet

    // Port Address
    @FXML
    TextField portTextField;

    // Constructor
    public NetSettingsController() {
        System.out.println("NetSettingsController   [" + this + "]  : contructor");
    }

    // FXML Controls init
    @FXML
    private void initialize() {
        // TextFields init
        addNumericFilter(aAddressTextField, 3);
        addNumericFilter(bAddressTextField, 3);
        addNumericFilter(cAddressTextField, 3);
        addNumericFilter(dAddressTextField, 3);
        addNumericFilter(portTextField, 5);

        
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
}
