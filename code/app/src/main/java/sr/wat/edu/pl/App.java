package sr.wat.edu.pl;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import sr.wat.edu.pl.core.sys.RaSystem;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        stage.setResizable(false);
        
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("/fxml/primary.fxml"));
        
        StackPane stackPane = loader.load();

        scene = new Scene(stackPane);

        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        RaSystem.getInstance().getUdpMulticastServer().stop();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch();
    }

}
