package Parser;

/**
 * A class to be used by the Parser class . implements alot of functions to check certain rules that the Parser has
 * to check.
 */

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;

public class ParsingStrategies {

    private HashMap<String,Integer> monthsMap;
    private HashMap<String,String> indicatorsDict ;
    private HashMap<Integer,String> normalizedMonths;
    private HashSet<Character> cleanSigns , partialCleanDict;
    private HashSet<String> qualifiersSet;
    private DecimalFormat decimalFormat;

    public ParsingStrategies(){
        this.decimalFormat = new DecimalFormat("#.#####");
        initializeDictionaries();
    }


    public String handleYearMonth(String month , String year){

        Integer monthnum = this.monthsMap.get(month.toLowerCase());
        return year + "-" +this.normalizedMonths.get(monthnum.intValue());
    }

    /**
     * check if its a year in the formal yyyy
     * @param year string
     * @return true if its a year
     */
    public boolean cheeckForYear(String year){

        try{
            Integer num = stringToInt(year);
            return num.intValue() >= 1000 && num.intValue() < 10000;
        }catch (Exception e){
            return false;
        }
    }

    /**
     * initialize all the needed dictionaries for this class to be affective
     */
    private void initializeDictionaries(){

        this.monthsMap = new HashMap<String, Integer>(){
            {
                put("january",new Integer(1));
                put("jan",new Integer(1));
                put("february",new Integer(2));
                put("feb",new Integer(2));
                put("march",new Integer(3));
                put("mar",new Integer(3));
                put("april",new Integer(4));
                put("apr",new Integer(4));
                put("may",new Integer(5));
                put("june",new Integer(6));
                put("jun",new Integer(6));
                put("july",new Integer(7));
                put("jul",new Integer(7));
                put("august",new Integer(8));
                put("aug",new Integer(8));
                put("september",new Integer(9));
                put("sep",new Integer(9));
                put("oct",new Integer(10));
                put("october",new Integer(10));
                put("nov",new Integer(11));
                put("november",new Integer(11));
                put("december",new Integer(12));
                put("dec",new Integer(12));
            }
        };

        this.indicatorsDict = new HashMap<String,String>(){
            {
                put("Thousand","K");
                put("Million","M");
                put("Trillion","T");
                put("Billion","B");
                put("thousand","K");
                put("million","M");
                put("billion","B");
                put("trillion","T");
                put("m","million");
                put("bn","billion");
            }
        };

        this.qualifiersSet = new HashSet<String>(){{
            add("meter");
            add("meters");
            add("kilometer");
            add("kilometers");
            add("mile");
            add("miles");
            add("ton");
            add("tons");
            add("kilogram");
            add("kilograms");
            add("gram");
            add("grams");
            add("kiloton");
            add("kilotons");
            add("centimeter");
            add("centimeters");
            add("feet");

        }};

        this.normalizedMonths = new HashMap<Integer, String>(){
            {
                put(new Integer(1),"01");
                put(new Integer(2),"02");
                put(new Integer(3),"03");
                put(new Integer(4),"04");
                put(new Integer(5),"05");
                put(new Integer(6),"06");
                put(new Integer(7),"07");
                put(new Integer(8),"08");
                put(new Integer(9),"09");
                put(new Integer(10),"10");
                put(new Integer(11),"11");
                put(new Integer(12),"12");
            }
        };

        this.partialCleanDict = new HashSet<Character>(){{
            int i = 48;
            while(i < 58)
                add((char)i++);

            i = 65;
            while(i <= 90)
                add((char)i++);

            i = 96;
            while(i++ < 122)
                add((char)i);

          //  add(',');
            add('.');
            add('-');
            add('%');
            add('$');
            add('/');
        }};

        this.cleanSigns = new HashSet<Character>(){{
            int i = 48;
            while(i < 58)
                add((char)i++);

            i = 65;
            while(i <= 90)
                add((char)i++);

            i = 96;
            while(i++ < 122)
                add((char)i);
        }};
    }


    public String partialStripSigns(String word){
        String ans = "";
        int idx = 0;
        char c ;
        while(idx < word.length()){
            c = word.charAt(idx++);
            if(this.partialCleanDict.contains(c))
                ans += c;
        }

        return ans;
    }

    /**
     * clean words fom unwanted signs
     * @param word
     * @return
     */
    public String stripSigns(String word){

        String ans = "";
        int idx = 0 ;
        char c ;
        while(idx < word.length()){
            c = word.charAt(idx++);
            if(this.cleanSigns.contains(c))
                ans += c;
        }

        return ans;
    }

    /**
     * handle the case of a dollar sign without indicator e.g <$NUMBER>
     * @param number
     * @return the wanted term by thr rule
     */
    public String handleDollarSignWithoutIndicator(String number){

        return number + "Dollars";

    }

    /**
     * normalize a month in the form of MM to the form of a number , e.g november -> 11
     * @param month
     * @param number
     * @return
     */
    public String handleMonthNumber(String month , String number){
        String ans = "";
        try {
            Integer num = this.monthsMap.get(month.toLowerCase());
            ans = normalizedMonths.get(num.intValue()) + "-" + number;
        }catch (Exception e){
            System.out.println("month = [" + month + "], number = [" + number + "]");
        }
        return ans ;
    }

    /**
     * check if the given word is a month
     * @param word
     * @return true if its a month
     */
    public boolean checkForMonth(String word){
        return monthsMap.containsKey(word.toLowerCase());
    }

    /**
     * check if a word represents a form of percents
     * @param word
     * @return
     */
    public boolean isPercent(String word){

        String temp = word.toLowerCase();
        return temp.equals("percent") || temp.equals("percentage") ;
    }

    private String fromBNtoM(String number){
        int num ;
        double dnum;
        String ans = "";
        if(number.indexOf('.') == -1) {
            num = stringToInt(number);
            return ans+num*1000;
        }else{
            dnum = stringToDouble(number);
            return ans+dnum*1000;
        }
    }

    /**
     * handle prices withour indicator . e.g <Number> <Dollars>
     * @param number
     * @return
     */
    public String handlePricesWithoutIndicators(String number) {
        String num = "";
        if (number.charAt(number.length() - 1) == 'm') {
            num = handleNumbersWithIndicators(number, "million");
            return num.substring(0,num.length()-1)+ " M Dollars";
        }
        if (number.charAt(number.length() - 1) == 'n'){
            num = handleNumbersWithIndicators(number, "billion");
            return fromBNtoM(num.substring(0,num.length()-1)) + " M Dollars" ;
        }

        return ""+num.substring(0,num.length()-1)+" " +num.charAt(num.length()-1) +" Dollars";
    }

    public String handlePricesWithFraction(String number , String fraction){
        return number+" "+fraction+" Dollars";
    }


    public String handleNumbersWithIndicatorsNoChange(String number , String indicator){


        if(indicator.equals("trillion") || indicator.equals("Trillion")){
            double dnum;
            int inum;
            if(number.indexOf('.') != -1){
                dnum = stringToDouble(number);
                dnum *= 1000 ;
                number = ""+dnum;
            }else{
                inum = stringToInt(number);
                inum *= 1000;
                number = ""+inum;
            }
            indicator = "billion";
        }


        String indic = this.indicatorsDict.get(indicator.toLowerCase());
        return number+indic;
    }

    public boolean checkForQuantifiers(String word){

        if(this.qualifiersSet.contains(word.toLowerCase()))
            return true;

        return false;
    }

    public String handleQuantifiers(String number , String qualifier){

        return number +" " + qualifier;
    }

    public String handlePercents(String word){

        String ans = "";

        for(int i = 0 ; i < word.length()-1 ; i++)
            ans += word.charAt(i);

        return ans + "%";
    }

    /**
     * handle the rule of numbers with indicator . e.g 100B
     * @param number
     * @param indicator
     * @return
     */
    public String handleNumbersWithIndicators(String number , String indicator){

        String ans = "";
        int index = number.indexOf('.');
        indicator = this.indicatorsDict.get(indicator.toLowerCase());
        if(indicator.equals("K"))
            if(index == -1)
                return stringToInt(number)+indicator;
            else
                return ans + stringToDouble(number) + indicator;
        else if (indicator.equals("M"))
            if(index == -1)
                return stringToInt(number) + indicator;
            else
                return ans + stringToDouble(number) + indicator;
        else if(indicator.equals("B"))
            if(index == -1)
                return stringToInt(number)+indicator;
            else
                return ans + stringToDouble(number) + indicator;

        return "INDICATOR NOT FOUND";
    }

    /**
     * handle the rule "... U.S. Dollars"
     * @param number
     * @param indicator
     * @return
     */
    public String handleUsDollars(String number , String indicator){
        int factor = getIndicatorFactor(indicator);
        return "" + decimalFormat.format(stringToDouble(number)*factor).toString() + " M Dollars";
    }


    public String handlePricesAlone(String number){
        int index = number.indexOf('.');
        String ans = "";

        if(index == -1){
            int num = stringToInt(number);
            if(num >= 1000000)
                num /= 1000000;
            ans += num ;
        }else{
            double num = stringToDouble(number);
            if(num >= 1000000)
                num /= 1000000;
            ans += num;
        }

        return ans+" M Dollars";
    }

    /**
     * handle dollar rules with indicators
     * @param number
     * @param indicator
     * @return
     */
    public String handleDollarsignWithIndicator(String number , String indicator){

        String ans ;
        int factor = getIndicatorFactor(indicator);
        double value = stringToDouble(number)*factor;
        ans = ""+decimalFormat.format(value).toString()+" M Dollars";
        return ans ;
    }

    /**
     * check if a word is an indicator . e.g "million"
     * @param indicator
     * @return
     */
    public boolean isIndicator(String indicator){
        return indicatorsDict.containsKey(indicator);
    }

    /**
     * handle rule of regualr numbers as terms
     * @param number
     * @return
     */
    public String handleNumbersAlone(String number){

        String ans = "";
        stripSigns(number);
        int dotindex = number.indexOf('.');
        if(dotindex == -1) {
            //Integer num = Integer.parseInt(number);
            int num = stringToInt(number);
            ans = handleIntNumbers(num);
        }else{
                Double num = stringToDouble(number);
                ans = handleDoubleNumbers(num);
            }

        return ans;
    }

    /**
     * handle rule of simple double numbers
     * @param num
     * @return
     */
    private String handleDoubleNumbers(Double num){
        if(num < 1000)
            return "" + decimalFormat.format(num);
        else if(num >= 1000 && num <= 1000*1000)
            return "" + decimalFormat.format(num/1000) + "K";
        else if(num >= 1000*1000 && num < 1000*1000*1000)
            return "" + decimalFormat.format(num/(1000*1000)) + "M";
        else if(num >= 1000*1000*1000)
            return "" + decimalFormat.format(num/(1000*1000*1000)) + "B";

        return "";
    }

    /**
     * handle rule of simple int numbers
     * @param num
     * @return
     */
    private String handleIntNumbers(int num){

        if(num < 1000)
            return "" + num;
        else if(num >= 1000 && num < 1000*1000)
            return "" + num/1000 + "K";
        else if(num >= 1000*1000 && num < 1000*1000*1000)
            return "" + num/(1000*1000) + "M";
        else if(num >= 1000*1000*1000)
            return "" + num/(1000*1000*1000) + "B";

      //  System.out.println("num = [" + num + "] something bad happened");
        return "";
    }

    /**
     * get the factor of an indicator . e.g K -> 1000
     * @param indicator
     * @return
     */
    private int getIndicatorFactor(String indicator){

        if (indicator.toLowerCase().equals("trillion"))
            return 1000*1000;
        else if (indicator.toLowerCase().equals("billion") || indicator.equals("bn"))
            return 1000;

        return 1;
    }


    /**
     * check if a number is in the months range 01 - 31
     * @param number
     * @return true if its in the range
     */
    public Boolean checkForMonthsRange(String number){
        try{
            Integer num = stringToInt(number);
            return num < 32 && num > 0;
        }catch (Exception e){
            return false;
        }
    }


    public String handleBetweenRangeFirstIndicatorSecondIndicator(String[] words){

        String ans = "";
        ans += words[0];
        ans += " ";
        ans += handleNumbersWithIndicators(words[1],words[2]);
        ans += " ";
        ans += words[3];
        ans += " ";
        ans += handleNumbersWithIndicators(words[4],words[5]);

        return ans;
    }


    public String handleBetweenRangeFirstIndicatorSecondFraction(String[] words){
        String ans = "" ;
        ans += words[0] ;
        ans += " " ;
        ans += handleNumbersWithIndicators(words[1],words[2]);
        ans += " " ;
        ans += words[3];
        ans += " ";
        ans += handleNumbersAlone(words[4]);
        ans += " ";
        ans += words[5];
        return ans;
    }


    public String handleBetweenRangeFirstIndicatorSecondAlone(String[] words){
        String ans = "";
        ans += words[0];
        ans += " ";
        ans += handleNumbersWithIndicators(words[1],words[2]);
        ans += " ";
        ans += words[3];
        ans += " ";
        ans += handleNumbersAlone(words[4]);
        return ans;
    }


    public String handleBetweenRangeFirstAloneSecondIndicator(String[] words){
        String ans = "";
        ans += words[0];
        ans += " " ;
        ans += handleNumbersAlone(words[1]);
        ans += " ";
        ans += words[2];
        ans += " " ;
        ans += handleNumbersWithIndicators(words[3],words[4]);
        return ans;
    }


    public String handleBetweenRangeFirstAloneSecondFraction(String[] words){
        String ans = "";
        ans += words[0];
        ans += " ";
        ans += handleNumbersAlone(words[1]);
        ans += " ";
        ans += words[2];
        ans += " ";
        ans += handleNumbersAlone(words[3]);
        ans += " ";
        ans += words[4];
        return ans;
    }


    public String handleBetweenRangeBothAlone(String[] words){

        String ans = "";
        ans += words[0];
        ans += " ";
        ans += handleNumbersAlone(words[1]);
        ans += " ";
        ans += words[2];
        ans += " ";
        ans += handleNumbersAlone(words[3]);
        return ans ;
    }


    public String handleBetweenRangeFirstFractionSecondIndicator(String[] words){
        String ans = " ";
        ans += words[0];
        ans += " ";
        ans += handleNumbersAlone(words[1]);
        ans += (" " + words[2] + " ");
        ans += (words[3] + " " + handleNumbersWithIndicators(words[4],words[5]));
        return ans;
    }


    public String handleBetweenRangeFirstFractionSecondFraction(String[] words){
        String ans = "";
        ans += words[0] ;
        ans += " ";
        ans += handleNumbersAlone(words[1]);
        ans += " ";
        ans += words[2];
        ans += " ";
        ans += words[3];
        ans += " ";
        ans += handleNumbersAlone(words[4]);
        ans += " " ;
        ans += words[5];
        return ans ;
    }


    public String handleBetweenRangeFirstFractionSecondAlone(String[] words){
        String ans = ("" + words[0] + " " +handleNumbersAlone(words[1]) + " " +words[2] + " "
                + words[3] + " " + handleNumbersAlone(words[4]));

        return ans;
    }


    public boolean isFraction(String word){

        int slashIndex = word.indexOf('/');
        if(slashIndex == -1)
            return false;

        if(!checkForNumber(word.substring(0,slashIndex)) || !checkForNumber(word.substring(slashIndex+1)))
            return false;

        return true ;
    }


    /**
     * convert a string to an int . replaces the Integer.parseInt method because its super slow
     * @param number string
     * @return the number it represents
     */
    public int stringToInt(String number){
        int i = 0 , ans = 0 ;
        boolean isNeg = false;

        String fixednum = "";
        if(number.charAt(number.length()-1) == 'm')
            fixednum = number.substring(0,number.length()-1);
        else if(number.charAt(number.length()-1) == 'n')
            fixednum = number.substring(0,number.length()-2);
        else
            fixednum = number;

        int len = fixednum.length();

        //check if negative
        if (fixednum.charAt(0) == '-') {
            isNeg = true;
            i = 1;
        }
        while( i < len) {
            ans *= 10;
            ans += fixednum.charAt(i++) - '0'; //Minus the ASCII code of '0' to get the value of the charAt(i++).
        }

        if (isNeg)
            ans = -ans;

        return ans;
    }

    /**
     * convert a string to a double . replaces the Double.parseDouble method because its super slow
     * @param str string
     * @return the number it represents
     */
    public double stringToDouble(String str) {

        String fixednum = "";

        if(str.charAt(str.length()-1) == 'm')
            fixednum = str.substring(0,str.length()-1);
        else if(str.charAt(str.length()-1) == 'n')
            fixednum = str.substring(0,str.length()-2);

        else
            fixednum = str;

        double num = 0;
        double num2 = 0;
        int idForDot = fixednum.indexOf('.');
        boolean isNeg = false;
        String st;
        int start = 0;
        int end = fixednum.length();

        if (idForDot != -1) {
            st = fixednum.substring(0, idForDot);
            for (int i = fixednum.length() - 1; i >= idForDot + 1; i--) {
                num2 = (num2 + fixednum.charAt(i) - '0') / 10;
            }
        } else {
            st = fixednum;
        }

        if (st.charAt(0) == '-') {
            isNeg = true;
            start++;
        } else if (st.charAt(0) == '+') {
            start++;
        }

        for (int i = start; i < st.length(); i++) {
            if (st.charAt(i) == ',') {
                continue;
            }
            num *= 10;
            num += st.charAt(i) - '0';
        }

        num = num + num2;
        if (isNeg) {
            num = -1 * num;
        }
        return num;
    }

    /**
     * check if a given string is a number
     * @param number
     * @return true if its a number
     */
    public boolean checkForNumber(String number) {

        if(number.equals(""))
            return false;

        int i = 0, len = number.length();

        while (i < len) {
            char c = number.charAt(i);
            if (!(c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9' || c == '0' || c == '.' || c==',')){
                if(c == 'b' && i == number.length()-2) {
                    i++;
                    continue;
                }else if((c == 'n' || c == 'm') && i == number.length()-1) {
                    i++;
                    continue;
                }else
                    return false;

            }
            i += 1;
        }
        return true;
    }

}
