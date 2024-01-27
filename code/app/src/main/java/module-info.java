module sr.wat.edu.pl {
    requires javafx.fxml;
    requires javafx.graphics;
    requires transitive javafx.controls;
    
    opens sr.wat.edu.pl to javafx.fxml;
    exports sr.wat.edu.pl;
}
