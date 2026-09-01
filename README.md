# Log Common

This project provides a set of utilities for logging and tracing method calls, API requests, and integrates with Spring Boot's logging infrastructure.

## Features:

### 1. **Traceable API Endpoints:**

You can use the following annotations to enable request/response logging for your controller methods. These annotations behave like Spring's mapping annotations but add structured logging before and after method execution:

- `@LoggableGetMapping` / `@LoggableGetExchange`
- `@LoggablePostMapping` / `@LoggablePostExchange`
- `@LoggablePutMapping` / `@LoggablePutExchange`
- `@LoggableDeleteMapping` / `@LoggableDeleteExchange`

#### Configuration Options:
Each annotation accepts two parameters:
- `level` (LogLevel): `INFO` (default) or `DEBUG` - controls the log level for start/success logs
- `slowThresholdMs` (Int): Threshold in milliseconds for slow method detection (logs at WARN level)

#### How to Use:
To enable this feature, define the following beans:

```kotlin
@Bean
@RequestScope
fun requestInfoService(request: HttpServletRequest): ExecutionContextInfoService {
    return RequestInfoService(request) // or your implementation
}

@Bean
fun logApiCallAspect(requestInfoService: ExecutionContextInfoService): LogApiCallAspect {
    return LogApiCallAspect(requestInfoService)
}
```

#### Example:
```kotlin
@RestController
class UserController {

    @LoggableGetMapping("/users/{id}", slowThresholdMs = 500)
    fun getUser(@PathVariable id: Long): User {
        return userService.findById(id)
    }

    @LoggablePostMapping("/users", level = LogLevel.DEBUG)
    fun createUser(@RequestBody user: UserDto): User {
        return userService.create(user)
    }
}
```

---

### 2. **Method Call Logging:**

The `@LogExecution` and `@DebugExecution` annotations allow you to log the start of any method, its inputs, its result, duration, and any exceptions that occur during execution.

#### Annotations:
- `@LogExecution(level = LogLevel.INFO, slowThresholdMs = 1000)` - Logs at INFO level
- `@DebugExecution(slowThresholdMs = 1000)` - Logs at DEBUG level

#### How to Use:
```kotlin
@Bean
fun methodExecutionLoggerAspect(
    requestInfoService: ExecutionContextInfoService
): MethodExecutionLoggerAspect {
    return MethodExecutionLoggerAspect(requestInfoService)
}
```

#### Example:
```kotlin
@Service
class UserService {

    @LogExecution(slowThresholdMs = 200)
    fun findById(id: Long): User? {
        return userRepository.findById(id)
    }

    @DebugExecution
    fun complexCalculation(input: CalculationInput): CalculationResult {
        // ... complex logic
    }
}
```

#### Log Output Structure:
Each logged method call includes:
- `groupId` - Unique correlation ID for the call chain
- `context` - Execution context (request ID, URI, method, user)
- `method` - Class, method name, arguments (formatted), and on completion: duration and result
- `cause` & `stackTrace` - On exception

---

### 3. **Customizable Argument Formatting:**

Method arguments and results are formatted via the `MethodArgumentsFormatter` interface. The default implementation `JsonMethodArgumentsFormatter` uses Jackson to serialize/deserialize arguments, ensuring they are JSON-safe for structured logging.

#### Custom Formatter:
```kotlin
@Bean
fun customArgumentsFormatter(): MethodArgumentsFormatter {
    return CustomMethodArgumentsFormatter()
}
```

---

### 4. **Exception Handling & ProblemDetail Resolution:**

The `ExceptionHandlerService` resolves exceptions to RFC 7807 `ProblemDetail` responses. Built-in resolvers include:

- `BluProblemDetailResolver` - Handles `BluException` with custom status codes and arguments
- `MvcProblemDetailResolver` - Handles Spring MVC exceptions (validation, binding, etc.)
- `ConstraintViolationProblemDetailResolver` - Handles `@Valid` constraint violations
- `IllegalArgumentProblemDetailResolver` - Handles `IllegalArgumentException`
- `GeneralProblemDetailResolver` - Fallback for unhandled exceptions

#### Custom Resolver:
```kotlin
@Bean
fun exceptionHandlerService(
    messageSource: MessageSource,
    webRequest: WebRequest,
    objectMapper: ObjectMapper,
    infoService: ExecutionContextInfoService
): ExceptionHandlerService {
    return CustomExceptionHandlerService(messageSource, webRequest, objectMapper, infoService)
}
```

---

### 5. **Custom Structured Logger for Spring Boot 3.4+:**

Starting with Spring Boot 3.4.0, a new logging structure has been introduced. This project provides a default logger that supports structured logging with JSON output.

#### How to Use:
Add the following configuration to your `application.yaml`:

```yaml
logging:
  structured:
    format:
      file: ir.bamap.blu.log.BluLogFormatter
```

---

## Configuration Summary

| Bean | Purpose |
|------|---------|
| `ExecutionContextInfoService` | Provides request context (request-scoped) |
| `LogApiCallAspect` | Enables `@Loggable*Mapping` / `@Loggable*Exchange` |
| `MethodExecutionLoggerAspect` | Enables `@LogExecution` / `@DebugExecution` |
| `MethodArgumentsFormatter` | Formats method args/results (default: JSON) |
| `ExceptionHandlerService` | Global exception → ProblemDetail resolution |
| `BluLogFormatter` | Structured JSON logging for file output |

## Message Properties

Default messages (override in `messages.properties`):
```properties
blu.general=Unknown error
blu.general.detail=An unknown error occurred on the server
blu.validation.argumentNotValid=Invalid input data
blu.validation.notNull={field} cannot be null
blu.validation.argumentNotValid.detail=Invalid input data
```
