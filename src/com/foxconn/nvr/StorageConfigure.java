package com.foxconn.nvr;

public class StorageConfigure {
	public static int DEFAULT_STORED_DAY = 30;
	
	private String jpegPath = null;
	private String videoPath = null;
	private final String address;
	private final int storedDay;
	
	public StorageConfigure(String address) {
		this(address, DEFAULT_STORED_DAY);
	}
	
	public StorageConfigure(String address, int storedDay) {
		this.address = address;
		this.storedDay = storedDay;
	}
	
	public String getAddress() {
		return this.address;
	}
	
	public String getVideoPath() {
		return this.videoPath;
	}
	
	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}
	
	public String getJPEGPath() {
		return this.jpegPath;
	}
	
	public void setJPEGPath(String jpegPath) {
		this.jpegPath = jpegPath;
	}
	
	public int getStoredDay() {
		return this.storedDay;
	}
	
}
