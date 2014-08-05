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

import org.djava.async.util.VoidType;

public class Callbacks {
	
	public static enum CallBackType {
		SUCCESS, FAILURE, NOTIFICATION;
		
		public boolean isSuccess() {
			return SUCCESS.equals(this);
		}
		
		public boolean isFailure() {
			return FAILURE.equals(this);
		}
		
		public boolean isNotification() {
			return NOTIFICATION.equals(this);
		}
	}
	
	public static class NotificationEvent {
		
		private Promise<?> src;

		public NotificationEvent(Promise<?> src) {
			this.src = src;
		}
		
		public Promise<?> getSrc() {
			return src;
		}
	}
	
	public static interface CallBack<R, A> {
		R call(A arg);
		CallBackType getType();
	}
	
	public abstract static class SuccessCallBack<F, A> implements CallBack<Object, A> {
		
		protected int index;

		public SuccessCallBack() {
			
		}
		
		public SuccessCallBack(int index) {
			this.index = index;
		}

		@Override
		public abstract Object call(A arg);
		
		@Override
		public CallBackType getType() {
			return CallBackType.SUCCESS;
		}
	}
	
	public abstract static class FailureCallBack implements CallBack<VoidType, Exception> {

		@Override
		public abstract VoidType call(Exception arg);
		
		@Override
		public CallBackType getType() {
			return CallBackType.FAILURE;
		}
	}
	
	public abstract static class NotificationCallBack implements CallBack<VoidType, NotificationEvent> {

		@Override
		public abstract VoidType call(NotificationEvent event);
		
		@Override
		public CallBackType getType() {
			return CallBackType.NOTIFICATION;
		}
	}
	
}
