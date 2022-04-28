package it.polimi.ingsw.client;

import it.polimi.ingsw.client.gui.Home;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientMainGUI extends Application {

    static String ip = "";

    public static void main(String[] args) {
        ip = args[0];
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        //load fxml file
        final String fxmlResource = "/home.fxml";
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(fxmlResource));
        Parent root = loader.load();

        //if loaded successfully
        ((Home)loader.getController()).connectToServer(ip);
        if( ((Home)loader.getController()).isConnected() ){
            Scene scene = new Scene(root);

            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.sizeToScene();
            primaryStage.setTitle("Santorini Game");
            primaryStage.setOnCloseRequest(windowEvent -> {
                System.exit(0);
            });
            primaryStage.show();
        }

    }
}
