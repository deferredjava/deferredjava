package org.djava.async;

import java.util.concurrent.Callable;

import org.djava.async.util.DeferredContainer.DeferredTask;
import org.djava.async.util.DeferredContainer.RunnableDeffered;


public class DeferredFactory {
	
	/**
	 * Factory method to create a deferred object.
	 * 
	 * @see Deferred
	 * @see Promise
	 * 
	 * @return the deferred object
	 */
	public static <R> Deferred<R> createDeferred() {
		Deferred<R> instance = new DeferredImpl<>();
		return instance;
	}
	
	/**
	 * Factory method to convert a callable to a deferred task. The task can be submitted to
	 * the deferred container for execution and its promise can be monitored.
	 * 
	 * @see DeferredTask
	 * @see RunnableDeffered
	 * 
	 * @param callable the callable
	 * 
	 * @return the deffered object
	 */
	public static <R> DeferredTask<R> promisify(final Callable<R> callable) {
		return new DeferredTask<>(callable);
	}
	
}
