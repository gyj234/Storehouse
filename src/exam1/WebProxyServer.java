package exam1;

import java.io.*;
import java.net.*;

/**
 * Start a Proxy Server.
 * @author 龚英杰
 *
 */
public class WebProxyServer {
    private final int port = 8000;//TCP port
    private ServerSocket welcomeSocket;

    public WebProxyServer() throws IOException {
        welcomeSocket = new ServerSocket(port);//Create a server-side socket.
    }

    /**
     * implement service
     */
    public void service() {
        System.out.println("Proxy Server Started:");
        while (true) {
            Socket socket = null;
            try {
                socket = welcomeSocket.accept();//Waiting for user connection.
                System.out.println("New Connection: " + socket.getPort());
                // create a thread for each request socket
                Thread workThread = new Thread(new Handler(socket));
                workThread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] argv) throws IOException {

        try {
            WebProxyServer wps = new WebProxyServer();
            wps.service();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
