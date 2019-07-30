import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        ReceiveListener listener = new ReceiveListener() {
            @Override
            public void onReceived(int clientId, String msg) {
                System.out.println(clientId + ":" + msg);
            }
        };
        ClientManager clientManager = ClientManager.getInstance(listener, 9898);
        clientManager.start();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    TimeUnit.SECONDS.sleep(5);
//                    clientManager.stop();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }
}
