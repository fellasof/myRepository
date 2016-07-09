package com.lchclearnet.cds.common.file;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * File name generator which generate a unique file name equals to concatenation of:
 * 
 * <pre>
 * <i>prefix + current timestamp</i> (formatted according pattern) <i>+ unique sequence + suffix</i>
 * </pre>
 * <p>
 * This generator is <i>thread safe</i>
 * 
 * @author Hichem BOURADA
 * @version 1.0, 09/11/2013
 */
public class TimestampFileNameGenerator implements FileNameGenerator {

	private static final String DEFAULT_FORMAT = "yyyyMMddHHmmssSSS";

	private static final String DEFAULT_PREFIX = "unknown";

	private final SimpleDateFormat formatter;

	private final AtomicInteger counter;

	private final String defaultPrefix;

	/**
	 * Default constructor
	 */
	public TimestampFileNameGenerator() {
		this(DEFAULT_FORMAT, DEFAULT_PREFIX);
	}

	/**
	 * Constructor
	 * 
	 * @param pattern
	 *            date/datetime pattern
	 * @param defaultPrefix
	 *            default prefix used when prefix is null
	 */
	public TimestampFileNameGenerator(String pattern, String defaultPrefix) {
		if (pattern == null) {
			throw new IllegalArgumentException("pattern is null");
		}
		if (defaultPrefix == null) {
			throw new IllegalArgumentException("defaultPrefix is null");
		}
		this.formatter = new SimpleDateFormat(pattern);
		this.counter = new AtomicInteger();
		this.defaultPrefix = defaultPrefix;
	}

	@Override
	public String generateName(String prefix, String suffix) {
		if (prefix == null) {
			prefix = getDefaultPrefix();
		}
		String timestamp = formatter.format(new Date());
		counter.compareAndSet(Integer.MAX_VALUE, 0); // reset to 0 to ensure that counter >= 0
		int id = counter.incrementAndGet();
		StringBuilder sb = new StringBuilder(64);
		sb.append(prefix).append(timestamp).append('-').append(id).append(suffix);
		return sb.toString();
	}

	/**
	 * 
	 * @return
	 */
	protected String getDefaultPrefix() {
		return defaultPrefix;
	}

}
