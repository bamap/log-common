package ir.bamap.blu.log.aspect

import ir.bamap.blu.info.execution.service.ExecutionContextInfoService
import ir.bamap.blu.log.annotation.DebugExecution
import ir.bamap.blu.log.formatter.JsonMethodArgumentsFormatter
import ir.bamap.blu.log.formatter.MethodArgumentsFormatter
import org.aspectj.lang.ProceedingJoinPoint
import ir.bamap.blu.log.annotation.LogExecution
import ir.bamap.blu.log.enums.LogLevel
import ir.bamap.blu.log.model.LogConfig
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

    @Around("@annotation(annotation)")
    fun logExecutionAround(joinPoint: ProceedingJoinPoint, annotation: LogExecution): Any? {
        val logConfig = LogConfig(
            annotation.level, annotation.slowThresholdMs,
            annotation.maskArgs, annotation.ignoreArgs,
            annotation.maskResultFields, annotation.maskResultFields
        )
        return executeMethodAndLog(joinPoint, logConfig)
    }

    @Around("@annotation(annotation)")
    fun debugExecutionAround(joinPoint: ProceedingJoinPoint, annotation: DebugExecution): Any? {
        val logConfig = LogConfig(
            LogLevel.DEBUG, annotation.slowThresholdMs,
            annotation.maskArgs, annotation.ignoreArgs,
            annotation.maskResultFields, annotation.maskResultFields
        )
        return executeMethodAndLog(joinPoint, logConfig)
    }
}