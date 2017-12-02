import java.io.*;
import java.util.*;

public class startRemotePeers {
    private class PeerInfo{
        public String peerId;
        public String peerAddress;
        public String peerPort;

        public PeerInfo(String pId, String pAddress, String pPort) {
            peerId = pId;
            peerAddress = pAddress;
            peerPort = pPort;
        }
    }
    static Vector peerInfoVector;
    static String workingDir;

public void getConfiguration()
    {
        String st;
        peerInfoVector = new Vector<PeerInfo>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(workingDir  + "\\PeerInfo.cfg"));
            while((st = in.readLine()) != null) {
                String[] tokens = st.split("\\s+");
                peerInfoVector.addElement(new PeerInfo(tokens[0], tokens[1], tokens[2]));
            }

            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            startRemotePeers myStart = new startRemotePeers();
            workingDir = System.getProperty("user.dir");
            myStart.getConfiguration();

            // get current path

            // start clients at remote hosts
            for (int i = 0; i < myStart.peerInfoVector.size(); i++) {
                PeerInfo pInfo = (PeerInfo) myStart.peerInfoVector.elementAt(i);
                String peerProcessName = "java peerProcess";
                String peerProcessArguments = pInfo.peerId;


                Runtime.getRuntime().exec("ssh " + pInfo.peerAddress + " cd " + workingDir + ";" +peerProcessName +" "+ peerProcessArguments);
            }
            System.out.println("Remote peers started.");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
