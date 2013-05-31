import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;


public class TagProcessor {
	
	private String keywordFileName;
	private File keywordFile;
	private Scanner scan;
	private ArrayList<GsonCheckpoint> gsonList;
	private ArrayList<String> keywordList;
	
	public TagProcessor(String keywordFileName) throws FileNotFoundException {
		this.keywordFileName = keywordFileName;
		keywordFile = new File(this.keywordFileName);
		scan = new Scanner(keywordFile);
		buildKeywordList();
	}
	
	private void buildKeywordList() {
		keywordList = new ArrayList<String>();
		while(scan.hasNext()) {
			keywordList.add(scan.nextLine());
		}
	}
	
	public void scanCheckpoints() {
		for(GsonCheckpoint checkpoint : gsonList) {
			String tags = pickTags(checkpoint);
			checkpoint.setAttribute("tags");
			checkpoint.setValue(tags);
		}
	}
	
	public String getJson() {
		Gson gson = new Gson();
		return gson.toJson(gsonList);
	}
	
	private String pickTags(GsonCheckpoint checkpoint) {
		String tagsFound = "";
		for(String keyword:keywordList) {
			if(checkpoint.getValue().toLowerCase().indexOf(keyword) > -1) {
				if(tagsFound.equals("")) {
					tagsFound = tagsFound + keyword;
				} else {
					tagsFound = tagsFound + ", " + keyword;
				}
			}
		}
		return tagsFound;
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
		
		public void setAttribute(String attribute) {
			this.attribute = attribute;
		}

		public String getValue() {
			return value;
		}
		
		public void setValue(String value) {
			this.value = value;
		}

		public String getKey() {
			return key;
		}
	}
}
