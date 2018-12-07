package Indexer;

import java.util.LinkedList;

public class BufferDataString extends ABufferData {

    /**
     * key - docID
     * value - Arraylist , where each odd element is docID and each even element is the info on it
     */

    private LinkedList<byte[]> data ;
    private String fullName , currency, population , countryName;
    private int termID;

    public BufferDataString(int termID, byte[] docID , byte[] onTitle, byte[] info , String fullName
    , String countryName, String currency , String population){
        super(termID,docID,onTitle,info);
        this.fullName = fullName;
        this.countryName = countryName;
        this.currency = currency;
        this.population = population;
        this.termID = termID;
    }


    public String getPopulation() {
        return population;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getFullName() {
        return fullName;
    }
}


