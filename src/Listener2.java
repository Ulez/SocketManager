/**
 * @author lcy
 */
public interface Listener2 {
    void onError(Exception e);

    void onServerClose();

    void onClientClose(int clientId);
}
