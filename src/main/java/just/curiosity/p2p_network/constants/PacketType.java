package just.curiosity.p2p_network.constants;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/27/22 - 10:05 AM
 */

public enum PacketType {
  OK,
  CLONE_NODES,
  ADD_NODE,
  SAVE_SHARD,
  SAVE_FILE,
  GET_SHARD,
  GET_FILE,
  DELETE_SHARD,
  DELETE_FILE,
  RENAME_SHARD,
  RENAME_FILE,
  FILE_NOT_FOUND,
  WRONG_SECRET
}
