package ir.bamap.blu.log

import ch.qos.logback.classic.pattern.ThrowableProxyConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.IThrowableProxy
import ch.qos.logback.classic.spi.ThrowableProxy
import org.slf4j.Marker
import org.slf4j.event.KeyValuePair
import org.springframework.boot.json.JsonWriter
import org.springframework.boot.logging.StackTracePrinter
import org.springframework.boot.logging.structured.ContextPairs
import org.springframework.boot.logging.structured.ElasticCommonSchemaProperties
import org.springframework.boot.logging.structured.JsonWriterStructuredLogFormatter
import org.springframework.boot.logging.structured.StructuredLoggingJsonMembersCustomizer
import org.springframework.core.env.Environment
import org.springframework.util.Assert
import java.util.*
import java.util.function.Consumer
import java.util.function.Function

class BluLogFormatter(
    environment: Environment,
    stackTracePrinter: StackTracePrinter?,
    contextPairs: ContextPairs,
    throwableProxyConverter: ThrowableProxyConverter,
    customizer: StructuredLoggingJsonMembersCustomizer<*>
) : JsonWriterStructuredLogFormatter<ILoggingEvent>(
    Consumer { members: JsonWriter.Members<ILoggingEvent> ->
        jsonMembers(environment, stackTracePrinter, contextPairs, throwableProxyConverter, members)
    }, customizer
) {
    companion object {
        @JvmStatic
        private val keyValuePairExtractor: JsonWriter.PairExtractor<KeyValuePair> = JsonWriter.PairExtractor.of(
            Function<KeyValuePair, Any> { pair: KeyValuePair -> pair.key },
            Function { pair: KeyValuePair -> pair.value })

        @JvmStatic
        private fun jsonMembers(
            environment: Environment,
            stackTracePrinter: StackTracePrinter?,
            contextPairs: ContextPairs,
            throwableProxyConverter: ThrowableProxyConverter,
            members: JsonWriter.Members<ILoggingEvent>
        ) {
            val extractor = Extractor(stackTracePrinter, throwableProxyConverter)
            members.add("@timestamp") { obj: ILoggingEvent -> obj.instant }
            members.add("level") { obj: ILoggingEvent -> obj.level }
            members.add("logger") { obj: ILoggingEvent -> obj.loggerName }

            members.add("process").usingMembers { process: JsonWriter.Members<ILoggingEvent> ->
                process.add("pid", environment.getProperty("spring.application.pid", Long::class.java) as Long)
                    .`when` { obj: Long? -> Objects.nonNull(obj) }
                process.add("thread").usingMembers { thread: JsonWriter.Members<ILoggingEvent> ->
                    thread.add("name") { obj: ILoggingEvent -> obj.threadName }
                }
            }
            ElasticCommonSchemaProperties.get(environment).jsonMembers(members)
            members.add("message") { obj: ILoggingEvent -> obj.formattedMessage }
            members.add().usingPairs(contextPairs.nested { pairs: ContextPairs.Pairs<ILoggingEvent> ->
                pairs.addMapEntries { obj: ILoggingEvent -> obj.mdcPropertyMap }
                pairs.add({ obj: ILoggingEvent -> obj.keyValuePairs }, keyValuePairExtractor)
            })

            members.add().whenNotNull(Function { obj: ILoggingEvent -> obj.throwableProxy })
                .usingMembers { throwableMembers: JsonWriter.Members<ILoggingEvent> ->
                    throwableMembers.add("error").usingMembers { error: JsonWriter.Members<ILoggingEvent> ->
                        error.add("type") { obj: ILoggingEvent -> obj.throwableProxy }
                            .`as` { obj: IThrowableProxy -> obj.className }
                        error.add("message") { obj: ILoggingEvent -> obj.throwableProxy }
                            .`as` { obj: IThrowableProxy -> obj.message }
                        Objects.requireNonNull(extractor)
                        error.add("stack_trace") { event: ILoggingEvent ->
                            extractor.stackTrace(event)
                        }
                    }
                }
            members.add("tags") { obj: ILoggingEvent -> obj.markerList }
                .whenNotNull()
                .`as` { markers: List<Marker> -> getMarkers(markers) }
                .whenNotEmpty()
//            members.add("ecs").usingMembers { ecs: Members<ILoggingEvent?> -> ecs.add("version", "8.11") }
        }

        @JvmStatic
        private fun getMarkers(markers: List<Marker>): Set<String?> {
            val result: MutableSet<String> = TreeSet<String>()
            addMarkers(result, markers.iterator())
            return result
        }

        @JvmStatic
        private fun addMarkers(result: MutableSet<String>, iterator: Iterator<Marker>) {
            while (iterator.hasNext()) {
                val marker = iterator.next()
                result.add(marker.name)
                if (marker.hasReferences()) {
                    addMarkers(result, marker.iterator())
                }
            }
        }

        internal class Extractor(
            private val stackTracePrinter: StackTracePrinter?,
            private val throwableProxyConverter: ThrowableProxyConverter
        ) {
            fun messageAndStackTrace(event: ILoggingEvent): String {
                val var10000 = event.formattedMessage
                return """
                    $var10000
                    
                    ${stackTrace(event)}
                    """.trimIndent()
            }

            fun stackTrace(event: ILoggingEvent): String {
                if (this.stackTracePrinter != null) {
                    val throwableProxy = event.throwableProxy
                    Assert.state(
                        throwableProxy is ThrowableProxy,
                        "Instance must be a ThrowableProxy in order to print exception"
                    )
                    val throwable = (throwableProxy as ThrowableProxy).throwable
                    return stackTracePrinter.printStackTraceToString(throwable)
                } else {
                    return throwableProxyConverter.convert(event)
                }
            }
        }
    }


}