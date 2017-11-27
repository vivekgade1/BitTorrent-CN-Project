import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;

public class MessageType {
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
		System.arraycopy(mess_length, 0, message, 0, MESSAGE_LENGTH_SIZE);
		System.arraycopy(mess_type, 0, message, MESSAGE_LENGTH_SIZE, MESSAGE_TYPE_LEN);
		//System.arraycopy(pid, 0, message, HEADER_LEN + ZERO_BITS_LEN, ID_LEN);
		}
		catch (Exception e)
		{
			message = null;
		}

		return message;

	}

	public static boolean receiveChoke(byte[] message)
    {

		PAYLOAD_LEN = 0;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
        byte[] msgtype = new byte[MESSAGE_TYPE_LEN];
		try
        {
            if(message.length!= Total_len)
                throw new Exception("Handshake message length incorrect");
            System.arraycopy(message, 0, msgtype, MESSAGE_LENGTH_SIZE, MESSAGE_TYPE_LEN);
            String received_message_type = new String(msgtype);
            if(received_message_type!="0")
            	return false;
        }
        catch (Exception e) {

        }
        return true;

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
		System.arraycopy(mess_length, 0, message, 0, MESSAGE_LENGTH_SIZE);
		System.arraycopy(mess_type, 0, message, MESSAGE_LENGTH_SIZE, MESSAGE_TYPE_LEN);
		//System.arraycopy(pid, 0, message, HEADER_LEN + ZERO_BITS_LEN, ID_LEN);
		}
		catch (Exception e)
		{
			message = null;
		}

		return message;

	}

	public static boolean receiveUnchoke(byte[] message)
    {

		PAYLOAD_LEN = 0;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
        byte[] msgtype = new byte[MESSAGE_TYPE_LEN];
		try
        {
            if(message.length!= Total_len)
                throw new Exception("Handshake message length incorrect");
            System.arraycopy(message, 0, msgtype, MESSAGE_LENGTH_SIZE, MESSAGE_TYPE_LEN);
            String received_message_type = new String(msgtype);
            if(received_message_type!="1")
            	return false;
        }
        catch (Exception e) {

        }
        return true;

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
		System.arraycopy(mess_length, 0, message, 0, MESSAGE_LENGTH_SIZE);
		System.arraycopy(mess_type, 0, message, MESSAGE_LENGTH_SIZE, MESSAGE_TYPE_LEN);
		//System.arraycopy(pid, 0, message, HEADER_LEN + ZERO_BITS_LEN, ID_LEN);
		}
		catch (Exception e)
		{
			message = null;
		}

		return message;

	}

	public static boolean receiveInterested(byte[] message)
    {

		PAYLOAD_LEN = 0;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
        byte[] msgtype = new byte[MESSAGE_TYPE_LEN];
		try
        {
            if(message.length!= Total_len)
                throw new Exception("Handshake message length incorrect");
            System.arraycopy(message, 0, msgtype, MESSAGE_LENGTH_SIZE, MESSAGE_TYPE_LEN);
            String received_message_type = new String(msgtype);
            if(received_message_type!="2")
            	return false;
        }
        catch (Exception e) {

        }
        return true;

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
		System.arraycopy(mess_length, 0, message, 0, MESSAGE_LENGTH_SIZE);
		System.arraycopy(mess_type, 0, message, MESSAGE_LENGTH_SIZE, MESSAGE_TYPE_LEN);
		//System.arraycopy(pid, 0, message, HEADER_LEN + ZERO_BITS_LEN, ID_LEN);
		}
		catch (Exception e)
		{
			message = null;
		}

		return message;

	}
	public static boolean receiveNotInterested(byte[] message)
    {

		PAYLOAD_LEN = 0;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
        byte[] msgtype = new byte[MESSAGE_TYPE_LEN];
		try
        {
            if(message.length!= Total_len)
                throw new Exception("Handshake message length incorrect");
            System.arraycopy(message, 0, msgtype, MESSAGE_LENGTH_SIZE, MESSAGE_TYPE_LEN);
            String received_message_type = new String(msgtype);
            if(received_message_type!="3")
            	return false;
        }
        catch (Exception e) {

        }
        return true;

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
		System.arraycopy(mess_length, 0, message, 0, MESSAGE_LENGTH_SIZE);
		System.arraycopy(mess_type, 0, message, MESSAGE_LENGTH_SIZE, MESSAGE_TYPE_LEN);
		System.arraycopy(payload, 0, message, MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN, PAYLOAD_LEN);
		}
		catch (Exception e)
		{
			message = null;
		}

		return message;

	}

	public static int receiveHave(byte[] message)
    {

		PAYLOAD_LEN = 4;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
        byte[] msgtype = new byte[MESSAGE_TYPE_LEN];
		byte[] returnPieceIndex = new byte[PAYLOAD_LEN];
        int result = 0;

		try
        {
            if(message.length!= Total_len)
                throw new Exception("Handshake message length incorrect");
            System.arraycopy(message, 0, msgtype, MESSAGE_LENGTH_SIZE, MESSAGE_TYPE_LEN);
    		System.arraycopy(message, 0, returnPieceIndex, MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN, PAYLOAD_LEN);

            String received_message_type = new String(msgtype);



            if(received_message_type.equals("4"))
            	return -1;


            for (int i=0; i<4; i++) {
              result = ( result << 8 ) - Byte.MIN_VALUE + (int) returnPieceIndex[i];
            }
            return result;

        }
        catch (Exception e) {

        }
        return result;

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
		System.arraycopy(mess_length, 0, message, 0, MESSAGE_LENGTH_SIZE);
		System.arraycopy(mess_type, 0, message, MESSAGE_LENGTH_SIZE, MESSAGE_TYPE_LEN);
		System.arraycopy(payload, 0, message, MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN, PAYLOAD_LEN);
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
		System.arraycopy(mess_length, 0, message, 0, MESSAGE_LENGTH_SIZE);
		System.arraycopy(mess_type, 0, message, MESSAGE_LENGTH_SIZE, MESSAGE_TYPE_LEN);
		System.arraycopy(payload, 0, message, MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN, PAYLOAD_LEN);
		}
		catch (Exception e)
		{
			message = null;
		}

		return message;

	}

	static byte[] integersToBytes(int[] values)
	{
	   //ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] bytes = new byte[values.length];
	   //DataOutputStream dos = new DataOutputStream(baos);
	   for(int i=0; i < values.length; ++i)
	   {
			bytes[i] = (byte)(values[i] >>> (i * 8));

	   }

	   return bytes;
	}

	public static byte[] sendBitfeild(int[] my_bitfeild)
	{
		PAYLOAD_LEN = my_bitfeild.length;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		//byte[] message =
		byte[] mess_length = Integer.toString(MESSAGE_LEN).getBytes();
		byte[] mess_type = "5".getBytes();
		byte[] payload = integersToBytes(my_bitfeild);
		ByteBuffer message = ByteBuffer.wrap(new byte[Total_len]);
		message.put(mess_length);
		message.put(mess_type);
		message.put(payload);
		/*System.arraycopy(mess_length, 0, message, 0, MESSAGE_LENGTH_SIZE);
		System.arraycopy(mess_type, 0, message, MESSAGE_LENGTH_SIZE, MESSAGE_TYPE_LEN);
		System.arraycopy(payload, 0, message, MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN, PAYLOAD_LEN);
		}
		catch (Exception e)
		{
			message = null;
		}*/

		return message.array();
	}

	public static int[] convertToIntArray(byte[] input)
	{
	    int[] ret = new int[input.length];
	    for (int i = 0; i < input.length; i++)
	    {
	        ret[i] = input[i] & 0xff; // Range 0 to 255, not -128 to 127
	    }
	    return ret;
	}

	public static int[] receiveBitfeild(byte[] bitfieldmessage)
	{
		PAYLOAD_LEN = bitfieldmessage.length - MESSAGE_LENGTH_SIZE - MESSAGE_TYPE_LEN;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		byte[] message = new byte[PAYLOAD_LEN];
		try
		{
		System.arraycopy(bitfieldmessage, MESSAGE_LENGTH_SIZE+MESSAGE_TYPE_LEN, message, 0, PAYLOAD_LEN);
		}
		catch (Exception e)
		{
			message = null;
		}

		return convertToIntArray(message);

	}
}
