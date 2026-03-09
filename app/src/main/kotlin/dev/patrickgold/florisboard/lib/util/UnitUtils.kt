

package dev.patrickgold.florisboard.lib.util

object UnitUtils {
    private const val KiB = (1024).toFloat()
    private const val MiB = (1024 * 1024).toFloat()
    private const val GiB = (1024 * 1024 * 1024).toFloat()

    fun formatMemorySize(sizeBytes: Long): String {
        return when {
            sizeBytes >= GiB -> String.format("%.2f GiB", sizeBytes / GiB)
            sizeBytes >= MiB -> String.format("%.2f MiB", sizeBytes / MiB)
            sizeBytes >= KiB -> String.format("%.2f KiB", sizeBytes / KiB)
            else -> String.format("%d bytes")
        }
    }
}
