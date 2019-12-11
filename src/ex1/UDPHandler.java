package ex1;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;
import javax.sound.midi.Receiver;

/**
 * UDPHandler
 * Use to transfer file to Client
 * Receive request from Client and start a UDP connection
 * @author 龚英杰
 */
public class UDPHandler implements Runnable {
    private final int PORT = 2020; // UDP Port
    public static DatagramPacket dataPacket;
    public static DatagramSocket dataSocket;
    public static byte[] sendDataByte;
    DatagramSocket datagramSocket;
    private final int LENGTH = 1024;

    /**
     * create a UDPHandler when server start
     * @param datagramSocket
     */
    public UDPHandler(DatagramSocket datagramSocket) {
        super();
        this.datagramSocket = datagramSocket;
    }

    /**
     * autorun the function
     */
    public void run() {
        while (true) {
            try {
                DatagramPacket receive = new DatagramPacket(new byte[LENGTH], LENGTH);
                datagramSocket.receive(receive);
                String msg = new String(receive.getData(), 0, receive.getLength());
                System.out.println("udp: " + msg + " Sender's Port:" + receive.getPort());
                File sendFile = new File(msg);
                if (sendFile.isFile()) {
                    byte[] sendBuff = new byte[LENGTH];
                    int sz = 0;
                    DatagramPacket send = new DatagramPacket(sendBuff, sendBuff.length, receive.getAddress(),receive.getPort());
                    BufferedInputStream bin = new BufferedInputStream(new FileInputStream(sendFile));
                    while ((sz = bin.read(sendBuff)) > 0) {
                        System.out.println("Sending" + sendBuff.length);
                        send.setData(sendBuff);
                        datagramSocket.send(send);
                        Thread.sleep(500);
                        TimeUnit.MICROSECONDS.sleep(500);
                    }
                    bin.close();
                    send.setData("end.".getBytes(), 0, "end.".length());
                    datagramSocket.send(send);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

}