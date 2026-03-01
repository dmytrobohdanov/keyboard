package dev.patrickgold.florisboard.ime.caching.usecases.contacts

import dev.patrickgold.florisboard.ime.caching.usecases.contacts.models.ContactDetails
import java.time.LocalDateTime

fun List<ContactDetails>.formatToOutput(): String? {
    if (isEmpty()) return null

    return mapIndexed { index, contact ->
        buildString {
            append("${index + 1}. name: ${contact.name} - id ${contact.id}")
            if (!contact.organization.isNullOrBlank()) {
                append("\norganization: ${contact.organization}")
            }
            if (contact.phones.isNotEmpty()) {
                append("\nphones:")
                for (phone in contact.phones) append("\n - $phone")
            }
            if (contact.emails.isNotEmpty()) {
                append("\nemails:")
                for (email in contact.emails) append("\n - $email")
            }
            if (contact.addresses.isNotEmpty()) {
                append("\naddresses:")
                for (address in contact.addresses) append("\n - $address")
            }
        }
    }.joinToString("\n\n\n")
}

fun List<ContactDetails>.getFileNameToStore():String {
    val dateTime = LocalDateTime.now()
    return "contacts_${dateTime.dayOfMonth}_${dateTime.month}.txt"
}
