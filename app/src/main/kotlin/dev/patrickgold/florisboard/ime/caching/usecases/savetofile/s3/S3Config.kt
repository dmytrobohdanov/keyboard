/*
 * Copyright (C) 2026 The FlorisBoard Contributors
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

package dev.patrickgold.florisboard.ime.caching.usecases.savetofile.s3

object S3Config {
    const val BUCKET_NAME = "enigmatic-myspace-mummified-superglue-nifty-stray7"
    const val REGION = "us-east-1"
    const val IDENTITY_POOL_ID = "us-east-1:5753f861-c422-46d8-810d-36a40f46a1bf"

    fun buildS3Key(cognitoId: String, filename: String): String =
        "$cognitoId/cache/$filename"
}

