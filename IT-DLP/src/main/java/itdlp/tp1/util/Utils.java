package itdlp.tp1.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;

public class Utils
{
    public static String printDate(SimpleDateFormat format, Calendar calendar)
    {
        return format.format(calendar.getTime());
    }

    public static Calendar fromDateString(SimpleDateFormat format, String date) throws ParseException
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(format.parse(date));
        return calendar;
    }

    public static byte[] fromBase64(String value){
        return Base64.getDecoder().decode(value);
    }

    public static String toBase64(byte[] value){
        return Base64.getEncoder().encode(value);
    }
}
