package ex1;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
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
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * TCPHandler
 * Use to handle User's command
 * Valid Command include 'ls', 'cd', 'get' and 'bye'
 * @author 龚英杰
 */
public class TCPHandler implements Runnable {
    ServerSocket serverSocket;
    private Socket socket;
    private final int PORT = 2021; // TCP Port
    public static DatagramPacket dataPacket;
    public static DatagramSocket dataSocket;
    public static byte[] sendDataByte;
    static final String HOST = "127.0.0.1"; // Server IP

    public TCPHandler(Socket socket) {
        this.socket = socket;
    }

    /**
     * run the system
     */
    public void run() {
        try {
            String filePath = "C:\\Users\\龚英杰\\Desktop\\大三上\\网络与分布式计算\\"; // root path
            System.out.println(socket.getInetAddress() + ":" + socket.getPort() + "> Connected");
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            PrintWriter pw = new PrintWriter(bw, true);
            String info = null;
            String[] strArray = null;
            while ((strArray = br.readLine().split("\\s")) != null) {
                System.out.println(strArray[0]);
                if (strArray[0].equals("ls")) { // list file
                    pw.println(getFiles(filePath));
                } else if (strArray[0].equals("cd")) {
                    if (strArray.length < 2) {
                        pw.println("Please choose a folder");
                        continue;
                    }
                    if (strArray[1].equals("..")) { // back to upper directory
                        if (filePath.equals("C:\\Users\\龚英杰\\Desktop\\大三上\\网络与分布式计算\\")) { // reach root directory
                            pw.println("This is the root");
                            continue;
                        } else {
                            System.out.println("Back to upper directory");
                            filePath = filePath.substring(0, filePath.lastIndexOf("\\"));
                            filePath = filePath.substring(0, filePath.lastIndexOf("\\")) + "\\";
                            pw.println(getFiles(filePath));
                        }
                    } else {
                        boolean flag = false;
                        File root = new File(filePath);
                        File[] files = root.listFiles();
                        for (int i = 0; i < files.length; i++) {
                            if (strArray[1].equals(files[i].getName()) && files[i].isDirectory())
                                flag = true;
                        }
                        if (flag) {
                            System.out.println("Jump to " + strArray[1]);
                            filePath = filePath + strArray[1] + "\\";
                            System.out.println(filePath);
                            pw.println(getFiles(filePath));
                        } else {
                            System.out.println("Directory doesn't exist");
                            pw.println("Directory doesn't exist");
                        }
                    }
                } else if (strArray[0].equals("get")) { // Get File, transfer through UDP
                    if (strArray.length < 2) {
                        pw.println("Please choose a file");
                        continue;
                    }
                    boolean flag = false;
                    File root = new File(filePath);
                    File[] files = root.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        if (strArray[1].equals(files[i].getName()) && !files[i].isDirectory())
                            flag = true;
                    }
                    String filename = strArray[1];
                    if (!flag) {

                        System.out.println("File doesn't exist");
                        pw.println("File doesn't exist");
                        continue;
                    } else {
                        // pw.println("Get file. Start transfer." + filename);
                        // System.out.println("Get file. Start transfer." + filename);
                        pw.println("OK");

                        String sendfilePath = filePath + filename;
                        File file = new File(sendfilePath);

                        pw.println(file.getAbsolutePath());
                        pw.println(file.length());
                        System.out.println("Send File Info to Client");

                    }
                } else if (strArray[0].equals("bye")) { // End & disconnect
                    System.out.println("Disconnected");
                    pw.println("Disconnected");
                    break;
                } else {
                    pw.println("No such command");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != socket) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private static ArrayList<String> filelist = new ArrayList<String>();

    /**
     * Get all files under certain directory
     *
     * @param filePath
     * @return
     */
    static String getFiles(String filePath) {
        String str = "";
        File root = new File(filePath);
        File[] files = root.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                getFiles(file.getPath());
                filelist.add(file.getPath());
                str = str + "<dir> " + file.getName() + "  " + file.length() + "@";
            } else {
                str = str + "<file> " + file.getName() + "  " + file.length() + "@";
            }
        }
        return str;
    }
}
