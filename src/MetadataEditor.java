import java.awt.List;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.SwingWorker;

import com.google.common.reflect.TypeToken;
import com.google.gdata.client.youtube.*;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ResourceNotFoundException;
import com.google.gdata.util.ServiceException;
import com.google.gson.Gson;

/**
 * @author linanqiu
 * @file_name MetadataEditor.java
 * 
 *            Construct using username and password for YouTube account (not
 *            gmail account. Unfortunately, those are not the same to the Google
 *            API.)
 * 
 *            Takes Json using the format {"attribute":"some_attribute",
 *            "key":"some_youtube_key",
 *            "value":"some_value_for_that_attribute"}. These are generated
 *            using the SpreadsheetParser.
 * 
 *            Then it sets the attribute (eg. description) of the youtube video
 *            bearing the key to the desired value using the updateVideos
 *            method.
 * 
 */
public class MetadataEditor {

	private String username;
	private String password;
	ArrayList<GsonCheckpoint> gsonList;
	private YouTubeService service;
	public static final String DEVELOPER_KEY = "AI39si5Sfx7cQ-LXq6ERi31r5K19y1x5w94Cnirtp1Vza30kDELcLFCr6LjoYDfplqlpRia19SBYn8ZUffQMXtwGbjuc2ezq0w";
	public static final String CLIENT_ID = "ol-youtube";
	private ArrayList<GsonCheckpoint> errorCheckpoints;

	/**
	 * takes YouTube username and password.
	 * 
	 * @param username
	 * @param password
	 * @throws AuthenticationException
	 */
	public MetadataEditor(String username, String password)
			throws AuthenticationException {
		this.username = username;
		this.password = password;
		errorCheckpoints = new ArrayList<GsonCheckpoint>();
		service = new YouTubeService(CLIENT_ID, DEVELOPER_KEY);
		service.setUserCredentials(this.username, this.password);
	}

	/**
	 * Runs through the list of deserialized GsonCheckpoints and updates the
	 * values one by one. Checkpoints that have errors are added to the
	 * errorCheckpoint ArrayList.
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void updateVideos() throws MalformedURLException, IOException,
			ServiceException {
		for (GsonCheckpoint gsonCheckpoint : gsonList) {
			if (gsonCheckpoint.getKey().equals("dummyvar10")) {
				System.out.println("Dummy Var Encountered");
			} else {
				updateMetadata(gsonCheckpoint);
			}
		}
		for (GsonCheckpoint errorCheckpoint : errorCheckpoints) {
			System.out.println("Error occured for " + errorCheckpoint.getKey());
		}
	}

	/**
	 * Updates the metadata of the single gsonCheckpoint. This method is used by
	 * updateVideo in an iteration.
	 * 
	 * @param gsonCheckpoint
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void updateMetadata(GsonCheckpoint gsonCheckpoint)
			throws MalformedURLException, IOException, ServiceException {

		try {
			String videoEntryURL = "http://gdata.youtube.com/feeds/api/users/username/uploads/VIDEO_ID";
			videoEntryURL = videoEntryURL.replaceAll("username", username);
			videoEntryURL = videoEntryURL.replaceAll("VIDEO_ID",
					gsonCheckpoint.getKey());

			YouTubeService workerService = new YouTubeService(CLIENT_ID,
					DEVELOPER_KEY);
			workerService.setUserCredentials(this.username, this.password);

			VideoEntry video = service.getEntry(new URL(videoEntryURL),
					VideoEntry.class);

			// filters for short keywords that fucks up the process. YouTube
			// does not accept keywords shorter than 1 character, but we have
			// some videos that, unfortunately, have short keywords. These will
			// be deleted.
			Collection<String> currentKeywords = video.getMediaGroup()
					.getKeywords().getKeywords();
			Collection<String> validKeywords = new ArrayList<String>();
			for (String currentKeyword : currentKeywords) {
				if (currentKeyword.length() > 1) {
					validKeywords.add(currentKeyword);
				} else {
					System.out.println("Cleaning keywords for "
							+ video.getTitle().getPlainText());
				}
			}

			video.getMediaGroup().getKeywords().clearKeywords();
			video.getMediaGroup().getKeywords().addKeywords(validKeywords);

			if (gsonCheckpoint.getAttribute().equals("title")) {

				// updates title
				video.getMediaGroup().getTitle()
						.setPlainTextContent(gsonCheckpoint.getValue());

				video.update();
				System.out.println(gsonCheckpoint.getKey() + " "
						+ gsonCheckpoint.getAttribute() + " updated");

			} else if (gsonCheckpoint.getAttribute().equals("description")) {

				// updates description
				video.getMediaGroup()
						.getDescription()
						.setPlainTextContent(
								String.format(gsonCheckpoint.getValue(), "%n"));
				video.update();
				System.out.println(gsonCheckpoint.getKey() + " "
						+ gsonCheckpoint.getAttribute() + " updated");

			} else if (gsonCheckpoint.getAttribute().equals("tag")) {

				// updates tag
				ArrayList<String> keywords = new ArrayList<String>(
						Arrays.asList(gsonCheckpoint.getValue().split(",")));
				video.getMediaGroup().getKeywords().clearKeywords();
				video.getMediaGroup().getKeywords().addKeywords(keywords);

				video.update();
				System.out.println(gsonCheckpoint.getKey() + " "
						+ gsonCheckpoint.getAttribute() + " updated");

			}
		} catch (ResourceNotFoundException e) {

			// for some reason, the key is invalid. Catches the exception and
			// adds an error checkpoint.
			errorCheckpoints.add(gsonCheckpoint);
		}
	}

	/**
	 * Takes Json input
	 * 
	 * @param json
	 */
	public void setJson(String json) {
		Gson gson = new Gson();
		Type collectionType = new TypeToken<Collection<GsonCheckpoint>>() {
		}.getType();
		gsonList = gson.fromJson(json, collectionType);
	}

	/**
	 * @author linanqiu
	 * @file_name MetadataEditor.java
	 * 
	 *            Subclass GsonCheckpoint that represents a deserialized single
	 *            checkpoint.
	 */
	private class GsonCheckpoint {

		private String attribute;
		private String value;
		private String key;

		public String toString() {
			return attribute + value + key;
		}

		public String getAttribute() {
			return attribute;
		}

		public String getValue() {
			return value;
		}

		public String getKey() {
			return key;
		}
	}

}