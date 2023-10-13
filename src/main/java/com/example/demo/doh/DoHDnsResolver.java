package com.example.demo.doh;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.UnknownHostException;
import java.net.spi.InetAddressResolver;
import java.net.spi.InetAddressResolverProvider.Configuration;
import java.util.Arrays;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InetAddresses;
import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class DoHDnsResolver implements InetAddressResolver {

	private static final String IP_4_ARPA_TEMPLATE = "%s.%s.%s.%s.in-addr.arpa.";

	private static final String BASE_URL = " https://dns.google";

	OkHttpClient client;

	Gson gson = new Gson();
	
	InetAddress googleDnsOne;
	InetAddress googleDnsTwo;
	
	private static DoHDnsResolver INSTANCE = null;
	
	private InetAddressResolver defaultResolver;

	private static Logger LOG = LoggerFactory.getLogger(DoHDnsResolver.class);

	public DoHDnsResolver(Configuration configuration) {
		if(configuration != null) {
			this.defaultResolver = configuration.builtinResolver();
		}
		
		googleDnsOne = InetAddresses.forString("8.8.8.8");
		googleDnsTwo = InetAddresses.forString("8.8.4.4");
		
		client = new OkHttpClient.Builder().proxy(new Proxy(Type.HTTP, new InetSocketAddress("127.0.0.1", 3128))).build();		
	}

	@Override
	public Stream<InetAddress> lookupByName(String host, LookupPolicy lookupPolicy) throws UnknownHostException {
		
		HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/resolve").newBuilder();
		urlBuilder.addQueryParameter("name", host);
		urlBuilder.addQueryParameter("type", "A");
		
		Request request = new Request.Builder().url(urlBuilder.build().toString()).build();
		
		new Request.Builder().url("https://dns.google/resolve?name=www.google.com&type=A").build();
		Call call = client.newCall(request);
		try {
			ResponseBody responseBody = call.execute().body();

			DoHResponse response = gson.fromJson(responseBody.string(), DoHResponse.class);
			if (response != null && response.successful()) {
				return Arrays.stream(response.getAnswers()).map(DoHDnsResolver::toInetAddress);
			} else {
				return Stream.empty();
			}
		} catch (IOException e) {
			LOG.error("PTR request failed", e);
			throw new UnknownHostException();
		}
		
	}
	
	public static InetAddress toInetAddress(Answer answer) {	
		return InetAddresses.forString(answer.getData());
	}

	@Override
	public String lookupByAddress(byte[] addr) throws UnknownHostException {
		String url = buildDoHRequestUrl(addr); 

		Request request = new Request.Builder().url(url).build();
		Call call = client.newCall(request);
		try {
			ResponseBody responseBody = call.execute().body();

			DoHResponse response = gson.fromJson(responseBody.string(), DoHResponse.class);
			if (response != null && response.successful()) {
				return response.getAnswers()[0].getData();
			}
		} catch (IOException e) {
			LOG.error("PTR request failed", e);
		}
		return null;
	}

	protected String buildDoHRequestUrl(byte[] addr) throws UnknownHostException {
		HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/resolve").newBuilder();
		if (addr.length == 4) {
			urlBuilder.addQueryParameter("name", getIp4NameParameter(addr));
		} else if (addr.length == 16) {
			urlBuilder.addQueryParameter("name", getIp6NameParameter(addr));
		}
		urlBuilder.addQueryParameter("type", "ptr");
		return urlBuilder.build().toString();
	}
	
	String getIp4NameParameter(byte[] addr){
		return IP_4_ARPA_TEMPLATE.formatted(addr[3] & 0xFF, addr[2] & 0xFF,
				addr[1] & 0xFF, addr[0] & 0xFF);
	}
	
	String getIp6NameParameter(byte[] addr) throws UnknownHostException{
		final InetAddress address = InetAddress.getByAddress(addr);					
		final String[] okktets = address.toString().replaceAll("[/]", "").split(":");
		StringBuffer sb = new StringBuffer();
		for (String string : okktets) {
			String formattedString = String.format("%1$" + 4 + "s", string).replace(' ', '0');
			sb.append(formattedString);
		}
		String test = sb.reverse().toString();
		StringBuffer urlStringBuffer = new StringBuffer();		
		test.chars().forEach(s -> urlStringBuffer.append(Character.valueOf((char)s)+"."));			
		urlStringBuffer.append("ip6.arpa");
		return urlStringBuffer.toString();
	}
	
	public InetAddressResolver getDefaultResolver() {
		return defaultResolver;
	}

	public static InetAddressResolver getInstance(Configuration conf) {
		if(INSTANCE == null) {
			INSTANCE = new DoHDnsResolver(conf);
		}
		return INSTANCE;
	}
}
