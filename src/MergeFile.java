import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MergeFile{

	public void Merge(int n) {
		int peerID = 0;
		String MergedFILE_NAME = "C:\\Users\\hamsi\\Desktop\\CNProject\\src\\";
		String SourceFILE_NAME = "C://Users//hamsi//Desktop//CNProject//src//TextFile.txt";

		boolean f = new File(MergedFILE_NAME+"peer_"+peerID).mkdir();

		File ofile = new File(MergedFILE_NAME+"\\peer_"+peerID+"\\NewFile.txt");
		FileOutputStream fos;
		FileInputStream fis;

		byte[] fileBytes;
		int bytesRead = 0;
		List<File> list = new ArrayList<File>();

		for(int i=0;i<n;i++)
		list.add(new File(SourceFILE_NAME+".part"+i));

		try {
		    fos = new FileOutputStream(ofile,false);
		    for (File file : list) {
		        fis = new FileInputStream(file);
		        fileBytes = new byte[(int) file.length()];
		        bytesRead = fis.read(fileBytes, 0,(int)  file.length());
		        fos.write(fileBytes);
		        fos.flush();
		        fileBytes = null;
		        fis.close();
		        fis = null;
		    }
		    fos.close();
		    fos = null;
		}catch (Exception exception){
			exception.printStackTrace();
		}
	}
}

