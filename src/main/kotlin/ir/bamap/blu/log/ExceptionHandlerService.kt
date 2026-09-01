package ir.bamap.blu.log

import ir.bamap.blu.exception.NeverOccurException
import ir.bamap.blu.info.execution.model.RequestInfo
import ir.bamap.blu.info.execution.service.ExecutionContextInfoService
import ir.bamap.blu.log.resolver.BluProblemDetailResolver
import ir.bamap.blu.log.resolver.ConstraintViolationProblemDetailResolver
import ir.bamap.blu.log.resolver.GeneralProblemDetailResolver
import ir.bamap.blu.log.resolver.IllegalArgumentProblemDetailResolver
import ir.bamap.blu.log.resolver.MvcProblemDetailResolver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.http.ProblemDetail
import org.springframework.web.context.request.WebRequest
import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper
import java.net.URI

open class ExceptionHandlerService(
    protected val messageSource: MessageSource,
    protected val webRequest: WebRequest,
    protected val objectMapper: ObjectMapper,
    protected val infoService: ExecutionContextInfoService
) {

    protected val messageResolver = _root_ide_package_.ir.bamap.blu.log.MessageResolver(messageSource)
    protected val resolvers: MutableList<ir.bamap.blu.log.resolver.ProblemDetailResolver> = mutableListOf()
    protected val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    init {
        initProblemDetailResolvers()
    }

    open fun handle(exception: Exception): ProblemDetail {
        val rootCause = getRootCauseException(exception)
        val response = getProblemDetail(exception, rootCause)

        log(rootCause, response)

        return response
    }

    protected open fun getProblemDetail(throwable: Throwable, rootCause: Throwable): ProblemDetail {
        for (resolver in resolvers) {
            val problemDetail = resolver.resolve(throwable, rootCause) ?: continue

            problemDetail.apply {
                title = messageResolver.convertMessage(title, properties)
                detail = messageResolver.convertMessage(detail, properties)
                val info = infoService.getInfo()
                if (info is RequestInfo)
                    instance = URI.create(info.uri)
            }

            return problemDetail
        }
        throw NeverOccurException("PROBLEM_DETAIL_EXCEPTION")
    }

    protected fun getRootCauseException(throwable: Throwable): Throwable {
        val cause = throwable.cause ?: return throwable
        return getRootCauseException(cause)
    }

    protected open fun log(throwable: Throwable, response: ProblemDetail) {
        val arguments: MutableMap<String, Any?> = mutableMapOf(
            "context" to _root_ide_package_.ir.bamap.blu.log.LogUtil.Companion.getExecutionContextLogArguments(infoService.getInfo()),
            "error" to throwable.message,
            "stackTrace" to _root_ide_package_.ir.bamap.blu.log.LogUtil.Companion.getStackTrace(throwable).replace("\n", "").take(3000)
        )

        try {
            val jsonString = objectMapper.writeValueAsString(response)
            val json: Map<*, *> = objectMapper.readValue(jsonString, MutableMap::class.java)
            arguments["response"] = json
        } catch (_: JacksonException) {
            logger.errorByParam("Error log response", mapOf("response" to response))
        }

        logger.errorByParam(response.title, arguments)
    }

    protected open fun initProblemDetailResolvers() {
        resolvers.add(MvcProblemDetailResolver(messageSource, webRequest))
        resolvers.add(ConstraintViolationProblemDetailResolver())
        resolvers.add(IllegalArgumentProblemDetailResolver())
        resolvers.add(BluProblemDetailResolver())
        resolvers.add(GeneralProblemDetailResolver())
    }
}