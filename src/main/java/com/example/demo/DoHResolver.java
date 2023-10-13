package com.example.demo;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

import com.example.demo.doh.DoHDnsResolver;

import io.netty.resolver.AddressResolver;
import io.netty.resolver.InetNameResolver;
import io.netty.resolver.InetSocketAddressResolver;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;

public class DoHResolver extends InetNameResolver{
	
	private volatile AddressResolver<InetSocketAddress> addressResolver;

	public DoHResolver(EventExecutor executor) {
		super(executor);	
	
	}

    @Override
    protected void doResolve(String inetHost, Promise<InetAddress> promise) throws Exception {
        try {
            promise.setSuccess(DoHDnsResolver.getInstance(null).lookupByName(inetHost, null).findFirst().get());
        } catch (UnknownHostException e) {
            promise.setFailure(e);
        }
    }

    @Override
    protected void doResolveAll(String inetHost, Promise<List<InetAddress>> promise) throws Exception {
        try {
            promise.setSuccess(DoHDnsResolver.getInstance(null).lookupByName(inetHost, null).toList());
        } catch (UnknownHostException e) {
            promise.setFailure(e);
        }
    }

	public AddressResolver<InetSocketAddress> asAddressResolver() {
        AddressResolver<InetSocketAddress> result = addressResolver;
        if (result == null) {
            synchronized (this) {
                result = addressResolver;
                if (result == null) {
                    addressResolver = result = new InetSocketAddressResolver(executor(), this);
                }
            }
        }
        return result;
    }
	
    @Override
    public void close() { }
}
