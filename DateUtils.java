package com.lchclearnet.cds.common.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtils {

	private static Logger LOGGER = LoggerFactory.getLogger(DateUtils.class);

	public static final String REPORT_DATE_HOUR_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String REPORT_DATE_FORMAT = "yyyy-MM-dd";
	public static final String DB_DATE_FORMAT = "yyyyMMdd";
	public static final String DATE_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	public static final String FORMAT_DATE_UTC = "yyyy-MM-dd'T'HH:mm:ss.SSS-HH:mm";
	public static final String FORMAT_DATE_LONG = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	public static final String DATE_TIMESTAMP_WITH_TIMEZONE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public static final String DATE_TIMESTAMP_WITH_TIMEZONE_FORMAT_FOR_REPORT = "yyyy-MM-dd'T'HH:mm:ssZ";
	public static final String DATE_TIMESTAMP_ATTACHED = "yyyyMMddHHmmss";
	public static final String TIMESTAMP_MILLIS = "yyyyMMddHHmmssSSS";

	private DateUtils() {
		super();
		// to avoid new DateUtils()
	}

	public static long getNumberOfDay(Date d1, Date d2) {
		GregorianCalendar c1 = new GregorianCalendar();
		c1.setTime(d1);
		GregorianCalendar c2 = new GregorianCalendar();
		c2.setTime(d2);
		long date1ZeroH = new GregorianCalendar(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), c1.get(Calendar.DATE)).getTimeInMillis();
		long date2ZeroH = new GregorianCalendar(c2.get(Calendar.YEAR), c2.get(Calendar.MONTH), c2.get(Calendar.DATE)).getTimeInMillis();
		return Math.abs(date1ZeroH - date2ZeroH) / (24 * 60 * 60 * 1000);
	}

	public static Date buildDbaDate(String sDate) {
		SimpleDateFormat dbaSdf = null;
		Date d = null;
		if (dbaSdf == null) {
			String sd = null;
			LOGGER.debug("NLS_DATE_FORMAT : " + (sd = DB_DATE_FORMAT));
			dbaSdf = new SimpleDateFormat(sd);
		}
		try {
			d = dbaSdf.parse(sDate);
		} catch (final ParseException e) {
			LOGGER.error("Error buildDbaDate : " + e.getMessage());
		}
		return d;
	}

	public static Date buildDtccDate(String sDate) {
		SimpleDateFormat dtccSdf = null;
		Date d = null;
		if (dtccSdf == null) {
			dtccSdf = new SimpleDateFormat(REPORT_DATE_FORMAT);
		}
		try {
			d = dtccSdf.parse(sDate);
		} catch (final ParseException e) {
			LOGGER.error("Error buildDTCCDate : " + e.getMessage());
		}
		return d;
	}

	public static Date buildDBusDate(String sDate) {
		SimpleDateFormat dtccSdf = null;
		Date d = null;
		if (dtccSdf == null) {
			dtccSdf = new SimpleDateFormat(REPORT_DATE_FORMAT);
		}
		try {
			d = dtccSdf.parse(sDate);
		} catch (final ParseException e) {
			LOGGER.error("Error buildDbusDate : " + e.getMessage());
		}
		return d;
	}

	public static boolean isInDaylightTime(Date businessDate) {
		boolean inDaylightTime = false;
		TimeZone paristz = TimeZone.getTimeZone("Europe/Paris");
		Calendar cal = Calendar.getInstance();
		cal.setTime(businessDate);
		inDaylightTime = paristz.inDaylightTime(cal.getTime());
		return inDaylightTime;
	}

	public static String formatDate(Date d) {
		if (d == null) {
			return "0000-00-00";
		}
		return new SimpleDateFormat(REPORT_DATE_FORMAT).format(d);
	}

	public static String formatDateFlatFile(Date d) {
		if (d == null) {
			return "00000000";
		}
		return new SimpleDateFormat(DB_DATE_FORMAT).format(d);
	}

	public static String formatDateHour(Date d) {
		if (d == null) {
			return "0000-00-00 00:00:00";
		}
		return new SimpleDateFormat(REPORT_DATE_HOUR_FORMAT).format(d);
	}

	public static String formatDateTimestampWithTimezoneForReport(Date d) {
		if (d == null) {
			return "0000-00-00 00:00:00";
		}
		return new SimpleDateFormat(DATE_TIMESTAMP_WITH_TIMEZONE_FORMAT_FOR_REPORT).format(d);
	}

	public static String getStandardDate(Date myDate) {
		String s = null;
		if (myDate != null) {
			s = new SimpleDateFormat(DB_DATE_FORMAT).format(myDate);
		}
		return s;
	}

	/**
	 * 
	 * date1 <= date2
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static boolean beforeOrEqual(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			return false;
		}

		final Calendar cal1 = Calendar.getInstance();
		final Calendar cal2 = Calendar.getInstance();
		cal1.setTime(date1);
		cal2.setTime(date2);

		return !(cal1.after(cal2));
	}

	public static Date buildDate(String date, String dateFormat) {
		try {
			return new SimpleDateFormat(dateFormat).parse(date);
		} catch (final ParseException e) {
			LOGGER.error("Error in parsing date : " + date, e);
			return null;
		}
	}

	public static Date buildCERGoldenRecordDate(String date, String dateFormat) throws ParseException {
		try {
			if (date == null || "".equals(date)) {
				return null;
			}
			DateFormat f = new SimpleDateFormat(dateFormat);
			Date d = f.parse(date);
			return d;
		} catch (final ParseException e) {
			LOGGER.error("Error in parsing date : " + date, e);
			throw e;
		}
	}

	/**
	 * Used to parse ISO8601 dates with time zone indicator (TZD)
	 * 
	 * @param date
	 * @return
	 */
	public static Date buildDateWithTimezone(String date) {
		// NOTE: SimpleDateFormat uses [-+]hhmm for the TZ (RFC 822)
		// so we have to change input date (ISO8601 compliant) to this format
		final StringBuilder sb = new StringBuilder(date);
		try {
			// strip the last ':'
			sb.replace(date.lastIndexOf(":"), date.lastIndexOf(":") + 1, "");
			date = sb.toString();
			return new SimpleDateFormat(DATE_TIMESTAMP_WITH_TIMEZONE_FORMAT).parse(date);
		} catch (final ParseException e) {
			LOGGER.error("Error in parsing date : " + date, e);
			return null;
		}
	}

	public static final String format(final Date date, final String datePattern) {
		if (date != null && StringUtils.isNotEmpty(datePattern)) {
			DateFormat result = new SimpleDateFormat(datePattern);
			return result.format(date);
		}
		return null;
	}

	public static Date removeTime(Date date) {
		final Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/**
	 * Return a Date from an XMLGregorianCalendar
	 * 
	 * @param xmlCal
	 * @return
	 */
	public static Date getDateFromXmlGregorianCalendar(XMLGregorianCalendar xmlCal) {
		if (xmlCal == null) {
			return null;
		} else {
			return xmlCal.toGregorianCalendar().getTime();
		}
	}

	/**
	 * Return a XMLGregorianCalendar from a Date
	 * 
	 * @param date
	 * @return
	 */
	public static XMLGregorianCalendar getXMLGregorianCalendarFromDate(Date date) {
		XMLGregorianCalendar xmlgc = null;
		if (date != null) {
			GregorianCalendar gCalendar = new GregorianCalendar();
			gCalendar.setTime(date);
			try {
				xmlgc = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);
			} catch (DatatypeConfigurationException e) {
				LOGGER.error("Error getXMLGregorianCalendarFromDate : " + e.getMessage());
			}
		}
		return xmlgc;
	}

	/**
	 * @param date
	 * @return XMLGregorianCalendar from a Date without timeZone
	 */
	public static XMLGregorianCalendar getXMLGregorianCalendarFromDateWithoutTimeZone(Date date) {
		XMLGregorianCalendar xmlgc = null;
		if (date != null) {
			GregorianCalendar gCalendar = new GregorianCalendar();
			gCalendar.setTime(date);
			try {
				xmlgc = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);
				xmlgc.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
			} catch (DatatypeConfigurationException e) {
				LOGGER.error("Error getXMLGregorianCalendarFromDate : " + e.getMessage());
			}
		}
		return xmlgc;
	}

	/**
	 * Return a XMLGregorianCalendar from a Date with or without TimeStamp
	 * 
	 * @param date
	 * @param timeStamp
	 * @return
	 */
	public static XMLGregorianCalendar getDateFormatted(String date, boolean timeStamp) {
		XMLGregorianCalendar xmlCalendar = null;
		Date dateToConvert = null;
		if (date != null && !"".equals(date)) {
			if (timeStamp) {
				dateToConvert = buildDateWithTimezone(date);
			} else {
				dateToConvert = buildDate(date, DateUtils.REPORT_DATE_FORMAT);
			}
			xmlCalendar = getXMLGregorianCalendarFromDate(dateToConvert);
		}
		return xmlCalendar;
	}

	/**
	 * Return the a date calculated from the input date delayed (+/-) of the given number of days
	 * 
	 * @param date
	 * @param days
	 * @return
	 */
	public static Date addDays(Date date, int days) {
		Date delayedDate = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, days);
		delayedDate.setTime(c.getTime().getTime());
		return delayedDate;
	}

	/**
	 * Return the date entered in a timestamp format and attached.
	 * 
	 * @param myDate
	 * @return
	 */
	public static String getAttachedTimeStampDate(Date myDate) {
		String s = null;
		if (myDate != null) {
			s = new SimpleDateFormat(DATE_TIMESTAMP_ATTACHED).format(myDate);
		}
		return s;
	}

	/**
	 * Return the date entered in a timestamp format and attached.
	 * 
	 * @param myDate
	 * @return
	 */
	public static String getTimestampMillisDate(Date myDate) {
		String s = null;
		if (myDate != null) {
			s = new SimpleDateFormat(TIMESTAMP_MILLIS).format(myDate);
		}
		return s;
	}

	public static XMLGregorianCalendar getXmlGregorianCalendarFromString(String date, String dateFormat) {
		if (date != null) {
			Date convertedDate = buildDate(date, dateFormat);
			return getXMLGregorianCalendarFromDate(convertedDate);
		} else {
			return null;
		}
	}

	/**
	 * Update the First Payment Date value such as: when the day of the month of the date is 21, 22, 23 or 24 then replace it by 20.
	 * 
	 * @param date
	 * @return
	 */
	public static Date updateFirstPaymentDate(Date date) {

		if (date != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
			if (21 <= dayOfMonth && dayOfMonth <= 24) {
				cal.set(Calendar.DAY_OF_MONTH, 20);
			}
			return cal.getTime();
		}
		return null;
	}
}
