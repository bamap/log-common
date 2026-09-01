package ir.bamap.blu.log.formatter

interface MethodArgumentsFormatter {
    fun format(arguments: Map<String, Any?>): Map<String, Any?>

    fun formatValue(value: Any?): Any?
}