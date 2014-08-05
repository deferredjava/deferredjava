package org.djava.async.util;

@SuppressWarnings("unchecked")
public class PromiseResult {
	
	private Object result;
	
	public PromiseResult(Object result) {
		this.result = result;
	}
	
	public <T> T get() {
		return (T) result;
	}

}
