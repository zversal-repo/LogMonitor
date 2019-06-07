package com.quodd.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.quodd.application.Main;

public class CronJobParser implements Runnable {

	private ConcurrentHashMap<String, HashMap<String, String>> userToJobMapper = new ConcurrentHashMap<>();

	public void run() {
		while (true) {
			try {
				parseCronTab();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			Main.setMap(userToJobMapper);
			try {
				Thread.sleep(7200000L);
			} catch (InterruptedException e) {

			}
		}
	}

	private void parseCronTab() throws IOException {
		/*
		 * Map providing relation between user and all the scripts assigned in cron Tab
		 */
		HashMap<String, HashSet<String>> userToCronJobMapper = new HashMap<>();

		String allCronTabsDirectory = "/var/spool/cron";

		File directory = new File(allCronTabsDirectory);
		File[] listOfFiles = directory.listFiles();
		for (File file : listOfFiles) {
			String username = file.getName();

			/*
			 * Check whether a temporary cronTab is not there and the user actually exists
			 */
			if ((!file.getName().startsWith("#")) && checkIfUserExists(username)) {

				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (!(line.isEmpty() || line.startsWith("#") || line.startsWith("@"))) {
						String[] fields = line.split("\\s+");

						// String that will contain absolute path of shell scripts to be run
						String commandString = null;
						for (String field : fields) {
							if (field.contains(".sh"))
								commandString = field;
						}

						if (commandString != null) {
							if (userToCronJobMapper.containsKey(username)) {
								userToCronJobMapper.get(username).add(commandString);
							} else {
								HashSet<String> templist = new HashSet<>();
								templist.add(commandString);
								userToCronJobMapper.put(username, templist);
							}
						}

					}
				}
				reader.close();

			}
		}
		parseScript(userToCronJobMapper);
	}

	/**
	 * This method uses the Map between user and shell scripts and generate a Map
	 * between every user and the Process to run along with base name of log file to
	 * be processed
	 * 
	 * @param userToCronJobMapper
	 * @throws IOException
	 */
	private void parseScript(HashMap<String, HashSet<String>> userToCronJobMapper) throws IOException {

		for (String username : userToCronJobMapper.keySet()) {
			for (String script : userToCronJobMapper.get(username)) {

				BufferedReader reader = new BufferedReader(new FileReader(script));
				String line;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if ((!line.isEmpty()) && (!line.startsWith("#")) && line.contains("-cp")) {
						String[] fields = line.split("\\s+");
						String className = null;
						String logFileName = null;

						for (int i = 0; i < fields.length; i++) {

							if (fields[i].contains("-cp")) {
								className = fields[i + 2];
							}
						}

						for (int i = 0; i < fields.length; i++) {
							if (fields[i].contains(">>") || fields[i].contains(">")) {

								Pattern pattern = Pattern.compile("\\/[A-Za-z0-9_]+_");
								Matcher matcher = pattern.matcher(fields[i + 1]);

								while (matcher.find()) {
									logFileName = matcher.group(0);
									break;
								}
							}
						}

						if (className != null && logFileName != null) {
							if (userToJobMapper.containsKey(username)) {
								userToJobMapper.get(username).put(className, logFileName);
							} else {
								HashMap<String, String> tempMap = new HashMap<>();
								tempMap.put(className, logFileName);
								userToJobMapper.put(username, tempMap);
							}
						}
					}
				}
				reader.close();
			}

		}
	}

	/**
	 * Check whether a given user actually exists in linux system by using
	 * /etc/passwd file
	 * 
	 * @param user
	 * @return true if user actually exists
	 */
	private boolean checkIfUserExists(String user) {

		String command = "cat /etc/passwd |grep " + user;
		Process process = null;
		BufferedReader reader = null;
		try {
			process = new ProcessBuilder().command("bash", "-c", command).start();
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String output;

			while ((output = reader.readLine()) != null) {

				String[] fields = output.split(":");

				if (user.equals(fields[0])) {
					return true;
				}
			}

			return false;

		} catch (IOException e) {
			return false;
		} finally {
			if (process != null) {
				process.destroy();
			}

			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {

				}
			}

		}
	}

}
