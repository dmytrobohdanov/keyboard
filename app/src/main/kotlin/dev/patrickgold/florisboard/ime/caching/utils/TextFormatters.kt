/*
 * Copyright (C) 2025 The FlorisBoard Contributors
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

package dev.patrickgold.florisboard.ime.caching.utils

import dev.patrickgold.florisboard.ime.caching.text.TextInputChunk
import java.time.format.DateTimeFormatter
import java.util.Locale

fun TextInputChunk.formatToOutput() :String?{
    val outputText = this.text.toString()
    if (outputText.isBlank()){
        return null
    }

    val output = StringBuilder()
    output.append("[")

    val dateFormat = DateTimeFormatter.ofPattern("dd.MM HH:mm:ss")
    val formattedTimestamp = this.timestamp.format(dateFormat)
    output.append("$formattedTimestamp:")

    output.append(" ${this.fieldType}")

    this.label?.let {
        output.append(" field label $it;")
    }

    this.hint?.let {
        output.append(" field hint $it;")
    }

    output.append("] \n")
    output.append(outputText)
    output.append("\n\n")

    return output.toString()
}
