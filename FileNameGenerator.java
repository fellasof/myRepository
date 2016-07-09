package com.lchclearnet.cds.common.file;

/**
 * Generate a unique name according to a file prefix/suffix.
 * <p>
 * <b>NB:</b> Implementations must be <i><b>thread-safe</b></i>
 * 
 * @author Hichem BOURADA
 * @version 1.0, 11/11/2014
 */
public interface FileNameGenerator {

	/**
	 * 
	 * @param prefix
	 *            File prefix
	 * @param suffix
	 *            File suffix (or extension)
	 * @return a unique file name equals to concatenation of: prefix + timestamp + unique identifier + suffix
	 */
	String generateName(String prefix, String suffix);

}
