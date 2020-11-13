package uni_ko.bpm.Data_Management.Util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeParser {
    public static Date createTimestamp(String timestamp) throws ParseException {
        if (timestamp.matches("^[A-Za-z].*$")) {
            DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
            return df.parse(timestamp);
        } else if(timestamp.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+]\\d{2}:\\d{2}")) {
        	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        	return df.parse(timestamp);
        }else if(timestamp.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+]\\d{3}:\\s\\d{2}")){
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS X", Locale.US);
            return df.parse(timestamp);
        }else {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSSX", Locale.US);
            return df.parse(timestamp);
        }
    }
}
