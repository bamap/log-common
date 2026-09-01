package ir.bamap.blu.log.aspect

import ir.bamap.blu.info.execution.service.ExecutionContextInfoService
import ir.bamap.blu.log.annotation.DebugExecution
import ir.bamap.blu.log.formatter.JsonMethodArgumentsFormatter
import ir.bamap.blu.log.formatter.MethodArgumentsFormatter
import org.aspectj.lang.ProceedingJoinPoint
import ir.bamap.blu.log.annotation.LogExecution
import ir.bamap.blu.log.enums.LogLevel
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect

@Aspect
class MethodExecutionLoggerAspect(
    requestInfoService: ExecutionContextInfoService,
    argumentsFormatter: MethodArgumentsFormatter = JsonMethodArgumentsFormatter()
) : MethodExecutionLogger(requestInfoService, argumentsFormatter) {

    /*@Around("@annotation(logMethodCall)")
    fun logAround(joinPoint: ProceedingJoinPoint, logMethodCall: LogMethodCall): Any? {
        val logConfig = LogConfig(logLevel = logMethodCall.level, 1_000_000)
        return callMethodAndLog(joinPoint, logConfig)
    }

    @Around("@annotation(debugMethodCall)")
    fun debugAround(joinPoint: ProceedingJoinPoint, debugMethodCall: DebugMethodCall): Any? {
        val logConfig = LogConfig(LogLevel.DEBUG, 1_000_000)
        return callMethodAndLog(joinPoint, logConfig)
    }*/

    @Around("@annotation(log)")
    fun logExecutionAround(joinPoint: ProceedingJoinPoint, log: LogExecution): Any? {
        val logConfig = LogConfig(logLevel = log.level, log.slowThresholdMs)
        return executeMethodAndLog(joinPoint, logConfig)
    }

    @Around("@annotation(log)")
    fun debugExecutionAround(joinPoint: ProceedingJoinPoint, log: DebugExecution): Any? {
        val logConfig = LogConfig(LogLevel.DEBUG, log.slowThresholdMs)
        return executeMethodAndLog(joinPoint, logConfig)
    }
}