package ReadFromWeb;


import Structures.Pair;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class is used to retrive and hold needed information about the documents origin cities.
 *
 */

public class ReadFromWeb {

    /**
     * allCities hashmap is used to hold the cities info
     * entries are cities first word in name.
     */
    public static HashMap<String,City> allCities = new HashMap<String, City>();
    public static HashMap<String,Pair<String,Integer>> countrySet = new HashMap<>();
    public static HashSet<String>countries = new HashSet<>();

    public static void reset(){

    }

    /**
     * get all city info from a given API url.
     * store this info inside allCities hashmap
     *
     */
    public static void getCities() {
        URL url = null;
        String APIAnswer = "";
        InputStream is = null;
        try {
            url = new URL("https://restcountries.eu/rest/v2/all");
            is = url.openStream();
        } catch (Exception e) {
            return;

        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String line = "";
            while ((line = br.readLine()) != null) {
                APIAnswer += line;
            }
        } catch (MalformedURLException e) {
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(APIAnswer);

        JsonArray allCountries = jsonTree.getAsJsonArray();

        //get the city full information
        for (JsonElement c : allCountries) {
            String[] capital = ((JsonObject) c).get("capital").toString().toUpperCase().replaceAll("[,'\".]","").split(" ");
            String fullCountryName = ((JsonObject) c).get("name").toString().toUpperCase().replace("[,'\".]","");
            String fullCityName = ((JsonObject) c).get("capital").toString().toUpperCase().replaceAll("[,'\".]","");
            long lPopultion = ((JsonObject) c).get("population").getAsLong();
            String popultion = longFormat(lPopultion);
            int parPos=-1;
            int endParPos=-1;
            int numOfWordsInCountryName=1;
            String tempFullCountryName="";
            String [] brokenCountry=null;
            if(fullCountryName.contains("(")) {
                parPos = fullCountryName.indexOf("(");
                endParPos = fullCountryName.indexOf(")");
                tempFullCountryName= fullCountryName.substring(0,parPos-1)+fullCountryName.substring(endParPos+1);

            }
            else{
                tempFullCountryName = fullCountryName;
            }
            brokenCountry = tempFullCountryName.split(" ");
            if(brokenCountry.length>1) {
                numOfWordsInCountryName = brokenCountry.length;
                tempFullCountryName = brokenCountry[0];
            }



            //now transform the number
            String currency = ((JsonObject) (((JsonObject) c).getAsJsonArray("currencies").get(0))).get("code").toString();
            if(capital != null && capital[0].length()>0) {

                String dictEntry = capital[0];

                allCities.put(dictEntry, new City(fullCityName, fullCountryName, currency, popultion, capital.length));
                for(int i = 1 ; i < capital.length ; i++){

                    dictEntry+=" "+capital[i];
                    allCities.put(dictEntry, new City(fullCityName, fullCountryName, currency, popultion, capital.length));

                }



                countrySet.put(tempFullCountryName,new Pair<>(fullCountryName,numOfWordsInCountryName));
               // System.out.println("added: "+tempFullCountryName);
                for(int i =1; i<numOfWordsInCountryName;i++){
                    tempFullCountryName+=" " + brokenCountry[i];
                    countrySet.put(tempFullCountryName,new Pair<>(fullCountryName,numOfWordsInCountryName));
                   // System.out.println("added: "+tempFullCountryName);
                }


            }
        }



    }

    /**
     * used to transform long numbers inside the requested KMB format
     * @param number a number to format
     * @return  a formated number as String
     */
    public static String longFormat(long number){
        double formatedValue = 0;
        String ans="";
        if(number >= 1000000000){
            formatedValue = number/1000000000.0;
            formatedValue = Math.round(formatedValue * 100.0) / 100.0;
            ans = formatedValue+"B";
        }
        else if(number >= 1000000){
            formatedValue = number/1000000.0;
            formatedValue = Math.round(formatedValue * 100.0) / 100.0;
            ans = formatedValue+"M";
        }
        else {
            formatedValue = number/1000.0;
            formatedValue = Math.round(formatedValue * 100.0) / 100.0;
            ans = formatedValue+"K";
        }
        return ans;

    }



}



