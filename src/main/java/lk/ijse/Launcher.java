package lk.ijse;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        showWindow(primaryStage, "/view/Server.fxml", "Server");

        Stage clientStage = new Stage();
        showWindow(clientStage, "/view/Client.fxml", "Client");

        primaryStage.setX(100);
        clientStage.setX(primaryStage.getX()+primaryStage.getWidth()+100);

    }

    private void showWindow(Stage stage, String fxmlPath, String title) throws Exception {
        Parent rootNode = FXMLLoader.load(getClass().getResource(fxmlPath));
        stage.setScene(new Scene(rootNode));
        stage.setTitle(title);
        stage.centerOnScreen();
        stage.show();

        stage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                stage.setMaximized(false);
            }
        });
    }
}