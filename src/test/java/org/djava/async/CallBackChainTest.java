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

import org.junit.Assert;
import org.junit.Test;

public class CallBackChainTest extends BaseDeferredJavaTest {
	
	@Test
	public void testThenResolve() {
		
		StringBuffer result = new StringBuffer();
		Deferred<String> d1 = DeferredFactory.createDeferred();
		
		d1.then(concatWith(" "), failure(result), null)
			.then(concatWith("World!!"), failure(result), null)
			.then(appendResolvedValue(result), failure(result), null);
		
		Assert.assertTrue(d1.promise().isPending());
		d1.resolve("Hello");
		Assert.assertTrue(d1.promise().isFulfilled());
		Assert.assertEquals("Hello World!!", result.toString());
		
		result = new StringBuffer();
		d1 = DeferredFactory.createDeferred();
		
		d1.promise()
			.then(concatWith(" "))
			.then(concatWith("World!!"))
			.fail(failure(result))
			.then(appendResolvedValue(result), failure(result), null);
		
		Assert.assertTrue(d1.promise().isPending());
		d1.resolve("Hello");
		Assert.assertTrue(d1.promise().isFulfilled());
		Assert.assertEquals("Hello World!!", result.toString());
	}
	
	@Test
	public void testThenReject() {
		
		StringBuffer result = new StringBuffer();
		Deferred<String> d1 = DeferredFactory.createDeferred();
		
		d1.then(concatWith(" "), failure(result), null)
			.then(concatWith("World!!"), failure(result), null)
			.then(appendResolvedValue(result), failure(result), null);
		
		Assert.assertTrue(d1.promise().isPending());
		d1.reject(new RuntimeException("Hello Hell!!"));
		Assert.assertTrue(d1.promise().isRejected());
		Assert.assertEquals("Hello Hell!!", result.toString());
		
		result = new StringBuffer();
		d1 = DeferredFactory.createDeferred();
		
		//we can consolidate all the exceptions to the last failure.
		//that should change our result.
		d1.promise()
			.then(concatWith(" "))
			.then(concatWith("World!!"))
			.then(appendResolvedValue(result))
			.fail(failure(result));
		
		Assert.assertTrue(d1.promise().isPending());
		d1.reject(new RuntimeException("Hello Hell!!"));
		Assert.assertTrue(d1.promise().isRejected());
		Assert.assertEquals("Hello Hell!!", result.toString());
		
		result = new StringBuffer();
		d1 = DeferredFactory.createDeferred();
		
		//reject for internal exception
		d1.promise()
			.then(concatWith(" "))
			.then(concatWith("World!!"))
			.then(throwExceptionInSuccessCallBack("Hello Hell!!"))
			.fail(failure(result));
		
		d1.resolve("Hello");
		Assert.assertEquals("Hello Hell!!", result.toString());
	}
	
	@Test
	public void testPromiseJoin() {
		StringBuffer result = new StringBuffer();
		
		Deferred<String> d1 = DeferredFactory.createDeferred();
		Deferred<String> d2 = DeferredFactory.createDeferred();
		
		Promise<String> p1 = d1
				.then(echoResolvedValue("Hello"))
				.then(concatWith(" "));
		
		Promise<String> p2 = d2
			.then(echoResolvedValue("World"))
			.then(concatWith("!!"));
		
		p1.join(p2, failure(result), null)
			.then(appendResolvedValues(result))
			.fail(failure(result));
		
		d1.resolve("");
		d2.resolve("");
		
		Assert.assertEquals("Hello World!!", result.toString());
	}

}
