import java.util.List;
import java.util.Random;
import java.util.TimerTask;

public class OptimisticallyUnchokePeerSelection extends TimerTask {
    private Random random;
    public OptimisticallyUnchokePeerSelection(){
        this.random = new Random();
    }
    @Override
    public void run() {
        List<Integer> chokelist = peerProcess.getInteresteddAndChokedList();
        if (chokelist.size()> 0){
            int random_peer = random.nextInt(chokelist.size());
            int opt_peer = chokelist.get(random_peer);
            Peer present_op_peer = peerProcess.presentOptUnchoked;
            if (present_op_peer == null || present_op_peer.id != opt_peer){
                if (peerProcess.presentOptUnchoked != null){
                    peerProcess.unchoked_list.remove(present_op_peer.id);
                    peerProcess.peer_info_map.get(peerProcess.presentOptUnchoked.id).opt_unchoked = false;
                }
                ConnectionHandler conn = peerProcess.active_connections.get(opt_peer);
                if (conn!=null){
                    conn.send(MessageType.sendUnchoke()); //if the new peer is the new unchoked peer send unchoke message
                    Logging.writeLog("Peer [peer_ID "+peerProcess.my_peer_id+"] has the optimistically unchoked neighbour ["+opt_peer+"].");

                    peerProcess.unchoked_list.add(opt_peer);
                }
            }
        }
    }

}

