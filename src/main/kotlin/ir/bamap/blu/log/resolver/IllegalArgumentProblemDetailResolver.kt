package ir.bamap.blu.log.resolver

import org.springframework.http.ProblemDetail

class IllegalArgumentProblemDetailResolver : ProblemDetailResolver {
    override fun resolve(throwable: Throwable, rootCause: Throwable): ProblemDetail? {
        if(rootCause !is IllegalArgumentException)
            return null

        return ProblemDetail.forStatus(500).apply {
            title = "{blu.general.title}"
            detail = rootCause.message ?: "{blu.general.detail}"
        }
    }
}