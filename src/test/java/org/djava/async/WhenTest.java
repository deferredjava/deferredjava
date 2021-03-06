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

import junit.framework.JUnit4TestAdapter;

import org.djava.async.util.DeferredContainer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class WhenTest extends BaseDeferredJavaTest {
	
	@BeforeClass
	public static void setup() throws InterruptedException {
		DeferredContainer.createNewContainer();
	}
	
	@AfterClass
	public static void shutdown() throws InterruptedException {
		DeferredContainer.getContainer().stop();
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testAsyncWhenOne() throws InterruptedException {
		StringBuffer result = new StringBuffer();
		
		Deferred.when(callable100("Hello"))
			.then(concatWith(" "))
			.then(concatWith("World!!"))
			.then(appendResolvedValue(result))
			.fail(failure(result));
		
		try {
			Thread.sleep(300);
		} catch (InterruptedException ex) {
			throw ex;
		}
		
		Assert.assertEquals("Hello World!!", result.toString());
		
		result = new StringBuffer();
		
		Deferred.when(callable100((Object)"Hello"), 
						callable100((Object)" "), 
						callable100((Object)"World"), 
						callable100((Object)"!!"))
					.then(appendResolvedValues(result))
					.fail(failure(result));
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			throw ex;
		}
		
		Assert.assertEquals("Hello World!!", result.toString());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testAsyncWhenAll() throws InterruptedException {
		StringBuffer result = new StringBuffer();
		
		Deferred.when(callable100((Object)"Hello"), 
						callable100((Object)" "), 
						callable100((Object)"World"), 
						callable100((Object)"!!"))
					.then(appendResolvedValues(result))
					.fail(failure(result));
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			throw ex;
		}
		
		Assert.assertEquals("Hello World!!", result.toString());
	}
	
	 public static junit.framework.Test suite( ) 
	   { 
	      return new JUnit4TestAdapter( WhenTest.class ); 
	   }
	
}
