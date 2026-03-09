

package org.florisboard.lib.kotlin

@Throws(NoSuchElementException::class)
fun <K, V> Map<K, V>.getKeyByValue(value: V): K {
    for ((k, v) in this.entries) {
        if (value == v) return k
    }
    throw NoSuchElementException("Value $value is missing in the map.")
}

inline fun <T, reified R> Array<T>.map(transform: (T) -> R): Array<R> {
    return Array(this.size) { n -> transform(this[n]) }
}
