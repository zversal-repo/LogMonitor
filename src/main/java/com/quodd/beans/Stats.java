package com.quodd.beans;

import java.util.ArrayList;

public class Stats {

	private String processName;
	private String startTime;
	private String endTime;
	private String status;
	private long warningCounter;
	private long errorCounter;
	private ArrayList<String> errorList;
	private String recordsProcessed;

	public Stats() {
		processName = "N/A";
		warningCounter = 0L;
		errorCounter = 0L;
		status = "Successful";
		errorList = new ArrayList<>();
		recordsProcessed = "N/A";
		endTime = "N/A";
	}

	public String getProceesName() {
		return processName;
	}

	public String getStartTime() {
		return startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public String getStatus() {
		return status;
	}

	public long getWarningCounter() {
		return warningCounter;
	}

	public long getErrorCounter() {
		return errorCounter;
	}

	public ArrayList<String> getErrorList() {
		return errorList;
	}

	public String getRecordsProcessed() {
		return recordsProcessed;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setWarningCounter(long warningCounter) {
		this.warningCounter = warningCounter;
	}

	public void setLogCounter(long errorCounter) {
		this.errorCounter = errorCounter;
	}

	public void setErrorList(ArrayList<String> errorList) {
		this.errorList = errorList;
	}

	public void setRecordsProcessed(String recordsProcessed) {
		this.recordsProcessed = recordsProcessed;
	}

	public void addError(String error) {
		errorList.add(error);
	}

	public void incrementErrorCounter() {
		errorCounter++;
	}

	@Override
	public String toString() {
		return "Stats [processName=" + processName + ", startTime=" + startTime + ", endTime=" + endTime + ", status="
				+ status + ", warningCounter=" + warningCounter + ", errorCounter=" + errorCounter + ", errorList="
				+ errorList + ", recordsProcessed=" + recordsProcessed + "]";
	}

	public void incrementWarningCounter() {
		warningCounter++;
	}

}
