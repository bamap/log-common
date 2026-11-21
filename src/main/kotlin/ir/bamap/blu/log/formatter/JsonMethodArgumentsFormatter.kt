package ir.bamap.blu.log.formatter

import org.slf4j.LoggerFactory
import tools.jackson.core.JacksonException
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.ArrayNode
import tools.jackson.databind.node.ObjectNode

open class JsonMethodArgumentsFormatter(
    protected open val objectMapper: ObjectMapper = ObjectMapper()
) : MethodArgumentsFormatter {

    private val logger = LoggerFactory.getLogger(JsonMethodArgumentsFormatter::class.java)

    override fun formatArguments(
        arguments: Map<String, Any?>,
        maskArgs: Array<String>,
        ignoreArgs: Array<String>
    ): Map<String, Any?> {

        return try {
            val summarizedArguments = arguments
                .filter { !ignoreArgs.contains(it.key) }
                .mapValues {
                    if (maskArgs.contains(it.key)) "*****" else formatValue(it.value)
                }

            val jsonNode = objectMapper.valueToTree<JsonNode>(summarizedArguments)
            sanitize(jsonNode, maskArgs.toSet(), ignoreArgs.toSet())

            return objectMapper.convertValue(jsonNode, Map::class.java) as Map<String, Any?>
        } catch (exception: JacksonException) {
            logger.error("Error formatting method arguments", exception)
            emptyMap()
        }
    }

    override fun formatResult(
        result: Any?,
        maskFields: Array<String>,
        ignoreFields: Array<String>
    ): Any? {
        if (result == null) return null

        return formatArguments(mapOf("result" to result), maskFields, ignoreFields)["result"]
    }

    protected open fun formatValue(value: Any?): Any? {
        return value
    }

    protected fun sanitize(node: JsonNode, maskFields: Set<String>, ignoreFields: Set<String>) {

        when (node) {
            is ObjectNode -> {
                val fields = node.propertyNames().toList()

                fields.forEach { field ->
                    when (field) {
                        in ignoreFields -> node.remove(field)
                        in maskFields -> node.put(field, "*****")
                        else -> sanitize(node.get(field), maskFields, ignoreFields)
                    }
                }
            }

            is ArrayNode -> node.forEach { child ->
                sanitize(child, maskFields, ignoreFields)
            }
        }
    }
}