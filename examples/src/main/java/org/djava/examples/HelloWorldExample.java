/*
 * Copyright 2014 The DeferredJava Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.djava.examples;

import org.djava.async.Callbacks;
import org.djava.async.Callbacks.FailureCallBack;
import org.djava.async.Callbacks.NotificationEvent;
import org.djava.async.Callbacks.SuccessCallBack;
import org.djava.async.Deferred;
import org.djava.async.DeferredFactory;
import org.djava.async.util.VoidType;

public class HelloWorldExample {
	
	public void printHelloWorldAccept() {
		Deferred<String> deferred = DeferredFactory.createDeferred();
		
		deferred.then(new SuccessCallBack<VoidType, String>() {
			@Override
			public Object call(String value) {
				System.out.println(value);
				return VoidType.NOTHING;
			}
		});
		
		deferred.resolve("Hello World!!");
	}
	
	public void printHelloWorldAReject() {
		Deferred<String> deferred = DeferredFactory.createDeferred();
		
		deferred.then(
			new SuccessCallBack<VoidType, String>() {
				@Override
				public Object call(String value) {
					System.out.println(value);
					return VoidType.NOTHING;
				}
			}, 
			new FailureCallBack() {
				@Override
				public VoidType call(Exception reason) {
					System.out.println(reason.getMessage());
					return VoidType.NOTHING;
				}
			}, null);
		
		deferred.reject(new RuntimeException("Hello Hell!!"));
	}
	
	public void printHelloWorldANotify() {
		Deferred<String> deferred = DeferredFactory.createDeferred();
		
		deferred.then(
			new Callbacks.SuccessCallBack<VoidType, String>() {
				@Override
				public Object call(String value) {
					System.out.println(value);
					return VoidType.NOTHING;
				}
			}, 
			new Callbacks.FailureCallBack() {
				@Override
				public VoidType call(Exception reason) {
					System.out.println(reason.getMessage());
					return VoidType.NOTHING;
				}
			}, 
			new Callbacks.NotificationCallBack() {
				@Override
				public VoidType call(NotificationEvent event) {
					System.out.println("An update received!!");
					return null;
				}
			});
		
		deferred.notify(new NotificationEvent(deferred.promise()));
	}
	
	public static void main(String[] args) {
		HelloWorldExample example = new HelloWorldExample();
		example.printHelloWorldAccept();
		example.printHelloWorldAReject();
		example.printHelloWorldANotify();
	}

}
