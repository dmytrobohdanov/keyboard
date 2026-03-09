package dev.patrickgold.florisboard.ime.caching.usecases.savetofile.s3

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import aws.sdk.kotlin.services.cognitoidentity.CognitoIdentityClient
import aws.sdk.kotlin.services.cognitoidentity.model.GetCredentialsForIdentityRequest
import aws.sdk.kotlin.services.cognitoidentity.model.GetIdRequest
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.collections.Attributes
import aws.smithy.kotlin.runtime.content.asByteStream
import java.io.File

private const val PREFS_NAME = "s3_backup_prefs"
private const val KEY_COGNITO_IDENTITY_ID = "cognito_identity_id"
const val KEY_UPLOADED_FILES = "uploaded_cache_files"

object S3Uploader {

    /**
     * Uploads the given file to S3 under key "<cognitoId>/cache/<filename>".
     * Uses Cognito unauthenticated (guest) identity to obtain temporary AWS credentials.
     */
    suspend fun upload(context: Context, file: File) {
        val cognitoId = getOrCreateIdentityId(context)
        val credentials = fetchGuestCredentials(cognitoId)
        val s3Key = S3Config.buildS3Key(cognitoId, file.name)

        buildS3Client(credentials).use { client ->
            client.putObject(
                PutObjectRequest {
                    bucket = S3Config.BUCKET_NAME
                    key = s3Key
                    body = file.asByteStream()
                }
            )
        }

        Log.d("S3Uploader", "Uploaded ${file.name} → s3://${S3Config.BUCKET_NAME}/$s3Key")
    }

    // ---------- Cognito helpers (internal so other workers can reuse) ----------

    internal suspend fun getOrCreateIdentityId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cached = prefs.getString(KEY_COGNITO_IDENTITY_ID, null)
        if (!cached.isNullOrBlank()) return cached

        val identityId = CognitoIdentityClient { region = S3Config.REGION }.use { client ->
            client.getId(
                GetIdRequest { identityPoolId = S3Config.IDENTITY_POOL_ID }
            ).identityId ?: error("Cognito returned null identityId")
        }

        prefs.edit { putString(KEY_COGNITO_IDENTITY_ID, identityId) }
        Log.d("S3Uploader", "New Cognito identity ID stored: $identityId")
        return identityId
    }

    internal suspend fun fetchGuestCredentials(identityId: String): Credentials {
        return CognitoIdentityClient { region = S3Config.REGION }.use { client ->
            val response = client.getCredentialsForIdentity(
                GetCredentialsForIdentityRequest { this.identityId = identityId }
            )
            val creds = response.credentials ?: error("Null credentials from Cognito")
            Credentials(
                accessKeyId = creds.accessKeyId ?: error("Null accessKeyId"),
                secretAccessKey = creds.secretKey ?: error("Null secretKey"),
                sessionToken = creds.sessionToken,
            )
        }
    }

    internal fun buildS3Client(credentials: Credentials): S3Client = S3Client {
        region = S3Config.REGION
        credentialsProvider = StaticCredentialsProvider(credentials)
    }

    // ---------- Uploaded-files tracking ----------

    fun isAlreadyUploaded(context: Context, filename: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_UPLOADED_FILES, emptySet())?.contains(filename) == true
    }

    fun markAsUploaded(context: Context, filename: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getStringSet(KEY_UPLOADED_FILES, emptySet())?.toMutableSet() ?: mutableSetOf()
        existing.add(filename)
        prefs.edit { putStringSet(KEY_UPLOADED_FILES, existing) }
    }
}

/** Minimal [CredentialsProvider] wrapping a static [Credentials] instance. */
private class StaticCredentialsProvider(private val credentials: Credentials) : CredentialsProvider {
    override suspend fun resolve(attributes: Attributes): Credentials = credentials
}

