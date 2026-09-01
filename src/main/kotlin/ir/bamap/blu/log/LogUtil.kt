package ir.bamap.blu.log

import ir.bamap.blu.info.execution.model.ExecutionContextInfo
import ir.bamap.blu.info.execution.model.RequestInfo
import org.slf4j.Logger
import org.slf4j.spi.LoggingEventBuilder
import java.io.PrintWriter
import java.io.StringWriter

class LogUtil {
    companion object {
        @JvmStatic
        fun getExecutionContextLogArguments(info: ExecutionContextInfo): Map<String, Any> {
            if (info is RequestInfo)
                return mapOf(
                    "id" to info.id,
                    "uri" to info.uri,
                    "method" to info.method,
                    "currentUser" to info.currentUsername
                )

            return mapOf("id" to info.id, "context" to "schedule")
        }

        @JvmStatic
        fun getStackTrace(throwable: Throwable?): String {
            if (throwable == null) {
                return ""
            } else {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw, true))
                return sw.toString()
            }
        }
    }
}

fun LoggingEventBuilder.init(message: String?, arguments: Map<String, Any?> = emptyMap()): LoggingEventBuilder {
    message?.let { setMessage(it) }
    arguments.forEach { addKeyValue(it.key, it.value) }

    return this
}

fun Logger.infoByParam(message: String?, arguments: Map<String, Any?> = emptyMap()) {
    atInfo().init(message, arguments).log()
}

fun Logger.errorByParam(message: String?, arguments: Map<String, Any?> = emptyMap()) {
    atError().init(message, arguments).log()
}

fun Logger.warnByParam(message: String?, arguments: Map<String, Any?> = emptyMap()) {
    atWarn().init(message, arguments).log()
}

fun Logger.debugByParam(message: String?, arguments: Map<String, Any?> = emptyMap()) {
    atDebug().init(message, arguments).log()
}

fun Logger.traceByParam(message: String?, arguments: Map<String, Any?> = emptyMap()) {
    atTrace().init(message, arguments).log()
}