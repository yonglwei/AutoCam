package com.hunter.camservice.connectivity;

public enum ConnectivityStatus {
	NO_CONNECT("No connection"),
	CONNECT_WIFI("Wifi"),
	CONNECT_4G("4g"),
	CONNECT_3G("3g"),
	CONNECT_2G("2g"),
	CONNECT_OTHER("Unkown");
	
	private String des;
	
	ConnectivityStatus(String des) {
		this.des = des;
	}
	
	public String toString() {
		return this.des;
	}
}
