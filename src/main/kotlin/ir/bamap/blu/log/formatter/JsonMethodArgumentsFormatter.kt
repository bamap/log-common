package ir.bamap.blu.log.formatter

import org.slf4j.LoggerFactory
import tools.jackson.core.JacksonException
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper

open class JsonMethodArgumentsFormatter(
    protected open val objectMapper: ObjectMapper = ObjectMapper()
) : MethodArgumentsFormatter {

    private val logger = LoggerFactory.getLogger(JsonMethodArgumentsFormatter::class.java)

    override fun format(arguments: Map<String, Any?>): Map<String, Any?> {

        return try {
            val summarizedArguments = arguments.mapValues { formatValue(it.value) }
            val result = objectMapper.writeValueAsString(summarizedArguments)

            objectMapper.readValue(result, object : TypeReference<Map<String, Any?>>() {})
        } catch (exception: JacksonException) {
            logger.error("Error formatting method arguments", exception)
            emptyMap()
        }
    }

    override fun formatValue(value: Any?): Any? {
        return value
    }
}