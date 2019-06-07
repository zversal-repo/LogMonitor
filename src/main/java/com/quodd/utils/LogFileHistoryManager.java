package com.quodd.utils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.quodd.scheduler.FileParseScheduler;

public class LogFileHistoryManager implements Runnable {

	public void run() {

		while (true) {
			try {
				Thread.sleep(86400000);
				cleaner();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	private void cleaner() {

		ConcurrentHashMap<String, HashMap<String, HashMap<String, LocalDateTime>>> logFileHistoryMap = FileParseScheduler
				.getLogFileHistoryMap();
		LocalDateTime dateTime = LocalDateTime.now();

		for (String username : logFileHistoryMap.keySet()) {
			for (String process : logFileHistoryMap.get(username).keySet()) {
				for (String file : logFileHistoryMap.get(username).get(process).keySet()) {
					if (logFileHistoryMap.get(username).get(process).get(file).isBefore(dateTime)) {
						logFileHistoryMap.get(username).get(process).remove(file);
					}
				}
			}
		}

	}

}
