package org.qulad;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ControllerImage implements Initializable {
    public Canvas bitmap;
    public ImageView leftView;
    public ImageView rightView;
    public Button exitProgram;
    public Button next;
    private Scene scene;
    private String path= ControllerFileImage.path;
    private BMP bmp;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            bmp = new BMP(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        leftView.setFitWidth(bmp.getImageWidth());
        leftView.setFitHeight(bmp.getImageHeight());
        leftView.setImage(bmp.getOriginalImage());

        rightView.setFitWidth(bmp.getImageWidth());
        rightView.setFitHeight(bmp.getImageHeight());
        rightView.setImage(bmp.getRecoveredImage());


    }

    public void exitEvent(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void nextEvent(ActionEvent actionEvent) throws IOException {
        //Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("src/main/resources/org/qulad/ImageView2.fxml"));
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        //scene = new Scene(root, 1600, 1100);
        App.setRoot("ImageView2");
        //stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public void goback(ActionEvent actionEvent) throws IOException {
        App.setRoot("fileChooserImage");
    }
}
