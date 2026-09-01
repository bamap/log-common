package ir.bamap.blu.log

import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

open class MessageResolver(
    val messageSource: MessageSource
) {
    protected open val logger = LoggerFactory.getLogger(this.javaClass)

    fun convertMessage(message: String?, arguments: Map<String, Any?>?): String? {
        if(message == null) return null

        val finalArguments = arguments ?: emptyMap()
        val replacedArguments = finalArguments.entries.fold(message) { acc, (key, value) ->
            acc.replace("{$key}", value?.toString() ?: "")
        }

        val regex = "\\{(.*?)}".toRegex()
        return regex.replace(replacedArguments) {
            getMessageByKey(it.value.removePrefix("{").removeSuffix("}"), finalArguments)
        }
    }

    protected fun getMessageByKey(key: String, arguments: Map<String, Any?>): String {
        val locale = LocaleContextHolder.getLocale()
        val message = messageSource.getMessage(key, emptyArray(), null, locale)
        if(message == null) {
            logger.warn("Message for key `$key` not found in locale ${locale.toLanguageTag()}!")
            return key
        }

        return arguments.entries.fold(message) { acc, (key, value) ->
            acc.replace("{$key}", value?.toString() ?: "")
        }
    }
}
