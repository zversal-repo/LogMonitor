package com.quodd.scheduler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.quodd.application.Main;
import com.quodd.parsers.LogParser;

public class FileParseScheduler implements Runnable {

	private static ConcurrentHashMap<String, HashMap<String, HashMap<String, LocalDateTime>>> logFileHistoryMap = new ConcurrentHashMap<>();
	private static ArrayList<String> currentlyRunningThreads = new ArrayList<>();

	private ExecutorService pool = Executors.newCachedThreadPool();

	public void run() {
		while (true) {

			ConcurrentHashMap<String, HashMap<String, String>> userToJobMapper = Main.getMap();

			for (String username : userToJobMapper.keySet()) {
				for (String process : userToJobMapper.get(username).keySet()) {

					String logFileBaseName = userToJobMapper.get(username).get(process);

					ArrayList<String> logFileList = runCommand(username, logFileBaseName);
										
					if (logFileList != null && (!logFileList.isEmpty())) {
						
						for (String latestLogFile : logFileList) {
							
							if (logFileList != null) {
								
								if (logFileHistoryMap.containsKey(username)) {
									if (logFileHistoryMap.get(username).containsKey(process)) {
										if (!logFileHistoryMap.get(username).get(process).containsKey(latestLogFile)) {
											if (!currentlyRunningThreads.contains(process + latestLogFile)) {
												LogParser parser = new LogParser(username, process, latestLogFile);
												pool.execute(parser);
											}
										}
									}else{
										LogParser parser = new LogParser(username, process, latestLogFile);
										pool.execute(parser);
									}
								}else{
									LogParser parser = new LogParser(username, process, latestLogFile);
									pool.execute(parser);
								}

							}
						}
					}
				}
			}
		}

	}

	/**
	 * This function find out the latest log generated for a process
	 * 
	 * @param username        The user account the process is running from
	 * @param logFileBaseName The base name of log file
	 * @return latest logFile for given process
	 */
	private ArrayList<String> runCommand(String username, String logFileBaseName) {

		ArrayList<String> filesToProcessed = new ArrayList<String>();

		String directory;
		if (username.equals("root")) {
			directory = "/root/logs";
		} else {
			directory = "/home/" + username + "/logs";
		}

		String filename = directory.concat(logFileBaseName);

		String command = "find " + filename + "* -mtime -1";

		Process process = null;
		String line = null;
		BufferedReader reader = null;
		try {
			process = new ProcessBuilder().command("bash", "-c", command).start();
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			while ((line = reader.readLine()) != null) {
				filesToProcessed.add(line.trim());
			}
			return filesToProcessed;

		} catch (IOException e) {
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static synchronized void addSuccessfulParsedFileName(String username, String process,
			String latestLogFileName) {

		if (logFileHistoryMap.containsKey(username)) {
			if (logFileHistoryMap.get(username).containsKey(process)) {
				if (!logFileHistoryMap.get(username).get(process).containsKey(latestLogFileName))
					logFileHistoryMap.get(username).get(process).put(latestLogFileName, LocalDateTime.now());

			} else {
				HashMap<String, LocalDateTime> tempMap = new HashMap<>();
				tempMap.put(latestLogFileName, LocalDateTime.now());
				logFileHistoryMap.get(username).put(process, tempMap);
			}
		} else {
			HashMap<String, LocalDateTime> tempMap = new HashMap<>();
			tempMap.put(latestLogFileName, LocalDateTime.now());
			HashMap<String, HashMap<String, LocalDateTime>> tempMap1 = new HashMap<>();
			tempMap1.put(process, tempMap);
			logFileHistoryMap.put(username, tempMap1);

		}
	}

	public static synchronized void removeLatestKilledThreads(String threadname) {
		currentlyRunningThreads.remove(threadname);
	}

	public static synchronized void addCurrentlyRunningThreads(String threadname) {
		currentlyRunningThreads.add(threadname);
	}

	public static synchronized ConcurrentHashMap<String, HashMap<String, HashMap<String, LocalDateTime>>> getLogFileHistoryMap() {
		return logFileHistoryMap;
	}

}
