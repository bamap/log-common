package ir.bamap.blu.log.resolver

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail

class ConstraintViolationProblemDetailResolver : ProblemDetailResolver {

    override fun resolve(throwable: Throwable, rootCause: Throwable): ProblemDetail? {
        if (rootCause !is ConstraintViolationException)
            return null

        val violationOption = rootCause.constraintViolations.stream().findFirst()
        if (violationOption.isEmpty)
            return ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)

        val violation = violationOption.get()
        val messageError = "'" + violation.propertyPath.toString() + "'" + " " + violation.message
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, messageError)
        problemDetail.title = messageError
        problemDetail.setProperty("property", violation.propertyPath.toString())

        return problemDetail
    }
}