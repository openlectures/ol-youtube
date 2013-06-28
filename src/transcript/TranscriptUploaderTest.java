package transcript;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class TranscriptUploaderTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			TranscriptUploader test = new TranscriptUploader("openlecturessg",
					"tequilatequila");

			test.parsePlaylist(test.getPlaylistLinkEntries().get(20));

			test.parseTranscripts(new File("./transcripts"));

			test.uploadAll();

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
