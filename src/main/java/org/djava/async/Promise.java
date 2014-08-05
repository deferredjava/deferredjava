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

import java.util.List;

import org.djava.async.Callbacks.FailureCallBack;
import org.djava.async.Callbacks.NotificationCallBack;
import org.djava.async.Callbacks.SuccessCallBack;

/**
 * Public interface to access the deferred object.
 * 
 * @author Prasun Paul
 *
 * @param <R> the resolve value type of the promise
 */
public abstract class Promise<R> {
	
	/**
	 * Promise states.
	 * 
	 * Every promise initially is in pending state. The state can be moved from 
	 * pending to fulfilled or rejected. Once any promise is in fulfilled or rejected sate 
	 * it cannot be moved to any other state.
	 *
	 */
	enum STATE {
		PENDING, FULFILLED, REJECTED;
	}
	
	/**
	 * The main then method.
	 * 
	 * <p>
	 * All success, failure and notification callbacks are optional and therfore can be null. This method
	 * can be called multiple times and each call will return a new <tt>promise</tt>.
	 * </p>
	 * 
	 * <p>
	 * When promise is accepted all respective success callbacks will be executed. When promise is rejected 
	 * all respective failure callbacks will be executed. If there is no success or failure callback, the
	 * respective pending <tt>promise</tt> will be called with promise's value or rejection reason.
	 * </p>
	 * 
	 * <p>
	 * The callbacks will not be executed more than once.
	 * </p>
	 * 
	 * @param success the success callback, can be optional
	 * @param failure the failure callback, can be optional
	 * @param notification the notification callback, can be optional
	 * 
	 * @return a new promise
	 */
	public abstract <F> Promise<F> then(SuccessCallBack<F, R> success, FailureCallBack failure, NotificationCallBack notification);
	
	/**
	 * The then method. It is helpful to push any failure or notification from
	 * upper or current level to lower level.
	 * 
	 * This method can be called multiple times and each call will return a new <tt>promise</tt>.
	 * 
	 * @param success the success callback which can be optional
	 * 
	 * @return a new promise
	 */
	public <F> Promise<F> then(SuccessCallBack<F, R> success) {
		return then(success, null, null);
	}
	
	/**
	 * The fail method to receive failures. It is useful to consolidate all failures
	 * from the above levels.
	 * 
	 * @param failure the failure callback
	 * 
	 * @return a new promise
	 */
	public Promise<R> fail(FailureCallBack failure) {
		return then(null, failure, null);
	}
	
	/**
	 * The notification method to consolidate all of the notifications from the above level.
	 * 
	 * @param notification the notification callback
	 * 
	 * @return a new promise
	 */
	public Promise<R> notify(NotificationCallBack notification) {
		return then(null, null, notification);
	}
	
	/**
	 * The join method to join two different promises and forwards their results in a list
	 * to the next promise in the chain.
	 * 
	 * @param promise the another promise
	 * @param failure the failure callback. it is optional
	 * @param notification the notification callback. it is optional
	 * 
	 * @return the list of resolved values
	 */
	public abstract Promise<List<Object>> join(final Promise<?> promise, FailureCallBack failure, NotificationCallBack notification);
	
	/**
	 * The join method without failure and notification callbacks. The failure and
	 * notification calls are forwarded to the lower levels in the chain. 
	 * 
	 * @param promise the another promise
	 * 
	 * @return the list of resolved values
	 */
	public Promise<List<Object>> join(final Promise<?> promise) {
		return join(promise, null, null);
	}
	
	/**
	 * Gets the promise'e resolved value. It returns null when the promise is
	 * rejected and throws exception when the promise is pending.
	 * 
	 * @return the resolved value
	 */
	public abstract <T> T getResult();
	
	public abstract boolean isPending();
	
	public abstract boolean isFulfilled();
	
	public abstract boolean isRejected();
	
	/**
	 * Gets the current promise object.
	 * 
	 * @return the promise
	 */
	public Promise<R> get() {
		return this;
	}
	
	/**
	 * The thenable interface. It helps to convert non-promise objects to promise 
	 * objects easily.
	 *
	 */
	public static interface Thenable {
		void then(SuccessCallBack<?, ?> resolvePromise, FailureCallBack rejectPromise, NotificationCallBack notifyPromise);
	}
	
}
