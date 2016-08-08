package com.lchclearnet.cds.report.common.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.util.StringValueResolver;

/**
 * Base template for {@link ReportGenerator} interface.
 * 
 * @author Hichem BOURADA
 * @version 1.31, 07/09/2013
 */
public abstract class AbstractReportGenerator implements ReportGenerator, EmbeddedValueResolverAware, InitializingBean {

	private static final int BUFFER_SIZE = 65536;

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private StringValueResolver resolver;

	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		initialize();
	}

	/**
	 * <p>
	 * Initialize necessary data, default implementation does nothing
	 * </p>
	 * <b>NB:</b> Sub classes must call <code>super.initialize()</code> before/after their implementation to ensure coherence.
	 * 
	 * @throws Exception
	 */
	protected void initialize() throws Exception {
	}

	@Override
	public void generate(Object params) throws IOException {
		try {
			preGenerate(params);
			doGenerate(params);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		} finally {
			postGenerate(params);
		}
	}

	/**
	 * Pre generate stuff, default is logging start file generation.
	 * 
	 * @param params
	 */
	protected void preGenerate(Object params) {
		LOGGER.info("Start generating file {}", getFilePath(params));
	}

	/**
	 * Real implementation algorithm.
	 * 
	 * @throws IOException
	 */
	protected abstract void doGenerate(Object params) throws IOException;

	/**
	 * Post generate stuff, default is logging end file generation.
	 * 
	 * @param params
	 */
	protected void postGenerate(Object params) {
		LOGGER.info("End generating file {}", getFilePath(params));
	}

	/**
	 * @param params
	 *            any object containing eventual parameters
	 * @return data to be written by this generator
	 */
	protected abstract Object getData(Object params);

	/**
	 * 
	 * @param key
	 *            property key
	 * @param defaultValue
	 * @return if key exist return its value otherwise default value
	 */
	protected final String getProperty(String key, String defaultValue) {
		String value;
		try {
			value = getProperty0(key);
		} catch (Exception e) {
			value = defaultValue;
		}
		LOGGER.trace("Resolving property {} -> {}", key, value);
		return value;
	}

	/**
	 * 
	 * @param key
	 *            property key
	 * @return value of property key
	 * @throws IllegalArgumentException
	 */
	protected final String getProperty(String key) {
		String value = getProperty0(key);
		LOGGER.trace("Resolving property {} -> {}", key, value);
		return value;
	}

	private final String getProperty0(String key) {
		return resolver.resolveStringValue("${" + key + "}");
	}

	/**
	 * @param params
	 *            eventual parameters.
	 * @return writer where generated data will be written, default return a {@link BufferedWriter} with default encoding.
	 * @throws IOException
	 */
	protected Writer getWriter(Object params) throws IOException {
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getFilePath(params)), getCharsetName()), BUFFER_SIZE);
	}

	/**
	 * 
	 * @return charset name (UTF-8, ISO-8859-1 ... etc), default is default charset for current JVM.
	 */
	protected String getCharsetName() {
		return Charset.defaultCharset().name();
	}

	/**
	 * 
	 * @param params
	 *            eventual parameters.
	 * @return full path for file i.e directory + name + extension
	 */
	public String getFilePath(Object params) {
		return getFileDirectory(params) + File.separator + getFileName(params) + getFileExtension(params);
	}

	/**
	 * @param params
	 *            eventual parameters
	 * @return file name of generated file without extension.
	 */
	protected abstract String getFileName(Object params);

	/**
	 * @param params
	 *            eventual parameters
	 * @return directory path where generated file is saved.
	 */
	protected abstract String getFileDirectory(Object params);

	/**
	 * 
	 * @param params
	 *            eventual parameters
	 * @return file extension including dot ('.') character.
	 */
	protected abstract String getFileExtension(Object params);

	// /**
	// *
	// * @return Short class name in lower-case without 'Generator' or 'ReportGenerator'
	// */
	// protected final String defaultPropertyRootName() {
	// String name = getClass().getSimpleName();
	// if (name.endsWith(LONG_SUFFIX)) {
	// name = name.substring(0, name.length() - LONG_SUFFIX.length());
	// } else if (name.endsWith(SHORT_SUFFIX)) {
	// name = name.substring(0, name.length() - SHORT_SUFFIX.length());
	// }
	// return name.toLowerCase();
	// }

}
