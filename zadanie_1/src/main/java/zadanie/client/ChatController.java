package zadanie.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class ChatController {
    private TcpConnectionHandler tcpConnectionHandler;
    private Thread tcpThread;
    private PrintWriter out;
    private Socket tcpSocket;

    private UdpConnectionHandler udpConnectionHandler;
    private Thread udpThread;
    private DatagramSocket udpSocket;

    private MultiConnectionHandler multiConnectionHandler;
    private Thread multiThread;
    private MulticastSocket multiSocket;
    private SocketAddress group_addr;

    private InetAddress address;
    private InetAddress multicast_addres;
    private int port;
    private String nick;
    enum Type {
        UDP,
        TCP,
        MULTI
    }
    private Type currentProtocol=Type.TCP;

    @FXML
    private ListView messagesView;

    @FXML
    private TextField newMessageField;

    @FXML
    private Button sendMessageButton;

    @FXML
    private Label protocolLabel;

    @FXML
    private Label errorLabel;


    @FXML
    protected void buttonAction(ActionEvent event) throws InterruptedException, IOException {
        if(newMessageField.getText().length()>0){
            switch (newMessageField.getText()){
                case "/stop":
                    handleStop();
                    break;
                case "U":
                    currentProtocol=Type.UDP;
                    protocolLabel.setText("UDP");
                    break;
                case "T":
                    currentProtocol=Type.TCP;
                    protocolLabel.setText("TCP");
                    break;
                case "M":
                    currentProtocol=Type.MULTI;
                    protocolLabel.setText("Multicast UDP");
                    break;
                default:
                    sendMessage(newMessageField.getText());
                    break;
            }
        }
        newMessageField.setText("");
    }
        protected void handleStop(){
            try {
            tcpConnectionHandler.stop();
            currentProtocol=Type.TCP;
            sendMessage("[DISCONNECTED]");

            udpConnectionHandler.stop();
            currentProtocol=Type.UDP;
            udpSocket.close();



            multiConnectionHandler.stop();
            multiSocket.close();
            currentProtocol=Type.MULTI;
//            sendMessage("[DISCONNECTED]");

            udpThread.join();
            tcpThread.join();
            multiThread.join();
            } catch (IOException|InterruptedException e) {
                e.printStackTrace();
            } finally {
                Platform.exit();
            }

        }
    protected void sendMessage(String msg) throws IOException {
        errorLabel.setText("");
        byte[] sendBuffer = null;
        Message message;
        switch (currentProtocol){
            case TCP:
                out.println(nick+"&&"+msg);
                message=new Message(nick,msg);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        messagesView.getItems().add(drawMessage(message));
                        messagesView.scrollTo(messagesView.getItems().size()-1);
                    }
                });
                break;
            case UDP:
                sendBuffer = (nick+"&&"+msg).getBytes();
                if(sendBuffer.length<1024) {
                    message=new Message(nick,msg);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            messagesView.getItems().add(drawMessage(message));
                            messagesView.scrollTo(messagesView.getItems().size()-1);
                        }
                    });
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, port);
                    udpSocket.send(sendPacket);
                }else{
                    errorLabel.setText("Max msg length is 1024 bytes!");
                }
                break;
            case MULTI:
                sendBuffer = (nick+"&&"+msg).getBytes();
                if(sendBuffer.length<1024) {

                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, multicast_addres, port+1);
                    multiSocket.send(sendPacket);
                }else{
                    errorLabel.setText("Max msg length is 1024 bytes!");
                }
                break;
            default:
                break;
        }
    }



    public void receiveData(Socket tcpSocket,String nick,String hostName,int port, Stage stage){
        this.port=port;
        this.nick=nick;
        this.tcpSocket=tcpSocket;

        try {
            address = InetAddress.getByName(hostName);


            udpSocket= new DatagramSocket();

            byte[] sendBuffer = (nick+"&&"+"CONNECT").getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, port);
            udpSocket.send(sendPacket);

            multicast_addres=InetAddress.getByName("239.1.0.0");
            group_addr = new InetSocketAddress("239.1.0.0", port+1);
            NetworkInterface nif= NetworkInterface.getByInetAddress(multicast_addres);
            multiSocket=new MulticastSocket(port+1);
            multiSocket.joinGroup(group_addr,nif);

            out = new PrintWriter(tcpSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            out.println(nick);

            tcpConnectionHandler = new TcpConnectionHandler(in,messagesView,address);

            udpConnectionHandler = new UdpConnectionHandler(messagesView, udpSocket);

            multiConnectionHandler = new MultiConnectionHandler(messagesView, multiSocket);


            tcpThread=new Thread(tcpConnectionHandler);
            tcpThread.start();

            udpThread=new Thread(udpConnectionHandler);
            udpThread.start();

            multiThread=new Thread(multiConnectionHandler);
            multiThread.start();

            stage.setOnCloseRequest(event -> {
                event.consume();
                handleStop();
            });
        } catch (IOException e) {
            e.printStackTrace();
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
}
