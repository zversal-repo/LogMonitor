package com.quodd.application;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.quodd.parsers.CronJobParser;
import com.quodd.scheduler.FileParseScheduler;
import com.quodd.utils.LogFileHistoryManager;

public class Main {

	static ConcurrentHashMap<String, HashMap<String, String>> jobs = new ConcurrentHashMap<>();

	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {

		Thread cronJobParser = new Thread(new CronJobParser());
		Thread fileParseScheduler = new Thread(new FileParseScheduler());
		Thread logFileHistoryManager = new Thread(new LogFileHistoryManager());
		cronJobParser.start();
		while (true) {
			if (cronJobParser.isAlive()) {
				fileParseScheduler.start();
				logFileHistoryManager.start();
				break;
			}
		}
	}

	public static ConcurrentHashMap<String, HashMap<String, String>> getMap() {
		return jobs;
	}

	public static void setMap(ConcurrentHashMap<String, HashMap<String, String>> userToJobMapper) {
		jobs = userToJobMapper;
	}

}
