package org.qulad;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;


public class App extends Application {

    private static Scene scene;
    @Override
    public void start(Stage primaryStage) throws Exception{

        scene= new Scene(loadFXML("fileChooserAudio"),1600,1100);
        primaryStage.centerOnScreen();
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();


    }
    public static void setRoot(String fxml) throws IOException{
        scene.setRoot(loadFXML(fxml));

    }
    private static Parent loadFXML(String fxml) throws IOException{
        FXMLLoader fxmlLoader= new FXMLLoader(App.class.getResource(fxml+".fxml"));
        return fxmlLoader.load();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
