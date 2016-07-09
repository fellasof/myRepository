package com.lchclearnet.cds.common.utils;

import java.util.List;

public class ListUtils {

	/**
	 * Return true of the list in parameter is <b>not empty</b>
	 * 
	 * @param list
	 *            of objects
	 * @return boolean
	 */
	public static <T> boolean isNotEmpty(List<T> list) {
		if (list != null && list.size() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * Return true of the list in parameter is <b>empty</b>
	 * 
	 * @param list
	 *            of objects
	 * @return boolean
	 */
	public static <T> boolean isEmpty(List<T> list) {
		return !isNotEmpty(list);
	}

}
