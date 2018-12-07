package Controller;

import java.util.Comparator;

public class IntComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {

        int int1 = stringToInt(o1);
        int int2 = stringToInt(o2);

        if(int1 > int2)
            return 1;
        else if(int1 == int2)
            return 0;

        return -1;
    }

    private int stringToInt(String number){
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

}
