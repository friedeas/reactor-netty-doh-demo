package com.example.demo.doh;

import java.net.spi.InetAddressResolver;
import java.net.spi.InetAddressResolverProvider;

public class TestInetAddressResolverProvider extends InetAddressResolverProvider{

	@Override
	public InetAddressResolver get(Configuration configuration) {
		return DoHDnsResolver.getInstance(configuration);
	}

	@Override
	public String name() {
		return "TestInetAddressResolver";
	}

}
