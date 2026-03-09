

package org.florisboard.lib.kotlin

fun Number.toStringWithoutDotZero(): String = this.toString().removeSuffix(".0")
