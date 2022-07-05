package just.curiosity.p2p_network.server.packet;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/27/22 - 10:05 AM
 */

public enum PacketType {
  CLONE_NODES,
  ADD_NODE,
  SAVE_DATA,
  GET_DATA,
  DELETE_DATA,
  RENAME_DATA
}
