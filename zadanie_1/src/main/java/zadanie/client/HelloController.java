package zadanie.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import zadanie.Client;

import java.net.Socket;

public class HelloController {
    @FXML
    private TextField nickTextField;
    @FXML
    private TextField hostNameTextField;
    @FXML
    private Label errorLabel;
    @FXML
    private TextField portTextField;
    protected boolean checkStart(){
        return nickTextField.getText().length() != 0 && hostNameTextField.getText().length() != 0 && portTextField.getText().length() != 0;
    }

    @FXML
    protected void onConnect(ActionEvent event) {
        if(checkStart()){
            try {
                Socket tcpSocket = new Socket(hostNameTextField.getText(), Integer.parseInt(portTextField.getText()));
                FXMLLoader loader = new FXMLLoader(Client.class.getResource("chat-view.fxml"));
                Parent root = loader.load();

                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setTitle(nickTextField.getText()+" chat");
                stage.setScene(scene);
                ChatController boardController = loader.getController();
                boardController.receiveData(tcpSocket,nickTextField.getText(),hostNameTextField.getText(), Integer.parseInt(portTextField.getText()),stage);

            } catch (Exception e) {
                e.printStackTrace();
                errorLabel.setText(e.getMessage());
            }
            System.out.println(nickTextField.getText()+" "+hostNameTextField.getText()+" "+portTextField.getText());
        }
    }
}