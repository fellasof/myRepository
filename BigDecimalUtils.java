package com.lchclearnet.cds.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

import com.lchclearnet.cds.common.exception.CdsTechnicalException;

public class BigDecimalUtils {

	private static Locale PARSING_LOCALE = Locale.UK;

	public static boolean isBigDecimalNullOrZero(BigDecimal value) {
		return value == null || BigDecimal.ZERO.compareTo(value) == 0;
	}

	/**
	 * return sum of all bigdecimals in parameter
	 *
	 * @param values
	 * @return BigDecimal
	 */
	public static BigDecimal sum(BigDecimal... values) {
		BigDecimal sum = BigDecimal.ZERO;
		if (values == null || values.length == 0) {
			return sum;
		} else {
			for (BigDecimal value : values) {
				sum = addSafeNullPointerException(sum, value);
			}
			return sum;
		}
	}

	/**
	 * Compares two {@code BigDecimal} for equality. Unlike "equals", this method considers two {@code BigDecimal} objects equal even if they are different in scale (thus 2.0 is equal to 2.00 when
	 * compared by this method).
	 *
	 * @param value1
	 *            first {@code BigDecimal} to compare
	 * @param value2
	 *            second {@code BigDecimal} to compare
	 *
	 * @return {@code true} if the two values are not null and are {@code BigDecimal} whose value are equal; {@code false} otherwise
	 */
	public static boolean equalsBigDecimal(BigDecimal value1, BigDecimal value2) {
		return value1 != null && value2 != null && value1.compareTo(value2) == 0;
	}

	/**
	 * @param value1
	 * @param value2
	 * @return {@code true} if the two values are null OR both not null and are equal; {@code false} otherwise
	 */
	public static boolean equalsSafeBigDecimal(BigDecimal value1, BigDecimal value2) {
		if (value1 == null && value2 == null) {
			return true;
		} else {
			return equalsBigDecimal(value1, value2);
		}
	}

	/**
	 * Check is the current value is strictly positive
	 *
	 * @param value
	 *            The current value to check
	 * @return <ul>
	 *         <li>true: The current value is strictly positive</li>
	 *         <li>false: The current value is not strictly positive</li>
	 *         </ul>
	 */
	public static boolean isStrictlyPositive(BigDecimal value) {
		return value != null && BigDecimal.ZERO.compareTo(value) < 0;
	}

	/**
	 * Converting and trim a String number value to a bigDecimal
	 *
	 * @param stringValueNumber
	 *            The string value
	 * @return Valid BigDecimal value else a null
	 */
	public static BigDecimal convertToBigDecimal(String stringValueNumber) {

		if (StringUtils.isEmpty(stringValueNumber)) {
			return null;
		}

		BigDecimal bigDecimal = null;
		try {
			bigDecimal = new BigDecimal(stringValueNumber.trim());
		} catch (NumberFormatException e) {
			bigDecimal = null;
		}
		return bigDecimal;
	}

	/**
	 * Converting and trim a String number value to a bigDecimal else return 0.
	 *
	 * @param object
	 *            The object to convert to bigDecimal
	 * @param required
	 *            Check if the returned value must be a number while the object converting exception
	 *            <ul>
	 *            <li>true: The returned value must be a valid number else 0</li>
	 *            <li>false: The return value must be a valid number else null</li>
	 *            </ul>
	 * @return Valid BigDecimal value else a null or 0 depending on the <code>required</code>
	 *
	 */
	public static BigDecimal convertToBigDecimal(Object object, boolean required) {
		// used by the reports
		BigDecimal bigDecimal = null;
		if (object == null) {
			return BigDecimal.ZERO;
		}

		String stringValue = String.valueOf(object);

		try {
			bigDecimal = new BigDecimal(stringValue.trim());
		} catch (NumberFormatException e) {
			if (required) {
				bigDecimal = BigDecimal.ZERO;
			} else {
				bigDecimal = null;
			}

		}
		return bigDecimal;
	}

	/**
	 * @param value
	 * @return value rounded to the nearest whole value
	 */
	public static BigDecimal roundTotheNearestWholeValue(BigDecimal value) {
		return value.setScale(0, BigDecimal.ROUND_HALF_UP);
	}

	/**
	 * Returns a safe nullPointerException {@code BigDecimal} whose value is {@code (first + second)}, and whose scale is {@code max(first.scale(),
	 * second.scale())}
	 *
	 * @param first
	 *            The first number BigDecimal value
	 * @param second
	 *            The second number BigDecimal value
	 * @return the sum of the two numbers (@pa)
	 */
	public static BigDecimal addSafeNullPointerException(BigDecimal first, BigDecimal second) {

		if (isBigDecimalNullOrZero(first) && isBigDecimalNullOrZero(second)) {
			return new BigDecimal(0);
		}

		if (isBigDecimalNullOrZero(first) && !isBigDecimalNullOrZero(second)) {
			return second;
		}

		if (!isBigDecimalNullOrZero(first) && isBigDecimalNullOrZero(second)) {
			return first;
		}

		return first.add(second);

	}

	/**
	 * Returns a BigDecimal with the proper format for prices, i.e. rounded to 7 decimals with commercial rounding.
	 */

	public static BigDecimal getPriceFormat(BigDecimal value) {
		return value.setScale(7, RoundingMode.HALF_UP);
	}

	/**
	 * Returns a BigDecimal with the proper format for prices, i.e. rounded to 2 decimals with commercial rounding.
	 */
	public static BigDecimal getAmountFormat(BigDecimal value) {
		return value.setScale(2, RoundingMode.HALF_UP);
	}

	/**
	 * Returns a BigDecimal parsed from the input String. The String may contain the special UK/US thousands delimiter char (',').
	 *
	 * @param value
	 * @return
	 */
	public static BigDecimal parseString(String value) {
		DecimalFormat df = new DecimalFormat();
		df.setParseBigDecimal(true);
		df.setDecimalFormatSymbols(new DecimalFormatSymbols(PARSING_LOCALE));
		try {
			return (BigDecimal) df.parse(value);
		} catch (ParseException pe) {
			throw new CdsTechnicalException("Could not parse '" + value + "' as BigDecimal.");
		}
	}

}
