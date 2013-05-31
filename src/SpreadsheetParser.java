import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gdata.client.spreadsheet.*;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.ServiceException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author linanqiu
 * @file_name SpreadsheetParser.java
 * 
 *            Parses the Website Data spreadsheet and produces various content
 *            in Json, the format being {"attribute":"some_attribute",
 *            "key":"some_youtube_key",
 *            "value":"some_value_for_that_attribute"}. These values are then
 *            used by other classes.
 * 
 */
public class SpreadsheetParser {

	private String username;
	private String password;
	private String urlString;
	private SpreadsheetService service;
	private CellFeed cellFeed;
	private ArrayList<Checkpoint> checkpointList;
	private String boilerplateDescription;
	private HashMap<String, String> topicLookup;
	private HashMap<String, String> seabLookup;
	private HashMap<String, String> subjectLookup;

	public SpreadsheetParser(String username, String password, String urlString)
			throws IOException, ServiceException, URISyntaxException {
		this.urlString = urlString;
		this.username = username;
		this.password = password;
		checkpointList = new ArrayList<Checkpoint>();
		File boilerReader = new File("boilerplate_description.txt");
		Scanner boilerScan = new Scanner(boilerReader);
		boilerplateDescription = "";
		while (boilerScan.hasNext()) {
			boilerplateDescription = boilerplateDescription + "\n"
					+ boilerScan.nextLine();
		}

		service = new SpreadsheetService("ol-youtube-spreadsheet-parser");
		service.setUserCredentials(this.username, this.password);

		buildLookups();

		URL url = new URI(getCheckpointWorksheetURL(this.urlString)).toURL();
		cellFeed = service.getFeed(url, CellFeed.class);

		buildCheckpointList();
	}

	/**
	 * Gets the topicLookup Hashmap. Used to find topic / SEAB / lesson of a
	 * checkpoint. It is of the format <lesson, topic> it is used to find the
	 * topic of a checkpoint from the lesson of the checkpoint. This is
	 * basically because lesson and topic are on two different pages of the
	 * Website Data spreadsheet, and cannot be parsed directly without
	 * establishing a separate feed. Doing so for every single checkpoint will
	 * be inefficient, so a lookup table is much better.
	 * 
	 * @return HashMap<String, String> topicLookup
	 */
	public HashMap<String, String> getTopicLookup() {
		return topicLookup;
	}

	/**
	 * Gets the seabLookup Hashmap. Used to find topic / SEAB / lesson of a
	 * checkpoint. The lookup is of the format <topic, seab> It is used to find
	 * the seab of a checkpoint from the topic of a checkpoint.
	 * 
	 * @return HashMap<String, String> seabLookup
	 */
	public HashMap<String, String> getSeabLookup() {
		return seabLookup;
	}

	/**
	 * Gets the subjectLookup HashMap. Used to find the topic / SEAB / lesson of
	 * a checkpoint. The lookup is of the format <topic, subject>. Finds subject
	 * using the topic of a checkpoint. Again, different sheet of the
	 * spreadhseet, hence the subject of a checkpoint cannot be inferred
	 * directly.
	 * 
	 * @return HashMap<String, String> subjectLookup
	 */
	public HashMap<String, String> getSubjectLookup() {
		return subjectLookup;
	}

	/**
	 * Builds all the 3 lookups by looking at the sheet that is fed to the
	 * SpreadsheetParser during construction, and two other sheets (the
	 * TopicWorksheet and the LessonWorksheet), each of which's URL is provided
	 * by the methods below this one.
	 * 
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void buildLookups() throws URISyntaxException, IOException,
			ServiceException {
		URL topicUrl = new URI(getLessonWorksheetURL(this.urlString)).toURL();
		CellFeed topicCellFeed = service.getFeed(topicUrl, CellFeed.class);

		topicLookup = new HashMap<String, String>();

		String lessonColumn = null;
		String topicColumn = null;

		String lesson = null;
		String topic = null;

		// The topicLookup HashMap is built first, so that one can easily infer
		// the topic of a checkpoint given the lesson of a checkpoint. The
		// lesson of a checkpoint can be directly copied from the same
		// spreadsheet, so that step is done trivially in the checkpoint
		// subclass itself.

		for (CellEntry cell : topicCellFeed.getEntries()) {
			int cellRow = Integer.valueOf(cell.getTitle().getPlainText()
					.replaceAll("[A-Za-z]+", ""));

			if (cellRow == 1) {
				if (cell.getPlainTextContent().equals("lesson")) {
					lessonColumn = cell.getTitle().getPlainText()
							.replaceAll("[0-9]+", "");
				}
				if (cell.getPlainTextContent().equals("topic")) {
					topicColumn = cell.getTitle().getPlainText()
							.replaceAll("[0-9]+", "");
				}
			} else {

				if (cell.getTitle().getPlainText().indexOf(lessonColumn) > -1) {
					lesson = cell.getPlainTextContent();
				}
				if (cell.getTitle().getPlainText().indexOf(topicColumn) > -1) {
					topic = cell.getPlainTextContent();
				}

				if (lesson != null && topic != null) {
					topicLookup.put(lesson, topic);
					lesson = null;
					topic = null;
				}

			}
		}

		// Now time to build the SEAB lookup. Now that we have a lookup that
		// matches topics to lessons, and since we have the lesson of the
		// checkpoint directly, we want to find the SEAB using the topic. We can
		// also look for the subject of a checkpoint using the topic. Both of
		// these lookups are built in the next few lines.

		URL seabUrl = new URI(getTopicWorksheetURL(this.urlString)).toURL();
		CellFeed seabCellFeed = service.getFeed(seabUrl, CellFeed.class);

		seabLookup = new HashMap<String, String>();
		subjectLookup = new HashMap<String, String>();

		String seabColumn = null;
		topicColumn = null;
		String subjectColumn = null;

		String seab = null;
		topic = null;
		String subject = null;

		for (CellEntry cell : seabCellFeed.getEntries()) {
			int cellRow = Integer.valueOf(cell.getTitle().getPlainText()
					.replaceAll("[A-Za-z]+", ""));

			if (cellRow == 1) {
				if (cell.getPlainTextContent().equals("topic")) {
					topicColumn = cell.getTitle().getPlainText()
							.replaceAll("[0-9]+", "");
				}
				if (cell.getPlainTextContent().equals("SEAB Sub Topic")) {
					seabColumn = cell.getTitle().getPlainText()
							.replaceAll("[0-9]+", "");
				}
				if (cell.getPlainTextContent().equals("subject")) {
					subjectColumn = cell.getTitle().getPlainText()
							.replaceAll("[0-9]+", "");
				}
			} else {

				if (cell.getTitle().getPlainText().indexOf(topicColumn) > -1) {
					topic = cell.getPlainTextContent();
				}
				if (cell.getTitle().getPlainText().indexOf(seabColumn) > -1) {
					seab = cell.getPlainTextContent();
				}
				if (cell.getTitle().getPlainText().indexOf(subjectColumn) > -1) {
					subject = cell.getPlainTextContent();
				}
				if (seab != null && topic != null && subject != null) {
					seabLookup.put(topic, seab);
					subjectLookup.put(topic, subject);
					topic = null;
					seab = null;
					subject = null;
				}

			}
		}
	}

	/**
	 * YouTube API only accepts URL in a very weird format, basically
	 * 
	 * https://spreadsheets.google.com/feeds/cells/KEY/GID/private/basic
	 * 
	 * Key refers to the key of the psreadsheet, and GID refers to the worksheet
	 * number. Private means that the worksheet need not be published. Basic,
	 * just leave it as it is.
	 * 
	 * @param urlString
	 * @return spreadsheetURL the URL of the checkpoint worksehet in the
	 *         spreadsheet
	 */
	private String getCheckpointWorksheetURL(String urlString) {
		Pattern pattern = Pattern.compile("key=(.*)#gid");
		Matcher matcher = pattern.matcher(urlString);

		String key = "";

		if (matcher.find()) {
			key = matcher.group(1);
		}

		Pattern pattern2 = Pattern.compile("gid=(\\d)");
		Matcher matcher2 = pattern2.matcher(urlString);

		String gid = "";

		if (matcher2.find()) {
			gid = matcher2.group(1);
		}

		String spreadsheetURL = "https://spreadsheets.google.com/feeds/cells/"
				+ key + "/" + gid + "/private/basic";

		return spreadsheetURL;
	}

	/**
	 * This returns the url of the lesson sheet in the spreadsheet. This sheet
	 * of the spreadsheet is important because it links the lesson of a
	 * checkpoint to the topic, allowing us to build a lookup table. While the
	 * original sheet of the spreadsheet has a #gid=6, this one has #gid-5, ie
	 * one sheet before the original one.
	 * 
	 * @param urlString
	 * @return spreadsheetURL the URL of the Lesson sheet in the spreadsheet
	 */
	private String getLessonWorksheetURL(String urlString) {
		Pattern pattern = Pattern.compile("key=(.*)#gid");
		Matcher matcher = pattern.matcher(urlString);

		String key = "";

		if (matcher.find()) {
			key = matcher.group(1);
		}

		Pattern pattern2 = Pattern.compile("gid=(\\d)");
		Matcher matcher2 = pattern2.matcher(urlString);

		String gid = "";

		if (matcher2.find()) {
			gid = matcher2.group(1);
		}

		int lessonsGid = Integer.valueOf(gid) - 1;

		String spreadsheetURL = "https://spreadsheets.google.com/feeds/cells/"
				+ key + "/" + lessonsGid + "/private/basic";

		return spreadsheetURL;
	}

	/**
	 * Builds the TopicWorksheetURL from the original URL. In the original URL,
	 * the sheet for checkpoints has a #gid=6. That sheet contains data of the
	 * checkpoint, and the lesson that the checkpoint belongs to. It doesn't
	 * contain topic or subject or SEAB data. #gid=4, which is two sheets
	 * earlier than the Checkpoint sheet, contains data of the topic, subject,
	 * and SEAB sub topic. We want to get this URL.
	 * 
	 * @param urlString
	 * @return spreadsheetURL the URL of the Topic sheet in the spreadsheet
	 */
	private String getTopicWorksheetURL(String urlString) {
		Pattern pattern = Pattern.compile("key=(.*)#gid");
		Matcher matcher = pattern.matcher(urlString);

		String key = "";

		if (matcher.find()) {
			key = matcher.group(1);
		}

		Pattern pattern2 = Pattern.compile("gid=(\\d)");
		Matcher matcher2 = pattern2.matcher(urlString);

		String gid = "";

		if (matcher2.find()) {
			gid = matcher2.group(1);
		}

		int lessonsGid = Integer.valueOf(gid) - 2;

		String spreadsheetURL = "https://spreadsheets.google.com/feeds/cells/"
				+ key + "/" + lessonsGid + "/private/basic";

		return spreadsheetURL;
	}

	/**
	 * Builds an ArrayList<Checkpoint> representing all the checkpoints
	 * currently existing in the website data spreadsheet
	 */
	private void buildCheckpointList() {
		int currentRow = 2;
		int id = -1;
		String title = null;
		String description = null;
		String lecturerName = null;
		String url = null;
		String lesson = null;

		String idColumn = null;
		String titleColumn = null;
		String descriptionColumn = null;
		String lecturerNameColumn = null;
		String urlColumn = null;
		String lessonColumn = null;

		// Go through all the cells. Unfortunately, I couldn't get the row by
		// row function to work. So oh well.

		for (CellEntry cell : cellFeed.getEntries()) {

			// Get the row number of each row that we are iterating
			int cellRow = Integer.valueOf(cell.getTitle().getPlainText()
					.replaceAll("[A-Za-z]+", ""));

			// For the header row, find the columns that match each of these
			// titles
			if (cellRow == 1) {
				if (cell.getPlainTextContent().equals("id")) {
					idColumn = cell.getTitle().getPlainText()
							.replaceAll("[0-9]+", "");
				}
				if (cell.getPlainTextContent().equals("checkpoint")) {
					titleColumn = cell.getTitle().getPlainText()
							.replaceAll("[0-9]+", "");
				}
				if (cell.getPlainTextContent().equals("description")) {
					descriptionColumn = cell.getTitle().getPlainText()
							.replaceAll("[0-9]+", "");
				}
				if (cell.getPlainTextContent().equals("lecturer")) {
					lecturerNameColumn = cell.getTitle().getPlainText()
							.replaceAll("[0-9]+", "");
				}
				if (cell.getPlainTextContent().equals("videourl")) {
					urlColumn = cell.getTitle().getPlainText()
							.replaceAll("[0-9]+", "");
				}
				if (cell.getPlainTextContent().equals("lesson")) {
					lessonColumn = cell.getTitle().getPlainText()
							.replaceAll("[0-9]+", "");
				}
			} else {

				// for all other non header rows, if we have skipped to a next
				// row or if we have reached the last cell of the last row
				if (cellRow != currentRow
						|| cell.getTitle()
								.getPlainText()
								.equals(cellFeed.getEntries()
										.get(cellFeed.getEntries().size() - 1)
										.getTitle().getPlainText())) {

					// calls a new checkpoint and adds it to the checkpointlist
					checkpointList.add(new Checkpoint(title, description,
							lecturerName, id, url, lesson));

					// updates current row and resets the variable values
					currentRow = cellRow;
					title = null;
					description = null;
					lecturerName = null;
					url = null;

					// updates the id of the checkpoint, since id tends to be
					// the first cell of the newest row. This line makes sure
					// that the cell isn't skipped over.
					if (cell.getTitle().getPlainText().indexOf(idColumn) > -1) {
						id = Integer.valueOf(cell.getPlainTextContent());
					}

					// find the right column, and update the variables
					// accordingly. When the pointer moves to the next row, this
					// information will be added to the checkpointlist.
				} else {
					if (cell.getTitle().getPlainText().indexOf(idColumn) > -1) {
						id = Integer.valueOf(cell.getPlainTextContent());
					}
					if (cell.getTitle().getPlainText().indexOf(titleColumn) > -1) {
						title = cell.getPlainTextContent();
					}
					if (cell.getTitle().getPlainText()
							.indexOf(descriptionColumn) > -1) {
						description = cell.getPlainTextContent();
					}
					if (cell.getTitle().getPlainText()
							.indexOf(lecturerNameColumn) > -1) {
						lecturerName = cell.getPlainTextContent();
					}
					if (cell.getTitle().getPlainText().indexOf(urlColumn) > -1) {
						url = cell.getPlainTextContent();
					}
					if (cell.getTitle().getPlainText().indexOf(lessonColumn) > -1) {
						lesson = cell.getPlainTextContent();
					}
				}
			}
		}

		// just for useless error checking.
		System.out.println(checkpointList.size()
				+ " Checkpoints with complete information found.");

		// kenneth wanted numbering for each of the checkpoints in the same
		// topic. Hence this part checks if checkpoints belong to the same
		// topic. Obviously, this assumes that the website data is organized
		// according to the topics and not messy. Eventually, these lines find
		// the numbering of the checkpoint in the topic and adds it to the
		// checkpoing.

		boolean sameTopic = true;
		ArrayList<Checkpoint> tempCheckpoint = new ArrayList<Checkpoint>();
		tempCheckpoint.add(checkpointList.get(0));
		for (int i = 1; i < checkpointList.size(); i++) {
			if (checkpointList.get(i).getTopic()
					.equals(checkpointList.get(i - 1).getTopic())) {
				tempCheckpoint.add(checkpointList.get(i));
			} else {
				for (int j = 0; j < tempCheckpoint.size(); j++) {
					int index = j + 1;
					tempCheckpoint.get(j).setNumbering(
							"[" + index + "/" + tempCheckpoint.size() + "]");
				}
				tempCheckpoint.clear();
				tempCheckpoint.add(checkpointList.get(i));
			}
		}
		for (int j = 0; j < tempCheckpoint.size(); j++) {
			int index = j + 1;
			tempCheckpoint.get(j).setNumbering(
					"[" + index + "/" + tempCheckpoint.size() + "]");
		}
	}

	/**
	 * returns Json of the checkpointList collection in the format
	 * 
	 * {"attribute":"some_attribute", "key":"some_youtube_key",
	 * "value":"some_value_for_that_attribute"}
	 * 
	 * accepted attributes are "description" "title" topic" "playlist"
	 * 
	 * @param attribute
	 * @return Json serialized checkpointList using Gson
	 */
	public String getJson(String attribute) {
		System.out.println("Retrieving json for attribute " + attribute);
		ArrayList<GsonCheckpoint> output = new ArrayList<GsonCheckpoint>();
		for (Checkpoint checkpoint : checkpointList) {
			GsonCheckpoint gsonCheckpoint = new GsonCheckpoint(attribute,
					checkpoint);
			output.add(gsonCheckpoint);
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(output);
	}

	/**
	 * @author linanqiu
	 * @file_name SpreadsheetParser.java
	 * 
	 *            subclass GsonCheckpoint, representing a checkpoint stripped of
	 *            all its details except for attribute, value, key. Convenient
	 *            for serializing.
	 */
	private class GsonCheckpoint {

		private String attribute;
		private String value;
		private String key;

		/**
		 * Prepares the GsonCheckpoint for serialization by building the "value"
		 * 
		 * @param attribute
		 * @param checkpoint
		 */
		public GsonCheckpoint(String attribute, Checkpoint checkpoint) {
			this.attribute = attribute;

			// concatenates the description string. the others for title topic
			// and playlist should be quite straightforward
			if (attribute.equals("description")) {
				String description = "";
				description = description + checkpoint.getTitle() + "\n"
						+ checkpoint.getNumbering() + "\n";
				description = description + "by " + checkpoint.getLecturer()
						+ "\n\n";
				description = description + checkpoint.getDescription() + "\n";
				description = description
						+ SpreadsheetParser.this.boilerplateDescription;

				value = description;
			} else if (attribute.equals("title")) {
				value = checkpoint.getTitle();
			} else if (attribute.equals("topic")) {
				value = checkpoint.getTopic();
			} else if (attribute.equals("playlist")) {
				value = checkpoint.getPlaylist();
			} else {
				value = null;
			}
			key = checkpoint.getKey();
		}
	}

	/**
	 * Subclass Checkpoint representing a checkpoint. Why GsonCheckpoitn and
	 * Checkpoint? Checkpoint represents a complete checkpoint with all the
	 * information. GsonCheckpoint represents only hte inforation demanded by
	 * the Json request
	 * 
	 * @author linanqiu
	 * @file_name SpreadsheetParser.java
	 */
	private class Checkpoint {

		private String description;
		private String title;
		private String lecturer;
		private int id;
		private String url;
		private String key;
		private String lesson;
		private String topic;
		private String numbering;
		private String seab;
		private String subject;
		private String playlist;

		public Checkpoint(String title, String description, String lecturer,
				int id, String url, String lesson) {
			this.description = description;
			this.title = title;
			this.lecturer = lecturer;
			this.id = id;
			this.url = url;
			this.lesson = lesson;

			// parses the URL of the checkpoint into the Key of the checkpoint,
			// removing the unnecessary shit.
			parseURLintoKey();

			// looks up for the topic of the checkpoint
			buildTopic();

			// using the topic, looks up for the seab of the checkpoint
			buildSeab();

			// using the topic, looks up for the subject of the checkpoint
			buildSubject();

			// using the topic and the subject, builds the playlist of the
			// checkpoint
			buildPlaylist();
		}

		public String getPlaylist() {
			return playlist;
		}

		/**
		 * allows the checkpoint to be numbered
		 * 
		 * @param numbering
		 */
		public void setNumbering(String numbering) {
			this.numbering = numbering;
		}

		public String getNumbering() {
			return numbering;
		}

		/**
		 * builds the playlist title of the checkpoint. YouTube doesn't accept
		 * playlist titles longer than 60 characters.
		 */
		private void buildPlaylist() {
			playlist = subject + " / " + topic;
			playlist = playlist.replaceAll("[^0-9A-Za-z\\s/]*", "");
			if (playlist.length() > 60) {
				playlist = playlist.substring(0, 60);
			}
		}

		private void buildTopic() {
			topic = SpreadsheetParser.this.getTopicLookup().get(lesson);
		}

		private void buildSeab() {
			seab = SpreadsheetParser.this.getSeabLookup().get(topic);
		}

		private void buildSubject() {
			subject = SpreadsheetParser.this.getSubjectLookup().get(topic);
		}

		public String getTopic() {
			return topic;
		}

		public void parseURLintoKey() {
			key = url.replaceAll("http://youtu.be/", "");
		}

		public String getTitle() {
			return title;
		}

		public String getDescription() {
			return description;
		}

		public String getLecturer() {
			return lecturer;
		}

		public int getId() {
			return id;
		}

		public String getURL() {
			return url;
		}

		public String getKey() {
			return key;
		}
	}

}
