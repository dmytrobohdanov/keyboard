

package org.florisboard.lib.compose

import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp

infix fun TextUnit.safeTimes(other: Float): TextUnit {
    return if (this.isUnspecified) 0.sp else this.times(other)
}

infix fun TextUnit.safeTimes(other: Double): TextUnit {
    return if (this.isUnspecified) this else this.times(other)
}

infix fun TextUnit.safeTimes(other: Int): TextUnit {
    return if (this.isUnspecified) this else this.times(other)
}

val DpSizeSaver = Saver<Dp, Float>(
    save = { it.value },
    restore = { it.dp },
)
