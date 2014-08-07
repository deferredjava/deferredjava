# DeferredJava Project

**`DeferredJava`** is a thread safe non-blocking `deferred/promise` implementation for `Java`. It provides an easy to use fluent `deferred/promise` API that can be used to manage complex flow of asynchronus operations easily.

## Usage

Both `deferred` and `promise` means the same thing a `promise` that represents the result of a `asynchronus operation` that is not completed. The difference is that `deferred` provides a `interface` that can access to the `promise object` to change its state where as `promise` provides an interface that cannot.

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

## Quick Examples

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