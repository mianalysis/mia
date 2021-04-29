package wbif.sjx.MIA;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Test {
    public static void main(String[] args) {
        String str = "VID1247_A2_1_2021y04m01d_20h39m.tif";
        String format = "[^_]+_([^_]+)_.+";
        
        Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(str);
        System.out.println(matcher.matches());
        System.out.println(pattern.matcher("").groupCount());
        System.out.println(matcher.group(0));
        System.out.println(matcher.group(1));
    }
}