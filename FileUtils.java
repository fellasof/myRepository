package com.lchclearnet.cds.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {

	private static Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

	public static void fileArchiver(File file, String targetDirectory) {

		try {

			DateFormat df = new SimpleDateFormat(DateUtils.DATE_TIMESTAMP_ATTACHED);
			StringBuilder newFileName = new StringBuilder(file.getName());
			newFileName.append('.');
			newFileName.append(df.format(new Date()));
			createFolderTreeIfNotExists(targetDirectory + File.separator + newFileName.toString());
			Files.move(Paths.get(file.getAbsolutePath()), Paths.get(targetDirectory).resolve(newFileName.toString()));
			LOGGER.info("The file '{}' has been moved to '{}'.",file.getAbsolutePath(),Paths.get(targetDirectory).resolve(newFileName.toString()));

		} catch (Exception e) {
			LOGGER.warn("Unable to move this file : " + file.getName(), e);
		}

	}

	
	/**
	 * Create all the folder tree of a file if it doesn't exists
	 * 
	 * @param absoluteFilePath
	 *            The absolute path of the file to generate (/foo/bar/.../myFile.csv)
	 */
	public static void createFolderTreeIfNotExists(String absoluteFilePath) {
		File myFile = new File(absoluteFilePath);
		File parentFile = myFile.getParentFile();

		if (parentFile != null && !parentFile.exists()) {
			parentFile.mkdirs();
		}
	}

	public static String getFileHeader(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String header = reader.readLine();
		reader.close();
		return header;
	}

	public static int getLineNumber(File cerFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(cerFile));
		int lines = 0;
		while (reader.readLine() != null) {
			lines++;
		}
		reader.close();
		return lines;
	}

}
