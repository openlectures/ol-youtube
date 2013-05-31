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

	public PlaylistEditor(String username, String password)
			throws MalformedURLException, IOException, ServiceException {
		this.username = username;
		this.password = password;
		errorCheckpoints = new ArrayList<GsonCheckpoint>();
		service = new YouTubeService(CLIENT_ID, DEVELOPER_KEY);
		service.setUserCredentials(this.username, this.password);
		buildPlaylistURL();
	}

	private void buildPlaylistURL() {
		playlistURL = "http://gdata.youtube.com/feeds/api/users/username/playlists";
		playlistURL = playlistURL.replaceAll("username", this.username);
		System.out.println(playlistURL);
	}

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
			System.out.println(playlistFeed.getEntries().size());
		}
	}

	public void deletePlaylists() throws MalformedURLException, IOException,
			ServiceException {
		playlistLinkFeed = service.getFeed(new URL(playlistURL
				+ "?max-results=50"), PlaylistLinkFeed.class);

		for (PlaylistLinkEntry playlist : playlistLinkFeed.getEntries()) {
			if (playlist.getTitle().getPlainText().indexOf("Economics") > -1) {
				playlist.delete();
			} else {
				System.out.println(playlist.getTitle().getPlainText());
			}
		}

	}

	public void scanPlaylists() throws MalformedURLException, IOException,
			ServiceException {

		playlistLinkFeed = service.getFeed(new URL(playlistURL),
				PlaylistLinkFeed.class);

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

		// Corrects Existing Playlists First

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
								.println("Playlist contains videos but website data has no checkpoints. Check?");
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

					System.out.println("Checkpoint number check passed for "
							+ playlist.getTitle().getPlainText());

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
					}

				}
			}

		}

		// Then add remaining playlists without any existing playlist to match
		// to

		if (checkpointsWithoutPlaylist.size() > 0) {
			ArrayList<GsonCheckpoint> tempPlaylist = new ArrayList<GsonCheckpoint>();
			tempPlaylist.add(checkpointsWithoutPlaylist.get(0));

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

	public void setJson(String json) {
		Gson gson = new Gson();
		Type collectionType = new TypeToken<Collection<GsonCheckpoint>>() {
		}.getType();
		gsonList = gson.fromJson(json, collectionType);
	}

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
