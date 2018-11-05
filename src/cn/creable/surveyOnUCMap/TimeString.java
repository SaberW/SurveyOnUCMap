package cn.creable.surveyOnUCMap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
 
public class TimeString {
 
    private String valueOfString(String str, int len) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len - str.length(); i++) {
            sb.append("0");
        }
        return (sb.length() == 0) ? (str) : (sb.toString() + str);
    }
     
    public String getDateFormat(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(new Date());        
    }   
     
    public Date getDateFormat(String time){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return df.parse(time);
        } catch (ParseException e) {            
            e.printStackTrace();
        }   
        return null;
    }
 
    private String getTimeString(Calendar calendar) {       
        StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf(calendar.get(Calendar.YEAR)))      
          .append(this.valueOfString(String.valueOf(calendar.get(Calendar.MONTH) + 1),2))
          .append(this.valueOfString(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)),2))
          .append(this.valueOfString(String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)),2))
          .append(this.valueOfString(String.valueOf(calendar.get(Calendar.MINUTE)),2))
          .append(this.valueOfString(String.valueOf(calendar.get(Calendar.SECOND)),2))
          .append(this.valueOfString(String.valueOf(calendar.get(Calendar.MILLISECOND)),3));        
        return sb.toString();
    }   
     
    public String getTimeString(String time){
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(this.getDateFormat(time));
        return this.getTimeString(calendar);
    }
     
    public String getTimeString(){
        Calendar calendar = new GregorianCalendar();
        return this.getTimeString(calendar);
    }
     
    public static void main(String[] args) {
        TimeString ts = new TimeString();
        System.out.println(ts.getTimeString());
        System.out.println(ts.getTimeString("2015-4-30 8:51:52"));
    }
}