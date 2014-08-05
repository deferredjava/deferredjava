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
