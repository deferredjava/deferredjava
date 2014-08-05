package org.djava.async;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.djava.async.Callbacks.CallBack;
import org.djava.async.Callbacks.FailureCallBack;
import org.djava.async.Callbacks.NotificationCallBack;
import org.djava.async.Callbacks.NotificationEvent;
import org.djava.async.Callbacks.SuccessCallBack;
import org.djava.async.util.PromiseResult;
import org.djava.async.util.VoidType;

/**
 * A thread safe non-blocking deferred implementation. It implements both unrestricted({@link Deferred}) 
 * and public({@link Promise}) interfaces to access the deferred object. Public or restricted <tt>Promise</tt>
 * implementation is required so that the deferred consumers other than the creator of the <tt>Deferred</tt> 
 * can not access the <tt>Promise</tt> methods that can change the Promise's state.
 * 
 * <p>
 * Use {@link DeferredFactory} to create the deferred object.
 * </p>
 * 
 * <p>
 * See <tt>Promises/A+</tt> specification, <a>https://github.com/promises-aplus/promises-spec</a>
 * </p>
 * 
 * @author Prasun Paul
 * 
 * @see Deferred
 * @see Promise
 *
 * @param <R> the result type of the Promise
 */
@SuppressWarnings("unchecked")
class DeferredImpl<R> extends Deferred<R> {
	
	private PromiseImpl promise;
	
	public DeferredImpl() {
		promise = new PromiseImpl(); 
	}
	
	/**
	 * Gets the public or restricted promise object.
	 * 
	 * @return the promise object.
	 */
	public Promise<R> promise() {
		return promise;
	}
	
	/**
	 * Method to resolve the <tt>promise</tt>. This method should not be called more
	 * than once. If called more than once it is ensured that the promise's state
	 * does not change. 
	 * 
	 * @see PromiseImpl
	 * 
	 * @param value the resolved value
	 * 
	 */
	public void resolve(R value) {
		if(value instanceof Exception) {
			reject((Exception) value);
			return;
		}
		
		if(value instanceof Promise) {
			PromiseImpl promise = (PromiseImpl) value;
			if(promise.isPending()) {
				if(!promise.resolveCalledButWaiting.compareAndSet(false, true)) {
					throw new RuntimeException("Resolve allready called but waiting to be fullfilled.");
				}
				
				promise.deferred.thenResolve(this, value);
				return;
			}
		}
		
		fulfill(value);
	}
	
	/**
	 * Method to fulfill the <tt>promise</tt>
	 * 
	 * @param the resolved value
	 */
	private void fulfill(Object value) {
		if(!promise.state.compareAndSet(STATE.PENDING, STATE.FULFILLED)) {
			throw new RuntimeException("Promise is resolved.");
		}
		
		promise.newResult = new PromiseResult(value);
		signalHandlersResolved(value);
	}

	/**
	 * Method to reject the promise.
	 * 
	 * @see PromiseImpl
	 * 
	 * @param ex the rejection reason
	 */
	public void reject(Exception ex) {
		if(!promise.state.compareAndSet(STATE.PENDING, STATE.REJECTED)) {
			throw new RuntimeException("Promise is resolved.");
		}
		
		promise.newResult = new PromiseResult(ex);
		signalHandlersRejected((Exception) promise.newResult.get());
	}
	
	/**
	 * Method to notify about the promise.
	 * 
	 * @param the notification event
	 */
	public void notify(NotificationEvent event) {
		if(!isPending()) {
			//we don't need to get notified for a resolved promise
			return;
		}
		
		for(CompletionHandler<?, ?> handler : promise.handlers) {
			handler.notify(event);
		}
	}
	
	/**
	 * Calls resolve handlers.
	 * 
	 * @param value the resolved value
	 */
	private void signalHandlersResolved(Object value) {
		while(true) {
			CompletionHandler<?, ?> handler = promise.handlers.poll();
			if(handler == null) {
				break;
			}
			
			handler.resolve(value);
		}
	}
	
	/**
	 * Calls rejection handlers.
	 * 
	 * @param ex the exception
	 */
	private void signalHandlersRejected(Exception ex) {
		while(true) {
			CompletionHandler<?, ?> handler = promise.handlers.poll();
			if(handler == null) {
				break;
			}
			
			handler.reject(ex);
		}
	}
	
	/**
	 * The then method. The call is delegated to the public promise interface.
	 * 
	 * @see PromiseImpl
	 * 
	 * @return a new promise
	 */
	@Override
	public <F> Promise<F> then(SuccessCallBack<F, R> success,
			FailureCallBack failure, NotificationCallBack notification) {
		return promise.then(success, failure, notification);
	}

	/**
	 * The join method. The call is delegated to the public promise interface.
	 * 
	 * @see PromiseImpl
	 * 
	 * @return a new promise
	 */
	@Override
	public Promise<List<Object>> join(Promise<?> otherPromise,
			FailureCallBack failure, NotificationCallBack notification) {
		return promise.join(otherPromise, failure, notification);
	}
	
	/**
	 * Gets the promise's resolved value
	 * 
	 * @return the resolved value
	 */
	@Override
	public <T> T getResult() {
		return promise.getResult();
	}

	@Override
	public boolean isPending() {
		return promise.isPending();
	}

	@Override
	public boolean isFulfilled() {
		return promise.isFulfilled();
	}

	@Override
	public boolean isRejected() {
		return promise.isRejected();
	}
	
	/**
	 * The <tt>promise</tt> resolution procedure.
	 * 
	 * @param pendingDeferred
	 * @param value
	 */
	private <F> void fulfillPromise(final Deferred<F> pendingDeferred, final Object value) {
		//If both pending promise and the value are same object then reject
		//the pending promise with with a type error as the reason.
		if(pendingDeferred == value) {
			pendingDeferred.reject(new RuntimeException("Type error."));
			return;
		}
		
		//If value is a promise, pending promise should adapt its state.
		if(value instanceof Promise) {
			PromiseImpl promise = (PromiseImpl) value;
			promise.deferred.thenResolve(pendingDeferred);
			return;
		}
		
		//When the value is a thenable the pending promise should adapt its state under the assumption that
		//it behaves atleast somewhat like a <tt>promise</tt>.
		if(value instanceof Thenable) {
			Thenable thenable = (Thenable) value;
			try {
				thenable.then(new SuccessCallBack<Object, Object>() {
					@Override
					public Object call(Object value) {
						//This can lead to a infinite recursion, for example t1->t2->t1
						fulfillPromise(pendingDeferred, value);
						return VoidType.NOTHING;
					}
				}, new FailureCallBack() {
					@Override
					public VoidType call(Exception reason) {
						pendingDeferred.reject(reason);
						return VoidType.NOTHING;
					}
				}, new NotificationCallBack() {
					@Override
					public VoidType call(NotificationEvent event) {
						pendingDeferred.notify(event);
						return VoidType.NOTHING;
					}
				});
			}
			catch(Exception ex) {
				pendingDeferred.reject(ex);
			}
			
			return;
		}
		
		//If the value is an object then resolve the pending promise with the value
		pendingDeferred.resolve((F) value);
	}

	/**
	 * Executes a callback safely. All exception will be catched and
	 * returns as exception. 
	 * 
	 * @param callBack
	 * @param value
	 * @return
	 */
	private <P> Object executeCallBack(CallBack<?, P> callBack, P value) {
		
		if(value == null) return null;
		
		try {
			Object callBackResult = callBack.call(value);
			return callBackResult;
		}
		catch(Exception ex) {
			return ex;
		}
		
	}
	
	private <F> void thenResolve(final Deferred<F> deferred, final Object resolveValue) {
		then(new SuccessCallBack<F, R>() {
			@Override
			public Object call(R value) {
				deferred.resolve((F) resolveValue);
				return VoidType.NOTHING;
			}
		}, new FailureCallBack() {
			@Override
			public VoidType call(Exception ex) {
				deferred.reject(ex);
				return VoidType.NOTHING;
			}
		}, null);
	}
	
	private <F> void thenResolve(final Deferred<F> deferred) {
		then(new SuccessCallBack<F, R>() {
			@Override
			public Object call(R value) {
				deferred.resolve((F) value);
				return VoidType.NOTHING;
			}
		}, new FailureCallBack() {
			@Override
			public VoidType call(Exception ex) {
				deferred.reject(ex);
				return VoidType.NOTHING;
			}
		}, null);
	}
	
	/**
	 * Non-blocking threadsafe public or restricted <tt>Promise</tt> implementation. This interface can be
	 * supplied to the consumers safely.
	 *
	 * Why threadsafety?
	 * If two or more different consumers from different threads want to register their callbacks 
	 * to the same promise object, we should add the callbacks in a threadsafe way otherwise unexpected 
	 * result might happen.
	 */
	private class PromiseImpl extends Promise<R> {
		
		AtomicBoolean resolveCalledButWaiting = new AtomicBoolean(false);
		AtomicReference<STATE> state = new AtomicReference<>(STATE.PENDING);
		
		PromiseResult newResult;
		
		DeferredImpl<R> deferred = (DeferredImpl<R>) DeferredImpl.this;
		
		ConcurrentLinkedQueue<CompletionHandler<?, ?>> handlers;
		
		public PromiseImpl() {
			handlers = new ConcurrentLinkedQueue<CompletionHandler<?, ?>>();
		}

		/**
		 * The main then method.
		 * 
		 * <p>
		 * All success, failure and notification callbacks are optional and can be null. This method
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
		@Override
		public <F> Promise<F> then(SuccessCallBack<F, R> success, FailureCallBack failure, NotificationCallBack notification) {
			DeferredImpl<F> deferred = new DeferredImpl<>();
			
			CompletionHandler<F, R> handler = new CompletionHandler<F, R>(success, failure, notification, deferred);
			
			if(!isPending()) {
				
				waitTillResultUpdated();
				
				//FIXME: order is not thread safe
//				if(!isRejected()) handler.resolve(newResult.get());
//				else handler.reject((Exception) newResult.get());
				
				if(!isRejected()) {
					handlers.add(handler);
					signalHandlersResolved(newResult.get());
				}
				else {
					signalHandlersRejected((Exception) newResult.get());
				}
				
				return deferred.promise();
			}
			
			handlers.add(handler);
			
			return deferred.promise();
		}

		/**
		 * The main join method to join two different promises. The results of the both promises forward
		 * to the next promise's in the chain.
		 * 
		 * @param promise the another promise to join with current promise
		 * @param failure the failure callback
		 * @param notification the notification callback
		 * 
		 * @return the results from the both promises
		 */
		public Promise<List<Object>> join(final Promise<?> promise, FailureCallBack failure, NotificationCallBack notification) {
			DeferredImpl<List<Object>> deferred = new DeferredImpl<>();
			
			promise.fail(failure);
			
			final Promise<?> self = this;
			SuccessCallBack<List<Object>, Object> success = new SuccessCallBack<List<Object>, Object>() {
				@Override
				public Object call(Object arg) {
					return Deferred.when((Promise<Object>)self, (Promise<Object>)promise);
				}
			}; 
			
			CompletionHandler<List<Object>, Object> handler = new CompletionHandler<>(success, failure, notification, deferred);
			if(!isPending()) {
				if(isFulfilled()) {
					handler.resolve(getResult());
				}
				if(isRejected()) {
					handler.reject((Exception) getResult());
				}
				return deferred.promise();
			}
			
			handlers.add(handler);
			
			return deferred.promise();
		}
		
		/**
		 * As we are implementing non-blocking thread safety it might happen when
		 * the state is updated but result is not yet updated. So, wait a little bit till
		 * the result update is finished.
		 * 
		 */
		private void waitTillResultUpdated() {
			while(promise.newResult == null) {
				//wait loop
			}
		}
		
		@Override
		public boolean isPending() {
			return STATE.PENDING.equals(state.get());
		}
		
		@Override
		public boolean isFulfilled() {
			return STATE.FULFILLED.equals(state.get());
		}
		
		@Override
		public boolean isRejected() {
			return STATE.REJECTED.equals(state.get());
		}
		
		@Override
		public <T> T getResult() {
			if(isPending()) {
				throw new RuntimeException("The promise is not resolved.");
			}
			
			waitTillResultUpdated();
			
			return newResult.get();
		}

	}
	
	/**
	 * The completion handler.
	 * 
	 * @param <F> the type of final resolved value
	 * @param <P> the type of current promise's resolved value
	 */
	private class CompletionHandler<F, P> {
		
		private SuccessCallBack<F, P> success;
		private FailureCallBack failure;
		private NotificationCallBack notification;
		private Deferred<F> deferred;

		CompletionHandler(SuccessCallBack<F, P> success, FailureCallBack failure, NotificationCallBack notification, Deferred<F> deferred) {
			this.success = success;
			this.failure = failure;
			this.notification = notification;
			this.deferred = deferred;
		}
		
		void resolve(Object value) {
			if(success == null) {
				deferred.resolve((F)value);
				return;
			}
			
			Object callBackValue = executeCallBack(success, (P)value);
			if(callBackValue == null) {
				deferred.resolve((F)value);
				return;
			}
			
			if(callBackValue instanceof Exception) {
				deferred.reject((Exception) callBackValue);
				return;
			}
			
			fulfillPromise(deferred, callBackValue);
		}
		
		void reject(Exception ex) {
			if(failure == null) {
				deferred.reject(ex);
				return;
			}
			
			failure.call(ex);
		}
		
		void notify(NotificationEvent event) {
			if(notification == null) {
				return;
			}
			
			notification.call(event);
		}
	}
}
