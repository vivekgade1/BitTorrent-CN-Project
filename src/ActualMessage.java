import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ActualMessage {
	public static  int Total_len;
	public static  int MESSAGE_LENGTH_SIZE = 4;
	public static int MESSAGE_TYPE_LEN = 1;
	public static int PAYLOAD_LEN;
	public static int MESSAGE_LEN;


	public static byte[] sendChoke()
	{
		PAYLOAD_LEN = 0;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		byte[] message = new byte[Total_len];
		try
		{
		byte[] mess_length = Integer.toString(MESSAGE_LEN).getBytes();
		byte[] mess_type = "0".getBytes();
		//byte[] pid = (Integer.toString(peerId)).getBytes();
		System.arraycopy(mess_length, 0, message, 0, MESSAGE_LEN);
		System.arraycopy(mess_type, 0, message, MESSAGE_LEN, MESSAGE_TYPE_LEN);
		//System.arraycopy(pid, 0, message, HEADER_LEN + ZERO_BITS_LEN, ID_LEN);
		}
		catch (Exception e)
		{
			message = null;
		}

		return message;

	}

	public static byte[] sendUnchoke()
	{
		PAYLOAD_LEN = 0;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		byte[] message = new byte[Total_len];
		try
		{
		byte[] mess_length = Integer.toString(MESSAGE_LEN).getBytes();
		byte[] mess_type = "1".getBytes();
		//byte[] pid = (Integer.toString(peerId)).getBytes();
		System.arraycopy(mess_length, 0, message, 0, MESSAGE_LEN);
		System.arraycopy(mess_type, 0, message, MESSAGE_LEN, MESSAGE_TYPE_LEN);
		//System.arraycopy(pid, 0, message, HEADER_LEN + ZERO_BITS_LEN, ID_LEN);
		}
		catch (Exception e)
		{
			message = null;
		}

		return message;

	}

	public static byte[] sendInterested()
	{
		PAYLOAD_LEN = 0;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		byte[] message = new byte[Total_len];
		try
		{
		byte[] mess_length = Integer.toString(MESSAGE_LEN).getBytes();
		byte[] mess_type = "2".getBytes();
		//byte[] pid = (Integer.toString(peerId)).getBytes();
		System.arraycopy(mess_length, 0, message, 0, MESSAGE_LEN);
		System.arraycopy(mess_type, 0, message, MESSAGE_LEN, MESSAGE_TYPE_LEN);
		//System.arraycopy(pid, 0, message, HEADER_LEN + ZERO_BITS_LEN, ID_LEN);
		}
		catch (Exception e)
		{
			message = null;
		}

		return message;

	}

	public static byte[] sendNotInterested()
	{
		PAYLOAD_LEN = 0;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		byte[] message = new byte[Total_len];
		try
		{
		byte[] mess_length = Integer.toString(MESSAGE_LEN).getBytes();
		byte[] mess_type = "3".getBytes();
		//byte[] pid = (Integer.toString(peerId)).getBytes();
		System.arraycopy(mess_length, 0, message, 0, MESSAGE_LEN);
		System.arraycopy(mess_type, 0, message, MESSAGE_LEN, MESSAGE_TYPE_LEN);
		//System.arraycopy(pid, 0, message, HEADER_LEN + ZERO_BITS_LEN, ID_LEN);
		}
		catch (Exception e)
		{
			message = null;
		}

		return message;

	}

	public static byte[] sendHave(int pieceIndex)
	{
		PAYLOAD_LEN = 4;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		byte[] message = new byte[Total_len];
		try
		{
		byte[] mess_length = Integer.toString(MESSAGE_LEN).getBytes();
		byte[] mess_type = "4".getBytes();
		byte[] payload = (Integer.toString(pieceIndex)).getBytes();
		System.arraycopy(mess_length, 0, message, 0, MESSAGE_LEN);
		System.arraycopy(mess_type, 0, message, MESSAGE_LEN, MESSAGE_TYPE_LEN);
		System.arraycopy(payload, 0, message, MESSAGE_LEN + MESSAGE_TYPE_LEN, PAYLOAD_LEN);
		}
		catch (Exception e)
		{
			message = null;
		}

		return message;

	}

	public static byte[] sendRequest(int pieceIndex)
	{
		PAYLOAD_LEN = 4;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		byte[] message = new byte[Total_len];
		try
		{
		byte[] mess_length = Integer.toString(MESSAGE_LEN).getBytes();
		byte[] mess_type = "6".getBytes();
		byte[] payload = (Integer.toString(pieceIndex)).getBytes();
		System.arraycopy(mess_length, 0, message, 0, MESSAGE_LEN);
		System.arraycopy(mess_type, 0, message, MESSAGE_LEN, MESSAGE_TYPE_LEN);
		System.arraycopy(payload, 0, message, MESSAGE_LEN + MESSAGE_TYPE_LEN, PAYLOAD_LEN);
		}
		catch (Exception e)
		{
			message = null;
		}

		return message;

	}

	public static byte[] sendPiece(int pieceIndex)
	{
		PAYLOAD_LEN = 4;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		byte[] message = new byte[Total_len];
		try
		{
		byte[] mess_length = Integer.toString(MESSAGE_LEN).getBytes();
		byte[] mess_type = "7".getBytes();
		byte[] payload = (Integer.toString(pieceIndex)).getBytes();
		System.arraycopy(mess_length, 0, message, 0, MESSAGE_LEN);
		System.arraycopy(mess_type, 0, message, MESSAGE_LEN, MESSAGE_TYPE_LEN);
		System.arraycopy(payload, 0, message, MESSAGE_LEN + MESSAGE_TYPE_LEN, PAYLOAD_LEN);
		}
		catch (Exception e)
		{
			message = null;
		}

		return message;

	}

	static byte[] integersToBytes(int[] values)
	{
	   ByteArrayOutputStream baos = new ByteArrayOutputStream();
	   DataOutputStream dos = new DataOutputStream(baos);
	   for(int i=0; i < values.length; ++i)
	   {
	        try {
				dos.writeInt(values[i]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   }

	   return baos.toByteArray();
	}

	public static byte[] sendBitfeild(int[] my_bitfeild)
	{
		PAYLOAD_LEN = 4;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		byte[] message = new byte[Total_len];
		try
		{
		byte[] mess_length = Integer.toString(MESSAGE_LEN).getBytes();
		byte[] mess_type = "5".getBytes();
		byte[] payload = integersToBytes(my_bitfeild);
		System.arraycopy(mess_length, 0, message, 0, MESSAGE_LEN);
		System.arraycopy(mess_type, 0, message, MESSAGE_LEN, MESSAGE_TYPE_LEN);
		System.arraycopy(payload, 0, message, MESSAGE_LEN + MESSAGE_TYPE_LEN, PAYLOAD_LEN);
		}
		catch (Exception e)
		{
			message = null;
		}

		return message;

	}
}
