import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

// Final Project
// Tal Mordokh, Ezra Ford, Chris Dennis

public class ExtractSummary {
    protected HashMap<String, Double> wordCounts; // aka occurrence frequency of each word
    protected HashMap<String, Double> weightedOccFreq; // normalized occurrence frequency
    protected ArrayList<String> stopList;

    /**
     * Constructor
     * @param inputFile input filepath
     * @param outputFile output filepath
     * @param stopListFile stopList filepath
     */
    public ExtractSummary (String inputFile, String outputFile, String stopListFile) throws IOException {
        // Initialize data structures
        wordCounts = new HashMap<>();
        weightedOccFreq = new HashMap<>();
        stopList = new ArrayList<>();

        // Read stop words from file and add them to stopList
        File file = new File(stopListFile);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st = br.readLine();

        while (st != null) {
            String stopWord = st.toLowerCase();
            stopList.add(stopWord);

            st = br.readLine();
        }

        /* Split the article into sentences, then preprocess the text by
        lowercasing, removing stop words, numbers, punctuation, and other
        special characters from the sentences */
        file = new File(inputFile);
        br = new BufferedReader(new FileReader(file));
        st = br.readLine();

        while (st != null) {
            String stLower = st.toLowerCase();

            String[] words = stLower.split("\\s+");
            ArrayList<String> rawWords = new ArrayList<String>();

            // Preprocess sentence
            for (String word : words) {
                // Skip stop words
                if (stopList.contains(word)) {
                    continue;
                }

                // Add all words that are exclusively letters
                if (isAlpha(word)) {
                    rawWords.add(word);
                }
            }

            // Iterate over each word in the sentence and process it
            for (int j = 0; j < rawWords.size(); j++) {
                String jWord = rawWords.get(j);


                // Increment wordCount
                wordCounts.put(jWord, wordCounts.getOrDefault(jWord, 0.0) + 1);
            }

            st = br.readLine();
        }

        System.out.println(wordCounts);

        // Find frequency of most recurrent word
        double maxValue = 0;
        for (String word : wordCounts.keySet()) {
            maxValue = Math.max(maxValue, wordCounts.get(word));
        }

        // Normalize word occurrence frequencies
        for (String word : wordCounts.keySet()) {
            // Divide each count by the frequency of the most recurrent word
            weightedOccFreq.put(word, wordCounts.get(word)/maxValue);
        }

        System.out.println(weightedOccFreq);


    }

    /**
     Determines if a given string consists only of letters.
     @param word the string to be checked
     @return true if the string consists only of letters, false otherwise
     */
    public boolean isAlpha(String word) {
        return word.matches("[a-zA-Z]+");
    }

    public static void main(String[] args) throws IOException {
        String inputFile = "/Users/ezraford/Desktop/School/CS 159/Final-Project/data/input.txt";
        String outputFile = "/Users/ezraford/Desktop/School/CS 159/Final-Project/data/output.txt";
        String stopListFile = "/Users/ezraford/Desktop/School/CS 159/Final-Project/data/stoplist.txt";

        ExtractSummary sum = new ExtractSummary(inputFile, outputFile, stopListFile);
    }
}
