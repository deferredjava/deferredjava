# DeferredJava Project

**`DeferredJava`** is a non-blocking, thread safe `Deferred/Promise` implementation for `Java`. It provides an easy to use fluent `Deferred/Promise` API that can be used to manage complex flow of asynchronus operations easily.

## Usage

Both `Deferred` and `Promise` means the same thing a `promise` that represents the result of a `asynchronus operation` that is not completed. The difference is that `Deferred` provides a `interface` that can access to the `Promise object` to change its state where as `Promise` provides an interface that cannot.

It is not very easy to controll a complex flow of asynchronus operations specially where there are many asynchronus operations needs to be done and one or more operations depands on another one or some other operations to complete and those other operations may wait for other operations to complete. Promise helps us to establish a callback chain that can be used to maintain any cmplex flow of asynchronus operations easily.

## Features

* Thread safe and non-blocking
* Easy to use fluent API
	* `deferred = DeferredFactory.createDeferred()
	deferred.then(success).fail(fail).notify(update)`
	* `aDeferred.then(success,fail,notify)`
	* `aDeferred.resolve(aResolvedValue)`
* Provides both deferred and promise interface
	* `aDeferred.then(success)`
	* `aDeferred.promise().then(success)`
* Supports promise chaining
	* `aDeferred.then(success,fail,notify)
	.then(success,fail,notify)
	.then(success,fail,notify)` 
* Supports downward streaming
	* `aDeferred.then(success).then(success)
		.notify(notify).fail(fail)`
* Provides a `Thenable` interface for better interoperability between `promise` and `non-promise` system.
* Supports when
	* `Deferred.when(promise1).then(success)`
	* `Deferred.when(promise1,promise2,promise3)
		.then(success).then(fail)`
* Asynchronus task execution
	* Has built in container for asynchronus task execution
		* `DeferredFactory.createNewContainer()`
		* `DeferredContainer.getContainer().stop()`
	* Asynchronus task with callables
		* `when(callable1).then(...)`
		* `when(callable1,callable2,callable3).then(...)`
	* After finish of a callable the deferred object will be automatically resolved by the return value. However, `RunnableDeferred` class can be used to controll when the deferred is resolved.
	
## How to Build?

* Need JDK7 or more
* Need Apache Maven
* Use `mvn clean package install` to build the project and then install in your local maven repository.

Use below maven dependency in your applications.
	
```xml
	
	<dependency>
  		<groupId>org.djava</groupId>
  		<artifactId>deferredjava</artifactId>
  		<version>0.1-SNAPSHOT</version>
  	</dependency>
	
``` 

## Quick Examples

Below are some quick example snippets. For complete examples please see examples in source code.

### 1. Hello World!!

```java

		Deferred<String> deferred = DeferredFactory.createDeferred();
		
		deferred.then(
			new Callbacks.SuccessCallBack<VoidType, String>() {
				@Override
				public Object call(String value) {
					System.out.println(value);
					return VoidType.NOTHING;
				}
			}, 
			new Callbacks.FailureCallBack() {
				@Override
				public VoidType call(Exception reason) {
					System.out.println(reason.getMessage());
					return VoidType.NOTHING;
				}
			}, 
			new Callbacks.NotificationCallBack() {
				@Override
				public VoidType call(NotificationEvent event) {
					System.out.println("An update received!!");
					return null;
				}
			});
		
		//to resolve use deferred.resolve("Hello World!!);
	 	//to notify use deferred.notify(new NotificationEvent(deferred.promise()));
	 	//to reject use deferred.reject(new RuntimeException("message here..."))
```

### 2. Promise Chaining

```java

		Deferred<String> deferred = DeferredFactory.createDeferred();
		
		deferred.then(new Callbacks.SuccessCallBack<String, String>() {
			@Override
			public Object call(String value) {
				return value + " ";
			}
		})
		.then(new Callbacks.SuccessCallBack<String, String>() {
			@Override
			public Object call(String value) {
				return value + "World";
			}
		})
		.then(new Callbacks.SuccessCallBack<String, String>() {
			@Override
			public Object call(String value) {
				return value + "!!";
			}
		})
		.then(new Callbacks.SuccessCallBack<String, String>() {
			@Override
			public Object call(String value) {
				System.out.println(value);
				return null;
			}
		})
		.fail(new FailureCallBack() {
			@Override
			public VoidType call(Exception ex) {
				System.out.println(ex.getMessage());
				return null;
			}
		});
		
		//to resolve use deferred.resolve("Hello");
		//to reject use deferred.reject(new RuntimeException("Hello Hell!!"));
```

### 3. When usage examples.

For now we have implemented when.all method. Later we will implement other popular when methods.

You can use Callable objects as arguments of these method. The library will convert the callable objects to a deffered task and will be submitted automatically to its built in asynchronous task execution container. So you souuld run the container before using whens. There are also other types like DeferredTask, RunnnableDeffered and Promise that the when method also supports other than Callable type. 

DefferedTask and RunnableDeffered tasks will not be automatically submitted to the container. You can use these classes' submit methods to submit the tasks.

There is a small difference between DeferredTask and RunnableDeffered. For DefferedTask the library will automatically resolve the deferred object as soon as the call returns where as in a RunnableDeferred object you will decide when the deferred obeject will be resolved.

#### 3.1 When example1

```java
		
		//first, you should create the container
		DeferredContainer.createNewContainer();
		
		Deferred.when(new Callable<String>(){
			public String call() throws Exception {
				Thread.sleep(100);
				return "Hello";
			}
		}, new Callable<String>(){
			public String call() throws Exception {
				Thread.sleep(200);
				
				//note: set accept value to false to raise an exception
				//uses for experiment
				if(accept) {
					return " ";
				}
				
				throw new RuntimeException("Second call failed");
			}
		}, new Callable<String>(){
			public String call() throws Exception {
				return "World!!";
			}
		}).then(new SuccessCallBack<Object, List<String>>() {
			@Override
			public Object call(List<String> values) {
				String result = "";
				for(String value : values) {
					result = result + value;
				}
				System.out.println(result);
				return VoidType.NOTHING;
			}
		}).fail(new Callbacks.FailureCallBack() {
			@Override
			public VoidType call(Exception reason) {
				System.out.println(reason.getMessage());
				return VoidType.NOTHING;
			}
		});
		
		//now wait sometimes to give some time to finish the asynchronous tasks
		Thread.wait(1000);
		
		//finally, you should stop the container
		DeferredContainer.getContainer().stop();
```

#### 3.2 When example2

```java

		//first, you should create the container
		DeferredContainer.createNewContainer();

		DeferredTask<String> dt1 = new DeferredTask<>(new 			Callable<String>(){
				public String call() throws Exception {
					Thread.sleep(100);
					return "Hello";
				}
		});
		
		DeferredTask<String> dt2 = new DeferredTask<>(new Callable<String>(){
			public String call() throws Exception {
				Thread.sleep(200);
				if(accept) {
					return " ";
				}
				
				throw new RuntimeException("Second call failed");
			}
		});
		
		DeferredTask<String> dt3 = new DeferredTask<>(new Callable<String>(){
			public String call() throws Exception {
				return "World!!";
			}
		});
		
		Deferred.when(dt1, dt2, dt3).then(new SuccessCallBack<Object, List<String>>() {
			@Override
			public Object call(List<String> values) {
				String result = "";
				for(String value : values) {
					result = result + value;
				}
				System.out.println(result);
				return VoidType.NOTHING;
			}
		}).fail(new Callbacks.FailureCallBack() {
			@Override
			public VoidType call(Exception reason) {
				System.out.println(reason.getMessage());
				return VoidType.NOTHING;
			}
		});
		
		dt1.submit();
		dt2.submit();
		dt3.submit();

		//now wait sometimes to give some time to finish the asynchronous tasks
		Thread.wait(1000);
		
		//finally, you should stop the container
		DeferredContainer.getContainer().stop();
		
```