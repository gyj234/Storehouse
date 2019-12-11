package ex1;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * FileServer
 * @author 龚英杰
 */
public class FileServer {
    ServerSocket serverSocket;
    DatagramSocket datagramSocket;
    final int port = 2020; 	//UDP Port
    private final int PORT = 2021; // TCP Port
    public static DatagramPacket dataPacket;
    public static DatagramSocket dataSocket;
    public static byte[] sendDataByte;
    static final String HOST = "127.0.0.1";  //Server IP
    static final int POOL_SIZE = 10;  //Size of thread pool
    ExecutorService executorService;  //Thread pool

    /**
     * Start a new server with TCPHandler and UDPHandler
     * @throws IOException
     */
    public FileServer() throws IOException {
        serverSocket = new ServerSocket(PORT, 2);
        datagramSocket = new DatagramSocket(port);
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);
        System.out.println("Server Started");
    }

    public static void main(String[] args) throws Exception {
        new FileServer().service();
    }

    /**
     * service
     * @throws InterruptedException
     */
    public void service() throws Exception {
        Socket socket = null;
        new Thread(new UDPHandler(datagramSocket)).start();
        while (true) {
            try {
                socket = serverSocket.accept();
                executorService.execute(new TCPHandler(socket));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
