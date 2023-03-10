package zadanie.client;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MultiConnectionHandler implements Runnable {

    private final ListView messagesView;
    private boolean running=true;
    private final MulticastSocket socket;


    public MultiConnectionHandler( ListView messagesView, MulticastSocket socket) {
        this.messagesView = messagesView;
        this.socket = socket;

    }

    @Override
    public void run() {
        byte[] receiveBuffer = new byte[1024];

        try {
            while (running) {
                Arrays.fill(receiveBuffer, (byte)0);
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                try{
                socket.receive(receivePacket);
                }catch (SocketException e){
                    break;
                }
                String newLine = new String(receivePacket.getData(), StandardCharsets.UTF_8);
                String[] data=newLine.split("&&",2);
                Message msg=new Message(data[0],data[1]);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        messagesView.getItems().add(drawMessage(msg));
                        messagesView.scrollTo(messagesView.getItems().size()-1);
                    }
                });

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
            System.out.println("KONIEC MULTI");
        }
    }

    protected VBox drawMessage(Message msg){
        Label nick=new Label(msg.getNick()+": ");
        nick.setFont(Font.font("Verdana", FontPosture.ITALIC, 13));
        Label text=new Label(msg.getText());
        text.setFont(Font.font("Verdana", 15));
        VBox box = new VBox(nick,text);
        box.setStyle("-fx-spacing: 10");
        return box;
    }
    public void stop(){
        running=false;
    }
}