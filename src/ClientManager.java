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

    public void stopAllClient() {
        serverThread.stopClient();
    }

    public void stop() {
        serverThread.stopServer();
        serverThread = null;
        instance = null;
    }

    public void start() {
        if (serverThread == null) {
            serverThread = new ServerThread(receiveListener, port);
        }
        new Thread(serverThread).start();
    }

    public void sendStopFlag() {
        System.out.println("------------------------sendStopFlag----------");
        serverThread.sendStopFlag();
    }
}
