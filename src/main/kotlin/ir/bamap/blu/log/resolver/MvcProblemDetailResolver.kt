package ir.bamap.blu.log.resolver

import ir.bamap.blu.exception.LogicalException
import org.springframework.beans.ConversionNotSupportedException
import org.springframework.beans.TypeMismatchException
import org.springframework.context.MessageSource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.http.converter.HttpMessageNotWritableException
import org.springframework.validation.FieldError
import org.springframework.validation.method.MethodValidationException
import org.springframework.web.ErrorResponseException
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.ServletRequestBindingException
import org.springframework.web.context.request.WebRequest
import org.springframework.web.context.request.async.AsyncRequestNotUsableException
import org.springframework.web.context.request.async.AsyncRequestTimeoutException
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.support.MissingServletRequestPartException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.servlet.resource.NoResourceFoundException

open class MvcProblemDetailResolver(
    messageSource: MessageSource,
    private val webrequest: WebRequest
) : ProblemDetailResolver, ResponseEntityExceptionHandler() {

    protected open val supportedExceptions = getExceptions()

    init {
        setMessageSource(messageSource)
    }

    override fun resolve(throwable: Throwable, rootCause: Throwable): ProblemDetail? {
        if(!isResolvable(throwable, rootCause))
            return null

        val exceptionToHandle = when {
            supportedExceptions.contains(rootCause::class.java) -> rootCause as Exception
            supportedExceptions.contains(throwable::class.java) -> throwable as Exception
            else -> throw LogicalException("MVC_RESOLVER")
        }

        val responseEntity = handleException(exceptionToHandle, webrequest)
            ?: throw LogicalException("MVC_PROBLEM_DETAIL")
        val body = responseEntity.body ?: throw LogicalException("MVC_PROBLEM_DETAIL_BODY")
        if (body !is ProblemDetail) throw LogicalException("MVC_PROBLEM_DETAIL_BODY_TYPE")
        return body
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException, headers: HttpHeaders, status: HttpStatusCode, request: WebRequest
    ): ResponseEntity<Any>? {
        val errorKey = "blu.validation.argumentNotValid"
        val errors = ex.bindingResult.allErrors.map { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.getDefaultMessage() ?: ""
            mapOf(fieldName to errorMessage)
        }

        val problemDetail = ProblemDetail.forStatus(status)
            .apply {
                title = "{${errorKey}}"
                detail = "{${errorKey}.detail}"
                setProperty("errors", errors)
            }

        return ResponseEntity.status(status).body(problemDetail)
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException, headers: HttpHeaders, status: HttpStatusCode, request: WebRequest
    ): ResponseEntity<Any>? {
        val detailMessage = (ex.rootCause ?: ex.cause)?.message ?: ""

        val body = createProblemDetail(ex, status, "Failed to read request", null, null, request)
            .apply { detail = detailMessage }

        if (detailMessage.contains("non-nullable")) {
            val nullValueRegex =
                """due to missing \(therefore NULL\) value for creator parameter (\w+) which is a non-nullable""".toRegex()
            val fieldName = nullValueRegex.find(detailMessage)?.groupValues?.get(1)
            if (fieldName != null) {
                body.detail = "{blu.validation.notNull}"
                body.setProperty("field", fieldName)
            }
        }

        return handleExceptionInternal(ex, body, headers, status, request)
    }

    protected open fun getExceptions(): List<Class<out Exception>> {
        return listOf(
            HttpRequestMethodNotSupportedException::class.java,
            HttpMediaTypeNotSupportedException::class.java,
            HttpMediaTypeNotAcceptableException::class.java,
            MissingPathVariableException::class.java,
            MissingServletRequestParameterException::class.java,
            MissingServletRequestPartException::class.java,
            ServletRequestBindingException::class.java,
            MethodArgumentNotValidException::class.java,
            HandlerMethodValidationException::class.java,
            NoHandlerFoundException::class.java,
            NoResourceFoundException::class.java,
            AsyncRequestTimeoutException::class.java,
            ErrorResponseException::class.java,
            MaxUploadSizeExceededException::class.java,
            ConversionNotSupportedException::class.java,
            TypeMismatchException::class.java,
            HttpMessageNotReadableException::class.java,
            HttpMessageNotWritableException::class.java,
            MethodValidationException::class.java,
            AsyncRequestNotUsableException::class.java
        )
    }

    protected open fun isResolvable(throwable: Throwable, rootCause: Throwable): Boolean {
            return supportedExceptions.any { exceptionClass ->
                exceptionClass.isInstance(throwable) || exceptionClass.isInstance(rootCause)
            }
    }
}
