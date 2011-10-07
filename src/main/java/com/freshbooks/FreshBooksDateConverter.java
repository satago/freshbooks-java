package com.freshbooks;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DateUtils;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import com.thoughtworks.xstream.core.util.ThreadSafeSimpleDateFormat;

/**
 * Takes care of FreshBooks date idiosyncrasies.
 * See this for more info:
 * http://community.freshbooks.com/forums/viewtopic.php?id=5047
 */
public class FreshBooksDateConverter extends AbstractSingleValueConverter {

  /**
   * String representation for the date format without hour.
   */
  static final String DATE_FORMAT_WITHOUT_HOUR = "yyyy-MM-dd";

  /**
   * String representation for the date format with hour.
   */
  static final String DATE_FORMAT_WITH_HOUR = "yyyy-MM-dd HH:mm:ss";

  /**
   * The time zone from the FreshBooks servers
   */
  private TimeZone freshBooksTimeZone;
  
  /**
   * The client's time zone. 
   */
//  private TimeZone systemTimeZone;

  /**
   * Instantiates object with default time zone for FreshBooks.
   * (currently, is EST/EDT).
   * See http://community.freshbooks.com/forums/viewtopic.php?id=5047 for more
   * info.
   */
  public FreshBooksDateConverter() {
    this(TimeZone.getTimeZone("EST5EDT"));
  }
  
  /**
   * Instantiates object with system timezone.
   * See http://obscuredclarity.blogspot.com/2010/08/get-all-timezones-available-in-timezone.html
   * for a list of available time zones in java.
   * @param freshBooksTimeZone
   */
  public FreshBooksDateConverter(TimeZone freshBooksTimeZone) {
//    this.systemTimeZone    = Calendar.getInstance().getTimeZone();
    this.freshBooksTimeZone = freshBooksTimeZone; 
  }
  
  
  /**
   * The default date format, with hour and seconds.
   */
  private ThreadSafeSimpleDateFormat dateWithHourFormat =
      new ThreadSafeSimpleDateFormat(DATE_FORMAT_WITH_HOUR, 4, 20, true);

  /**
   * The default date format, with date only.
   */
  private ThreadSafeSimpleDateFormat dateWithoutHourFormat =
      new ThreadSafeSimpleDateFormat(DATE_FORMAT_WITHOUT_HOUR, 4, 20, true);

  /**
   * The class this class can marshall to XML data.
   */
  public boolean canConvert(Class type) {
    return type.equals(Date.class);
  }

  /**
   * Converting from XML raw data value.
   * @param str the String object to be converted into a Date object
   */
  @Override
  public Object fromString(String str) {

    ThreadSafeSimpleDateFormat choosenFormat;
    
    //identifying which parser to use
    if (str.length() == FreshBooksDateConverter.DATE_FORMAT_WITHOUT_HOUR.length()) {
      choosenFormat = this.dateWithoutHourFormat;
    } else if (str.length()
        == FreshBooksDateConverter.DATE_FORMAT_WITH_HOUR.length()) {
      choosenFormat = this.dateWithHourFormat;
    } else {
      throw new ConversionException("Cannot parse date " + str);
    }
    
    //parsing and returning
    
    try{
      Date date = choosenFormat.parse(str);
      return fromFreshBooksDate(date);
      
    } catch (ParseException e) {
      throw new ConversionException("Cannot parse date " + str);
    }
  }

  /**
   * The Date object to be mashalled into XML data.
   * @param obj A Date object
   */
  public String toString(Object obj) {
    Date date = (Date) obj;

    String str = dateWithHourFormat.format(date);
    
    return str;
  }
  
  /**
   * Computes the amount of seconds the system time is ahead of the FreshBooks
   * servers. Returns negative values as well if the system in a time zone where
   * the time is inferior to the FreshBooks servers.
   *
   * - System to FreshBook time is time-systemSecondsBehindFBServer().
   * - System from FreshBook time is time+systemSecondsBehindFBServer().
   * 
   * @return the amount of seconds
   */
  public int systemSecondsBehindFBServer() {
    Date date = new Date(); //now
    
    long time = date.getTime();
    
    return  ( Calendar.getInstance().getTimeZone().getOffset(time)
        - this.freshBooksTimeZone.getOffset(time) )/1000;
  }
  
  /**
   * Converts a Date at the system time zone to a date at the FreshBooks
   * time zone. 
   * @param date the date at system's time zone.
   * @return the date at the FreshBooks' time zone. 
   */
//  public Date toFreshBooksTime(Date date) {
////    return DateUtils.addSeconds(date, -this.systemSecondsBehindFBServer());
//    return date;
//  }
  
  /**
   * Converts a Date at the FreshBooks' time zone to a date at the system's  
   * time zone. 
   * @param date the date at FreshBooks' time zone.
   * @return the date at the system's time zone. 
   */
  public Date fromFreshBooksDate(Date date) {
    return DateUtils.addSeconds(date, this.systemSecondsBehindFBServer());
  }
  

}
