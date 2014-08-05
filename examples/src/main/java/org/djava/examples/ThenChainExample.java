package org.djava.examples;

import org.djava.async.Callbacks;
import org.djava.async.Callbacks.FailureCallBack;
import org.djava.async.Deferred;
import org.djava.async.DeferredFactory;
import org.djava.async.util.VoidType;

public class ThenChainExample {
	
	public void printHelloWorld(boolean accept) {
		Deferred<String> deferred = DeferredFactory.createDeferred();
		
		deferred.then(new Callbacks.SuccessCallBack<String, String>() {
			@Override
			public Object call(String value) {
				return value + " ";
			}
		})
		.then(new Callbacks.SuccessCallBack<String, String>() {
			@Override
			public Object call(String value) {
				return value + "World";
			}
		})
		.then(new Callbacks.SuccessCallBack<String, String>() {
			@Override
			public Object call(String value) {
				return value + "!!";
			}
		})
		.then(new Callbacks.SuccessCallBack<String, String>() {
			@Override
			public Object call(String value) {
				System.out.println(value);
				return null;
			}
		})
		.fail(new FailureCallBack() {
			@Override
			public VoidType call(Exception ex) {
				System.out.println(ex.getMessage());
				return null;
			}
		});
		
		if(accept) {
			deferred.resolve("Hello");
			return;
		}
		
		deferred.reject(new RuntimeException("Hello Hell!!"));
	}
	
	public static void main(String[] args) {
		ThenChainExample example = new ThenChainExample();
		example.printHelloWorld(true);
		example.printHelloWorld(false);
	}

}
