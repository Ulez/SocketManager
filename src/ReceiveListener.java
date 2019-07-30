/**
 * @author lcy
 */
public interface ReceiveListener {
    /**
     * @param clientId
     * @param msg
     */
    void onReceived(int clientId, String msg);
}
