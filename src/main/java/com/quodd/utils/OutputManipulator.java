package com.quodd.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quodd.beans.Stats;

public class OutputManipulator {

	/**
	 * Add a record for a particular parsed file for given process
	 * 
	 * @param username The user under which process is run
	 * @param process  The process that is being run
	 * @param date     The date of the log file that is parsed
	 * @param stats    The Stats Object having information after successful parsing
	 * @return true if saving to the json file is successful
	 */
	public static synchronized boolean addToStatFile(String username, String process, String date, Stats stats) {

		String outputDirectoryName = "/home/LogMonitor/generated/" + date;
		String outputFileName = null;

		File directory = new File(outputDirectoryName);

		if (!directory.exists()) {
			try {
				directory.mkdirs();
				outputFileName = directory.getAbsolutePath() + "/" + username;
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}

		if (outputFileName != null) {

			File file = new File(outputFileName);

			List<Object> list;
			try {

				if (file.exists() && ((list = new ObjectMapper().readValue(file, List.class)) != null)) {

					list.add(stats);

				} else {
					list = new ArrayList<>();
					list.add(stats);
				}
				new ObjectMapper().writeValue(file, list);
				return true;
			} catch (IOException e) {
				return false;
			}
		} else {
			return false;
		}
	}

}
