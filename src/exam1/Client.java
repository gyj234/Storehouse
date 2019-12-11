package exam1;

import java.io.*;

/**
 * Class <em>Client</em> is a class representing a simple HTTP client.
 *
 * @author 龚英杰
 */

public class Client {

    /**
     * default HTTP port is port 80
     */
    private static int port = 80;

    /**
     * Allow a maximum buffer size of 8192 bytes
     */
    private static int buffer_size = 8192;

    /**
     * The end of line character sequence.
     */
    private static String CRLF = "\r\n";

    /**
     * Input is taken from the keyboard
     */
    static BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Output is written to the screen (standard out)
     */
    static PrintWriter screen = new PrintWriter(System.out, true);

    public static void main(String[] args) throws Exception {
        try {
            /**
             * Create a new HttpClient object.
             */
            HttpClient myClient = new HttpClient();

            /**
             * Parse the input arguments.
             */
            String serverAddr = "127.0.0.1";
            if (args.length == 1) {
                serverAddr = args[0];
            } else if (args.length > 1) {
                System.err.println("Only one or no parameter is allowed.");
                System.exit(0);
            }

            /**
             * Connect to the input server
             */
            myClient.connect(serverAddr);

            /**
             * Read the get request from the terminal.
             */
            screen.println(serverAddr + " is listening to your request:");
            String request = keyboard.readLine();// 输入请求行

            if (request.startsWith("GET")) {
                /**
                 * Ask the client to process the GET request.
                 */
                myClient.processGetRequest(request);

            } else {
                /**
                 * Do not process other request.
                 */
                screen.println("Bad request! \n");
                myClient.close();
                return;
            }

            /**
             * Get the headers and display them.
             */
            String header = myClient.getHeader();

            screen.println("Header: \n");
            screen.print(myClient.getHeader() + "\n");// 打印Header信息
            screen.flush();

            if (header.contains("404 Not Found")) {
                myClient.close();
                System.exit(0);
            }
            if (request.startsWith("GET")) {
                /**
                 * Ask the user to input a name to save the GET resultant web page.
                 */
                screen.println();
                screen.print("Enter the name of the file to save: ");
                screen.flush();
                String filename = keyboard.readLine();// 输入保存路径
                File file = new File(filename);
                File fileDir = new File(filename.substring(0, filename.lastIndexOf("\\")));
                if (!file.exists()) {
                    fileDir.mkdirs();
                    file.createNewFile();
                }
                FileOutputStream outfile = new FileOutputStream(filename);

                /**
                 * Save the response to the specified file.
                 */
                String response = myClient.getResponse();// 获取响应的body信息
                outfile.write(response.getBytes("utf-8"));// 往文件中写
                System.out.println("File Saved to Assigned Path");
                outfile.flush();
                outfile.close();// 关闭文件流
            }

            /**
             * Close the connection client.
             */
            myClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
