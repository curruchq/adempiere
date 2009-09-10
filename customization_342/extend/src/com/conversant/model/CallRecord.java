package com.conversant.model;

import java.io.Serializable;

public class CallRecord  implements Serializable
{
	private String originNumber = "";
	private String destinationNumber = "";
	private String description = "";
	private String status = "";
	private String date = "";
	private String time = "";
	private String duration = "";
	private String charge = "";
	private String listenId = "";
	
	public String toString() {
		return "CallRecord[" + listenId + "]";
	}

	public String getOriginNumber() {
		return originNumber;
	}

	public void setOriginNumber(String originNumber) {
		this.originNumber = originNumber;
	}

	public String getDestinationNumber() {
		return destinationNumber;
	}

	public void setDestinationNumber(String destinationNumber) {
		this.destinationNumber = destinationNumber;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if (description != null && !description.equals("&nbsp;"))
			this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getCharge() {
		return charge;
	}

	public void setCharge(String charge) {
		this.charge = charge;
	}

	public String getListenId() {
		return listenId;
	}

	public void setListenId(String listenId) {
		this.listenId = listenId;
	}
	
	
}
