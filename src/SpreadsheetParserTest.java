import java.io.IOException;
import java.net.URISyntaxException;

import com.google.gdata.util.ServiceException;

public class SpreadsheetParserTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SpreadsheetParser parser;

		try {
			parser = new SpreadsheetParser(
					"openlectures.sg@gmail.com",
					"tequilatequila",
					"https://docs.google.com/spreadsheet/ccc?key=0AgaMT0yBV-pKdFVjUnF1S0JLM1Z5SVl2dUhXQzg3SHc#gid=6");

			// TagProcessor tag = new TagProcessor("keyword_processed.txt");
			// tag.setJson(parser.getJson("description"));
			// tag.scanCheckpoints();
			// System.out.println(tag.getJson());

			PlaylistEditor playlistEditor = new PlaylistEditor(
					"openlecturessg", "tequilatequila");
			//
			playlistEditor.setJson(parser.getJson("playlist"));
			// playlistEditor.deletePlaylists();
			playlistEditor.scanPlaylists();

			// MetadataEditor edit = new MetadataEditor("openlecturessg",
			// "tequilatequila");
			// edit.setJson(parser.getJson("description"));
			// edit.updateVideos();
			//
			// MetadataEditor edit2 = new MetadataEditor("openlecturessg",
			// "tequilatequila");
			// edit2.setJson(parser.getJson("title"));
			// edit2.updateVideos();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
