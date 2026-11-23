package ir.bamap.blu.log.aspect

import ir.bamap.blu.info.execution.service.ExecutionContextInfoService
import ir.bamap.blu.log.annotation.LoggableDeleteExchange
import ir.bamap.blu.log.annotation.LoggableDeleteMapping
import ir.bamap.blu.log.annotation.LoggableGetExchange
import ir.bamap.blu.log.annotation.LoggableGetMapping
import ir.bamap.blu.log.annotation.LoggablePostExchange
import ir.bamap.blu.log.annotation.LoggablePostMapping
import ir.bamap.blu.log.annotation.LoggablePutExchange
import ir.bamap.blu.log.annotation.LoggablePutMapping
import ir.bamap.blu.log.formatter.JsonMethodArgumentsFormatter
import ir.bamap.blu.log.formatter.MethodArgumentsFormatter
import ir.bamap.blu.log.model.LogConfig
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect

@Aspect
class LogApiCallAspect(
    requestInfoService: ExecutionContextInfoService,
    argumentsFormatter: MethodArgumentsFormatter = JsonMethodArgumentsFormatter()
) : MethodExecutionLogger(
    requestInfoService, argumentsFormatter,
    "Received a request", "Request handle successfully", "Request failed"
) {

    @Around("@annotation(annotation)")
    fun getMappingLogAround(joinPoint: ProceedingJoinPoint, annotation: LoggableGetMapping): Any? {
        val logConfig = LogConfig(
            annotation.level, annotation.slowThresholdMs,
            annotation.maskArgs, annotation.ignoreArgs,
            annotation.maskResultFields, annotation.ignoreResultFields
        )
        return executeMethodAndLog(joinPoint, logConfig)
    }

    @Around("@annotation(annotation)")
    fun postMappingLogAround(joinPoint: ProceedingJoinPoint, annotation: LoggablePostMapping): Any? {
        val logConfig = LogConfig(
            annotation.level, annotation.slowThresholdMs,
            annotation.maskArgs, annotation.ignoreArgs,
            annotation.maskResultFields, annotation.ignoreResultFields
        )
        return executeMethodAndLog(joinPoint, logConfig)
    }

    @Around("@annotation(annotation)")
    fun putMappingLogAround(joinPoint: ProceedingJoinPoint, annotation: LoggablePutMapping): Any? {
        val logConfig = LogConfig(
            annotation.level, annotation.slowThresholdMs,
            annotation.maskArgs, annotation.ignoreArgs,
            annotation.maskResultFields, annotation.ignoreResultFields
        )
        return executeMethodAndLog(joinPoint, logConfig)
    }

    @Around("@annotation(annotation)")
    fun deleteMappingLogAround(joinPoint: ProceedingJoinPoint, annotation: LoggableDeleteMapping): Any? {
        val logConfig = LogConfig(
            annotation.level, annotation.slowThresholdMs,
            annotation.maskArgs, annotation.ignoreArgs,
            annotation.maskResultFields, annotation.ignoreResultFields
        )
        return executeMethodAndLog(joinPoint, logConfig)
    }

    // Exchange
    @Around("@annotation(annotation)")
    fun getExchangeLogAround(joinPoint: ProceedingJoinPoint, annotation: LoggableGetExchange): Any? {
        val logConfig = LogConfig(
            annotation.level, annotation.slowThresholdMs,
            annotation.maskArgs, annotation.ignoreArgs,
            annotation.maskResultFields, annotation.ignoreResultFields
        )
        return executeMethodAndLog(joinPoint, logConfig)
    }

    @Around("@annotation(annotation)")
    fun postExchangeLogAround(joinPoint: ProceedingJoinPoint, annotation: LoggablePostExchange): Any? {
        val logConfig = LogConfig(
            annotation.level, annotation.slowThresholdMs,
            annotation.maskArgs, annotation.ignoreArgs,
            annotation.maskResultFields, annotation.ignoreResultFields
        )
        return executeMethodAndLog(joinPoint, logConfig)
    }

    @Around("@annotation(annotation)")
    fun putExchangeLogAround(joinPoint: ProceedingJoinPoint, annotation: LoggablePutExchange): Any? {
        val logConfig = LogConfig(
            annotation.level, annotation.slowThresholdMs,
            annotation.maskArgs, annotation.ignoreArgs,
            annotation.maskResultFields, annotation.ignoreResultFields
        )
        return executeMethodAndLog(joinPoint, logConfig)
    }

    @Around("@annotation(annotation)")
    fun deleteExchangeLogAround(joinPoint: ProceedingJoinPoint, annotation: LoggableDeleteExchange): Any? {
        val logConfig = LogConfig(
            annotation.level, annotation.slowThresholdMs,
            annotation.maskArgs, annotation.ignoreArgs,
            annotation.maskResultFields, annotation.ignoreResultFields
        )
        return executeMethodAndLog(joinPoint, logConfig)
    }
}