package ReadFromWeb;


import com.google.gson.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.HashMap;

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
            String fullCountryName = ((JsonObject) c).get("name").toString().toUpperCase().replace("\"","");
            String fullCityName = ((JsonObject) c).get("capital").toString().toUpperCase().replaceAll("[,'\".]","");
            long lPopultion = ((JsonObject) c).get("population").getAsLong();
            String popultion = longFormat(lPopultion);


            //now transform the number
            String currency = ((JsonObject) (((JsonObject) c).getAsJsonArray("currencies").get(0))).get("code").toString();
            if(capital != null && capital[0].length()>0) {

                String dictEntry = capital[0];

                allCities.put(dictEntry, new City(fullCityName, fullCountryName, currency, popultion, capital.length));
                for(int i = 1 ; i < capital.length ; i++){

                    dictEntry+=" "+capital[i];
                    allCities.put(dictEntry, new City(fullCityName, fullCountryName, currency, popultion, capital.length));
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



