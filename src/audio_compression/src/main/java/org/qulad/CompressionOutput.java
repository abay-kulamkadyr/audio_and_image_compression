package org.qulad;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.zip.DataFormatException;

public class CompressionOutput implements Initializable {
    public TextField showCompression;
    public Button exit;
    String path = ControllerFileAudio.path;
    WAVE wave;
    public void initialize(URL url, ResourceBundle resourceBundle) {
            wave= new WAVE(path);
        try {
            wave.read();
        } catch (IOException | DataFormatException e) {
            e.printStackTrace();
        }
        showCompression.setText("Compression ratio: "+ wave.getCompressionRatio());
            showCompression.setEditable(false);
            showCompression.setFont(Font.font("Helvetica", FontWeight.BOLD,13));;
    }

    public void exitEvent(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void goBack(ActionEvent actionEvent) throws IOException {
        App.setRoot("fileChooserAudio");
    }
}