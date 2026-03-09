

package org.florisboard.lib.android

import androidx.collection.SparseArrayCompat

fun <T> SparseArrayCompat<T>.removeAndReturn(key: Int): T? {
    val elem = get(key)
    return if (elem == null) {
        null
    } else {
        remove(key)
        elem
    }
}
