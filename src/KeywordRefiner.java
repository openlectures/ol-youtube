
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author linanqiu
 * @file_name KeywordRefiner.java
 * <p/>
 * Runs through a random list of Keywords and formats them. Input must be
 * keyword.txt that contains keywords separated by breaks. For an example, look
 * in the root directory of this repostiory.
 */
public class KeywordRefiner {

    public static final String FILENAME = "keyword.txt";
    private static final Pattern spacePattern = Pattern.compile("[,-]");
    private static final Pattern blankPattern = Pattern.compile("[^A-Za-z0-9\\s]");
    private static ArrayList<String> processed;
    private static Set<String> duplicate;

    /**
     * Runs through the list, replaces commas and other weird symbols, replacing
     * everything that are not alphanumeric and whitespaces, and deletes
     * duplicates.
     * <p/>
     * Finally, sorts them alphabetically. Useful for taking a list of keywords
     * for a certain subject and formatting them quickly.
     * <p/>
     * @param args
     * @throws IOException If there is an error reading/writing the file.
     */
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(FILENAME));
        processed = new ArrayList<String>();
        duplicate = new HashSet<String>();

        String line;
        while ((line = reader.readLine()) != null) {

            line = regexReplace(line, spacePattern, " ");
            line = regexReplace(line, blankPattern, "");
            line = line.toLowerCase();

            //Check for duplicates using hash map
            if (line.length() > 1 && !duplicate.contains(line)) {
                duplicate.add(line);
                processed.add(line);
            }
        }

        reader.close();

        Collections.sort(processed);
        System.out.println(processed);

        BufferedWriter writer = new BufferedWriter(new FileWriter("keyword_processed.txt"));

        for (String word : processed) {
            writer.write(word);
            writer.newLine();
        }
        writer.close();
    }

    /**
     * Replaces regular expressions found in the text String with the
     * replacement as specified.
     * <p/>
     * This function uses a
     * <code>StringBuffer</code> to search through and replace the text.
     * <p/>
     * @param text        The original text.
     * @param regex       The regular expression to search for.
     * @param replacement The replacement String.
     * @return The processed text String.
     */
    private static String regexReplace(String text, Pattern regex, String replacement) {
        Matcher matcher = regex.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
