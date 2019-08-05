import java.io.*;
import java.net.Socket;

public class HandleMsgTask implements Runnable {
    private PrintWriter writer;
    private final int clientId;
    private final Socket client;
    private volatile boolean connectStop = false;
    private final ReceiveListener receiveListener;

    public HandleMsgTask(int i, Socket client, ReceiveListener receiveListener) {
        this.clientId = i;
        this.client = client;
        this.receiveListener = receiveListener;
    }

    public void disconnectClient() {
        connectStop = true;
        try {
            client.close();
            if (receiveListener != null) {
                receiveListener.onClientClose(clientId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String line = "";
        try {
            final String address = client.getRemoteSocketAddress().toString();
            System.out.println(clientId + "连接成功，连接的设备为：" + address);
            InputStream inputStream = client.getInputStream();
            writer = new PrintWriter(client.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            reader.ready();
            while (!connectStop && ((line = reader.readLine()) != null)) {
                if (receiveListener != null) {
                    receiveListener.onReceived(clientId, line);
                }
            }
        } catch (IOException e) {
            if (!connectStop && receiveListener != null && !line.contains("-end-")) {
                receiveListener.onError(e);
            }
            e.printStackTrace();
        }
    }

    public void sendStopFlag() {
        //打断任务；
        writer.println("interrupt\n");
        System.out.println("writer.println interrupt");
    }
}
