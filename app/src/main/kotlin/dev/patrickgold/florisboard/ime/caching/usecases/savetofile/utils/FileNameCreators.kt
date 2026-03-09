

package dev.patrickgold.florisboard.ime.caching.usecases.savetofile.utils

import java.time.LocalDate

object FileNameCreators {
    fun createTextFileName(
        appName: String
    ): String {
        val timestamp = LocalDate.now().toString()

        return "text_${appName}_$timestamp.txt"
    }

    fun createLocationsFileName(): String {
        val timestamp = LocalDate.now().toString()
        return "locations_$timestamp.txt"
    }

    fun createContactsFileName(): String {
        val timestamp = LocalDate.now().toString()
        return "contacts_$timestamp.txt"
    }
}
