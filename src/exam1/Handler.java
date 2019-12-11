package exam1;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;

/**
 * @author 龚英杰
 *
 */
public class Handler implements Runnable {
    private Socket socket;

    // the constructor
    public Handler(Socket socket) {
        this.socket = socket;
    }

    /**
     * implement the abstract method run
     *
     */
    public void run() {
        try {
            while (true) {
                BufferedReader inFromKeyBoard = new BufferedReader(new InputStreamReader(System.in));
                OutputStream outToClient = socket.getOutputStream();
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                DataInputStream inFromServer = null;
                DataOutputStream outToServer = null;
                String cmd = inFromClient.readLine();
                System.out.println("The Client request for: " + cmd);
                if (cmd == null)
                    continue;

                /**
                 * Process the command from client and identify the method and address.
                 * If command error, write the error response and break.
                 *
                 */
                String[] strs = cmd.split(" ");

                String method = strs[0];

                if (!method.equals("GET")) {
                    continue;
                }

                String url = strs[1];
                if (!url.contains("://")) {
                    url = url + "http://";
                }

                /**
                 * Process the file name and make sure the server name, the file path and the number of port.
                 *
                 */
                int port = 0;
                String serverName = "";
                String filePath = "";

                URL new_url = new URL(url);
                serverName = new_url.getHost();
                port = new_url.getPort();
                if (port == -1) {
                    port = 80;
                }
                url = url.substring(url.indexOf("//") + 2);
                filePath = url.substring(url.indexOf("/"));

                /**
                 * Response the right format request. Identify the file type and write it to the client.
                 *
                 */
                System.out.println("HOST: " + serverName);
                System.out.println("PORT: " + port);
                System.out.println(filePath);
                System.out.println("Now respond the Client \'s request: ");

                Socket clientSocket = new Socket(serverName, port);// the Proxy Server Functions as client

                inFromServer = new DataInputStream(clientSocket.getInputStream());
                outToServer = new DataOutputStream(clientSocket.getOutputStream());

                outToServer.writeBytes("GET " + filePath + " HTTP/1.0\r\n\r\n");

                byte[] bytes = new byte[1024];

                int size = 0;

                /**
                 * Read from Server and Write to Client.
                 *
                 */
                while ((size = inFromServer.read(bytes, 0, 1024)) != -1) {
                    outToClient.write(bytes, 0, size);
                    outToClient.flush();
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // close the socket
        finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
