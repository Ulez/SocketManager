import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerThread implements Runnable {
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
            if (receiveListener != null) {
                receiveListener.onError(e);
            }
            System.out.println("启动server失败，错误原因：" + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (!isExit.get()) {
                // 进入等待环节
                System.out.println("Wait NO:" + i + " device");
                System.out.println("server.accept()---------------------------------");
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

    public void stopServer() {
        // 关闭所有server socket 和 清空Map
        stopClient();
        isExit.set(true);
        if (server != null) {
            try {
                server.close();
                System.out.println("server.close()");
                if (receiveListener != null) {
                    receiveListener.onServerClose();
                }
                System.out.println("已关闭server");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopClient() {
        // 关闭所有server socket 和 清空Map
        if (tasks != null) {
            for (HandleMsgTask task : tasks.values()) {
                task.disconnectClient();
            }
            tasks.clear();
        }
    }

    public void sendStopFlag() {
        for (HandleMsgTask task : tasks.values()) {
            task.sendStopFlag();
        }
    }
}