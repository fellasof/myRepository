package com.lchclearnet.cds.common.file;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple file name generator which generate a unique file name by appending a unique sequence
 * <p>
 * 
 * @author Hichem BOURADA
 * @version 1.0
 */
public class SequenceFileNameGenerator implements FileNameGenerator {

	private final AtomicLong counter;

	/**
	 * Default constructor
	 */
	public SequenceFileNameGenerator() {
		this.counter = new AtomicLong();
	}

	@Override
	public String generateName(String prefix, String suffix) {
		counter.compareAndSet(Long.MAX_VALUE, 0);
		return prefix + counter.incrementAndGet() + suffix;
	}

}
