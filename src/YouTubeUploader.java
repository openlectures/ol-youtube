import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.google.common.reflect.TypeToken;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.media.MediaFileSource;
import com.google.gdata.data.media.mediarss.MediaCategory;
import com.google.gdata.data.media.mediarss.MediaDescription;
import com.google.gdata.data.media.mediarss.MediaTitle;
import com.google.gdata.data.youtube.PlaylistLinkFeed;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.data.youtube.YouTubeNamespace;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class YouTubeUploader {

	private String username;
	private String password;
	private YouTubeService service;
	public static final String DEVELOPER_KEY = "AI39si5Sfx7cQ-LXq6ERi31r5K19y1x5w94Cnirtp1Vza30kDELcLFCr6LjoYDfplqlpRia19SBYn8ZUffQMXtwGbjuc2ezq0w";
	public static final String CLIENT_ID = "ol-youtube";
	private PlaylistLinkFeed playlistLinkFeed;
	public final File folder = new File("./upload");
	private ArrayList<File> videos;

	private ArrayList<String> titles;
	private ArrayList<String> descriptions;
	private ArrayList<String> keys;
	private String postUrl;

	private ArrayList<Checkpoint> checkpoints;

	public YouTubeUploader(String username, String password, String postUrl)
			throws MalformedURLException, IOException, ServiceException,
			InterruptedException, ExecutionException {
		this.username = username;
		this.password = password;
		this.postUrl = postUrl;

		titles = new ArrayList<String>();
		descriptions = new ArrayList<String>();
		keys = new ArrayList<String>();

		checkpoints = new ArrayList<Checkpoint>();

		videos = new ArrayList<File>(Arrays.asList(folder.listFiles()));
		cleanNonVideos();
		service = new YouTubeService(CLIENT_ID, DEVELOPER_KEY);
		service.setUserCredentials(this.username, this.password);

		uploadAll();
	}

	private void cleanNonVideos() {
		ArrayList<File> onlyVideos = new ArrayList<File>();

		for (File video : videos) {
			String videoFileName = video.getName().toLowerCase();
			if (videoFileName.indexOf(".mp4") == videoFileName.length() - 4) {
				onlyVideos.add(video);
			}
		}

		videos = onlyVideos;
	}

	private void uploadAll() throws MalformedURLException, IOException,
			ServiceException, InterruptedException, ExecutionException {
		testOnly();
		ArrayList<SwingWorker<String, Void>> workerPool = new ArrayList<SwingWorker<String, Void>>();

		for (int i = 0; i < videos.size(); i++) {
			SwingWorker<String, Void> worker = new UploadWorker(videos.get(i),
					titles.get(i), descriptions.get(i), service);
			workerPool.add(worker);
			worker.execute();
		}

		for (SwingWorker<String, Void> worker : workerPool) {
			keys.add(worker.get());
		}

		if (keys.size() == titles.size() && keys.size() == descriptions.size()) {

			for (int i = 0; i < keys.size(); i++) {
				checkpoints.add(new Checkpoint(titles.get(i), descriptions
						.get(i), keys.get(i)));
			}
		} else {
			System.out.println("Unsuccessful upload");
		}
	}

	public String getJson() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(checkpoints);
	}

	public void postJson() throws MalformedURLException, IOException {		
		URLConnection connection = new URL(postUrl).openConnection();
		connection.setDoOutput(true);
		connection.setRequestProperty("Accept-Charset", "UTF-8");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + "UTF-8");
		OutputStream output = null;
		
		output = connection.getOutputStream();
		output.write(getJson().getBytes("UTF-8"));
	}

	private void testOnly() {
		titles.add("Title 1");
		titles.add("Title 2");
		titles.add("Title 3");

		descriptions.add("Description 1");
		descriptions.add("Description 2");
		descriptions.add("Description 3");
	}

	public class UploadWorker extends SwingWorker<String, Void> {

		private File video;
		private String title;
		private String description;
		private YouTubeService service;

		public UploadWorker(File video, String title, String description,
				YouTubeService service) {
			this.video = video;
			this.title = title;
			this.description = description;
			this.service = service;
			System.out.println("upload worker constructed");
		}

		@Override
		protected String doInBackground() throws Exception {
			System.out.println("executing");
			VideoEntry newEntry = new VideoEntry();
			YouTubeMediaGroup mg = newEntry.getOrCreateMediaGroup();
			mg.setTitle(new MediaTitle());
			mg.getTitle().setPlainTextContent(title);
			mg.addCategory(new MediaCategory(YouTubeNamespace.CATEGORY_SCHEME,
					"Education"));
			mg.setDescription(new MediaDescription());
			mg.getDescription().setPlainTextContent(description);
			mg.setPrivate(false);

			MediaFileSource ms = new MediaFileSource(video, "video/quicktime");
			newEntry.setMediaSource(ms);

			String uploadUrl = "http://uploads.gdata.youtube.com/feeds/api/users/default/uploads";

			System.out.println("uploading " + video.getName());

			VideoEntry createdEntry = service.insert(new URL(uploadUrl),
					newEntry);

			if (createdEntry.isDraft()) {
				System.out.println("is uploaded");
			}

			String key = createdEntry.getId().substring(
					createdEntry.getId().indexOf("video") + 6);

			return key;
		}

	}

	public class Checkpoint {

		private String title;
		private String description;
		private String subject;
		private String url;

		public Checkpoint(String title, String description, String key) {
			this.title = title;
			this.description = description;
			this.url = "http://youtu.be/" + key;
			this.description = "Unsorted";
		}

		public String getTitle() {
			return title;
		}

		public String getDescription() {
			return description;
		}

		public String getSubject() {
			return subject;
		}

		public String getUrl() {
			return url;
		}
	}
}
