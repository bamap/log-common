package ir.bamap.blu.log.aspect

import ir.bamap.blu.info.execution.service.ExecutionContextInfoService
import ir.bamap.blu.log.LogUtil
import ir.bamap.blu.log.MethodCallInfoModel
import ir.bamap.blu.log.debugByParam
import ir.bamap.blu.log.enums.LogLevel
import ir.bamap.blu.log.errorByParam
import ir.bamap.blu.log.formatter.JsonMethodArgumentsFormatter
import ir.bamap.blu.log.formatter.MethodArgumentsFormatter
import ir.bamap.blu.log.infoByParam
import ir.bamap.blu.log.model.LogConfig
import ir.bamap.blu.log.warnByParam
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

open class MethodExecutionLogger(
    protected val executionContextInfoService: ExecutionContextInfoService,
    protected val argumentsFormatter: MethodArgumentsFormatter = JsonMethodArgumentsFormatter(),
    protected val startCallMethodMessage: String = "Calling method",
    protected val successfulCallMethodMessage: String = "Successful call method",
    protected val failedCallMethodMessage: String = "Exception occurred in method call"
) {

    protected val loggerMap: MutableMap<String, Logger> = ConcurrentHashMap()

    companion object {
        protected const val LOG_GROUP_ID = "groupId"
    }

    open fun executeMethodAndLog(joinPoint: ProceedingJoinPoint, logConfig: LogConfig): Any? {
        val startTime = System.currentTimeMillis()
        val result: Any?

        val methodInfo = initMethodExecutionInfo(joinPoint, logConfig)

        logStartMethodExecution(methodInfo, logConfig)

        try {
            result = joinPoint.proceed()
        } catch (throwable: Throwable) {
            val duration = System.currentTimeMillis() - startTime
            logMethodExecutionException(methodInfo.copy(duration = duration), throwable, logConfig)
            throw throwable
        }

        val duration = System.currentTimeMillis() - startTime

        val resultFormatted = argumentsFormatter.formatResult(
            result, logConfig.maskResultFields, logConfig.ignoreResultFields
        )
        val runMethodInfo = methodInfo.copy(result = resultFormatted, duration = duration)
        logMethodExecutionCompleted(runMethodInfo, logConfig)

        logMethodExecutionIfSlow(runMethodInfo, logConfig, duration)
        return result
    }

    protected fun initMethodExecutionInfo(joinPoint: ProceedingJoinPoint, logConfig: LogConfig): MethodCallInfoModel {

        val signature = joinPoint.signature as MethodSignature
        val argumentsMap = signature.parameterNames
            .zip(joinPoint.args)
            .toMap()

        val arguments = argumentsFormatter.formatArguments(argumentsMap, logConfig.maskArgs, logConfig.ignoreArgs)

        val requestInfo = executionContextInfoService.getInfo()
        val executionContext = LogUtil.getExecutionContextLogArguments(requestInfo)

        return MethodCallInfoModel(
            signature.declaringTypeName,
            signature.name,
            arguments,
            executionContext
        )
    }

    protected open fun logMethodExecutionIfSlow(
        methodInfo: MethodCallInfoModel,
        logConfig: LogConfig,
        duration: Long
    ) {
        if (duration < logConfig.slowThresholdMs)
            return

        val logger = getLoggerByJoinPoint(methodInfo.cls)
        val arguments = mapOf(
            LOG_GROUP_ID to logConfig.groupId,
            "duration" to duration,
            "context" to methodInfo.executionContext,
            "method" to methodInfo.getFullMethodSignatureMap(),
        )

        logger.warnByParam("Slow method", arguments)
    }

    protected open fun logStartMethodExecution(methodInfo: MethodCallInfoModel, logConfig: LogConfig) {

        val arguments = mapOf(
            LOG_GROUP_ID to logConfig.groupId,
            "context" to methodInfo.executionContext,
            "method" to methodInfo.getMethodSignatureMap()
        )

        log(methodInfo.cls, startCallMethodMessage, arguments, logConfig.logLevel)
    }

    protected open fun logMethodExecutionCompleted(methodInfo: MethodCallInfoModel, logConfig: LogConfig) {
        val arguments = mapOf(
            LOG_GROUP_ID to logConfig.groupId,
            "context" to methodInfo.executionContext,
            "method" to methodInfo.getFullMethodSignatureMap(),
        )

        log(methodInfo.cls, successfulCallMethodMessage, arguments, logConfig.logLevel)
    }

    protected open fun logMethodExecutionException(
        methodInfo: MethodCallInfoModel,
        throwable: Throwable?,
        logConfig: LogConfig
    ) {
        val logger = getLoggerByJoinPoint(methodInfo.cls)

        val logArguments: Map<String, Any?> = mapOf(
            LOG_GROUP_ID to logConfig.groupId,
            "context" to methodInfo.executionContext,
            "method" to methodInfo.getMethodSignatureMap(),

            "cause" to throwable,
            "stackTrace" to LogUtil.getStackTrace(throwable).take(2000)
        )

        logger.errorByParam(failedCallMethodMessage, logArguments)
    }

    protected open fun getLoggerByJoinPoint(cls: String): Logger {
        return loggerMap.computeIfAbsent(cls) { LoggerFactory.getLogger(it) }
    }

    protected fun log(cls: String, message: String, arguments: Map<String, Any?>, level: LogLevel) {
        val logger = getLoggerByJoinPoint(cls)
        if (level == LogLevel.INFO)
            logger.infoByParam(message, arguments)
        else // its DEBUG
            logger.debugByParam(message, arguments)
    }
}