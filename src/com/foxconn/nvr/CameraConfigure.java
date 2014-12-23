package com.foxconn.nvr;

public class CameraConfigure {
	
//	public static void main(String[] args) {
//		String p = "10.120.135.47==B04-2F-D5-037==B04-2F";
//		String[] pp = p.split("==");
//		System.out.println(pp.length);
//		for (String s : pp) {
//			System.out.println(s);
//		}
//	}
	
	private final String address;
	private final String title;
	private final String model;
	private String user = null;
	private String password = null;
	private int cctvId = 0;
	
	public CameraConfigure(String address, String title, String model) {
		if (address == null)
			throw new NullPointerException("address is NULL");
		if (title == null)
			throw new NullPointerException("title is NULL");
		if (model == null)
			throw new NullPointerException("model is NULL");
		this.address = address;
		this.title = title;
		this.model = model;
	}
	
	public String getAddress() {
		return this.address;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public String getModel() {
		return this.model;
	}
	
	public int getCctvId() {
		return this.cctvId;
	}
	
	public void setCctvId(int cctvId) {
		this.cctvId = cctvId;
	}
	
	public String getUser() {
		return this.user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
}
