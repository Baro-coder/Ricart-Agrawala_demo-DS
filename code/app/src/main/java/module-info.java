module sr.wat.edu.pl {
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires transitive javafx.controls;
    
    opens sr.wat.edu.pl to javafx.fxml;
    opens sr.wat.edu.pl.controllers to javafx.fxml;  // Open the controllers package
    exports sr.wat.edu.pl;
}
