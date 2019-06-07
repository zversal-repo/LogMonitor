package com.quodd.parsers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.quodd.beans.Stats;
import com.quodd.scheduler.FileParseScheduler;
import com.quodd.utils.OutputManipulator;

public class LogParser implements Runnable {

	private String filename;
	private String username;
	private String process;

	public LogParser(String username, String process, String filename) {
		this.filename = filename;
		this.username = username;
		this.process = process;
	}

	public void run() {
		try {
			FileParseScheduler.addCurrentlyRunningThreads(process + filename);
			parse();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Parse the log file defined by filename of the object and check whether the
	 * parsed output is successfully saved or not
	 * 
	 * It also manipulates the List of names of currently running threads of
	 * FileScheduler Class
	 * 
	 * @throws IOException
	 */
	private void parse() throws IOException {

		Stats statsObject = new Stats();

		statsObject.setProcessName(process);

		List<String> lines = Files.readAllLines(Paths.get(filename), Charset.defaultCharset());

		Pattern timePattern = Pattern.compile("((\\d+)-)*(\\d+)\\W((\\d+):)*(\\d+)\\.(\\d+)");

		System.out.println(filename+"   "+lines.get(0));
		Matcher matcher = timePattern.matcher(lines.get(0));
		while (matcher.find()) {
			statsObject.setStartTime(matcher.group(0));
			break;
		}

		matcher = timePattern.matcher(lines.get(lines.size() - 1));
		while (matcher.find()) {
			statsObject.setEndTime(matcher.group(0));
			break;
		}
		String error = new String();
		for (String line : lines) {

			if (line.startsWith("[")) {
				if (!error.isEmpty()) {
					statsObject.addError(error);
					error = "";
				}

				if (line.contains("Number of records processed: ")) {

					Pattern numberPattern = Pattern.compile("\\d+");
					matcher = numberPattern.matcher(line);
					
					String numberOfRecordsProcessed = "N/A";
					
					while (matcher.find()) {
						numberOfRecordsProcessed = matcher.group();
					}
					statsObject.setRecordsProcessed(numberOfRecordsProcessed);

				}

				else if (line.contains("WARNING")) {
					if (line.contains("MISSING Property")) {
						statsObject.incrementErrorCounter();
						statsObject.addError(line);
					} else {
						statsObject.incrementWarningCounter();

					}
				}

			} else if (line.contains("Exception") || (line.contains("Error"))) {
				if (!error.isEmpty()) {
					statsObject.addError(error);
					error = "";
				}
				statsObject.incrementErrorCounter();
				error = error + line;
			} else {
				error = error + line ;
			}

		}
		// reader.close();
		if (statsObject.getErrorCounter() > 0) {
			statsObject.setStatus("Failure");
		}

		Pattern pattern = Pattern.compile("\\d\\d\\d\\d\\d\\d\\d\\d");
		matcher = pattern.matcher(filename);
		String date = null;
		while (matcher.find()) {
			date = matcher.group(0);
			break;
		}
		boolean saveSuccessful = OutputManipulator.addToStatFile(username, process, date, statsObject);

		if (saveSuccessful) {
			FileParseScheduler.addSuccessfulParsedFileName(username, process, filename);
		}

		FileParseScheduler.removeLatestKilledThreads(process + filename);
	}

	public String getName() {
		return process + filename;
	}

}
