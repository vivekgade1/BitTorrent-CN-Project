import java.util.*;


public class UnchokeTask extends TimerTask {
    public int preferredN;
    private Random random;
    private int peerId;
    Queue<Peer> peerMinQueue;
    public UnchokeTask(int pId, int k){
        this.random = new Random();
        this.preferredN = k;
        this.peerId = pId;
        this.peerMinQueue = new PriorityQueue<>(downloadrateComparator);
    }


    @Override
    public void run() {
        List<Integer> peers = peerProcess.interested_peers;
        selectedTopK(peers);

        HashSet<Integer> new_unchoked_list = new HashSet<Integer>();

        for (Peer p : peerMinQueue){
            if (!peerProcess.unchoked_list.contains(p.id)){ // if the is choked
                ConnectionHandler conn = peerProcess.active_connections.get(p.id);
                if (conn!=null)
                    conn.send(MessageType.sendUnchoke()); //if the new peer is the new unchoked peer send unchoke message
            }

            new_unchoked_list.add(p.id); // added to unchoke list
        }

        peerProcess.updateUnchokeList(new_unchoked_list); //reset the current unchoked peer and add new one
        if (peerProcess.presentOptUnchoked != null){
            peerProcess.unchoked_list.add(peerProcess.presentOptUnchoked.id);
        }

        // send choke msg to all choked neighbours
        for (int pId: peerProcess.getInteresteddAndChokedList()){
            ConnectionHandler conn = peerProcess.active_connections.get(pId);
            if (conn!=null)
                conn.send(MessageType.sendChoke()); // peer send choke msg
        }

    }

    public static Comparator<Peer> downloadrateComparator = new Comparator<Peer>(){

        @Override
        public int compare(Peer p1, Peer p2) {
            return (int) (p1.rate - p2.rate);
        }
    };

    public void selectedTopK(List<Integer> peers){
        this.peerMinQueue.clear();
        StringBuffer list_str = new StringBuffer();
        if (peerProcess.my_info.hasFile && peers.size() > this.preferredN){
            while(this.peerMinQueue.size() < this.preferredN){
                int i = this.random.nextInt(peers.size());
                Peer p = peerProcess.peer_info_map.get(peers.get(i));
                p.get_downloadrate();
                this.peerMinQueue.offer(p);
                list_str.append(p.id);
                peers.remove(i);
            }
        } else {
            for (int i=0; i < peers.size();i++){
                Peer p = peerProcess.peer_info_map.get(peers.get(i));
                p.get_downloadrate();
                if (this.peerMinQueue.size() >= this.preferredN){
                    Peer top = this.peerMinQueue.peek();
                    if (top.rate == p.rate){
                        if (this.random.nextBoolean()){
                            this.peerMinQueue.poll();
                            this.peerMinQueue.offer(p);
                            list_str.append(p.id);
                        }
                    } else if (top.rate < p.rate){
                        this.peerMinQueue.poll();
                        this.peerMinQueue.offer(p);
                        list_str.append(p.id);
                    }
                } else {
                    this.peerMinQueue.offer(p);
                    list_str.append(p.id);
                }
            }
        }
        Logging.writeLog("Peer [peer_ID "+peerProcess.my_peer_id+"] has the preferred neighbours "+  list_str.toString() +".");
    }
}
