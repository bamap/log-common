# Log Common

This project provides a set of utilities for logging and tracing method calls, API requests, and integrates with Spring Boot's logging infrastructure.

## Features:
### 1. **Traceable API Endpoints:**

You can use `TraceableGetMapping` and `TraceablePostMapping` annotations, which behave like Spring’s `@GetMapping` and `@PostMapping`, but with the added functionality of tracing logs before the method is executed. This is useful for tracking API requests and responses.
#### How to Use:
To enable this feature, define the following beans:
```java
@Bean
@RequestScope
public IamRequestInfoService requestInfoService(HttpServletRequest request) {
    return new IamRequestInfoService(request);
}

@Bean
public TraceApiCallAspect traceApiCallAspect(RequestInfoService requestInfoService) {
   return new TraceApiCallAspect(requestInfoService);
}
```

### 2. Method Call Logging:

The `@LogMethodCall` annotation allows you to log the start of a method, its inputs, its result, and any exceptions that occur during its execution. This is useful for debugging and monitoring method calls.

#### How to Use:
To use this feature, define the following beans:
```java
@Bean
@RequestScope
public IamRequestInfoService requestInfoService(HttpServletRequest request) {
    return new IamRequestInfoService(request);
}

@Bean
public MethodCallLoggerAspect methodCallLoggerAspect(RequestInfoService requestInfoService) {
    return new MethodCallLoggerAspect(requestInfoService);
}
```
### 3. Custom Structured Logger for Spring Boot 3.4+:

Starting with Spring Boot 3.4.0, a new logging structure has been introduced. This project provides a default logger that supports structured logging.
#### How to Use:
To use the default logger, add the following configuration to your `application.properties`:
```yaml
logging.structured.format.file = ir.rahgozin.common.log.BluLogFormatter
```
