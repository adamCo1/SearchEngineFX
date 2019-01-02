package IO;

import Structures.Pair;
import Structures.TrieTree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


/**
 * A class used to handle semantics.
 * given a word check for her vector in a glove file and calculate similarity between it and other words
 * bring back the  3 most relevant words
 */
public class SemanticHandler {

    /**
     * wordVectors collection as a hashmap
     * a stopwords collection as a trie tree
     * the corpus and glove file paths
     */
    public static HashMap<String,ArrayList<Double>> wordsVectors = new HashMap<>();
    private static TrieTree sw;
    //corpus path is set on engine run or load
    public static String corpusPath;
    public static String gloveFile;
    public static boolean includeSemantics=false;


    /**
     * this will read  from the glove file and load the word vectors into wordsVectors hashmap
     *
     */

    public static void readGloveFile(){
        Long startTime = System.currentTimeMillis();

        try {
            if(sw == null)
                sw = new TrieTree();
            sw.insertFromTextFile(corpusPath+"\\stop_words.txt");
            try (BufferedReader br = new BufferedReader(new FileReader(new File(corpusPath+"\\"+gloveFile)))) {
                String line;


                while ((line = br.readLine()) != null) {
                    String [] vector = line.split(" ");
                    String  word = vector[0];
                    if(sw.search(word) || word.length()<2)
                        continue;
                    ArrayList<Double>vectorVals = new ArrayList<>();
                    for(int  i = 1 ; i < vector.length ; i ++){
                            vectorVals.add(Double.parseDouble(vector[i]));
                    }
                    wordsVectors.put(word,vectorVals);



                }
                Long endTime = System.currentTimeMillis();
                System.out.println("time to read all gloves and process it: "+(endTime-startTime)/1000+" seconds");
                sw=null;

            }



        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("could not load the glove file");
        }

    }

    /**
     *this will return all the related words for a given query (3 for each word)
     * @param originalQueryWords list of the query words
     * @return realted words for a given query
     */


    public static ArrayList<String> getRelatedWords(List<String> originalQueryWords){
        ArrayList<String> ans = new ArrayList<>();
        for(String word:originalQueryWords){
            ArrayList<String>wordRelatedWords = getThreeBestMatches(word);
            for(int i = 0 ; i < wordRelatedWords.size() ; i++)
                ans.add(wordRelatedWords.get(i));

        }
        return ans;

    }


    /**
     *return words this maximum simalirty over all the corpus
     * @param unProcessedWord 1 query word
     * @return  3 related words by GLOVE model
     */
    public  static ArrayList<String> getThreeBestMatches(String unProcessedWord){
        String word = unProcessedWord.toLowerCase();
        TreeSet<Pair<Double,String>> bestMatches = new TreeSet<>();
        if(wordsVectors.get(word)==null)
            return new ArrayList<>();

        for(String key:wordsVectors.keySet()){
            if(key.length()<=2 || key.equals(word) || key.equals(word+'s'))
                continue;

            bestMatches.add(new Pair<>(getSimilarity(word,key),key));

            //remove the word with the min similarity
            if(bestMatches.size()>3) {
                Pair<Double,String> currLastElement =null;
                int i = 0;
                Iterator<Pair<Double,String> >it = bestMatches.iterator();
                while(i<4) {
                    currLastElement = it.next();
                    i++;
                }
                bestMatches.remove(currLastElement);
            }

        }

        ArrayList<String>ans = new ArrayList<>();
        Iterator<Pair<Double,String> >it = bestMatches.iterator();
        String w1 = it.next().getSecondValue();
        String w2 = it.next().getSecondValue();
        String w3 = it.next().getSecondValue();

        ans.add(w1) ;
        ans.add(w2) ;
        ans.add(w3) ;

        return ans;

    }


    /**
     * calcualting  similarity
     * @param unprocessedWord
     * @param unprocessedOtherWord
     * @return
     */
    private static double getSimilarity(String unprocessedWord,String unprocessedOtherWord){
        String word = unprocessedWord.toLowerCase();
        String otherWord = unprocessedOtherWord.toLowerCase();
        if(wordsVectors.get(word)==null || wordsVectors.get(otherWord) == null)
            return 0;
        ArrayList<Double>wordVector = wordsVectors.get(word);
        ArrayList<Double>otherWordVector = wordsVectors.get(otherWord);
        double score = 0;
        for(int i =0 ; i < Math.min(wordVector.size(),otherWordVector.size());i++){
            score+=wordVector.get(i)*otherWordVector.get(i);
        }
        return score;

    }

    /**
     * use it to clear the memory
     */
    public static void clearWordsVecs(){
        wordsVectors = new HashMap<>();
    }





//    public static void main(String []args){
//        readGloveFile("Stanford_glove.6B.50d.txt");
//        ArrayList<String> q = new ArrayList<>();
//        q.add("Falkland");
//        q.add("petroleum");
//        q.add( "exploration");
//        ArrayList<String> ans = getRelatedWords(q);
//        System.out.println("for word: israel "+ans.toString());
//
//    }
}
