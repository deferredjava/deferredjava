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
