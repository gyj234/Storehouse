package exam1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

/**
 * Class <em>HttpClient</em> is a class representing a simple HTTP client.
 *
 * @author 龚英杰
 */

public class HttpClient {

    /**
     * default HTTP port is port 80
     */
    private static int port = 80;

    /**
     * Allow a maximum buffer size of 8192 bytes
     */
    private static int buffer_size = 8192;

    /**
     * Response is stored in a byte array.
     */
    private byte[] buffer;

    /**
     * My socket to the world.
     */
    Socket socket = null;

    /**
     * Default port is 8000.
     */
    private static final int PORT = 8000;

    /**
     * Output stream to the socket.
     */
    BufferedOutputStream ostream = null;

    /**
     * Input stream from the socket.
     */
    BufferedInputStream istream = null;

    /**
     * StringBuffer storing the header
     */
    private StringBuffer header = null;

    /**
     * StringBuffer storing the response.
     */
    private StringBuffer response = null;

    /**
     * String to represent the Carriage Return and Line Feed character sequence.
     */
    static private String CRLF = "\r\n";

    /**
     * HttpClient constructor;
     */
    public HttpClient() {
        buffer = new byte[buffer_size];
        header = new StringBuffer();
        response = new StringBuffer();
    }

    /**
     * <em>connect</em> connects to the input host on the default http port -- port
     * 80. This function opens the socket and creates the input and output streams
     * used for communication.
     */
    public void connect(String host) throws Exception {

        /**
         * Open my socket to the specified host at the default port.
         */
        socket = new Socket(host, PORT);

        /**
         * Create the output stream.
         */
        ostream = new BufferedOutputStream(socket.getOutputStream());

        /**
         * Create the input stream.
         */
        istream = new BufferedInputStream(socket.getInputStream());
    }

    /**
     * <em>processGetRequest</em> process the input GET request.
     */
    public void processGetRequest(String request) throws Exception {
        /**
         * Send the request to the server.
         */
        request += CRLF + CRLF;
        buffer = request.getBytes();
        ostream.write(buffer, 0, request.length());
        ostream.flush();
        /**
         * waiting for the response.
         */
        processResponse();
    }

    /**
     * <em>processPutRequest</em> process the input PUT request.
     */
    public void processPutRequest(String request) throws Exception {
        // =======start your job here============//

        String file_part = request.split(" ")[1];// 以空格做分割，获取文件名
        String filename = file_part.substring(1);
        String rootName = "C:\\Users\\龚英杰\\Desktop\\大三上\\网络与分布式计算\\实验\\";
        String fileDir = rootName + filename;
        System.out.println("File dir: " + fileDir);
        File file = new File(fileDir);
        // 文件不存在
        if (!file.exists()) {
            System.out.println("File Not Found!");
            return;
        }
        request += CRLF;// 请求行结束
        buffer = request.getBytes();// 转换为字节数组
        ostream.write(buffer, 0, request.length());// 往BufferedOutputStream中存放
        ostream.flush();
        FileInputStream fi = new FileInputStream(file);// 构造文件流
        int content_length = fi.available();// 获取大小
        String content_type;// 获取文件类型
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            content_type = "image/jpeg";
        } else if (filename.endsWith(".html") || filename.endsWith(".htm")) {
            content_type = "text/html";
        } else {
            content_type = "other";
        }
        request = "Host:GYJ" + CRLF; //这里不能用中文
        request += "Content-type:" + content_type + CRLF;
        request += "Content-length:" + content_length + CRLF;

        request += CRLF;
        buffer = request.getBytes();// 转换为字节数组
        ostream.write(buffer, 0, request.length());// 往BufferedOutputStream中存放
        ostream.flush();
        /**
         * 下面开始传输文件
         */
        int buffer_length = 0;
        while ((buffer_length = fi.read(buffer)) != -1) {
            ostream.write(buffer, 0, buffer_length);
            ostream.flush();
        }

        /**
         * waiting for the response.
         */
        processResponse();

        // =======end of your job============//
    }

    /**
     * <em>processResponse</em> process the server response.
     *
     */
    public void processResponse() throws Exception {
        int last = 0, c = 0;
        /**
         * Process the header and add it to the header StringBuffer.
         */
        boolean inHeader = true; // loop control

        while (inHeader && ((c = istream.read()) != -1)) {

            switch (c) {
                case '\r':
                    // '\r': return 到当前行的最左边
                    break;
                case '\n':
                    // '\n': newline 向下移动一行，并不移动左右
                    if (c == last) {// ASCII码=0时表示字符为NULL
                        inHeader = false;
                        break;
                    }
                    last = c;// 最后一个字符为last
                    header.append("\n");
                    break;
                default:
                    last = c;
                    header.append((char) c);// 追加c（强转为char）
            }
        }

        /**
         * Read the contents and add it to the response StringBuffer.
         *
         */
        buffer = new byte[8192];// 由于前面用过了buffer，此处重新指向一个新数组
        while (istream.read(buffer) != -1) {
            response.append(new String(buffer, "iso-8859-1"));// 编码方式是iso-8859-1
            // 将buffer（字节数组）转换为字符串
        }

    }

    /**
     * Get the response header.
     */
    public String getHeader() {
        return header.toString();
    }

    /**
     * Get the server's response.
     */
    public String getResponse() {
        return response.toString();
    }

    /**
     * Close all open connections -- sockets and streams.
     */
    public void close() throws Exception {
        socket.close();
        istream.close();
        ostream.close();
    }
}
