/**
 * Copyright (c) 2024 Vitor Pamplona
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.vitorpamplona.quartz.events

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.fasterxml.jackson.annotation.JsonProperty
import com.vitorpamplona.quartz.encoders.HexKey
import com.vitorpamplona.quartz.signers.NostrSigner
import com.vitorpamplona.quartz.utils.TimeUtils
import java.io.ByteArrayInputStream

@Stable
class AppMetadata {
    var name: String? = null
    var username: String? = null

    @JsonProperty("display_name")
    var displayName: String? = null
    var picture: String? = null

    var banner: String? = null
    var image: String? = null
    var website: String? = null
    var about: String? = null
    var subscription: Boolean? = false
    var cashuAccepted: Boolean? = false
    var encryptionSupported: Boolean? = false
    var personalized: Boolean? = false
    var amount: String? = null

    var nip05: String? = null
    var domain: String? = null
    var lud06: String? = null
    var lud16: String? = null

    var twitter: String? = null

    @Transient
    var tags: ImmutableListOfLists<String>? = null

    fun anyName(): String? {
        return displayName ?: name ?: username
    }

    fun anyNameStartsWith(prefix: String): Boolean {
        return listOfNotNull(name, username, displayName, nip05, lud06, lud16).any {
            it.contains(prefix, true)
        }
    }

    fun lnAddress(): String? {
        return lud16 ?: lud06
    }

    fun bestName(): String? {
        return displayName ?: name ?: username
    }

    fun nip05(): String? {
        return nip05
    }

    fun profilePicture(): String? {
        return picture
    }

    fun cleanBlankNames() {
        if (picture?.isNotEmpty() == true) picture = picture?.trim()
        if (nip05?.isNotEmpty() == true) nip05 = nip05?.trim()

        if (displayName?.isNotEmpty() == true) displayName = displayName?.trim()
        if (name?.isNotEmpty() == true) name = name?.trim()
        if (username?.isNotEmpty() == true) username = username?.trim()
        if (lud06?.isNotEmpty() == true) lud06 = lud06?.trim()
        if (lud16?.isNotEmpty() == true) lud16 = lud16?.trim()

        if (website?.isNotEmpty() == true) website = website?.trim()
        if (domain?.isNotEmpty() == true) domain = domain?.trim()

        if (picture?.isBlank() == true) picture = null
        if (nip05?.isBlank() == true) nip05 = null
        if (displayName?.isBlank() == true) displayName = null
        if (name?.isBlank() == true) name = null
        if (username?.isBlank() == true) username = null
        if (lud06?.isBlank() == true) lud06 = null
        if (lud16?.isBlank() == true) lud16 = null

        if (website?.isBlank() == true) website = null
        if (domain?.isBlank() == true) domain = null
    }
}

@Immutable
class AppDefinitionEvent(
    id: HexKey,
    pubKey: HexKey,
    createdAt: Long,
    tags: Array<Array<String>>,
    content: String,
    sig: HexKey,
) : BaseAddressableEvent(id, pubKey, createdAt, KIND, tags, content, sig) {
    @Transient private var cachedMetadata: AppMetadata? = null

    fun appMetaData() =
        if (cachedMetadata != null) {
            cachedMetadata
        } else {
            try {
                val newMetadata =
                    mapper.readValue(
                        ByteArrayInputStream(content.toByteArray(Charsets.UTF_8)),
                        AppMetadata::class.java,
                    )

                cachedMetadata = newMetadata

                newMetadata
            } catch (e: Exception) {
                e.printStackTrace()
                Log.w("MT", "Content Parse Error ${e.localizedMessage} $content")
                null
            }
        }

    fun supportedKinds() =
        tags
            .filter { it.size > 1 && it[0] == "k" }
            .mapNotNull { runCatching { it[1].toInt() }.getOrNull() }

    fun includeKind(kind: String) = tags.any { it.size > 1 && it[0] == "k" && it[1] == kind }

    fun publishedAt() = tags.firstOrNull { it.size > 1 && it[0] == "published_at" }?.get(1)

    companion object {
        const val KIND = 31990

        fun create(
            details: UserMetadata,
            signer: NostrSigner,
            createdAt: Long = TimeUtils.now(),
            onReady: (AppDefinitionEvent) -> Unit,
        ) {
            val tags =
                arrayOf(
                    arrayOf("alt", "App definition event for ${details.name}"),
                )
            signer.sign(createdAt, KIND, tags, "", onReady)
        }
    }
}
