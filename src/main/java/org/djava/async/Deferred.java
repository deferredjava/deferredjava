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
package org.djava.async;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.djava.async.Callbacks.FailureCallBack;
import org.djava.async.Callbacks.NotificationCallBack;
import org.djava.async.Callbacks.NotificationEvent;
import org.djava.async.Callbacks.SuccessCallBack;
import org.djava.async.util.DeferredContainer;
import org.djava.async.util.VoidType;
import org.djava.async.util.DeferredContainer.DeferredTask;
import org.djava.async.util.DeferredContainer.RunnableDeffered;

/**
 * Unrestricted interface to access the deferred object. The interface should not be
 * supplied to a consumer who should not change the promise's state. Use {@link Promise} for
 * restricted access.
 * 
 * <p>
 * Use {@link DeferredFactory} to create a deferred object.
 * </p>
 * 
 * @author Prasun Paul
 *
 * @param <R> the resolved value type of the promise
 */
@SuppressWarnings("unchecked")
public abstract class Deferred<R> extends Promise<R> {
	
	public abstract Promise<R> promise();
	
	public abstract void resolve(R value);
	
	public abstract void reject(Exception ex);
	
	public abstract void notify(NotificationEvent event);
	
	/**
	 * Gets the current deferred object.
	 * 
	 * @return the deferred object
	 */
	public Deferred<R> get() {
		return this;
	}
	
	/**
	 * The when method.
	 * 
	 * <p>
	 * When all of the promises are done the next pending promise will be
	 * resolved. The resolve value will be list of resolve values of the promises. The
	 * list order is maintained how the promises appear in when arguments.  
	 * </p>
	 * 
	 * @param promises  the array of promises
	 * 
	 * @return a new <tt>promise</tt>
	 */
	public static <T> Promise<List<T>> when(Promise<T>... promises) {
		final Deferred<List<T>> deferred = DeferredFactory.createDeferred();
		
		if(promises == null || promises.length == 0) {
			deferred.resolve(null);
			return deferred.promise();
		}
		
		final T[] result = (T[]) new Object[promises.length];
		final AtomicInteger completionRemainCount = new AtomicInteger(promises.length);
		
		for(int i = 0; i < promises.length; i++) {
			promises[i].then(new SuccessCallBack<Object, T>(i) {
				@Override
				public Object call(T value) {
					result[index] = value;
					if(completionRemainCount.decrementAndGet() == 0) {
						deferred.resolve(Arrays.asList(result));
					}
					return VoidType.NOTHING;
				}
			}, new FailureCallBack() {
				@Override
				public VoidType call(Exception reason) {
					deferred.reject(reason);
					return VoidType.NOTHING;
				}
			}, new NotificationCallBack() {
				@Override
				public VoidType call(NotificationEvent event) {
					deferred.notify(event);
					return VoidType.NOTHING;
				}
			});
		}
		
		return deferred.promise();
	}
	
	/**
	 * The when method. A useful helper method when there is only one promise
	 * to resolve compare to multiple promise version.
	 * 
	 * @param promise the promise
	 * 
	 * @return a new promise
	 */
	public static <P> Promise<P> when(Promise<P> promise) {
		
		final Deferred<P> deferred = DeferredFactory.createDeferred();
		promise.then(new SuccessCallBack<P, P>() {
			@Override
			public Object call(P value) {
				deferred.resolve(value);
				return VoidType.NOTHING;
			}
		}, new FailureCallBack() {
			@Override
			public VoidType call(Exception reason) {
				deferred.reject(reason);
				return VoidType.NOTHING;
			}
		}, new NotificationCallBack() {
			@Override
			public VoidType call(NotificationEvent event) {
				deferred.notify(event);
				return VoidType.NOTHING;
			}
		});
		
		return deferred.promise();
	}
	
	/**
	 * When method for a callable. The callable will be converted to a <tt>deferred</tt>
	 * task. The task will be submitted to the deferred container for execution. Deferred promise
	 * of the task will be returned.
	 * 
	 * @see DeferredTask
	 * @see DeferredContainer
	 * 
	 * @param callable the callable
	 * 
	 * @return the promise
	 */
	public static <T> Promise<T> when(Callable<T> callable) {
		RunnableDeffered<T> rp = DeferredFactory.promisify(callable);
		rp.submit();
		return rp.promise();
	}
	
	/**
	 * The when method for multiple callables. All of the callables will be converted to its
	 * deferred task. When all of the deferred tasks are completed next pending promise will
	 * be resolved.
	 * 
	 * @see DeferredTask
	 * @see DeferredContainer
	 * 
	 * @param callables the array of callables
	 * 
	 * @return a new promise
	 */
	public static <T> Promise<List<T>> when(Callable<T>... callables) {
		Promise<T>[] promises = (Promise<T>[]) new Promise<?>[callables.length];
		for(int i = 0; i < callables.length; i++) {
			promises[i] = when(callables[i]);
		}
		return when(promises);
	}
	
}
