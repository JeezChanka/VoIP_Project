package pl.poznan.put.voip.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.poznan.put.voip.client.utils.Controller;
import pl.poznan.put.voip.core.utils.Logs;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("connectView.fxml"));
        scene = new Scene(fxmlLoader.load(), 500, 720);

        Logs.log("Client init");
        Controller controller = fxmlLoader.getController();
        Client.init(stage, scene, controller);

        stage.setTitle("WindyTalks");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Client.getClient().disconnect());
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}