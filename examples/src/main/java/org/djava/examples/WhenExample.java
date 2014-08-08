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

import java.util.List;
import java.util.concurrent.Callable;

import org.djava.async.Callbacks;
import org.djava.async.Callbacks.SuccessCallBack;
import org.djava.async.Deferred;
import org.djava.async.util.DeferredContainer;
import org.djava.async.util.DeferredContainer.DeferredTask;
import org.djava.async.util.VoidType;

public class WhenExample {
	
	@SuppressWarnings("unchecked")
	public void whenAllExample(final boolean accept) throws Exception {
		
		Deferred.when(new Callable<String>(){
			public String call() throws Exception {
				Thread.sleep(100);
				return "Hello";
			}
		}, new Callable<String>(){
			public String call() throws Exception {
				Thread.sleep(200);
				if(accept) {
					return " ";
				}
				
				throw new RuntimeException("Second call failed");
			}
		}, new Callable<String>(){
			public String call() throws Exception {
				return "World!!";
			}
		}).then(new SuccessCallBack<Object, List<String>>() {
			@Override
			public Object call(List<String> values) {
				String result = "";
				for(String value : values) {
					result = result + value;
				}
				System.out.println(result);
				return VoidType.NOTHING;
			}
		}).fail(new Callbacks.FailureCallBack() {
			@Override
			public VoidType call(Exception reason) {
				System.out.println(reason.getMessage());
				return VoidType.NOTHING;
			}
		});
		
	}
	
	@SuppressWarnings("unchecked")
	public void whenAllExampleWithDeferredTask(final boolean accept) throws Exception {
		
		DeferredTask<String> dt1 = new DeferredTask<>(new Callable<String>(){
			public String call() throws Exception {
				Thread.sleep(100);
				return "Hello";
			}
		});
		
		DeferredTask<String> dt2 = new DeferredTask<>(new Callable<String>(){
			public String call() throws Exception {
				Thread.sleep(200);
				if(accept) {
					return " ";
				}
				
				throw new RuntimeException("Second call failed");
			}
		});
		
		DeferredTask<String> dt3 = new DeferredTask<>(new Callable<String>(){
			public String call() throws Exception {
				return "World!!";
			}
		});
		
		Deferred.when(dt1, dt2, dt3).then(new SuccessCallBack<Object, List<String>>() {
			@Override
			public Object call(List<String> values) {
				String result = "";
				for(String value : values) {
					result = result + value;
				}
				System.out.println(result);
				return VoidType.NOTHING;
			}
		}).fail(new Callbacks.FailureCallBack() {
			@Override
			public VoidType call(Exception reason) {
				System.out.println(reason.getMessage());
				return VoidType.NOTHING;
			}
		});
		
		dt1.submit();
		dt2.submit();
		dt3.submit();
		
	}
	
	public static void main(String[] args) throws Exception {
		//first, we should create the container
		DeferredContainer.createNewContainer();
		
		WhenExample example = new WhenExample();
		example.whenAllExample(true);
		
		//give some time to finish the asynchronous tasks
		Thread.sleep(1000);
		
		example.whenAllExample(false);
		
		///////////////////////////////////////////
		
		example.whenAllExampleWithDeferredTask(true);
		
		//give some time to finish the asynchronous tasks
		Thread.sleep(1000);
		
		example.whenAllExampleWithDeferredTask(false);
		
		//give some time to finish the asynchronous tasks
		Thread.sleep(1000);
		
		//finally, we should stop the container
		DeferredContainer.getContainer().stop();
	}

}
