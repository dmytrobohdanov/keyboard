/*
 * Copyright (C) 2025-2026 The FlorisBoard Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.patrickgold.florisboard.ime.caching.usecases.contacts

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import dev.patrickgold.florisboard.ime.caching.usecases.contacts.models.ContactDetails

/**
 * Fetches all contacts and their associated details (phones, emails, addresses, organization).
 *
 * @param context The context to access the ContentResolver.
 * @return A list of [ContactDetails].
 */
@SuppressLint("Range")
fun getAllContactDetails(context: Context): List<ContactDetails> {
    val contentResolver = context.contentResolver

    // This map will hold the contacts as we build them. Key is the Contact ID.
    val contactsMap = mutableMapOf<String, MutableContact>()

    // --- Pass 1: Get all Contact IDs and Names ---

    val contactProjection = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
        ContactsContract.Contacts.PHOTO_URI
    )

    // Query the main Contacts table
    contentResolver.query(
        ContactsContract.Contacts.CONTENT_URI,
        contactProjection,
        null, // No selection, get all contacts
        null, // No selection args
        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC" // Sort by name
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID)
        val nameColumn = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
        val photoUriColumn = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)

        while (cursor.moveToNext()) {
            val id = cursor.getString(idColumn)
            val name = cursor.getString(nameColumn) ?: "No Name"
            val photoUriString = cursor.getString(photoUriColumn)
            val photoUri = if (photoUriString != null) Uri.parse(photoUriString) else null
            // Initialize the contact in our map
            contactsMap[id] = MutableContact(id = id, name = name, photoUri = photoUri)
        }
    }

    // --- Pass 2: Get all details from the Data table ---

    // Define the columns to retrieve from the Data table
    val dataProjection = arrayOf(
        ContactsContract.Data.CONTACT_ID,
        ContactsContract.Data.MIMETYPE,
        ContactsContract.Data.DATA1, // Phone, Email, Org Name
        ContactsContract.Data.DATA2, // Type (Home, Work) for Phone, Email, Address
        ContactsContract.Data.DATA4, // Street
        ContactsContract.Data.DATA7, // City
        ContactsContract.Data.DATA8, // State
        ContactsContract.Data.DATA9, // Postal Code
        ContactsContract.Data.DATA10 // Country
    )

    // Define the selection criteria
    // We only want rows that are phones, emails, addresses, or organizations
    val dataSelection = "${ContactsContract.Data.MIMETYPE} IN (?, ?, ?, ?)"
    val dataSelectionArgs = arrayOf(
        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
    )

    // Query the Data table
    contentResolver.query(
        ContactsContract.Data.CONTENT_URI,
        dataProjection,
        dataSelection,
        dataSelectionArgs,
        null // No specific sort order needed for this pass
    )?.use { cursor ->

        // Get column indices
        val idColumn = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
        val mimeTypeColumn = cursor.getColumnIndex(ContactsContract.Data.MIMETYPE)
        val data1Column = cursor.getColumnIndex(ContactsContract.Data.DATA1)

        // Address columns
        val streetColumn = cursor.getColumnIndex(ContactsContract.Data.DATA4)
        val cityColumn = cursor.getColumnIndex(ContactsContract.Data.DATA7)
        val stateColumn = cursor.getColumnIndex(ContactsContract.Data.DATA8)
        val postalCodeColumn = cursor.getColumnIndex(ContactsContract.Data.DATA9)
        val countryColumn = cursor.getColumnIndex(ContactsContract.Data.DATA10)

        while (cursor.moveToNext()) {
            val id = cursor.getString(idColumn)

            // Find the contact this data belongs to
            val contact = contactsMap[id] ?: continue // Skip if data doesn't match a contact from Pass 1

            val mimeType = cursor.getString(mimeTypeColumn)
            val data1 = cursor.getString(data1Column)

            when (mimeType) {
                // Phone Number
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {
                    if (!data1.isNullOrBlank()) {
                        contact.phones.add(data1)
                    }
                }
                // Email Address
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> {
                    if (!data1.isNullOrBlank()) {
                        contact.emails.add(data1)
                    }
                }
                // Organization
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE -> {
                    if (!data1.isNullOrBlank()) {
                        contact.organization = data1
                    }
                }
                // Structured Address
                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE -> {
                    val street = cursor.getString(streetColumn)
                    val city = cursor.getString(cityColumn)
                    val state = cursor.getString(stateColumn)
                    val postalCode = cursor.getString(postalCodeColumn)
                    val country = cursor.getString(countryColumn)

                    // Build a formatted address string
                    val formattedAddress = listOfNotNull(street, city, state, postalCode, country)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")

                    if (formattedAddress.isNotBlank()) {
                        contact.addresses.add(formattedAddress)
                    }
                }
            }
        }
    }

    // --- Pass 3: Convert mutable map to final immutable list ---
    return contactsMap.values.map { it.toContactDetails() }
}

private data class MutableContact(
    val id: String,
    val name: String,
    val phones: MutableList<String> = mutableListOf(),
    val emails: MutableList<String> = mutableListOf(),
    val addresses: MutableList<String> = mutableListOf(),
    var organization: String? = null,
    val photoUri: Uri? = null
) {
    /**
     * Converts the mutable builder object into the final immutable data class.
     */
    fun toContactDetails(): ContactDetails = ContactDetails(
        id = id,
        name = name,
        phones = phones,
        emails = emails,
        addresses = addresses,
        organization = organization,
        photoUri = photoUri
    )
}
