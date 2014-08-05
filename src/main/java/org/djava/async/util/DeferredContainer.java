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
package org.djava.async.util;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.djava.async.Deferred;
import org.djava.async.DeferredFactory;
import org.djava.async.Promise;

/**
 * A <tt>micro container</tt> for executing deferred tasks. Use the factory
 * methods to create a container instance.
 * 
 * @author Prasun Paul
 *
 */
public class DeferredContainer {
	
	private ThreadPoolExecutor executor;
	private static DeferredContainer container;
	
	private DeferredContainer() {
		int availableProcessors = Runtime.getRuntime().availableProcessors();
		int threadPoolSize = availableProcessors*2;
		executor = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS,  
						new LinkedBlockingQueue<Runnable>(), new DeferredContainerThreadFactory());
	}
	
	private DeferredContainer(int threadPoolSize) {
		this(new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS,  
				new LinkedBlockingQueue<Runnable>(), new DeferredContainerThreadFactory()));
	}
	
	private DeferredContainer(ThreadPoolExecutor executor) {
		this.executor = executor;
	}
	
	/**
	 * Creates a container with the default setup.
	 * 
	 */
	public synchronized static void createNewContainer() {
		if(container == null) {
			container = new DeferredContainer();
		} 
		
		return;
	}
	
	/**
	 * Creates a container by passing a {@link ThreadPoolExecutor}.
	 * 
	 * @param executor
	 */
	public static void createNewContainer(ThreadPoolExecutor executor) {
		if(container == null) {
			container = new DeferredContainer(executor);
		}
		
		return;
	}
	
	/**
	 * Creates a container with a thread pool size.
	 * 
	 * @param threadPoolSize the size of the thread pool
	 */
	public static void createNewContainer(int threadPoolSize) {
		if(container == null) {
			container = new DeferredContainer(threadPoolSize);
		}
		
		return;
	}

	/**
	 * Stops the container
	 * 
	 */
	public synchronized void stop() {
		executor.shutdown();
		container = null;
	}
	
	/**
	 * Main method to submit a task to the executor. The container should be running before
	 * submitting any task.
	 * 
	 * @param runnable the runnable deferred
	 * @return future that can be used to further control the task
	 */
	private <R> Future<?> submit(RunnableDeffered<R> runnable) {
		if(executor.isShutdown()) {
			throw new RuntimeException("The task can not be submitted. The container is not running.");
		}
		
		if(executor.isTerminating()) {
			throw new RuntimeException("The task can not be submitted. The container is terminating.");
		}
		
		return executor.submit(runnable);
	}
	
	/**
	 * Gets the container.
	 * 
	 * @return the deferred container to execute the tasks
	 */
	public static DeferredContainer getContainer() {
		return container;
	}
	
	/**
	 * The main interface to implement a deferred runnable task which can be submitted to
	 * a {@link Executor} for executing asynchronously. 
	 * 
	 * <p>
	 * @see DeferredTask An example implementation.
	 * </p>
	 *
	 * @param <R>
	 */
	public static abstract class RunnableDeffered<R> implements Runnable {
		
		private Deferred<R> deferred = DeferredFactory.createDeferred();
		private Future<?> future = null;
		private boolean submitted = false;

		@Override
		public abstract void run();
		
		protected Deferred<R> deferred() {
			return deferred;
		}
		
		public Promise<R> promise() {
			return deferred.promise();
		}
		
		public boolean cancel(boolean cancelRunningTask) {
			if(!isSubmitted()) {
				return true;
			}
			
			future.cancel(cancelRunningTask);
			future = null;
			
			submitted = false;
			
			return true;
		}
		
		public void submit() {
			future = DeferredContainer.getContainer().submit(this);
			submitted = true;
		}
		
		public boolean isSubmitted() {
			return submitted;
		}
		
	}
	
	/**
	 * An implementation of {@link RunnableDeffered} which can be used to convert
	 * a callable object to a deferred object. 
	 *
	 * @param <R>
	 */
	public static class DeferredTask<R> extends RunnableDeffered<R> {
		
		private Callable<R> callable;

		public DeferredTask(Callable<R> callable) {
			this.callable = callable;
		}

		/**
		 * The run method.
		 * 
		 * <p>
		 * First, the callable object is called to get the result. Then the promise 
		 * is resolved with that result.
		 * </p>
		 * 
		 */
		@Override
		public void run() {
			try {
				if(callable == null) {
					deferred().reject(new NullPointerException("Callable is empty."));
				}
				
				R result = callable.call();
				deferred().resolve(result);
			} catch (Exception ex) {
				deferred().reject(ex);
			}
		}
		
	}
	
	/**
	 * The container thread factory for creating new threads. 
	 *
	 */
	static class DeferredContainerThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DeferredContainerThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                                  Thread.currentThread().getThreadGroup();
            namePrefix = "deferred-container-pool-" + poolNumber.getAndIncrement() + "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                                  namePrefix + threadNumber.getAndIncrement(),
                                  0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            
            return t;
        }
    }

}
