import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Peer {
    public int id;
    public int port;
    public String ip;
    public BitSet bitfield;
    public boolean hasFile;
    public boolean hadFile;
    public int total_pieces;
    public int rate;
    public boolean is_choked;
    public boolean opt_unchoked;
    public boolean remote_choke;
    public Random selection;
    private AtomicInteger downloadrate;

    public Peer(int id,String ip,int port, int file){
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.total_pieces = peerProcess.piece_cnt;
        this.bitfield = new BitSet(peerProcess.piece_cnt);
        if(file == 1){
            this.hasFile = true;
            this.hadFile = true;

        }else{
            this.hasFile = false;
            this.hadFile = false;
        }
        this.bitfield = new BitSet(peerProcess.piece_cnt);
        for (int i = 0; i < peerProcess.piece_cnt ; i++) {
            this.bitfield.set(i,this.hasFile);
        }
        this.is_choked = true;
        this.opt_unchoked = false;
        this.remote_choke = true;
        this.downloadrate = new AtomicInteger(0);
    }

    public void setPiece(int index){
        this.bitfield.set(index);
        if(bitfield.cardinality() == this.total_pieces){
            this.hasFile = true;
        }else{
            this.hasFile = false;
        }
        peerProcess.IsSomethingLeftToDownload();
    }

    public int getPieceToDownload(BitSet peerA){
        ArrayList<Integer> indx = new ArrayList<>();
        for (int i = 0; i < total_pieces ; i++) {
            if(peerA.get(i)&& !this.bitfield.get(i)){
                indx.add(i);
            }
        }
        Collections.shuffle(indx);
        if(indx.size() == 0){
            return -1;
        }else{
            return indx.get(0);
        }
    }

    public void updateBitField(BitSet received_bitfield){

        this.bitfield = received_bitfield;
    }

    public int get_downloadrate() {
        this.rate =  downloadrate.getAndSet(0);
        return this.rate;
    }

    public int set_downloadrate(int bytelenght) {
        this.rate = this.downloadrate.addAndGet(bytelenght);
        return this.rate;
    }

}
