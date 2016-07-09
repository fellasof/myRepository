package com.lchclearnet.cds.common.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lchclearnet.cds.common.exception.CdsTechnicalException;

public class ResourcesHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourcesHelper.class);

	private ResourcesHelper() {
	}

	private static String getStringFromInputStream(InputStream is) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
		} catch (IOException e) {
			LOGGER.error("Error : " + e.getMessage());
			throw new CdsTechnicalException("Error while reading the stream ", e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					LOGGER.error("Error : " + e.getMessage());
				}
			}
		}
		return sb.toString();
	}

	public static String getResourceAsString(String resource) {
		LOGGER.debug("Load String Resource : " + resource);
		return getStringFromInputStream(getResourceAsStream(resource));
	}

	public static URL getResource(String resource) {
		LOGGER.debug("Load URL Resource : " + resource);
		return ResourcesHelper.class.getClassLoader().getResource(resource);
	}

	public static InputStream getResourceAsStream(String resource) {
		LOGGER.debug("Load Resource As a stream : " + resource);
		try {
			return getResource(resource).openStream();
		} catch (IOException e) {
			throw new CdsTechnicalException("Error while reading the content of the resource", e);
		}
	}

	@SuppressWarnings("rawtypes")
	public static TStringHelper getStringHelper(Class clazz) {
		return new TStringHelper(clazz);
	}

	public static TStringHelper getStringHelper(Object obj) {
		return new TStringHelper(obj.getClass());
	}
}
