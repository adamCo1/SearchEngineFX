package Indexer;

import java.util.LinkedList;

/**
 * buffer used for holding information on cities . the strings are expected to be in in the format of bytes[]
 * using the father's functions and getters and setters
 */

public class BufferDataString extends ABufferData {

    //private LinkedList<byte[]> data ;
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


