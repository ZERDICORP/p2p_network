package just.curiosity.p2p_network.constants;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/29/22 - 12:58 PM
 */

public interface Const {
  int PORT = 8080;
  int SHARD_SIZE = 5;
  String shardsDirectory = "./data/shards";
  String signaturesDirectory = "./data/signatures";
  String sharedDirectory = "./data/shared";
}
