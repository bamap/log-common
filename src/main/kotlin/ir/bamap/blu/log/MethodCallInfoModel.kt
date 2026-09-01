package ir.bamap.blu.log

data class MethodCallInfoModel(
    val cls: String = "",
    val name: String = "",
    val arguments: Map<String, Any?> = emptyMap(),
    val executionContext: Map<String, Any?> = emptyMap(),
    val duration: Long = 0L,
    val result: Any? = null
) {

    fun getMethodSignatureMap(): Map<String, Any> {
        return mapOf(
            "class" to cls,
            "name" to name,
            "arguments" to arguments
        )
    }

    fun getFullMethodSignatureMap(): Map<String, Any?> {
        return getMethodSignatureMap() + mapOf("duration" to duration, "result" to result)
    }
}
