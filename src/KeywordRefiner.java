import java.io.*;
import java.util.*;

/**
 * @author linanqiu
 * @file_name KeywordRefiner.java
 * 
 * Runs through a random list of Keywords and 
 */
public class KeywordRefiner {

	public static final String FILENAME = "keyword.txt";
	private static File input;
	private static File output;
	private static PrintWriter print;
	private static ArrayList<String> processed;

	public static void main(String[] args) throws FileNotFoundException {
		input = new File("keyword.txt");
		output = new File("keyword_processed.txt");
		processed = new ArrayList<String>();

		Scanner scan = new Scanner(input);

		while (scan.hasNext()) {
			String line = scan.nextLine();
			line = line.replaceAll(",", " ");
			line = line.replaceAll("-", " ");
			line = line.replaceAll("[^A-Za-z0-9\\s]", "");
			line = line.toLowerCase();
			boolean duplicate = false;
			for (String processedEntries : processed) {
				if (processedEntries.equals(line)) {
					duplicate = true;
				}
			}
			if (line.length() > 1 && duplicate == false) {
				processed.add(line);
			}
		}
		Collections.sort(processed);
		System.out.println(processed);

		print = new PrintWriter(output);
		for (String line : processed) {
			print.println(line);
		}
		print.close();
	}
}
