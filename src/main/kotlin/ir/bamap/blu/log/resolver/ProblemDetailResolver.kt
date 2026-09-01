package ir.bamap.blu.log.resolver

import org.springframework.http.ProblemDetail

interface ProblemDetailResolver {
    fun resolve(throwable: Throwable, rootCause: Throwable): ProblemDetail?
}