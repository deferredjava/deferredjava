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
import java.util.concurrent.Callable;

import org.djava.async.Callbacks.FailureCallBack;
import org.djava.async.Callbacks.SuccessCallBack;
import org.djava.async.util.VoidType;

public abstract class BaseDeferredJavaTest {
	
	protected SuccessCallBack<String, String> echoResolvedValue(final String nv) {
		SuccessCallBack<String, String> success = new SuccessCallBack<String, String>() {
			@Override
			public Object call(String value) {
				return nv;
			}
		};
		return success;
	}
	
	protected SuccessCallBack<String, String> concatWith(final String concatTo) {
		SuccessCallBack<String, String> success = new SuccessCallBack<String, String>() {
			@Override
			public Object call(String value) {
				String newValue = value + concatTo;
				return newValue;
			}
		};
		return success;
	}
	
	protected SuccessCallBack<String, String> throwExceptionInSuccessCallBack(final String message) {
		SuccessCallBack<String, String> success = new SuccessCallBack<String, String>() {
			@Override
			public Object call(String value) {
				throw new RuntimeException(message);
			}
		};
		return success;
	}
	
	protected SuccessCallBack<String, String> appendResolvedValue(final StringBuffer appendTo) {
		SuccessCallBack<String, String> success = new SuccessCallBack<String, String>() {
			@Override
			public Object call(String value) {
				appendTo.append(value);
				return VoidType.NOTHING;
			}
		};
		return success;
	}
	
	protected SuccessCallBack<String, List<Object>> appendResolvedValues(final StringBuffer appendTo) {
		SuccessCallBack<String, List<Object>> success = new SuccessCallBack<String, List<Object>>() {
			@Override
			public Object call(List<Object> values) {
				for(Object value : values) {
					appendTo.append(value);
				}
				return VoidType.NOTHING;
			}
		};
		return success;
	}
	
	protected FailureCallBack failure(final StringBuffer appendTo) {
		return new FailureCallBack() {
			@Override
			public VoidType call(Exception reason) {
				appendTo.append(reason.getMessage());
				return VoidType.NOTHING;
			}
		};
	}
	
	protected <R> Callable<R> callable100(final R value) {
		return new Callable<R>() {
			@Override
			public R call() throws Exception {
				Thread.sleep(100);
				return value;
			}
		};
	}

	
}
