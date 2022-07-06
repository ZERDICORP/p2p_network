package just.curiosity.p2p_network.constants;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 7/6/22 - 3:49 PM
 */

public interface LogMsg {
  String CANT_SEND_PACKET = "can't send package to ?.. ?";
  String CANT_CONNECT_TO_PEER = "can't connect to peer ?.. ?";
  String HANDLER_HAS_NO_ANNOTATION = "handler ? has no annotation ?.. ignore";
  String SERVER_STARTED = "server has been started on port ?..";
  String SECRET_IS_TOO_SHORT = "secret is too short.. min length = 6";
  String CANT_SEND_PACKET_TO_LOCAL_NODE = "can't send packet to local node.. ?";
  String FAILED_TO_SEND_PACKET = "failed to send packet.. ?";
  String FILE_NOT_FOUND = "file not found..";
}
