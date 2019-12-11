package ex2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * 处理线程
 *
 * @author 龚英杰
 *
 */

public class Handler implements Runnable { // 负责与单个客户通信的线程
    private Socket TCPsocket;// TCP套接字
    BufferedReader br;
    BufferedWriter bw;
    BufferedInputStream istream;
    BufferedOutputStream ostream;
    int port = 80;// TCP服务器端口
    public String path;
    static private String CRLF = "\r\n";
    public String response_header = null;

    /**
     * 构造函数
     *
     * @param socket Socket
     * @param path   服务器根目录
     * @throws IOException
     */

    public Handler(Socket socket, String path) throws IOException {
        this.TCPsocket = socket;
        this.path = path;
    }

    /**
     * 初始化字符流
     *
     * @throws IOException
     */

    public void initStream() throws IOException { // 初始化输入输出流对象方法
        br = new BufferedReader(new InputStreamReader(TCPsocket.getInputStream()));
        bw = new BufferedWriter(new OutputStreamWriter(TCPsocket.getOutputStream()));
        istream = new BufferedInputStream(TCPsocket.getInputStream());
        ostream = new BufferedOutputStream(TCPsocket.getOutputStream());
    }

    /**
     * 实现服务器的功能
     */

    public void run() { // 执行的内容
        try {

            System.out.println("New Connection Established: " + TCPsocket.getInetAddress() + ":" + TCPsocket.getPort()); // 客户端信息
            initStream(); // 初始化输入输出流对象
            String info = null;
            info = br.readLine();

            //while (null != (info = br.readLine())) {
            // 短连接，不用while循环

            if (info.startsWith("GET")) {
                handlerGET(info);
            } else if (info.startsWith("PUT")) {
                handlerPUT(info);
            }

            //}
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != TCPsocket) {
                try {
                    TCPsocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Response to GET request
     *
     * @param request 请求行
     * @throws IOException
     */

    public void handlerGET(String request) throws IOException {
        System.out.println(request);// 打印请求行
        String filename = path + request.split(" ")[1];
        // System.out.println("GET " + filename);
        File file = new File(filename);
        if (!file.exists()) {
            /**
             * File Not Found, Return 404 Not Found
             */
            System.out.println("File Not Found on server");

            String header = "";
            header = "HTTP/1.0 404 Not Found" + CRLF;
            header += "Server:MyHttpServer/1.0" + CRLF + CRLF;

            // Debug: System.out.println(header);
            ostream.write(header.getBytes(), 0, header.length());
            ostream.flush();
            String page = "<html>" + "<head>" + "<title>Error</title>" + "</head>" + "<body><h1>404 Not Found</h1>"
                    + "</body>" + "</html>";
            ostream.write(page.getBytes(), 0, page.length());
            ostream.flush();
            return;
        }
        /**
         * File Found, Return Files
         */

        FileInputStream fi = new FileInputStream(file);
        String content_type = null;
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            content_type = "image/jpeg";
        } else if (filename.endsWith(".html") || filename.endsWith(".htm")) {
            content_type = "text/html";
        } else {
            content_type = "other";
        }
        int content_length = fi.available(), word_number = 0, buffer_length = 0 ;

        /**
         * 构造Header
         */

        byte[] buffer = new byte[8192];
        response_header = "HTTP/1.0 200 OK" + CRLF;
        response_header += "Server:MyHttpServer/1.0" + CRLF;
        response_header += "Content-length:" + content_length + CRLF;
        response_header += "Content-type:" + content_type + CRLF;
        response_header += CRLF;
        buffer = response_header.getBytes();
        ostream.write(buffer, 0, response_header.length());
        ostream.flush();

        /**
         * Start Transferring File
         */

        buffer = new byte[8192];
        while ((buffer_length = fi.read(buffer)) != -1) {
            ostream.write(buffer, 0, buffer_length);
            ostream.flush();
        }
        System.out.println("File Transfer Success");
        fi.close();

    }

    /**
     * Response to PUT Response
     *
     * @param request 请求行
     * @throws IOException
     */

    public void handlerPUT(String request) throws IOException {
        System.out.println(request);
        String filename = request.split(" ")[1];
        String absoluteName = path + "\\" + filename;
        File file = new File(absoluteName);
        String response_header = "";
        if (!file.exists()) {
            /**
             * 服务器端不存在文件-创建
             */
            response_header = "HTTP/1.0 201 Created" + CRLF;
        } else {
            /**
             * 服务器端存在文件-更新
             */
            response_header = "HTTP/1.0 200 OK" + CRLF;
        }
        FileOutputStream fo = new FileOutputStream(path + "\\" + filename);
        System.out.println("Header:");
        char[] chars = new char[100];
        // 无法读取字符-->已解决：使用char型数组解析字节流，使之转换成字符串
        String header_line = null;
        int line_length = 0, content_length = 0, i = 0, last = 0, c = 0;
        String content_type = "";
        boolean inHeader = true; // 判断是否在Header中
        while (inHeader && ((c = istream.read()) != -1)) {
            switch (c) {
                case '\r':
                    /**
                     * 解析字节流
                     */
                    line_length = i;// 字符个数
                    header_line = new String(chars, 0, line_length);
                    if (header_line.split(":")[0].equals("Content-length")) {
                        content_length = Integer.parseInt(header_line.split(":")[1]);
                    }
                    if (header_line.split(":")[0].equals("Content-type")) {
                        content_type = header_line.split(":")[1];
                    }
                    i = 0;
                    line_length = 0;
                    break;
                case '\n':
                    if (c == last) {// ASCII码=0时表示字符为NULL
                        inHeader = false;
                        break;
                    }
                    last = c;// 最后一个字符为last
                    System.out.print((char) c);
                    break;
                default:
                    chars[i] = (char) c;
                    i++;
                    last = c;
                    System.out.print((char) c);// 追加c（强转为char）
            }
        }

        /**
         * 接收文件
         */
        int word_number = 0;
        while (word_number < content_length && (c = istream.read()) != -1) {
            fo.write(c);
            fo.flush();
            word_number++;
        }
        System.out.println("File Accepted");
        fo.close();

        /**
         * 构造Header
         */
        response_header += "Server:MyHttpServer/1.0" + CRLF;
        response_header += "Content-length:" + content_length + CRLF;
        response_header += "Content-type:" + content_type + CRLF;
        // System.out.println(header);
        ostream.write(response_header.getBytes(), 0, response_header.length());
        ostream.flush();

    }
}
