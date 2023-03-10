package zadanie.client;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class TcpConnectionHandler implements Runnable {

    private final BufferedReader in;
    private final ListView messagesView;
    private boolean running=true;
    private final InetAddress address;

    public TcpConnectionHandler(BufferedReader in, ListView messagesView, InetAddress address) {
        this.in = in;
        this.messagesView = messagesView;
        this.address = address;
    }

    @Override
    public void run() {
        try {
            while (running) {
                String newLine = in.readLine();
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
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("KONIEC TCP");
            }
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
