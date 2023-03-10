package zadanie.server;

import zadanie.client.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class TcpConnectionHandler implements Runnable {

    private final BufferedReader in;
    private final PrintWriter out;
    private final int id;
    private final String nick;
    private  boolean running=true;
    private LinkedBlockingQueue<PrintWriter> printers;


    public TcpConnectionHandler(BufferedReader in, PrintWriter out, LinkedBlockingQueue<PrintWriter>  printers, int id, String nick) {
        this.in = in;
        this.id = id;
        this.out = out;
        this.nick = nick;
        this.printers=printers;
    }

    @Override
    public void run() {
        try {
            while (running) {
                String msg = in.readLine();
                System.out.println("TCP msg: " +msg );
                String[] data=msg.split("&&",2);

                if(data[1].equals("[DISCONNECTED]")) {
                    running = false;
                    printers.forEach(printer -> {
                            printer.println(msg);
                    });
                    printers.remove(out);
                    in.close();
                }else {
                    printers.forEach(printer -> {
                            if (!printer.equals(out))
                                printer.println(msg);
                        });
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
