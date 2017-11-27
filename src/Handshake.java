public class Handshake {
    public static final int ZERO_BITS_LEN = 10;
    public static final int HEADER_LEN = 18;
    public static final int ID_LEN= 4;
    public static final int MESSAGE_LENGTH = 32;
    public static final String HEADER= "P2PFILESHARINGPROJ";
    public static String received_header;
    public static String received_peerID;
    public Handshake()
    {

    }

    public static byte[] sendMessage(int peerId)
    {
        byte[] message = new byte[MESSAGE_LENGTH];
        try
        {
            byte[] header = HEADER.getBytes();
            byte[] zeros = "0000000000".getBytes();
            byte[] pid = (Integer.toString(peerId)).getBytes();
            System.arraycopy(header, 0, message, 0, HEADER_LEN);
            System.arraycopy(zeros, 0, message, HEADER_LEN, ZERO_BITS_LEN);
            System.arraycopy(pid, 0, message, HEADER_LEN + ZERO_BITS_LEN, ID_LEN);
        }
        catch (Exception e)
        {
            message = null;
        }

        return message;

    }

    public static int verifyHandshake(int peer)
    {
        int verified = 0;
        if (received_peerID.equals(Integer.toString(peer)) && received_header.equals(HEADER))
            verified =1;
        return verified;
    }

    public static void receiveMessage(byte[] message)
    {
        byte[] msgHeader = new byte[HEADER_LEN];
        byte[] id = new byte[ID_LEN];
        try
        {
            if(message.length!= MESSAGE_LENGTH)
                throw new Exception("Handshake message length incorrect");
            System.arraycopy(message, 0, msgHeader, 0, HEADER_LEN);
            received_header = new String(msgHeader);
            System.arraycopy(message, HEADER_LEN + ZERO_BITS_LEN, id, 0, ID_LEN);
            received_peerID = new String(id);

        }
        catch (Exception e) {

        }

    }
}