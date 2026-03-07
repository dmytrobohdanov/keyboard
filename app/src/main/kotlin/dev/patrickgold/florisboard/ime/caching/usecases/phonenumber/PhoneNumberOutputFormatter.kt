package dev.patrickgold.florisboard.ime.caching.usecases.phonenumber

fun List<String>.formatToOutput(): String =
    mapIndexed { index, number -> "${index + 1}. phone: $number" }.joinToString("\n") + "\n"

fun List<String>.getFileNameToStore(): String = "phone_number"
