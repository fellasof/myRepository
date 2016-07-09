package com.lchclearnet.cds.common.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

public class CalendarHelper {

	/**
	 * Méthode qui permet de transformer une date sous forme de text en objet Calendar
	 * 
	 * @param dateText
	 * @return
	 * @throws ParseException
	 */
	public static Calendar transformeStringToCalendar(String dateText, String format) {

		if (StringUtils.isEmpty(dateText) || "null".equals(dateText) || StringUtils.isEmpty(format)) {
			return null;
		}

		if (DateUtils.FORMAT_DATE_UTC.equals(format)) {
			Calendar parseDateTime = DatatypeConverter.parseDateTime(dateText);
			return parseDateTime;
		}

		DateFormat formatter;
		Date date;
		formatter = new SimpleDateFormat(format);
		try {
			date = formatter.parse(dateText);
		} catch (ParseException e) {
			// date = null;
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		return cal;
	}

	public static Calendar getCalendarWithourHour(Calendar calendar) {

		if (calendar == null) {
			return null;
		}

		calendar.set(Calendar.HOUR_OF_DAY, 0); // Hours
		calendar.set(Calendar.MINUTE, 0); // Minutes
		calendar.set(Calendar.SECOND, 0); // seconds
		calendar.set(Calendar.MILLISECOND, 0); // milliseconds

		return calendar;
	}

	/**
	 * d2 - d1
	 * 
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static long daysBetween(Date d1, Date d2) {
		Calendar start = Calendar.getInstance();
		start.setTime(d1);
		Calendar end = Calendar.getInstance();
		end.setTime(d2);
		int diffDays = 0;
		start.add(Calendar.DAY_OF_MONTH, (int) diffDays);
		while (start.before(end)) {
			start.add(Calendar.DAY_OF_MONTH, 1);
			diffDays++;
		}
		while (start.after(end)) {
			start.add(Calendar.DAY_OF_MONTH, -1);
			diffDays--;
		}
		return diffDays;
		// long diff = Math.abs((d2.getTime() / (1000 * 60 * 60 * 24)) - (d1.getTime()/(1000 * 60 * 60 * 24)));
		// return diff;
	}
}
