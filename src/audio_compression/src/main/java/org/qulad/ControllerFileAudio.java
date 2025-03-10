package org.qulad;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.zip.DataFormatException;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

public class ControllerFileAudio implements Initializable {

  public AnchorPane chooseFileBtn;
  public Button exit;
  List<String> lstFile;

  private Scene scene;

  public static String path;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    lstFile = new ArrayList<>();
    lstFile.add("*.wav");
  }

  public void openFileDialog(ActionEvent actionEvent) throws IOException, DataFormatException {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Wav files", lstFile));
    fileChooser.setTitle("Select a wav file");
    File file = fileChooser.showOpenDialog(null);
    if (file != null) {
      path = file.getAbsolutePath();

      App.setRoot("compressionOutput");
      // Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
      // stage.setScene(scene);                s
      //                stage.centerOnScreen();
      //                stage.show();

    }
  }

  public void exitEvent(ActionEvent actionEvent) {
    System.exit(0);
  }
}
