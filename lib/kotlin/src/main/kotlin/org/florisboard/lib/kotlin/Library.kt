

package org.florisboard.lib.kotlin

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun <R> tryOrNull(block: () -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return try {
        block()
    } catch (_: Throwable) {
        null
    }
}
