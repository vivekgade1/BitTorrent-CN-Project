import java.util.Timer;
import java.util.TimerTask;

public class Scheduler extends TimerTask{
    public int opt_unchoke_int;
    public int UnchokeIntl;
    public int peferredn;
    private Timer opt_timer;
    private Timer unchoke_timer;
    private boolean _set;
    private int peerID;
    public Scheduler(int peerid, int OptUnchokeIntl, int UnchokeIntl, int maxk){
        this.opt_unchoke_int = OptUnchokeIntl;
        this.UnchokeIntl = UnchokeIntl;
        this.peerID = peerid;
        this.peferredn = maxk; //number of preferred neighbours allowed
        opt_timer = new Timer(true);
        unchoke_timer = new Timer(true);
        this._set = false;
    }

    public void run() {
        if (this._set)
            return;
        synchronized(this){
            UnchokeTask unchoke_task = new UnchokeTask(this.peerID,this.peferredn);
            OptimisticallyUnchokePeerSelection opt_unchoke_task = new OptimisticallyUnchokePeerSelection();
            unchoke_timer.scheduleAtFixedRate(unchoke_task, 0, this.UnchokeIntl*1000);
            opt_timer.scheduleAtFixedRate(opt_unchoke_task, 0, this.opt_unchoke_int*1000);
            this._set = true;
        }
    }

    public void reload() {
        // TODO Auto-generated method stub
        opt_timer.cancel();
        unchoke_timer.cancel();
        this._set = false;

    }

    public void init() {
        // TODO Auto-generated method stub

    }


}