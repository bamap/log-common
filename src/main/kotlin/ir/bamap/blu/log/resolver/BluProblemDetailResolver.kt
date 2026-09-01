package ir.bamap.blu.log.resolver

import ir.bamap.blu.exception.BluException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail

open class BluProblemDetailResolver : ProblemDetailResolver {

    override fun resolve(throwable: Throwable, rootCause: Throwable): ProblemDetail? {
        if (throwable is BluException) return getProblemDetail(throwable)
        if (rootCause is BluException) return getProblemDetail(rootCause)

        return null
    }

    protected open fun getProblemDetail(exception: BluException): ProblemDetail {
        val statusCode = HttpStatus.resolve(exception.statusCode) ?: HttpStatus.INTERNAL_SERVER_ERROR
        return ProblemDetail.forStatus(statusCode)
            .apply {
                title = "{blu.general.title}"
                detail = exception.message
                exception.args.forEach {
                    setProperty(it.key, it.value)
                }
            }
    }
}