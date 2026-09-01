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
import ir.bamap.blu.log.warnByParam
import ir.bamap.blu.util.RandomUtil
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

open class MethodExecutionLogger(
    private val executionContextInfoService: ExecutionContextInfoService,
    private val argumentsFormatter: MethodArgumentsFormatter = JsonMethodArgumentsFormatter()
) {

    protected val loggerMap: MutableMap<String, Logger> = ConcurrentHashMap()

    companion object {
        protected const val LOG_GROUP_ID = "groupId"
    }

    open fun executeMethodAndLog(joinPoint: ProceedingJoinPoint, logConfig: LogConfig): Any? {
        val startTime = System.currentTimeMillis()
        val result: Any?

        val methodInfo = initMethodExecutionInfo(joinPoint)

        logStartMethodExecution(methodInfo, logConfig)

        try {
            result = joinPoint.proceed()
        } catch (throwable: Throwable) {
            val duration = System.currentTimeMillis() - startTime
            logMethodExecutionException(methodInfo.copy(duration = duration), throwable, logConfig)
            throw throwable
        }

        val duration = System.currentTimeMillis() - startTime

        val resultFormatted = argumentsFormatter.format(mapOf("result" to result))["result"]
        val runMethodInfo = methodInfo.copy(result = resultFormatted, duration = duration)
        logMethodExecutionCompleted(runMethodInfo, logConfig)

        logMethodExecutionDuration(runMethodInfo, logConfig, duration)
        return result
    }

    protected fun initMethodExecutionInfo(joinPoint: ProceedingJoinPoint): MethodCallInfoModel {

        val signature = joinPoint.signature as MethodSignature
        val argumentsMap = signature.parameterNames
            .zip(joinPoint.args)
            .toMap()

        val arguments = argumentsFormatter.format(argumentsMap)

        val requestInfo = executionContextInfoService.getInfo()
        val executionContext = LogUtil.getExecutionContextLogArguments(requestInfo)

        return MethodCallInfoModel(
            signature.declaringTypeName,
            signature.name,
            arguments,
            executionContext
        )
    }

    protected open fun logMethodExecutionDuration(methodInfo: MethodCallInfoModel, logConfig: LogConfig, duration: Long) {
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

        log(methodInfo.cls, "Calling method", arguments, logConfig.logLevel)
    }

    protected open fun logMethodExecutionCompleted(methodInfo: MethodCallInfoModel, logConfig: LogConfig) {
        val arguments = mapOf(
            LOG_GROUP_ID to logConfig.groupId,
            "context" to methodInfo.executionContext,
            "method" to methodInfo.getFullMethodSignatureMap(),
        )

        log(methodInfo.cls, "Successful call method", arguments, logConfig.logLevel)
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

        logger.errorByParam("Exception occurred in method call", logArguments)
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

    data class LogConfig(val logLevel: LogLevel = LogLevel.INFO, val slowThresholdMs: Int) {
        val groupId: String = PREFIX_LOG + LOG_COUNTER.incrementAndGet()
            .toString().reversed().chunked(3)
            .map { it.reversed() }
            .reversed()
            .mapIndexed { index, chunk -> if (index == 0) chunk.padStart(3, '0') else chunk }
            .joinToString("-")

        companion object {
            private val PREFIX_LOG = RandomUtil.nextString(3, RandomUtil.BIG_LETTERS).uppercase() + "-"
            private var LOG_COUNTER: AtomicLong = AtomicLong(0)
        }
    }
}