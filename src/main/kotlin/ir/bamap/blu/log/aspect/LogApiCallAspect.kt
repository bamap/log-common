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
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect

@Aspect
class LogApiCallAspect(
    requestInfoService: ExecutionContextInfoService,
    argumentsFormatter: MethodArgumentsFormatter = JsonMethodArgumentsFormatter()
) : MethodExecutionLogger(requestInfoService, argumentsFormatter) {

    @Around("@annotation(loggableMapping)")
    fun getMappingLogAround(joinPoint: ProceedingJoinPoint, loggableMapping: LoggableGetMapping): Any? {
        val logConfig = LogConfig(loggableMapping.level, loggableMapping.slowThresholdMs)
        return executeMethodAndLog(joinPoint, logConfig)
    }

    @Around("@annotation(loggableMapping)")
    fun postMappingLogAround(joinPoint: ProceedingJoinPoint, loggableMapping: LoggablePostMapping): Any? {
        val logConfig = LogConfig(loggableMapping.level, loggableMapping.slowThresholdMs)
        return executeMethodAndLog(joinPoint, logConfig)
    }

    @Around("@annotation(loggableMapping)")
    fun putMappingLogAround(joinPoint: ProceedingJoinPoint, loggableMapping: LoggablePutMapping): Any? {
        val logConfig = LogConfig(loggableMapping.level, loggableMapping.slowThresholdMs)
        return executeMethodAndLog(joinPoint, logConfig)
    }

    @Around("@annotation(loggableMapping)")
    fun deleteMappingLogAround(joinPoint: ProceedingJoinPoint, loggableMapping: LoggableDeleteMapping): Any? {
        val logConfig = LogConfig(loggableMapping.level, loggableMapping.slowThresholdMs)
        return executeMethodAndLog(joinPoint, logConfig)
    }

    // Exchange
    @Around("@annotation(loggableExchange)")
    fun getExchangeLogAround(joinPoint: ProceedingJoinPoint, loggableExchange: LoggableGetExchange): Any? {
        val logConfig = LogConfig(loggableExchange.level, loggableExchange.slowThresholdMs)
        return executeMethodAndLog(joinPoint, logConfig)
    }

    @Around("@annotation(loggableExchange)")
    fun postExchangeLogAround(joinPoint: ProceedingJoinPoint, loggableExchange: LoggablePostExchange): Any? {
        val logConfig = LogConfig(loggableExchange.level, loggableExchange.slowThresholdMs)
        return executeMethodAndLog(joinPoint, logConfig)
    }

    @Around("@annotation(loggableExchange)")
    fun putExchangeLogAround(joinPoint: ProceedingJoinPoint, loggableExchange: LoggablePutExchange): Any? {
        val logConfig = LogConfig(loggableExchange.level, loggableExchange.slowThresholdMs)
        return executeMethodAndLog(joinPoint, logConfig)
    }

    @Around("@annotation(loggableExchange)")
    fun deleteExchangeLogAround(joinPoint: ProceedingJoinPoint, loggableExchange: LoggableDeleteExchange): Any? {
        val logConfig = LogConfig(loggableExchange.level, loggableExchange.slowThresholdMs)
        return executeMethodAndLog(joinPoint, logConfig)
    }
}