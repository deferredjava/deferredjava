package org.djava.examples;

import java.util.List;
import java.util.concurrent.Callable;

import org.djava.async.Callbacks;
import org.djava.async.Callbacks.SuccessCallBack;
import org.djava.async.Deferred;
import org.djava.async.util.DeferredContainer;
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
	
	public static void main(String[] args) throws Exception {
		//first, we should create the container
		DeferredContainer.createNewContainer();
		
		WhenExample example = new WhenExample();
		example.whenAllExample(true);
		
		//give some time to finish the asynchronous tasks
		Thread.sleep(1000);
		
		example.whenAllExample(false);
		
		//give some time to finish the asynchronous tasks
		Thread.sleep(1000);
		
		//finally, we should stop the container
		DeferredContainer.getContainer().stop();
	}

}
