package com.teskalabs.bsmtt.ipaddress;

/**
 * Serves as a response handler for IP and DNS information obtained from a host.
 * @author Premysl Cerny
 */
public interface GetIPCallback {
	void onIPChanged(int IPFlag, String IPAddress);
	void onDNSChanged(String DNS);
}
