package sr.wat.edu.pl;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        stage.setResizable(false);
        
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(this.getClass().getResource("/fxml/primary.fxml"));
        
        StackPane stackPane = fxmlLoader.load();
        scene = new Scene(stackPane);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}