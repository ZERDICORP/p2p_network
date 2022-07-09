package just.curiosity.p2p_network.constants;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 7/6/22 - 3:49 PM
 */

public interface LogMsg {
  String CANT_CONNECT_TO_PEER = "can't connect to peer ?.. ?";
  String HANDLER_HAS_NO_ANNOTATION = "handler ? has no annotation ?.. ignore";
  String SERVER_STARTED = "server has been started on port ?..";
  String SECRET_IS_TOO_SHORT = "secret is too short.. min length = 6";
  String ERROR_SENDING_PACKET_TO_LOCAL_NODE = "error sending packet to local node.. ?";
  String FAILED_TO_SEND_PACKET = "failed to send packet.. ?";
  String FILE_NOT_FOUND = "file not found..";
  String SOCKET_HANDLING_ERROR = "socket handling error.. ?";
  String WRONG_SECRET = "wrong secret..";
  String FILE_DELETED_SUCCESSFULLY = "file deleted successfully!";
  String FILE_RENAMED_SUCCESSFULLY = "file renamed successfully!";
  String FILE_SAVED_SUCCESSFULLY = "file saved successfully!";
  String CANT_START_SERVER = "can't start server.. ?";
  String WRONG_USAGE = "wrong usage.. check out usage guide!";
  String FILE_CONTENTS_ARE_WRITTEN_TO = "file contents are written to: ?";
}
