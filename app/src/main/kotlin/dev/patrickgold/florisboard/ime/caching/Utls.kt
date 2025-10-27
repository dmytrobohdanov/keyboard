
package dev.patrickgold.florisboard.ime.caching

import android.util.Log

fun tracer (msg: String? = null ){
    Log.d("piing", "tracer: $msg")
    try{
        throw Exception("Tracing Exception")
    } catch (e: Exception){
        e.printStackTrace()
    }
}
