package com.example.demo;

import java.net.InetSocketAddress;

import io.netty.resolver.AddressResolver;
import io.netty.resolver.AddressResolverGroup;
import io.netty.util.concurrent.EventExecutor;

public class CustomResolverGroup extends AddressResolverGroup<InetSocketAddress>{
	
	public static final CustomResolverGroup INSTANCE = new CustomResolverGroup();

	private DoHResolver dohResolver;
	
	private CustomResolverGroup() { }
	
	@Override
	protected AddressResolver<InetSocketAddress> newResolver(EventExecutor executor) throws Exception {
		dohResolver = new DoHResolver(executor);
		return dohResolver.asAddressResolver();
	}

	@Override
	public void close() {
		dohResolver.close();
		super.close();
	}

}
