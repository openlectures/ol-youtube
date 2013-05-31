import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.google.common.reflect.TypeToken;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.youtube.PlaylistEntry;
import com.google.gdata.data.youtube.PlaylistFeed;
import com.google.gdata.data.youtube.PlaylistLinkEntry;
import com.google.gdata.data.youtube.PlaylistLinkFeed;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.InvalidEntryException;
import com.google.gdata.util.ResourceNotFoundException;
import com.google.gdata.util.ServiceException;
import com.google.gdata.util.ServiceForbiddenException;
import com.google.gson.Gson;

/**
 * @author linanqiu
 * @file_name PlaylistEditor.java
 * 
 *            Arranges YouTube videos into playlists based on Json input
 *            collection of attribute=playlist, key=some_key,
 *            value=playlist_value
 */
public class PlaylistEditor {

	private String username;
	private String password;
	ArrayList<GsonCheckpoint> gsonList;
	private YouTubeService service;
	public static final String DEVELOPER_KEY = "AI39si5Sfx7cQ-LXq6ERi31r5K19y1x5w94Cnirtp1Vza30kDELcLFCr6LjoYDfplqlpRia19SBYn8ZUffQMXtwGbjuc2ezq0w";
	public static final String CLIENT_ID = "ol-youtube";
	private ArrayList<GsonCheckpoint> errorCheckpoints;
	private ArrayList<String> currentPlaylistTitles;
	private String playlistURL;
	private PlaylistLinkFeed playlistLinkFeed;

	/**
	 * logs in and builds the playlistFeed URL
	 * 
	 * @param username
	 * @param password
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ServiceException
	 */
	public PlaylistEditor(String username, String password)
			throws MalformedURLException, IOException, ServiceException {
		this.username = username;
		this.password = password;
		errorCheckpoints = new ArrayList<GsonCheckpoint>();
		service = new YouTubeService(CLIENT_ID, DEVELOPER_KEY);
		service.setUserCredentials(this.username, this.password);
		buildPlaylistURL();
	}

	/**
	 * builds the playlist url from the default playlist URL by inserting the
	 * username in
	 */
	private void buildPlaylistURL() {
		playlistURL = "http://gdata.youtube.com/feeds/api/users/username/playlists";
		playlistURL = playlistURL.replaceAll("username", this.username);
		System.out.println(playlistURL);
	}

	/**
	 * Commonly used method to delete all entries from a playlist. This is
	 * rather contrived, because calling playlistEntry.delete() does not
	 * necessarily delete the video because YouTube is fucked up. Hence this
	 * method attempts to delete all PlaylistEntry s in the playlist first, then
	 * counts the number of videos in the playlist, then repeats this process
	 * again until the count size is zero.
	 * 
	 * @param playlist
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void clearPlaylist(PlaylistLinkEntry playlist)
			throws MalformedURLException, IOException, ServiceException {
		PlaylistFeed playlistFeed = service.getFeed(
				new URL(playlist.getFeedUrl()), PlaylistFeed.class);
		while (playlistFeed.getEntries().size() != 0) {
			for (PlaylistEntry playlistEntry : playlistFeed.getEntries()) {
				boolean success = false;
				while (!success) {
					try {
						playlistEntry.delete();
						success = true;
					} catch (ResourceNotFoundException e) {
						System.out.println("ERROR: "
								+ playlistEntry.getTitle().getPlainText()
								+ " resource not found.");
						success = true;
					} catch (InvalidEntryException e) {
						System.out.println("ERROR: "
								+ playlistEntry.getTitle().getPlainText()
								+ " invalid entry.");
						success = true;
					} catch (ServiceForbiddenException e) {
						System.out
								.println("Service Forbidden. Waiting 10 seconds.");
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
			playlistFeed = service.getFeed(new URL(playlist.getFeedUrl()
					+ "?max-results=50"), PlaylistFeed.class);
		}
	}

	/**
	 * Scans all playlists, updating them as necessary. First, it corrects
	 * existing playlists to make sure that it matches the website data. Then,
	 * it adds all videos that does not belong in any existing playlist to new
	 * playlists. The two private methods used should be quite self explanatory.
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void scanPlaylists() throws MalformedURLException, IOException,
			ServiceException {

		playlistLinkFeed = service.getFeed(new URL(playlistURL),
				PlaylistLinkFeed.class);

		correctExistingPlaylists(playlistLinkFeed);

		addOtherCheckpoints(playlistLinkFeed);
	}

	/**
	 * Runs through all current playlist.
	 * 
	 * For each playlist that the account has,
	 * 
	 * 1. Doesn't touch Gary Lee's zArchive, [OL, or Misc lectures.
	 * 
	 * 2. Runs through the gsonList of checkpoints and find the checkpoints that
	 * has the same playlist as the one currently being iterated. Adds them to
	 * samePlaylistCheckpoints ArrayList.
	 * 
	 * 3. If the samePlaylistCheckpoints does not have the same size as the
	 * playlist on YouTube,
	 * 
	 * 3a. If the samePlaylistCheckpoints ArrayList is empty, ie. there are
	 * videos on YouTube but none on the website data list, something is wrong.
	 * As a measure of safety, the playlist on YouTube is not deleted. Instead,
	 * the error message
	 * "PLAYLIST_TITLE  contains videos but website data has no checkpoints. Check?"
	 * will be printed.
	 * 
	 * 3b. If samePlaylistCheckpoints is not empty, then delete every single
	 * video in the playlist currently on YouTube. Then, readd all the
	 * checkpoints in the samePlaylistCheckpoints ArrayList. In other words,
	 * refreshing the playlist on YouTube.
	 * 
	 * 4. If the playlist on YouTube has the same size as sameCheckpointList,
	 * then check for mismatches (ie. run through each video entry along hte
	 * playlist on YouTube and the playlist in the sameCheckpointList and see if
	 * they are the same key. If they are, then check is passed and there will
	 * be no need to update the playlist on YouTube. If not, then delete all
	 * videos in the playlist on YouTube and readd.
	 * 
	 * @param playlistLinkFeed
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void correctExistingPlaylists(PlaylistLinkFeed playlistLinkFeed)
			throws MalformedURLException, IOException, ServiceException {

		for (PlaylistLinkEntry playlist : playlistLinkFeed.getEntries()) {

			if (playlist.getTitle().getPlainText().indexOf("zArchive") > -1
					|| playlist.getTitle().getPlainText().indexOf("[OL") > -1
					|| playlist.getTitle().getPlainText().indexOf("Misc /") > -1) {

			} else {
				PlaylistFeed playlistFeed = service.getFeed(
						new URL(playlist.getFeedUrl() + "?max-results=50"),
						PlaylistFeed.class);

				ArrayList<GsonCheckpoint> samePlaylistCheckpoints = new ArrayList<GsonCheckpoint>();

				for (GsonCheckpoint checkpoint : gsonList) {
					if (playlist.getTitle().getPlainText()
							.indexOf(checkpoint.getValue()) > -1
							&& checkpoint.getKey().indexOf("dummyvar") == -1) {
						samePlaylistCheckpoints.add(checkpoint);
					}
				}

				if (samePlaylistCheckpoints.size() != playlistFeed.getEntries()
						.size()) {

					if (samePlaylistCheckpoints.size() == 0) {
						System.out
								.println(playlist.getTitle().getPlainText()
										+ " contains videos but website data has no checkpoints. Check?");
					} else {

						clearPlaylist(playlist);

						System.out.println(playlist.getTitle().getPlainText()
								+ " Checkpoints will be readded");

						System.out.println("All Checkpoints Removed From "
								+ playlist.getTitle().getPlainText());

						for (GsonCheckpoint checkpoint : samePlaylistCheckpoints) {
							boolean success = false;
							while (!success) {
								try {
									String videoEntryUrl = "http://gdata.youtube.com/feeds/api/videos/"
											+ checkpoint.getKey();

									VideoEntry videoEntry = service.getEntry(
											new URL(videoEntryUrl),
											VideoEntry.class);

									PlaylistEntry playlistEntry = new PlaylistEntry(
											videoEntry);

									service.insert(
											new URL(playlist.getFeedUrl()),
											playlistEntry);

									System.out.println(checkpoint.getKey()
											+ " added to "
											+ playlist.getTitle()
													.getPlainText());
									success = true;
								} catch (ResourceNotFoundException e) {
									System.out.println("ERROR: "
											+ checkpoint.getKey()
											+ " resource not found.");
									success = true;
								} catch (InvalidEntryException e) {
									System.out.println("ERROR: "
											+ checkpoint.getKey()
											+ " invalid entry.");
									success = true;
								} catch (ServiceForbiddenException e) {
									System.out
											.println("Service Forbidden. Waiting 10 seconds.");
									try {
										Thread.sleep(10000);
									} catch (InterruptedException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								}
							}
						}

						System.out.println(playlist.getTitle().getPlainText()
								+ " successfully updated");
					}
				} else {

					boolean mismatch = false;

					for (int i = 0; i < samePlaylistCheckpoints.size(); i++) {
						if (!playlistFeed
								.getEntries()
								.get(i)
								.getMediaGroup()
								.getVideoId()
								.equals(samePlaylistCheckpoints.get(i).getKey())) {
							mismatch = true;
							System.out.println("found mismatch");
						}
					}

					if (mismatch) {
						System.out.println(playlist.getTitle().getPlainText()
								+ " Checkpoints will be readded");

						clearPlaylist(playlist);

						System.out.println("All Checkpoints Removed From "
								+ playlist.getTitle().getPlainText());

						for (GsonCheckpoint checkpoint : gsonList) {
							boolean success = false;
							while (!success) {
								try {
									String videoEntryUrl = "http://gdata.youtube.com/feeds/api/videos/"
											+ checkpoint.getKey();

									VideoEntry videoEntry = service.getEntry(
											new URL(videoEntryUrl),
											VideoEntry.class);

									PlaylistEntry playlistEntry = new PlaylistEntry(
											videoEntry);

									service.insert(
											new URL(playlist.getFeedUrl()),
											playlistEntry);

									System.out.println(checkpoint.getKey()
											+ " added to "
											+ playlist.getTitle()
													.getPlainText());
									success = true;
								} catch (ResourceNotFoundException e) {
									System.out.println("ERROR: "
											+ checkpoint.getKey()
											+ " resource not found.");
									success = true;
								} catch (InvalidEntryException e) {
									System.out.println("ERROR: "
											+ checkpoint.getKey()
											+ " invalid entry.");
									success = true;
								} catch (ServiceForbiddenException e) {
									System.out
											.println("Service Forbidden. Waiting 10 seconds.");
									try {
										Thread.sleep(10000);
									} catch (InterruptedException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								}
							}
						}

						System.out.println(playlist.getTitle().getPlainText()
								+ " successfully updated");
					} else {
						System.out.println(playlist.getTitle().getPlainText()
								+ " does not need updating.");
					}
				}
			}
		}
	}

	/**
	 * Adds all other checkpoints to new playlists.
	 * 
	 * 1. Runs through all checkpoints in gsonList. For each checkpoint, runs
	 * through all playlists to see if there's any match. If no match is found
	 * after trying to match all playlists, add checkpoints to
	 * checkpointsWithoutPlaylist.
	 * 
	 * 2. Go through checkpointsWithoutPlaylist, adds playlists for remaining
	 * videos.
	 * 
	 * @param playlistLinkFeed
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void addOtherCheckpoints(PlaylistLinkFeed playlistLinkFeed)
			throws MalformedURLException, IOException, ServiceException {

		ArrayList<GsonCheckpoint> checkpointsWithoutPlaylist = new ArrayList<GsonCheckpoint>();

		for (GsonCheckpoint checkpoint : gsonList) {
			boolean found = false;
			for (PlaylistLinkEntry playlist : playlistLinkFeed.getEntries()) {

				if (playlist.getTitle().getPlainText()
						.indexOf(checkpoint.getValue()) > -1) {
					found = true;
				}

			}
			if (!found) {
				checkpointsWithoutPlaylist.add(checkpoint);
			}
		}

		// if there are checkpoints left to add,
		if (checkpointsWithoutPlaylist.size() > 0) {

			// create a new tempPlaylist and add the first checkpoint in the
			// checkpointsWithoutPlaylist
			ArrayList<GsonCheckpoint> tempPlaylist = new ArrayList<GsonCheckpoint>();
			tempPlaylist.add(checkpointsWithoutPlaylist.get(0));

			// keep adding new checkpoints until a new playlist is reached. When
			// that happens, add the contents of the tempPlaylist into a newly
			// created playlist.
			for (int i = 1; i < checkpointsWithoutPlaylist.size(); i++) {

				if (checkpointsWithoutPlaylist
						.get(i)
						.getValue()
						.equals(checkpointsWithoutPlaylist.get(i - 1)
								.getValue())) {
					tempPlaylist.add(checkpointsWithoutPlaylist.get(i));
				} else {
					PlaylistLinkEntry newEntry = new PlaylistLinkEntry();

					newEntry.setTitle(new PlainTextConstruct(tempPlaylist
							.get(0).getValue()));
					newEntry.setSummary(new PlainTextConstruct(""));

					PlaylistLinkEntry createdEntry = service.insert(new URL(
							playlistURL), newEntry);

					for (GsonCheckpoint checkpoint : tempPlaylist) {
						boolean success = false;
						while (!success) {
							try {
								String newPlaylistURL = createdEntry
										.getFeedUrl();
								String videoEntryUrl = "http://gdata.youtube.com/feeds/api/videos/video_key";
								videoEntryUrl = videoEntryUrl.replaceAll(
										"video_key", checkpoint.getKey());
								VideoEntry videoEntry = service.getEntry(
										new URL(videoEntryUrl),
										VideoEntry.class);

								PlaylistEntry playlistEntry = new PlaylistEntry(
										videoEntry);
								service.insert(new URL(newPlaylistURL),
										playlistEntry);
								System.out.println(checkpoint.getKey()
										+ " added to "
										+ createdEntry.getTitle()
												.getPlainText());
								success = true;
							} catch (ResourceNotFoundException e) {
								System.out.println("ERROR: "
										+ checkpoint.getKey()
										+ " resource not found.");
								success = true;
							} catch (InvalidEntryException e) {
								System.out.println("ERROR: "
										+ checkpoint.getKey()
										+ " invalid entry.");
								success = true;
							} catch (ServiceForbiddenException e) {
								System.out
										.println("Service Forbidden. Waiting 10 seconds.");
								try {
									Thread.sleep(10000);
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
						}
					}

					createdEntry.update();

					System.out.println("Playlist Added: "
							+ createdEntry.getTitle().getPlainText());
					System.out.println("Video No.: " + tempPlaylist.size());

					tempPlaylist.clear();
					tempPlaylist.add(checkpointsWithoutPlaylist.get(i));
				}

				// account for the very last playlist
				if (i == checkpointsWithoutPlaylist.size() - 1) {

					PlaylistLinkEntry newEntry = new PlaylistLinkEntry();

					newEntry.setTitle(new PlainTextConstruct(tempPlaylist
							.get(0).getValue()));
					newEntry.setSummary(new PlainTextConstruct(""));

					PlaylistLinkEntry createdEntry = service.insert(new URL(
							playlistURL), newEntry);

					for (GsonCheckpoint checkpoint : tempPlaylist) {
						boolean success = false;
						while (!success) {
							try {
								String newPlaylistURL = createdEntry
										.getFeedUrl();
								String videoEntryUrl = "http://gdata.youtube.com/feeds/api/videos/video_key";
								videoEntryUrl = videoEntryUrl.replaceAll(
										"video_key", checkpoint.getKey());
								VideoEntry videoEntry = service.getEntry(
										new URL(videoEntryUrl),
										VideoEntry.class);

								PlaylistEntry playlistEntry = new PlaylistEntry(
										videoEntry);
								service.insert(new URL(newPlaylistURL),
										playlistEntry);
								System.out.println(checkpoint.getKey()
										+ " added to "
										+ createdEntry.getTitle()
												.getPlainText());
								success = true;
							} catch (ResourceNotFoundException e) {
								System.out.println("ERROR: "
										+ checkpoint.getKey()
										+ " resource not found.");
								success = true;
							} catch (InvalidEntryException e) {
								System.out.println("ERROR: "
										+ checkpoint.getKey()
										+ " invalid entry.");
								success = true;
							} catch (ServiceForbiddenException e) {
								System.out
										.println("Service Forbidden. Waiting 10 seconds.");
								try {
									Thread.sleep(10000);
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
						}
					}

					createdEntry.update();

					System.out.println("Playlist Added: "
							+ createdEntry.getTitle().getPlainText());
					System.out.println("Video No.: " + tempPlaylist.size());

					tempPlaylist.clear();
					tempPlaylist.add(checkpointsWithoutPlaylist.get(i));
				}
			}
		}
	}

	/**
	 * Takes in json
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
	 * @file_name PlaylistEditor.java
	 * 
	 *            Subclass that represents a deserialized cehckpoint
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
