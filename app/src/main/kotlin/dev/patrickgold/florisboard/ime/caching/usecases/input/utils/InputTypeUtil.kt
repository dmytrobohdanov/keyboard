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

package dev.patrickgold.florisboard.ime.caching.usecases.input.utils

import android.text.InputType

internal fun Int.getInputTypeLabel(): String {
    val summary = StringBuilder()
    when (this) {
        InputType.TYPE_NULL -> {
            summary.append("TYPE_NULL")
        }

        else -> {
            val tClass: String
            val tVariation: String
            when (this and InputType.TYPE_MASK_CLASS) {
                InputType.TYPE_CLASS_DATETIME -> {
                    tClass = "TYPE_CLASS_DATETIME"
                    tVariation = when (this and InputType.TYPE_MASK_VARIATION) {
                        InputType.TYPE_DATETIME_VARIATION_DATE -> "TYPE_DATETIME_VARIATION_DATE"
                        InputType.TYPE_DATETIME_VARIATION_NORMAL -> "TYPE_DATETIME_VARIATION_NORMAL"
                        InputType.TYPE_DATETIME_VARIATION_TIME -> "TYPE_DATETIME_VARIATION_TIME"
                        else -> String.format("0x%08x", this and InputType.TYPE_MASK_VARIATION)
                    }
                }

                InputType.TYPE_CLASS_NUMBER -> {
                    tClass = "TYPE_CLASS_NUMBER"
                    tVariation = when (this and InputType.TYPE_MASK_VARIATION) {
                        InputType.TYPE_NUMBER_VARIATION_NORMAL -> "TYPE_NUMBER_VARIATION_NORMAL"
                        InputType.TYPE_NUMBER_VARIATION_PASSWORD -> "TYPE_NUMBER_VARIATION_PASSWORD"
                        else -> String.format("0x%08x", this and InputType.TYPE_MASK_VARIATION)
                    }
                }

                InputType.TYPE_CLASS_PHONE -> {
                    tClass = "TYPE_CLASS_PHONE"
                    tVariation = String.format("0x%08x", this and InputType.TYPE_MASK_VARIATION)
                }

                InputType.TYPE_CLASS_TEXT -> {
                    tClass = "TYPE_CLASS_TEXT"
                    tVariation = when (this and InputType.TYPE_MASK_VARIATION) {
                        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS -> "TYPE_TEXT_VARIATION_EMAIL_ADDRESS"
                        InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT -> "TYPE_TEXT_VARIATION_EMAIL_SUBJECT"
                        InputType.TYPE_TEXT_VARIATION_FILTER -> "TYPE_TEXT_VARIATION_FILTER"
                        InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE -> "TYPE_TEXT_VARIATION_LONG_MESSAGE"
                        InputType.TYPE_TEXT_VARIATION_NORMAL -> "TYPE_TEXT_VARIATION_NORMAL"
                        InputType.TYPE_TEXT_VARIATION_PASSWORD -> "TYPE_TEXT_VARIATION_PASSWORD"
                        InputType.TYPE_TEXT_VARIATION_PERSON_NAME -> "TYPE_TEXT_VARIATION_PERSON_NAME"
                        InputType.TYPE_TEXT_VARIATION_PHONETIC -> "TYPE_TEXT_VARIATION_PHONETIC"
                        InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS -> "TYPE_TEXT_VARIATION_POSTAL_ADDRESS"
                        InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE -> "TYPE_TEXT_VARIATION_SHORT_MESSAGE"
                        InputType.TYPE_TEXT_VARIATION_URI -> "TYPE_TEXT_VARIATION_URI"
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD -> "TYPE_TEXT_VARIATION_VISIBLE_PASSWORD"
                        InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT -> "TYPE_TEXT_VARIATION_WEB_EDIT_TEXT"
                        InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS -> "TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS"
                        InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD -> "TYPE_TEXT_VARIATION_WEB_PASSWORD"
                        else -> String.format("0x%08x", this and InputType.TYPE_MASK_VARIATION)
                    }
                }

                else -> {
                    tClass = String.format("0x%08x", this and InputType.TYPE_MASK_CLASS)
                    tVariation = String.format("0x%08x", this and InputType.TYPE_MASK_VARIATION)
                }
            }
            summary.append("class=$tClass variation=$tVariation")
        }
    }

    return summary.toString()
}
