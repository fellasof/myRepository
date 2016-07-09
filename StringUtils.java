package com.lchclearnet.cds.common.utils;

import java.util.List;

import com.lchclearnet.cds.common.exception.CdsTechnicalException;

public class StringUtils {

	private static final char SQL_ANY_CHAR = '%';
	/**
	 * Wildcard character
	 */
	private static final char WILDCARD = '*';

	public static boolean isNotEmpty(String str) {
		return str != null && !"".equals(str.trim());
	}

	public static boolean isEmpty(String str) {
		return !isNotEmpty(str);
	}

	public static boolean equalsIgnoreCase(String str1, String str2) {
		if (str1 == null && str2 == null) {
			return true;
		}
		if (str1 != null) {
			return str1.equalsIgnoreCase(str2);
		}
		return false;
	}

	public static boolean strEquals(String str1, String str2) {
		if (str1 == null && str2 == null) {
			return true;
		}
		if (str1 != null) {
			return str1.equals(str2);
		}
		return false;
	}

	/**
	 * Fonction utilitaire qui permet de padder une chaine de caracteres e gauche avec des 0.
	 * 
	 * @param str
	 *            chaine de caracteres numerique.
	 * @param nbChiffres
	 *            nombre de chiffres a obtenir.
	 * @return
	 * @throws FinalException
	 *             Si la chaine de caracteres est trop longue.
	 */
	public static String lpad0(String str, int nbChiffres) {
		return lpad(str, nbChiffres, '0');
	}

	/**
	 * Fonction utilitaire qui permet de padder e gauche une chaine de caracteres.
	 * 
	 * @param str
	 *            chaine a padder.
	 * @param nbCar
	 *            nombre de caracteres.
	 * @param c
	 *            caractere a utiliser pour le padding.
	 * @return
	 * @throws FinalException
	 *             Si la chaine de caracteres est trop longue.
	 */
	public static String lpad(String str, int nbCar, char c) {
		String result =null;
		if(isNotEmpty(str)){
			result= str.trim();
			final int len = result.length();

			if (len > nbCar) {
				throw new CdsTechnicalException("** Chaine de caracteres trop longue : '" + result + "', nb carateres attendus : " + nbCar);
			} else {
				for (int i = 0; i + len < nbCar; i++) {
					result = c + result;
				}
			}
		}
		return result;
	}

	/**
	 * Fonction utilitaire qui permet de padder a droite une chaine de caracteres.
	 * 
	 * @param str
	 *            chaine a padder.
	 * @param nbCar
	 *            nombre de caracteres.
	 * @param c
	 *            caractere a utiliser pour le padding.
	 * @return
	 * @throws FinalException
	 *             Si la chaine de caracteres est trop longue.
	 */
	public static String rpad(String str, int nbCar, char c) {
		StringBuffer buf = new StringBuffer();
		buf.append(str.trim());
		final int len = buf.length();

		if (len > nbCar) {
			throw new CdsTechnicalException("** Chaine de caracteres trop longue : '" + buf + "', nb carateres attendus : " + nbCar);
		} else {
			for (int i = 0; i + len < nbCar; i++) {
				buf.append(c);
			}
		}
		
		return buf.toString();
	}

	/**
	 * @param s
	 * @param pattern
	 * @return
	 */
	public static String rTrim(String s, String pattern) {
		while (s.endsWith(pattern)) {
			s = s.substring(0, s.length() - 1);
		}

		return s;
	}

	/**
	 * @param s
	 * @param pattern
	 * @return
	 */
	public static String lTrim(String s, String pattern) {
		while (s.startsWith(pattern)) {
			s = s.substring(1);
		}

		return s;
	}

	/**
	 * Replace '*' wildcard with '%' character.
	 * 
	 * @param wildcardExpression
	 *            an expression that contains a wildcard.
	 * @return
	 */
	public static String like(String wildcardExpression) {
		if (wildcardExpression != null) {
			wildcardExpression = wildcardExpression.replace(WILDCARD, SQL_ANY_CHAR);
		}
		return wildcardExpression;
	}

	/**
	 * Replace '*' wildcard with '%' character and convert result to uppercase.
	 * 
	 * @param wildcardExpression
	 *            an expression that contains a wildcard.
	 * @return
	 */
	public static String likeUpper(String wildcardExpression) {
		if (wildcardExpression != null) {
			wildcardExpression = wildcardExpression.replace(WILDCARD, SQL_ANY_CHAR).toUpperCase();
		}
		return wildcardExpression;
	}

	/**
	 * Replace message parameters with their value
	 * 
	 * @param message
	 *            ex: "the trade (P1) is not valid"
	 * @param values
	 *            ex: "123456"
	 * @return
	 */
	public static String fillMessageWithArgs(String message, String... errorMessageArgs) {
		String result = message;
		for (String arg : errorMessageArgs) {
			result = result.replaceFirst("\\([\\w\\s\\/]*\\)", arg);
		}
		return result;
	}

	public static String concatenationWithComma(List<String> list) {
		StringBuffer buf = new StringBuffer();

		if (!list.isEmpty()) {
			buf.append(list.get(0));
			for (int i = 1; i < list.size(); i++) {
				buf.append(",");
				buf.append(list.get(i));
			}
		}
		return buf.toString();
	}
}
