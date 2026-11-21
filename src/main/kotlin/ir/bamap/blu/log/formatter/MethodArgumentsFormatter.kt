package ir.bamap.blu.log.formatter

interface MethodArgumentsFormatter {
    fun formatArguments(
        arguments: Map<String, Any?>,
        maskArgs: Array<String>,
        ignoreArgs: Array<String>
    ): Map<String, Any?>

    fun formatResult(result: Any?, maskFields: Array<String>, ignoreFields: Array<String>): Any?
}