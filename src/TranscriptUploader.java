import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.youtube.*;
import com.google.gdata.data.youtube.*;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class TranscriptUploader {

	private String playlistId, username, password;
	private YouTubeService service;
	private ArrayList<String> videoIds, transcripts;
	private File[] txts;

	public static final String CAPTION_FEED_URL_FORMAT = "http://gdata.youtube.com/feeds/api/videos/%s/captions";
	public static final String DEVELOPER_KEY = "AI39si5Sfx7cQ-LXq6ERi31r5K19y1x5w94Cnirtp1Vza30kDELcLFCr6LjoYDfplqlpRia19SBYn8ZUffQMXtwGbjuc2ezq0w";
	public static final String CLIENT_ID = "ol-youtube";
	public static final String CAPTION_FAILURE_TAG = "Caption Upload Failed";
	public static final String PLAYLIST_FEED_URL_FORMAT = "http://gdata.youtube.com/feeds/api/users/%s/playlists";

	public TranscriptUploader(String username, String password)
			throws MalformedURLException, IOException, ServiceException {

		transcripts = new ArrayList<String>();
		videoIds = new ArrayList<String>();

		this.username = username;
		this.password = password;
		service = new YouTubeService(CLIENT_ID, DEVELOPER_KEY);
		service.setUserCredentials(this.username, this.password);

		System.out.println("Authenticated");
	}

	public void parseTranscripts(File folder) throws IOException {
		txts = folder.listFiles();

		for (File txt : txts) {
			StringBuffer buffer = new StringBuffer();

			FileInputStream fis = new FileInputStream(txt);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

			Reader in = new BufferedReader(isr);
			int ch;
			while ((ch = in.read()) > -1) {
				buffer.append((char) ch);
			}
			in.close();
			String txtString = buffer.toString();

			transcripts.add(txtString);
		}

		System.out.println(transcripts.size());

		for (String transcript : transcripts) {
			System.out.println(transcript);
		}
	}

	public List<PlaylistLinkEntry> getPlaylistLinkEntries()
			throws MalformedURLException, IOException, ServiceException {
		String playlistUrl = String.format(PLAYLIST_FEED_URL_FORMAT, "default");

		PlaylistLinkFeed feed = service.getFeed(new URL(playlistUrl),
				PlaylistLinkFeed.class);

		List<PlaylistLinkEntry> playlistLinkEntries = feed.getEntries();

		return playlistLinkEntries;
	}

	public void parsePlaylist(PlaylistLinkEntry chosenPlaylist)
			throws MalformedURLException, IOException, ServiceException {
		String playlistUrl = chosenPlaylist.getFeedUrl();

		PlaylistFeed playlistFeed = service.getFeed(new URL(playlistUrl),
				PlaylistFeed.class);

		for (PlaylistEntry entry : playlistFeed.getEntries()) {

			// TODO

			System.out.println(entry.getId());
			videoIds.add(entry.getId());
		}

	}

	public void uploadAll() throws MalformedURLException, IOException,
			ServiceException {
		if (videoIds.size() == transcripts.size()) {
			for (int i = 0; i < videoIds.size(); i++) {
				uploadCaption(videoIds.get(i), transcripts.get(i));
			}
		}
	}

	private boolean uploadCaption(String videoId, String captionTrack)
			throws MalformedURLException, IOException, ServiceException {

		String captionsUrl = String.format(CAPTION_FEED_URL_FORMAT, videoId);

		GDataRequest request = service
				.createInsertRequest(new URL(captionsUrl));

		request.setHeader("Content-Language", "EN");
		request.setHeader("Content-Type",
				"application/vnd.youtube.timedtext; charset=UTF-8");
		request.getRequestStream().write(captionTrack.getBytes("UTF-8"));
		request.execute();

		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(request.getResponseStream()));
		StringBuilder builder = new StringBuilder();
		String line = null;

		while ((line = bufferedReader.readLine()) != null) {
			builder.append(line + "\n");
		}

		bufferedReader.close();

		String responseBody = builder.toString();
		System.out.println("Response to captions request: " + responseBody);
		if (responseBody.contains(CAPTION_FAILURE_TAG)) {
			return false;
		} else {
			System.out.printf(
					"It seems like %s transcript was uploaded successfully.",
					videoId);
			return true;
		}
	}
}
