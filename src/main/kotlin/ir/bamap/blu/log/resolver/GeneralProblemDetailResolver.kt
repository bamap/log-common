package ir.bamap.blu.log.resolver

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail

open class GeneralProblemDetailResolver : ProblemDetailResolver {

    override fun resolve(throwable: Throwable, rootCause: Throwable): ProblemDetail {
        val key = "blu.general"
        return ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY)
            .apply {
                title = "{${key}}.title"
                detail = "{${key}.detail}"
            }
    }
}