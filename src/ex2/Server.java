package ex2;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * tcp echo test, server side with thread pool
 *
 * @author 龚英杰
 *
 */

public class Server {
    ServerSocket serverSocket;// TCP套接字
    private final int TCPport = 80; // TCP服务器端口
    ExecutorService executorService; // 线程池
    final int POOL_SIZE = 5; // 单个处理器线程池工作线程数目
    public static String path;

    /**
     *
     * @throws IOException
     */
    public Server() throws IOException {
        serverSocket = new ServerSocket(TCPport); // 创建TCP服务器端套接字
        // 创建线程池
        // Runtime的availableProcessors()方法返回当前系统可用处理器的数目
        // 由JVM根据系统的情况来决定线程的数量
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);
        /**
         * 创建一个线程用于启动UDP服务器，等待客户端发来接收文件的命令
         */
        System.out.println("Server Started");

    }

    /**
     * 主函数，程序入口，执行TCP服务
     *
     * @param args 服务器根目录
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        /**
         * 判断参数是否正确
         */
        if (args.length == 0) {
            path = "C:\\Users\\龚英杰\\Desktop\\大三上\\网络与分布式计算\\实验\\Exercise 2\\新建文件夹\\test";
            System.out.println("路径已经设为默认路径，正在启动服务器…");
            new Server().servic(); // 启动服务
        } else {
            File file = new File(args[0]);

            if (file.isDirectory()) {
                System.out.println("路径已设为参数所给路径，正在启动服务器...");
                path = args[0];
                new Server().servic(); // 启动服务
            } else {
                System.out.println("指定参数不是目录，请重新设置参数");
            }
        }

    }

    /**
     * 服务的实现
     */
    public void servic() {
        Socket socket = null;
        while (true) {
            try {
                socket = serverSocket.accept(); // 等待用户连接
                executorService.execute(new Handler(socket, path)); // 把执行交给线程池来维护
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
