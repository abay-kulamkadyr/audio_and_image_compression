package org.qulad;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ControllerFileImage implements Initializable {
    public Button bmpDialog;
    private Scene scene;
    List<String> lstFile;
    public static String path;
    public void bmpDialogEvent(ActionEvent actionEvent) throws IOException {
        FileChooser fileChooser= new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("BMP files",lstFile));
        fileChooser.setTitle("Select a bmp file");
        File file = fileChooser.showOpenDialog(null);
        if (file!=null)
        {
            path=file.getAbsolutePath();
            //Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("src/main/resources/org/qulad/originalFileDisplay.fxml"));
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            //scene = new Scene(root, 1600, 1100);
            //stage.setScene(scene);
            App.setRoot("ImageView");
            stage.centerOnScreen();
            stage.show();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lstFile= new ArrayList<>();
        lstFile.add("*.bmp");
    }

    public void exitEvent(ActionEvent actionEvent) {
        System.exit(1);
    }
}
