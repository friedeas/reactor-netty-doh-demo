package com.example.demo.doh;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Dns;

public class CustomOkHttpDns implements Dns{
	
	private List<InetAddress> dnsServers;
	
	public CustomOkHttpDns(InetAddress... dnsServer) {
		dnsServers = Arrays.asList(dnsServer);
	}

	@Override
	public List<InetAddress> lookup(final String arg0) throws UnknownHostException {
		return dnsServers;
	}

}
