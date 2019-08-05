import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        ReceiveListener listener = new ReceiveListener() {
            @Override
            public void onReceived(int clientId, String msg) {
                System.out.println(clientId + ":" + msg);
            }

            @Override
            public void onError(Exception e) {

            }

            @Override
            public void onServerClose() {

            }

            @Override
            public void onClientClose(int clientId) {

            }
        };
        ClientManager clientManager = ClientManager.getInstance(listener, 9898);
        clientManager.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(15);
                    clientManager.sendStopFlag();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
