package zadanie.server;

import zadanie.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
    public static void main(String[] args) throws IOException {


        int portNumber = 12345;

        int id_count = 0;
        LinkedBlockingQueue<PrintWriter> printers=new LinkedBlockingQueue<>();
        LinkedBlockingQueue<UdpClient> udpClients=new LinkedBlockingQueue<>();

            // create tcp
            Thread tcp=new Thread(new Runnable() {
                ServerSocket serverSocket = null;
                @Override
                public void run() {
                    try {
                    System.out.println("JAVA TCP SERVER");
                    serverSocket = new ServerSocket(portNumber);

                    while (true) {

                        // accept client
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("client connected");

                        // in & out streams
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        printers.add(out);

                        // read msg, send response
                        String msg = in.readLine();
                        System.out.println("received msg: " + msg);

                        TcpConnectionHandler newTcpClient = new TcpConnectionHandler(in,out,printers, id_count, msg);
                        Thread tcpClient = new Thread(newTcpClient);
                        tcpClient.start();
//                out.println("Pong Java Tcp");

                    }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (serverSocket != null) {
                            try {
                                serverSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        tcp.start();

        Thread udp=new Thread(new Runnable() {
            DatagramSocket serverSocket = null;
            @Override
            public void run() {

                byte[] receiveBuffer = new byte[1024];
                try {
                    serverSocket = new DatagramSocket(portNumber);
                    System.out.print("Java UDP SERVER");
                    while (true) {
                        Arrays.fill(receiveBuffer, (byte)0);
                        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        serverSocket.receive(receivePacket);
                        String newLine = new String(receivePacket.getData(), StandardCharsets.UTF_8);
                        UdpClient incomingClient=new UdpClient(receivePacket.getAddress(),receivePacket.getPort());
                        String[] data=newLine.split("&&",2);

                        if(data[1].equals("[DISCONNECTED]")) {
                            for (UdpClient client : udpClients) {
                                if (client.equals(incomingClient)) {
                                    udpClients.remove(client);
                                    break;
                                }
                            }
                        }else {
                            if (!udpClients.contains(incomingClient)) {
                                System.out.print("New UDP client");
                                udpClients.add(incomingClient);
                            } else {
                                for (UdpClient client : udpClients) {
                                    if (!client.equals(incomingClient)) {
                                        DatagramPacket sendPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length, client.getAddress(), client.getPort());
                                        serverSocket.send(sendPacket);
                                    }
                                }
                            }
                        }


                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (serverSocket != null) {
                        serverSocket.close();
                    }
                }
            }
        });
        udp.start();


    }

}
