package ir.bamap.blu.log.model

import ir.bamap.blu.log.enums.LogLevel
import ir.bamap.blu.util.RandomUtil
import java.util.concurrent.atomic.AtomicLong

data class LogConfig(
    val logLevel: LogLevel = LogLevel.INFO,
    val slowThresholdMs: Int,
    val maskArgs: Array<String> = emptyArray(),
    val ignoreArgs: Array<String> = emptyArray(),
    val maskResultFields: Array<String> = emptyArray(),
    val ignoreResultFields: Array<String> = emptyArray()
) {

    val groupId: String = PREFIX_LOG + LOG_COUNTER.incrementAndGet()
        .toString().reversed().chunked(3)
        .map { it.reversed() }
        .reversed()
        .mapIndexed { index, chunk -> if (index == 0) chunk.padStart(3, '0') else chunk }
        .joinToString("-")

    companion object {
        private val PREFIX_LOG = RandomUtil.nextString(3, RandomUtil.BIG_LETTERS).uppercase() + "-"
        private var LOG_COUNTER: AtomicLong = AtomicLong(0)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LogConfig

        if (slowThresholdMs != other.slowThresholdMs) return false
        if (logLevel != other.logLevel) return false
        if (!maskArgs.contentEquals(other.maskArgs)) return false
        if (!ignoreArgs.contentEquals(other.ignoreArgs)) return false
        if (!maskResultFields.contentEquals(other.maskResultFields)) return false
        if (!ignoreResultFields.contentEquals(other.ignoreResultFields)) return false
        if (groupId != other.groupId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = slowThresholdMs
        result = 31 * result + logLevel.hashCode()
        result = 31 * result + maskArgs.contentHashCode()
        result = 31 * result + ignoreArgs.contentHashCode()
        result = 31 * result + maskResultFields.contentHashCode()
        result = 31 * result + ignoreResultFields.contentHashCode()
        result = 31 * result + groupId.hashCode()
        return result
    }
}