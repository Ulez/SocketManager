import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientManager {
    private static ServerThread serverThread = null;
    private static ClientManager instance = null;
    private final int port;
    private ReceiveListener receiveListener = null;

    private ClientManager(ReceiveListener receiveListener, int port) {
        this.receiveListener = receiveListener;
        this.port = port;
        serverThread = new ServerThread(receiveListener, port);
    }

    public static ClientManager getInstance(ReceiveListener receiveListener, int port) {
        if (instance == null) {
            synchronized (ClientManager.class) {
                if (instance == null) {
                    instance = new ClientManager(receiveListener, port);
                }
            }
        }
        return instance;
    }

    public void stop() {
        serverThread.Stop();
        serverThread = null;
    }

    public void start() {
        if (serverThread == null) {
            serverThread = new ServerThread(receiveListener, port);
        }
        new Thread(serverThread).start();
    }

    public static class ServerThread implements Runnable {
        private ReceiveListener receiveListener;
        private static Map<Integer, HandleMsgTask> tasks = new HashMap<>();
        private final AtomicBoolean isExit = new AtomicBoolean(false);
        private ServerSocket server;
        int i = 0;

        public ServerThread(ReceiveListener receiveListener, int port) {
            try {
                this.receiveListener = receiveListener;
                this.server = new ServerSocket(port);
            } catch (IOException e) {
                System.out.println("启动server失败，错误原因：" + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                while (!isExit.get()) {
                    // 进入等待环节
                    System.out.println("Wait NO:"+i+" device" );
                    Socket client = server.accept();
                    HandleMsgTask task = new HandleMsgTask(i, client, receiveListener);
                    new Thread(task).start();
                    tasks.put(i, task);
                    i++;
                    System.out.println("第" + i);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public static class HandleMsgTask implements Runnable {
            public final int clientId;
            public final Socket client;
            public static boolean connectStop = false;
            private final ReceiveListener ReceiveListener;

            public HandleMsgTask(int i, Socket client, ReceiveListener ReceiveListener) {
                this.clientId = i;
                this.client = client;
                this.ReceiveListener = ReceiveListener;
            }

            public void disconnectClient() {
                connectStop = true;
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void run() {
                try {
                    final String address = client.getRemoteSocketAddress().toString();
                    System.out.println(clientId + "连接成功，连接的设备为：" + address);
                    InputStream inputStream = client.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    reader.ready();
                    String line;
                    while (((line = reader.readLine()) != null) && !connectStop) {
                        if (ReceiveListener != null) {
                            ReceiveListener.onReceived(clientId, line);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void Stop() {
            // 关闭所有server socket 和 清空Map
            if (tasks != null) {
                for (HandleMsgTask task : tasks.values()) {
                    task.disconnectClient();
                }
                tasks.clear();
            }
            isExit.set(true);
            if (server != null) {
                try {
                    server.close();
                    System.out.println("已关闭server");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
