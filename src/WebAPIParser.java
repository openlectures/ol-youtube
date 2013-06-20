import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class WebAPIParser {

	private ArrayList<Checkpoint> checkpointList;
	private String boilerplateDescription;

	public WebAPIParser(String apiUrl) throws MalformedURLException,
			IOException {
		Scanner in = new Scanner(new URL(apiUrl).openStream());
		String json = "";
		while (in.hasNext()) {
			json = in.nextLine();
		}
		setJSON(json);
		
		File boilerReader = new File("boilerplate_description.txt");
		Scanner boilerScan = new Scanner(boilerReader);
		boilerplateDescription = "";
		while (boilerScan.hasNext()) {
			boilerplateDescription = boilerplateDescription + "\n"
					+ boilerScan.nextLine();
		}
	}

	private void buildNumbering() {
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

	public void setJSON(String json) {
		Gson gson = new Gson();
		Type collectionType = new TypeToken<Collection<Checkpoint>>() {
		}.getType();
		checkpointList = gson.fromJson(json, collectionType);
		for (Checkpoint checkpoint : checkpointList) {
			checkpoint.initialize();
		}
		buildNumbering();
	}

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
	 * @file_name WebAPIParser.java
	 * 
	 *            Represents one Gson Serializable depending on the request type
	 *            requested
	 */
	public class GsonCheckpoint {
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
						+ WebAPIParser.this.boilerplateDescription;
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
	 * @author linanqiu
	 * @file_name WebAPIParser.java
	 * 
	 *            Represents one checkpoint from Jethro's Web API
	 */
	public class Checkpoint {

		// provided by JSON
		private String title;
		private String description;
		private String lecturer;
		private String url;
		private String lesson;
		private String topic;
		private String seab;
		private String subject;

		// generated in situ
		private String playlist;
		private String key;
		private String numbering;

		public Checkpoint() {

		}

		/**
		 * builds the playlist and the key from existing information
		 */
		public void initialize() {
			buildPlaylist();
			buildKey();
		}

		private void buildPlaylist() {
			playlist = subject + " / " + topic;
			playlist = playlist.replaceAll("[^0-9A-Za-z\\s/]*", "");
			if (playlist.length() > 60) {
				playlist = playlist.substring(0, 60);
			}
		}

		public String getPlaylist() {
			return playlist;
		}

		private void buildKey() {
			key = url.replaceAll("http://youtu.be/", "");
		}

		public void setNumbering(String numbering) {
			this.numbering = numbering;
		}

		public String getNumbering() {
			return numbering;
		}

		public String getUrl() {
			return url;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getLecturer() {
			return lecturer;
		}

		public void setLecturer(String lecturer) {
			this.lecturer = lecturer;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getLesson() {
			return lesson;
		}

		public void setLesson(String lesson) {
			this.lesson = lesson;
		}

		public String getTopic() {
			return topic;
		}

		public void setTopic(String topic) {
			this.topic = topic;
		}

		public String getSeab() {
			return seab;
		}

		public void setSeab(String seab) {
			this.seab = seab;
		}

		public String getSubject() {
			return subject;
		}

		public void setSubject(String subject) {
			this.subject = subject;
		}

	}
}
