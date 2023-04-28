import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

// Final Project
// Tal Mordokh, Ezra Ford, Chris Dennis

public class ExtractSummary {
    protected HashMap<String, Double> wordCounts; // aka occurrence frequency of each word
    protected HashMap<String, Double> weightedOccFreq; // normalized occurrence frequency
    protected ArrayList<String> stopList;
    protected ArrayList<Double> scoresList; // stores the scores for each sentence in the original text
    protected int numSentences; // number of sentences in the original text
    protected String inputFile; // input filepath
    protected String outputFile; // output filepath
    protected String stopListFile; // stop list filepath
    protected ArrayList<String> abbList; // abbreviation list

    /**
     * Constructor
     * @param inputPath input filepath
     * @param outputPath output filepath
     * @param stopListPath stop list filepath
     */
    public ExtractSummary (String inputPath, String outputPath, String stopListPath, String abbFile) throws IOException {
        // Make filepaths accessible to all methods
        inputFile = inputPath;
        outputFile = outputPath;
        stopListFile = stopListPath;

        // Initialize data structures
        wordCounts = new HashMap<>();
        weightedOccFreq = new HashMap<>();
        stopList = new ArrayList<>();
        abbList = new ArrayList<>();

        // Read stop words from file and add them to stopList
        File file = new File(stopListFile);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st = br.readLine();

        while (st != null) {
            String stopWord = st.toLowerCase();
            stopList.add(stopWord);

            st = br.readLine();
        }

        file = new File(abbFile);
        br = new BufferedReader(new FileReader(file));
        st = br.readLine();
        while (st != null) {
            String stopWord = st.toLowerCase();
            abbList.add(stopWord);

            st = br.readLine();
        }

        /* Split the article into sentences, then preprocess the text by
        lowercasing, removing stop words, numbers, punctuation, and other
        special characters from the sentences */
        file = new File(inputFile);
        br = new BufferedReader(new FileReader(file));

        // goes over the text and builds a string
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append(" ");
        }
        String text = sb.toString();
        text = text.toLowerCase();

        // splits the long string to according to white spaces
        String[] splitText = text.split("\\s+");

        StringBuilder updatedSb = new StringBuilder();

        // removes all stop list words and removes punctuation marks from abbreviations
        for (String word : splitText){
            if (stopList.contains(word)) continue;
            if (abbList.contains(word)){
                updatedSb.append(word.replaceAll("\\.","")).append(" ");
            } else if (!isAlpha(word)) {
                if (word.charAt(word.length() - 1) == '.') {
                    updatedSb.append(word.replaceAll("\\.","")).append(" . ");
                } else {
                    updatedSb.append(word.replaceAll("\\.","")).append(" ");
                }
            } else {
                updatedSb.append(word).append(" ");
            }
        }

        String updatedText = updatedSb.toString();

        String newLinesAdded = updatedText.replaceAll("(?<=[.!?])\\s+", "$0\n");

        // puts a space before and after every punctuation mark
        newLinesAdded = newLinesAdded.replaceAll("\\s*.([/:',—+=@#$%^&*()\\[\\]{}><~`\"]+)\\s*", " $0 ");

        String [] sentences = newLinesAdded.split("\n");

        for (String s : sentences){
            System.out.println(s);
        }

        for (String sentence : sentences) {
            numSentences++;

            String[] words = sentence.split("\\s+");
            ArrayList<String> rawWords = new ArrayList<>();

            // Preprocess sentence
            for (String word : words) {
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

        calculateScores();

        System.out.println(scoresList);

        getTopSentences();
    }

    public void calculateScores() throws IOException {
        // Initialize scoresList with length equal to the number of sentences in the original text
        scoresList = new ArrayList<>(numSentences);

        /* Split the article into sentences, then preprocess the text by
        lowercasing, removing stop words, numbers, punctuation, and other
        special characters from the sentences */
        File file = new File(inputFile);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st = br.readLine();

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

            double sum = 0.0;
            // Iterate over each word in the sentence and process it
            for (int j = 0; j < rawWords.size(); j++) {
                double jWordScore = weightedOccFreq.get(rawWords.get(j));
                sum += jWordScore;
            }

            sum /= (double) rawWords.size();

            scoresList.add(sum);

            st = br.readLine();
        }
    }

    public void getTopSentences() throws IOException {
        ArrayList<Integer> indexList = new ArrayList<>(numSentences);

        for (int i = 0; i < numSentences; i++) {
            indexList.add(i);
        }

        System.out.println(indexList);

        modQuickSort(scoresList, 0, scoresList.size() - 1, indexList);

        System.out.println(scoresList);
        System.out.println(indexList);

        // Determine the number of sentences to print (adding two handles the case when there are less than 5 sentences)
        int numToPrint = numSentences / 5 + 2;

        System.out.println(numToPrint);

        ArrayList<Integer> topIndexes = new ArrayList<>(numToPrint);

        for (int j = numSentences - 1; j >= numSentences - numToPrint; j--) {
            topIndexes.add(indexList.get(j));
        }

        System.out.println(topIndexes);

        File file = new File(inputFile);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st = br.readLine();
        int sentenceNum = 0; // "index" of the sentence in the original text

        while (st != null) {
            if (topIndexes.contains(sentenceNum)) {
                System.out.println(st);
            }

            st = br.readLine();
            sentenceNum++;
        }
    }

    /**
     Determines if a given string consists only of letters.
     @param word the string to be checked
     @return true if the string consists only of letters, false otherwise
     */
    public boolean isAlpha(String word) {
        return word.matches("[a-zA-Z]+");
    }

    /**
     * Sorts an ArrayList of Doubles in non-decreasing order using the modified quicksort algorithm,
     * and updates a corresponding ArrayList of Integers to maintain their relation.
     *
     * @param arr the ArrayList of Doubles to be sorted
     * @param low the starting index of the sublist to be sorted
     * @param high the ending index of the sublist to be sorted
     * @param wordArr the ArrayList of Integers containing the corresponding words
     */
    public static void modQuickSort(ArrayList<Double> arr, int low, int high, ArrayList<Integer> wordArr) {
        if (low < high) {
            // Choose a pivot element and partition the list
            int pivotIndex = partition(arr, low, high, wordArr);
            // Recursively sort the left and right sublists
            modQuickSort(arr, low, pivotIndex - 1, wordArr);
            modQuickSort(arr, pivotIndex + 1, high, wordArr);
        }
    }


    /**
     * Partitions an ArrayList of Doubles around a pivot element, and updates a corresponding
     * ArrayList of Integers to maintain their relation.
     *
     * @param arr the ArrayList of Doubles to be partitioned
     * @param low the starting index of the sublist to be partitioned
     * @param high the ending index of the sublist to be partitioned
     * @param wordArr the ArrayList of Integers corresponding to the sentence number
     * @return the index of the pivot element after partitioning
     */
    public static int partition(ArrayList<Double> arr, int low, int high, ArrayList<Integer> wordArr) {
        // Choose the pivot element to be the last element in the sublist
        double pivot = arr.get(high);
        // i is the index of the last element in the left sublist
        int i = low - 1;
        for (int j = low; j < high; j++) {
            // If the current element is less than or equal to the pivot, move it to the left sublist
            if (arr.get(j) <= pivot) {
                i++;
                // Swap the current element with the first element in the right sublist
                double temp = arr.get(i);
                arr.set(i, arr.get(j));
                arr.set(j, temp);

                // Update the corresponding word array
                int temp2 = wordArr.get(i);
                wordArr.set(i, wordArr.get(j));
                wordArr.set(j, temp2);
            }
        }
        // Swap the pivot element with the first element in the right sublist
        double temp = arr.get(i + 1);
        arr.set(i + 1, arr.get(high));
        arr.set(high, temp);

        // Update the corresponding word array
        int temp2 = wordArr.get(i + 1);
        wordArr.set(i + 1, wordArr.get(high));
        wordArr.set(high, temp2);

        // Return the index of the pivot element after partitioning
        return i + 1;
    }


    public static void main(String[] args) throws IOException {
//        String inputFile = "/Users/ezraford/Desktop/School/CS 159/Final-Project/data/input.txt";
//        String outputFile = "/Users/ezraford/Desktop/School/CS 159/Final-Project/data/output.txt";
//        String stopListFile = "/Users/ezraford/Desktop/School/CS 159/Final-Project/data/stoplist.txt";
//        String abFile =

        String inputFile = "/Users/talmordoch/Library/Mobile Documents/com~apple~CloudDocs/Final-Project/data/input.txt";
        String outputFile = "/Users/talmordoch/Library/Mobile Documents/com~apple~CloudDocs/Final-Project/data/output.txt";
        String stopListFile = "/Users/talmordoch/Library/Mobile Documents/com~apple~CloudDocs/Final-Project/data/stoplist.txt";
        String abbFile = "/Users/talmordoch/Library/Mobile Documents/com~apple~CloudDocs/Final-Project/data/abbreviationList.txt";

        ExtractSummary sum = new ExtractSummary(inputFile, outputFile, stopListFile, abbFile);
//        String test = "/Users/talmordoch/Library/Mobile Documents/com~apple~CloudDocs/Final-Project/data/dataExample.txt";
//        String[] check = preProcess(test);

    }
}
