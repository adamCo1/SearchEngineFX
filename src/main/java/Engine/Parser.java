package Engine;

/**
 * Class for parsing text .
 * the Parser class first tokenize's the next tokens to be checked , and than the parser parses them by
 * a set of rules.
 *
 * the ReadFile class will feed this class with texts to be parsed , and the indexer class will take the results and
 * build its index with them.
 *
 */

import ReadFromWeb.City;
import Structures.Doc;
import Structures.Pair;
import Structures.TrieTree;
import sun.awt.Mutex;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;
import static ReadFromWeb.ReadFromWeb.allCities;

public class Parser {

    private ArrayList<String> termList ;
    private LinkedList<List> tokenListBuffers;

    private final String USDOLALRS = "U.S." , DOLLARS = "Dollars" ,_AND = "and" , _BETWEEN = "between";
    private final char DOLLAR_SIGN = '$' , PERCENT_SIGN = '%';
    private int startIndex = 0;
    byte currentOnTitle;
    Semaphore putBufferSem = new Semaphore(500) , getTakeBufferSem = new Semaphore(0);
    private Mutex mutex = new Mutex();
    private LinkedList<TreeMap<Integer,HashMap<Integer,Data>>> buffers ;
    private ParsingStrategies strategies;
    private HashMap<Integer,HashMap<Integer,Data>> termInfoMap;
    private HashMap<Integer,String> idTermMap; // ID - TERM map
    private HashMap<String, Pair> termIdMap ; //TERM - ID map
    private HashMap<String, City> cityDict;
    private ArrayList<String> docLangs;
    private int key ;
    private TrieTree stopWordsTrietree;
    private String currentText ;
    private boolean done ;
    private Stemmer porterStemmer;

    public Parser(String path) throws IOException {
        this.strategies = new ParsingStrategies();
        this.tokenListBuffers = new LinkedList<>();
        this.termList = new ArrayList<>();
        //this.buffers = new LinkedList<TreeMap<Integer, HashMap<Integer,Data>>>();
        this.stopWordsTrietree = new TrieTree();
        this.stopWordsTrietree.insertFromTextFile(path+"\\stoplist.txt");
        this.done = false;
        this.porterStemmer = new Stemmer();
        this.cityDict = new HashMap<>();
        this.docLangs = new ArrayList<>();
    }

    public void parse(Doc doc){

        if(doc.getDocLang().length()>0) {
            //System.out.println("Doc Lang is: "+doc.getDocLang());
            docLangs.add(doc.getDocLang());
        }
        if(doc.getDocOriginCity()!=null &&doc.getDocOriginCity().getName().length()>0)
            cityDict.put(doc.getDocOriginCity().getName().toUpperCase(),doc.getDocOriginCity());
        parseText(doc.getDocTitle(),(byte)1);

        parseText(doc.getDocText(),(byte)0);
    }

    public void parseText(String text , byte title) {


        this.currentText = text ;
        this.currentOnTitle = title;
        ArrayList<String> currentTokenList = new ArrayList<>();
        String[] words = new String[6];
        //int startindex = 0;
        boolean firstWordAfterPeriod = false;
        this.startIndex = 0;
        char current;
        int len = this.currentText.length();
        int position = 0 , goback = 0 ;

        // System.out.println("1");
        // System.out.println(docID);
        while (startIndex < len) {
            position += 1;
            try {


                words[0] = getNextWord(startIndex);

                if(words[0].equals(""))
                    continue;

                startIndex += words[0].length() + 1;//forward the pointer
                current = words[0].charAt(0);
                words[0] = strategies.partialStripSigns(words[0]);


                //OUR RULE - Word Word as one term
//                if(title == 0 && firstWordAfterPeriod == false && words[0].charAt(words[0].length()-1)!= '.' &&  current >= 64 && current < 90){
//                    words[1] = getNextWord(startIndex);
//                    if(startIndex < this.currentText.length() &&  words[1].charAt(0) >= 64 && words[1].charAt(0) < 90){
//                        startIndex += words[1].length();
//                        String term = (words[0] + " " + words[1]);
//                        currentTokenList.add(term);
//                        continue;
//                    }
//                }

                if(words[0].charAt(words[0].length()-1) == '.')
                    firstWordAfterPeriod = true;
                else
                    firstWordAfterPeriod = false;

                if(strategies.isFraction(words[0])){
                    currentTokenList.add(words[0]);
                    continue;
                }

                if (strategies.checkForNumber(words[0])) {// check for all possibilities for a number as the first word
                    words[1] = getNextWord(startIndex);
                    words[1] = strategies.partialStripSigns(words[1]);
                    if (strategies.isIndicator(words[1])) {//so its <number> <indicator> , check for dollars or U.S.
                        startIndex += words[1].length() + 1;
                        //words[1] = stripWordAndForwardIndex(words[1]);
                        words[2] = getNextWord(startIndex);
                        words[2] = strategies.partialStripSigns(words[2]);
                        if (words[2].equals(USDOLALRS)) {//so its <number> <indicator> <dollars> rule
                            startIndex += words[2].length() + 1;
                            //words[2] = stripWordAndForwardIndex(words[2]);
                            words[3] = getNextWord(startIndex);
                            words[3] = strategies.partialStripSigns(words[3]);
                            if (words[3].equals(DOLLARS)) {//so its <number> <indicator> <U.S.> <Dollars> rule
                                startIndex += words[3].length() + 1;
                                currentTokenList.add(strategies.handleUsDollars(words[0], words[1]));
                                continue;
                            } else {//so theres no dollars after 'U.S.' , its not relevant , <number> <indicator> rule
                                startIndex -= words[2].length() - 1;
                                currentTokenList.add(strategies.handleNumbersWithIndicators(words[0], words[1]));
                                continue;
                            }
                        } else if (words[2].equals(DOLLARS)) {//so its not "U.S" , <number> <indicator> rule
                            startIndex += words[2].length() + 1;
                            currentTokenList.add(strategies.handleDollarsignWithIndicator(words[0],words[1]));
                            continue;
                        } else {
                            currentTokenList.add(strategies.handleNumbersWithIndicatorsNoChange(words[0], words[1]));
                            continue;
                        }
                    }else if(strategies.isFraction(words[1])){
                        startIndex += words[1].length() + 1;
                        currentTokenList.add(words[0] + " " + words[1]);
                        continue;
                    }
                    if (words[1].toLowerCase().equals(DOLLARS)) {//its <number> <dollars> rule
                        startIndex += words[1].length() + 1;
                        currentTokenList.add(strategies.handlePricesWithoutIndicators(words[0]));
                        continue;

                    } else if (strategies.isPercent(words[1])) {//so its <number> < percent>
                        startIndex += words[1].length() + 1;
                        currentTokenList.add(words[0] + " " + words[1]);
                        continue;
                    } else if (strategies.checkForMonth(words[1])) {//so its a month , check for a number after
                        if (strategies.checkForMonthsRange(words[0])) {
                            startIndex += words[1].length() + 1;
                            words[2] = getNextWord(startIndex);
                            if(strategies.cheeckForYear(words[2])){
                                startIndex += words[2].length() + 1;
                                currentTokenList.add(strategies.handleMonthNumber(words[1],words[0])+"-"+words[2]);
                                continue;
                            }
                            currentTokenList.add(strategies.handleMonthNumber(words[1], words[0]));
                            continue;
                        }
                    } else {//so its a regular number
                        currentTokenList.add(this.strategies.handleNumbersAlone(words[0]));
                        continue;
                    }


                } else {//not a number , check if its a month
                    if (strategies.checkForMonth(words[0])) {//if it is a month , check if next word is the days DD number
                        words[1] = getNextWord(startIndex);
                        words[1] = strategies.partialStripSigns(words[1]);
                        startIndex += words[1].length() + 1;
                        if (strategies.checkForNumber(words[1])) {
                            if (strategies.cheeckForYear(words[1])) {
                                currentTokenList.add(strategies.handleYearMonth(words[0], words[1]));
                                continue;
                            } else if (strategies.checkForMonthsRange(words[1])) {
                                currentTokenList.add(strategies.handleMonthNumber(words[0], words[1]));
                                continue;
                            }
                        }
                        current = words[0].charAt(0);
                        if (current == '$') {//for if next words is a size indicator
                            words[1] = getNextWord(startIndex);
                            if (strategies.isIndicator(words[1])) {
                                startIndex += words[1].length() + 1;
                                currentTokenList.add(strategies.handleDollarsignWithIndicator(words[0].substring(1), words[1]));
                                continue;
                            } else {//so its '$' at the start with no indicator . <$number> rule
                                String str = words[0].substring(1);
                                if (strategies.checkForNumber(str)) {
                                    currentTokenList.add(words[0].substring(1));
                                    continue;
                                }
                            }
                        }//end of '$' check
                        else {//check for '%'
                            current = words[0].charAt(words[0].length() - 1);
                            if (current == '%') {// <number%> rule
                                currentTokenList.add(words[0]);
                                continue;
                            }
                        }


                    }
                }
                if (current == DOLLAR_SIGN) {
                    //for if next words is a size indicator
                    words[1] = getNextWord(startIndex);
                    words[1] = strategies.partialStripSigns(words[1]);
                    if (strategies.isIndicator(words[1])) {
                        startIndex += words[1].length() + 1;
                        currentTokenList.add(strategies.handleDollarsignWithIndicator(words[0].substring(1), words[1]));
                        continue;
                    } else {//so its '$' at the start with no indicator . <$number> rule
                        String str = words[0].substring(1);
                        if (strategies.checkForNumber(str)) {
                            currentTokenList.add(words[0].substring(1) + " Dollars");
                            continue;
                        }
                    }
                }//end of '$' check
                else {//check for '%'
                    if (words[0].length() > 1 && words[0].charAt(words[0].length() - 1) == PERCENT_SIGN) {// <number%> rule
                        currentTokenList.add(words[0]);
                        continue;
                    }
                }


                if(words[0].indexOf('-') != -1 && words[0].indexOf('-') != 0 && words[0].indexOf('-') != words[0].length()-1){
                    currentTokenList.add(words[0]);
                    continue;
                }


                if(words[0].equals(_BETWEEN)){//check for the range rules
                    words[1] = getNextWord(startIndex);
                    words[1] = strategies.partialStripSigns(words[1]);
                    if(strategies.checkForNumber(words[1])){
                        startIndex += words[1].length() + 1;//words[1] is number
                        goback += words[1].length() + 1;
                        words[2] = getNextWord(startIndex);
                        words[2] = strategies.partialStripSigns(words[2]);
                        if (strategies.isFraction(words[2])) {
                            startIndex += words[2].length() + 1;//words[2] is fraction
                            goback += words[2].length() + 1 ;
                            //words[2] = stripWordAndForwardIndex(words[2]);
                            words[3] = getNextWord(startIndex);
                            words[3] = strategies.partialStripSigns(words[3]);
                            if(words[3].equals(_AND)){
                                startIndex += words[3].length() + 1; //words[3] = 'and'
                                goback += words[3].length() + 1 ;
                                //words[3] = stripWordAndForwardIndex(words[3]);
                                words[4] = getNextWord(startIndex);
                                words[4] = strategies.partialStripSigns(words[4]);
                                if(strategies.checkForNumber(words[4])){
                                    startIndex += words[4].length() + 1 ; //words[4] = number . accept this no matter what
                                    goback += words[4].length() + 1;
                                    //words[4] = stripWordAndForwardIndex(words[4]);
                                    words[5] = getNextWord(startIndex);
                                    words[5] = strategies.partialStripSigns(words[5]);
                                    if(strategies.isIndicator(words[5])){
                                        startIndex += words[5].length() + 1 ;
                                        goback += words[5].length() + 1;
                                        //words[5] = stripWordAndForwardIndex(words[5]);
                                        currentTokenList.add(strategies.handleBetweenRangeFirstFractionSecondIndicator(words));
                                        continue;

                                    }else if(strategies.isFraction(words[5])){
                                        startIndex += words[5].length() + 1 ;
                                        goback += words[5].length()+1;
                                        //words[5] = stripWordAndForwardIndex(words[5]);
                                        currentTokenList.add(strategies.handleBetweenRangeFirstFractionSecondFraction(words));
                                        continue;
                                    }else{//so the last number alone
                                        currentTokenList.add(strategies.handleBetweenRangeFirstFractionSecondAlone(words));
                                    }
                                }//if after 'and' its not a number , its not the range rule .
                            }//same for if its not 'and'

                            //so after the first number its not a fraction . check for an indicator
                        }else if(strategies.isIndicator(words[2])){
                            startIndex += words[2].length() + 1 ; //words[2] is an indicator
                            goback += words[2].length()+1;
                            //words[2] = stripWordAndForwardIndex(words[2]);
                            words[3] = getNextWord(startIndex);
                            words[3] = strategies.partialStripSigns(words[3]);
                            if(words[3].equals(_AND)){
                                startIndex += words[3].length() + 1; //words[3] = 'and'
                                goback += words[3].length() + 1;
                                //words[3] = stripWordAndForwardIndex(words[3]);
                                words[4] = getNextWord(startIndex);
                                words[4] = strategies.partialStripSigns(words[4]);
                                if(strategies.checkForNumber(words[4])){
                                    startIndex += words[4].length() + 1 ; //words[4] = number . accept this no matter what
                                    goback += words[4].length() + 1;
                                    //words[4] = stripWordAndForwardIndex(words[4]);
                                    words[5] = getNextWord(startIndex);
                                    words[5] = strategies.partialStripSigns(words[5]);
                                    if(strategies.isIndicator(words[5])){
                                        startIndex += words[5].length() + 1 ;
                                        goback += words[5].length() + 1;
                                        //words[5] = stripWordAndForwardIndex(words[5]);
                                        currentTokenList.add(strategies.handleBetweenRangeFirstIndicatorSecondIndicator(words));
                                        continue;
                                    }else if(strategies.isFraction(words[5])){//so at the end its just a number alone
                                        startIndex += words[5].length() + 1 ;
                                        goback += words[5].length() + 1;
                                        //words[5] = stripWordAndForwardIndex(words[5]);
                                        currentTokenList.add(strategies.handleBetweenRangeFirstIndicatorSecondFraction(words));
                                        continue;
                                    }else{//so both alone
                                        currentTokenList.add(strategies.handleBetweenRangeFirstIndicatorSecondAlone(words));
                                        continue;
                                    }
                                }//if after 'and' its not a number , its not the range rule .
                            }//same for if its not 'and'

                        }else if(words[2].equals(_AND)){//so words[1] is a number alone
                            startIndex += words[2].length() + 1 ;
                            goback += words[2].length()+1;
                            //words[2] = stripWordAndForwardIndex(words[2]);
                            words[3] = getNextWord(startIndex);
                            words[3] = strategies.partialStripSigns(words[3]);
                            if(strategies.checkForNumber(words[3])){
                                startIndex += words[3].length() + 1 ;
                                goback+= words[3].length() +1;
                                //words[3] = stripWordAndForwardIndex(words[3]);
                                words[4] = getNextWord(startIndex);
                                words[4] = strategies.partialStripSigns(words[4]);
                                if(strategies.isIndicator(words[4])){
                                    startIndex += words[4].length() + 1 ;
                                    goback += words[4].length() + 1;
                                    //words[4] = stripWordAndForwardIndex(words[4]);
                                    currentTokenList.add(strategies.handleBetweenRangeFirstAloneSecondIndicator(words));
                                    continue;
                                }else if(strategies.isFraction(words[4])){
                                    startIndex += words[4].length() + 1 ;
                                    goback += words[4].length() + 1;
                                    // words[4] = stripWordAndForwardIndex(words[4]);
                                    currentTokenList.add(strategies.handleBetweenRangeFirstAloneSecondFraction(words));
                                    continue;
                                }else{
                                    currentTokenList.add(strategies.handleBetweenRangeBothAlone(words));
                                    continue;
                                }
                            }//not a number , not the rule

                        }
                    }//so second is not a number. its not the rule
                }//end of between rule

                startIndex -= goback;
                goback = 0;

                words[0] = this.strategies.stripSigns(words[0]);
/**
 //check for <word> and <word>
 words[1] = getNextWord(startIndex);
 if(words[1].equals("and")){
 startIndex += words[1].length() + 1 ;
 words[2] = getNextWord(startIndex);
 if(!words[2].equals("")) {
 startIndex += words[2].length() + 1;
 currentTokenList.add(words[0]);
 currentTokenList.add(words[2]);
 currentTokenList.add(words[0] + " " + words[2]);
 continue;
 }
 }
 **/

                if(words[0].equals("") || isStopWord(words[0].toLowerCase()))
                    continue;


                String cityTerm ="";
                int numOfWordsInCityName = 0;
                //check if the term is has Capitals
                if(words[0].charAt(0) >= 65 && words[0].charAt(0) <=90) {
                    //check if the term is a city
                    if (allCities.containsKey(words[0].toUpperCase())) {
                        cityTerm = words[0].toUpperCase();
                        //get the number of words inside the city name
                        numOfWordsInCityName = allCities.get(cityTerm).getNumberOfWordsInName();
                        //check if the city name is more than one word
                        if(numOfWordsInCityName > 1) {
                            //check if the next words are the excepted words of the city name
                            for (int i = 1; i < numOfWordsInCityName; i++) {
                                words[i] = getNextWord(startIndex);
                                if (words[i].charAt(0) >= 65 && words[i].charAt(0) <= 90) {
                                    cityTerm += " " + words[i].toUpperCase();
                                    if(!allCities.containsKey(cityTerm)) {
                                        break;
                                    }

                                    else {
                                        startIndex+=1+words[i].length();
                                        numOfWordsInCityName = allCities.get(cityTerm).getNumberOfWordsInName();
                                    }
                                }

                            }
                        }
                        if(allCities.containsKey(cityTerm)){
                            currentTokenList.add(cityTerm);
                            continue;
                        }



                    }
                }
//                        System.out.println(words[0]);




                if(words[0].length() > 1) {//OUR RULE - 1 char rule
                    if(noUpper(words[0]))
                        currentTokenList.add(this.porterStemmer.stripAffixes(words[0]));
                    else
                        currentTokenList.add(words[0]);

                }

            }catch(Exception e){
                //  System.out.println(text.length());
                // System.out.println(startIndex);
            }

        }//end while
       // System.out.println(currentTokenList);
        storeBuffer(currentTokenList);
        currentTokenList = new ArrayList<>();
    }

    private boolean noUpper(String word){

        return !(word.charAt(0)>=64 && word.charAt(0) < 90);
    }

    private boolean onlyFirstCharUpper(String word){

        return word.charAt(0) >= 64 && word.charAt(0) < 90;
    }

    /**
     * "cut" the next word to check from the given text
     * @param startIndex starting indx on the text
     * @return the next word to be checked
     */
    private String getNextWord(int startIndex){

        int i = startIndex ;

        if(i >= this.currentText.length())
            return "";

        while(currentText.charAt(i) == ' ' || currentText.charAt(i) == 10)
            i+= 1 ;

        try {
            char current = currentText.charAt(i);
            while(true){
                if(current == ' ' || current == 10 || current == 9  ) {
                    break;
                }
                i+=1;
                if(i >= currentText.length() || current == '|')
                    break;
                current = currentText.charAt(i);

            }

            //String ans = currentText.substring(startIndex,i);
            //return ans ;
            return this.currentText.substring(startIndex,i);

        }catch (Exception e){
            e.printStackTrace();
            return "";
        }

    }

    private boolean isStopWord(String word){
        try {
            char c = word.charAt(0);
            return stopWordsTrietree.search(word);
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public void setDone(boolean done){
        this.done = done;
        this.getTakeBufferSem.release();
    }

    public boolean isDone(){
        return this.done;
    }

    private ArrayList<String> deepCopy(ArrayList<String> toCopy){
        ArrayList<String> copy = new ArrayList<>();

        for (String str:
                toCopy) {
            copy.add(str);
        }

        return copy;
    }

    /**
     * add a buffer to the buffer list . to be taken by the indexer
     * @param buffer buffer to be saved
     */
    private void storeBuffer(ArrayList<String> buffer) {
        try {
            this.putBufferSem.acquire();
            mutex.lock();
            this.tokenListBuffers.addLast(deepCopy(buffer));
            this.getTakeBufferSem.release();
            mutex.unlock();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * get a buffer of termID - info map
     * @return hashmap<ID,DATA> containing all the data on a term in the text
     */
    public ArrayList<String> getBuffer(){
        try {
            this.getTakeBufferSem.acquire();
            mutex.lock();
            ArrayList<String> buff = (ArrayList<String>)this.tokenListBuffers.poll();
            this.putBufferSem.release();
            mutex.unlock();
            return buff;
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        return null;
    }


    public void setLookUpTermDict(HashMap<String, Pair> termIdMap){
        this.termIdMap = termIdMap;
    }


}