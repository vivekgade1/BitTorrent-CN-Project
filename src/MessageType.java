import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;
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
		byte[] mess_length = new byte[MESSAGE_LENGTH_SIZE];
		try
		{
			mess_length = ByteBuffer.allocate(4).putInt(1).array();
			System.arraycopy(mess_length, 0, message, 0, MESSAGE_LENGTH_SIZE);
			message[MESSAGE_LENGTH_SIZE] = (byte) 0;
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
				return false;
			Byte type = new Byte(message[MESSAGE_LENGTH_SIZE]);
			if(type.intValue() != 0)
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
		byte[] mess_length = new byte[MESSAGE_LENGTH_SIZE];
		try
		{
			mess_length = ByteBuffer.allocate(4).putInt(1).array();
			System.arraycopy(mess_length, 0, message, 0, MESSAGE_LENGTH_SIZE);
			message[MESSAGE_LENGTH_SIZE] = (byte) 1;
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
			Byte type = new Byte(message[MESSAGE_LENGTH_SIZE]);
			if(type.intValue() != 1)
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
		byte[] mess_length = new byte[MESSAGE_LENGTH_SIZE];
		try
		{
		 mess_length = ByteBuffer.allocate(4).putInt(1).array();
		 System.arraycopy(mess_length, 0, message, 0, MESSAGE_LENGTH_SIZE);
		 message[MESSAGE_LENGTH_SIZE] = (byte) 2;
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
			Byte type = new Byte(message[MESSAGE_LENGTH_SIZE]);
			if(type.intValue() != 2)
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
		byte[] mess_length = new byte[MESSAGE_LENGTH_SIZE];
		try
		{
			mess_length = ByteBuffer.allocate(4).putInt(1).array();
			System.arraycopy(mess_length, 0, message, 0, MESSAGE_LENGTH_SIZE);
			message[MESSAGE_LENGTH_SIZE] = (byte) 3;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return message;

	}
	public static boolean receiveNotInterested(byte[] message)
    {

//		PAYLOAD_LEN = 0;
//		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
//		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
//        byte[] msgtype = new byte[MESSAGE_TYPE_LEN];
//		try
//        {
//            if(message.length!= Total_len)
//                throw new Exception("Handshake message length incorrect");
//            System.arraycopy(message, MESSAGE_LENGTH_SIZE, msgtype, 0, msgtype.length);
//            String received_message_type = new String(msgtype);
//            if(received_message_type!="3")
//            	return false;
//        }
//        catch (Exception e) {
//
//        }
//        return true;

		PAYLOAD_LEN = 0;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		byte[] msgtype = new byte[MESSAGE_TYPE_LEN];
		try
		{
			if(message.length!= Total_len)
				throw new Exception("Handshake message length incorrect");
			Byte type = new Byte(message[MESSAGE_LENGTH_SIZE]);
			if(type.intValue() != 3)
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
		byte[] mess_length = new byte[MESSAGE_LENGTH_SIZE];
		byte[] mess_type = new byte [MESSAGE_TYPE_LEN];
		byte[] payload = new byte[PAYLOAD_LEN];
		try
		{
		    mess_length = ByteBuffer.allocate(4).putInt(1 + PAYLOAD_LEN).array();
			System.arraycopy(mess_length, 0, message, 0, MESSAGE_LENGTH_SIZE);
			message[MESSAGE_LENGTH_SIZE] = (byte) 4;
			payload = ByteBuffer.allocate(4).putInt(pieceIndex).array();
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

//		PAYLOAD_LEN = 4;
//		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
//		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
//        byte[] msgtype = new byte[MESSAGE_TYPE_LEN];
//		byte[] returnPieceIndex = new byte[PAYLOAD_LEN];
//        int result = 0;
//
//		try
//        {
//            if(message.length!= Total_len)
//                throw new Exception("Handshake message length incorrect");
//            System.arraycopy(message, MESSAGE_LENGTH_SIZE, msgtype, 0, msgtype.length);
//    		System.arraycopy(message, MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN, returnPieceIndex, 0, returnPieceIndex.length);
//
//            String received_message_type = new String(msgtype);
//
//
//
//            if(received_message_type.equals("4"))
//            	return -1;
//
//
//            for (int i=0; i<4; i++) {
//              result = ( result << 8 ) - Byte.MIN_VALUE + (int) returnPieceIndex[i];
//            }
//            return result;
//
//        }
//        catch (Exception e) {
//
//        }
//        return result;

		PAYLOAD_LEN = 4;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;

		int piece = -1;
		try
		{
			if(message.length!= Total_len)
				throw new Exception("piece message length incorrect");
			Byte type = new Byte(message[MESSAGE_LENGTH_SIZE+MESSAGE_TYPE_LEN]);
			piece = type.intValue();
		}
		catch (Exception e)
		{
			piece = -1;
		}

//		for (int i=0; i<4; i++) {
//			piece = ( piece << 8 ) - Byte.MIN_VALUE + (int) message[i];
//		}
		return piece;

    }



	public static byte[] sendRequest(int pieceIndex)
	{
		PAYLOAD_LEN = 4;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		byte[] message = new byte[Total_len];
		byte[] mess_length = new byte[MESSAGE_LENGTH_SIZE];
		byte[] mess_type = new byte [MESSAGE_TYPE_LEN];
		byte[] payload = new byte[PAYLOAD_LEN];
		try
		{
			mess_length = ByteBuffer.allocate(4).putInt(1 + PAYLOAD_LEN).array();
			System.arraycopy(mess_length, 0, message, 0, MESSAGE_LENGTH_SIZE);
			message[MESSAGE_LENGTH_SIZE] = (byte) 6;
			payload = ByteBuffer.allocate(4).putInt(pieceIndex).array();
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
		try
		{
			String f_path_str = peerProcess.my_path + peerProcess.config_info_map.get("FileName") + ".part" + pieceIndex;
			Path f_path = Paths.get(f_path_str);
			/*FileInputStream file_input_strm = new FileInputStream(file_to_send);
			BufferedInputStream file_buff_strm = new BufferedInputStream(file_input_strm);*/
			byte[] file_data = Files.readAllBytes(f_path);

			PAYLOAD_LEN = 4 + file_data.length;
			MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
			Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
			byte[] message = new byte[Total_len];
			byte[] mess_length = new byte[MESSAGE_LENGTH_SIZE];
			byte[] mess_type = new byte [MESSAGE_TYPE_LEN];
			byte[] pieceIndx = new byte[4];


		    mess_length = ByteBuffer.allocate(4).putInt(1 + PAYLOAD_LEN).array();
			System.arraycopy(mess_length, 0, message, 0, MESSAGE_LENGTH_SIZE);
			message[MESSAGE_LENGTH_SIZE] = (byte) 7;
			pieceIndx = ByteBuffer.allocate(4).putInt(pieceIndex).array();
			System.arraycopy(pieceIndx, 0, message, MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN, 4);
			System.arraycopy(file_data, 0, message, MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN+4, PAYLOAD_LEN-4);
			return message;
		}
		catch (Exception e)
		{
			System.out.println("sending piece failed : " + pieceIndex);
			return null;
		}
	}

	public static byte[] receivePiece(byte[] pieceMessage)
	{
		PAYLOAD_LEN = pieceMessage.length - 9;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		byte[] message = new byte[PAYLOAD_LEN];
		System.arraycopy(pieceMessage,MESSAGE_LENGTH_SIZE+ MESSAGE_TYPE_LEN + 4,message,0,PAYLOAD_LEN);
		return message;

	}


	public static int receiveRequest(byte[] pieceMessage)
	{
		PAYLOAD_LEN = 4;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;

		int piece = -1;
		try
		{
             if(pieceMessage.length!= Total_len)
			throw new Exception("piece message length incorrect");
			piece = ByteBuffer.wrap(pieceMessage,MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN,4).getInt();
			/*Byte[] type = new Byte[](pieceMessage[MESSAGE_LENGTH_SIZE+MESSAGE_TYPE_LEN]);
			piece = type.intValue();*/
		}
		catch (Exception e)
		{
			piece = -1;
		}

//		for (int i=0; i<4; i++) {
//			piece = ( piece << 8 ) - Byte.MIN_VALUE + (int) message[i];
//		}
		return piece;

	}

	static byte[] integersToBytes(int[] values)
	{
	   byte[] bytes = new byte[values.length];

	   for(int i=0; i < values.length; ++i)
	   {
			bytes[i] = (byte)(values[i] >>> (i * 8));

	   }

	   return bytes;
	}

	public static byte[] sendBitfeild(int[] my_bitfeild)
	{
		PAYLOAD_LEN = (int) Math.ceil((double)my_bitfeild.length/8);
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		Total_len = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		ByteBuffer msg_len = ByteBuffer.allocate(4);
		ByteBuffer msg_typ = ByteBuffer.allocate(1);
		ByteBuffer payload = ByteBuffer.allocate(PAYLOAD_LEN);

		msg_len.putInt(MESSAGE_LEN);
		msg_typ.put((byte)5);
		BitSet bits = new BitSet(my_bitfeild.length);
		int count = 0;
		for (int i = 0;i <my_bitfeild.length; i++) {
			if(my_bitfeild[i] == 2){
				bits.set(i,true);
			}else{
				bits.set(i,false);
			}
		}
		payload.put(bits.toByteArray());
		ByteBuffer message = ByteBuffer.allocate(Total_len);
		message.put(msg_len.array());
		message.put(msg_typ.array());
		message.put(payload.array());
		return message.array();
	}

	public static int[] convertToIntArray(byte[] input)
	{
	    int[] bit_field = new int[peerProcess.piece_cnt];
	    BitSet bits = BitSet.valueOf(input);

		for (int i = 0; i < peerProcess.piece_cnt; i++) {
			if(bits.get(i)){
				bit_field[i] = 2;
			}else{
				bit_field[i] = 0;
			}
		}
		return bit_field;
	}

	public static int[] receiveBitfeild(byte[] bitfieldmessage)
	{
		PAYLOAD_LEN = bitfieldmessage.length - MESSAGE_LENGTH_SIZE - MESSAGE_TYPE_LEN;
		MESSAGE_LEN = MESSAGE_TYPE_LEN + PAYLOAD_LEN;
		byte[] message = new byte[PAYLOAD_LEN];
		System.arraycopy(bitfieldmessage,MESSAGE_LENGTH_SIZE+MESSAGE_TYPE_LEN,message,0,PAYLOAD_LEN);
		return convertToIntArray(message);
	}
}
