package ex1;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * FileClient
 * Client end of the System
 * User would type in the command and system will send the command to server
 * @author 龚英杰
 */
public class FileClient {
    static final int PORT = 2021; // TCP Port
    static final int port = 2020; // UDP Port
    static final String HOST = "127.0.0.1"; // Server IP
    public static byte[] receiveByte;
    public static DatagramPacket dataPacket;
    public static DatagramSocket dataSocket;
    Socket socket = new Socket();

    /**
     *
     * @throws UnknownHostException
     * @throws IOException
     */
    public FileClient() throws UnknownHostException, IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(HOST, PORT));
    }

    /**
     * Start Client
     * @param args
     * @throws UnknownHostException
     * @throws IOException
     */
    public static void main(String[] args) throws UnknownHostException, IOException {
        new FileClient().client();
    }

    /**
     * handle the command input by user
     */
    public void client() {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(bw, true);
            Scanner sc = new Scanner(System.in);
            String msg = null;
            while (true) {
                msg = sc.nextLine();

                String[] strs = msg.split("\\s");
                // Debug: System.out.println("msg:" + msg);
                if (msg.equals("bye")) {
                    pw.println(msg);
                    String str = br.readLine();
                    if (str.equals("Disconnected")) {
                        System.out.println("Disconnected");
                    }
                    break;
                } else if (strs[0].equals("get")) { // Get File
                    // pw.println(msg + " " + dataSocket.getLocalPort());
                    pw.println(msg);
                    String str = br.readLine();
                    if (str.equals("File doesn't exist")) { // Server response: File doesn't exist
                        System.out.println("File doesn't exist");
                        continue;
                    }
                    if (str.equals("Please choose a file")) { // Doesn't input file
                        System.out.println("Please choose a file");
                        continue;
                    }

                    //Download File

                    String receiveRoot = "C:\\Users\\龚英杰\\Desktop\\大三上\\网络与分布式计算\\EmptyFolder";
                    String filePath = br.readLine();
                    long fileLength = Long.parseLong(br.readLine());
                    System.out.println("File Path: " + filePath + "  File Length: " + fileLength);
                    dataSocket = new DatagramSocket();
                    byte[] info = filePath.getBytes();
                    DatagramPacket send = new DatagramPacket(info, info.length, new InetSocketAddress(HOST, port));
                    String fileName = filePath.substring(filePath.lastIndexOf("\\"), filePath.length());
                    dataSocket.send(send);

                    String receiveAddress = receiveRoot+fileName;
                    System.out.println("Downloading File to: " + receiveAddress);

                    DataOutputStream fileOut = new DataOutputStream( // Transfer File to "C:\Users\龚英杰\Desktop\大三上\网络与分布式计算\EmptyFolder"
                            new BufferedOutputStream(new FileOutputStream(receiveAddress)));
                    receiveByte = new byte[1024];
                    dataPacket = new DatagramPacket(receiveByte, receiveByte.length, new InetSocketAddress(HOST, port));
                    while (true) {
                        System.out.println("File Receiving");

                        dataSocket.receive(dataPacket);
                        if (new String(dataPacket.getData(), 0, dataPacket.getLength()).equals("end.")) {
                            System.out.println("File Transfer Finish.");
                            fileOut.close();
                            break;
                        }
                        int length = dataPacket.getLength();
                        if (length > 0) {
                            fileOut.write(receiveByte, 0, length);
                            fileOut.flush();
                        }

                    }
                } else if (strs[0].equals("cd") || strs[0].equals("ls")) { // cd or ls command
                    pw.println(msg);
                    String str = null;
                    str = br.readLine();
                    // Debug: System.out.println(str);
                    if (str.equals("This is the root")) { // at root directory
                        System.out.println(str);
                        continue;
                    }
                    if (str.equals("Please choose a folder")) { // didn't input a folder
                        System.out.println(str);
                        continue;
                    }
                    if (str.equals("Directory doesn't exist")) {
                        System.out.println(str);
                        continue;
                    }
                    if (str.equals("")) {
                        System.out.println("Empty Folder");
                        continue;
                    }
                    String[] filenames = str.split("@");
                    String[] names = null;
                    System.out.printf("%-8s%-15s%-25s\n", "Type", "Name", "Size");
                    for (int i = 0; i < filenames.length; i++) {
                        names = filenames[i].split("\\s");
                        System.out.printf("%-8s%-15s%-25s\n", names[0], names[1], names[3]);
                    }
                } else {
                    System.out.println("No such Command, Please try ls, cd or get command");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != socket) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void doBye(String msg) {
        String[] str = msg.split("\\s");
    }
}
