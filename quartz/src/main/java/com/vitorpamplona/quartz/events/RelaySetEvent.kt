package com.vitorpamplona.quartz.events

import androidx.compose.runtime.Immutable
import com.vitorpamplona.quartz.utils.TimeUtils
import com.vitorpamplona.quartz.encoders.toHexKey
import com.vitorpamplona.quartz.crypto.CryptoUtils
import com.vitorpamplona.quartz.encoders.ATag
import com.vitorpamplona.quartz.encoders.HexKey
import com.vitorpamplona.quartz.signers.NostrSigner

@Immutable
class RelaySetEvent(
    id: HexKey,
    pubKey: HexKey,
    createdAt: Long,
    tags: List<List<String>>,
    content: String,
    sig: HexKey
) : BaseAddressableEvent(id, pubKey, createdAt, kind, tags, content, sig) {
    fun relays() = tags.filter { it.size > 1 && it[0] == "r" }.map { it[1] }
    fun description() = tags.firstOrNull() { it.size > 1 && it[0] == "description" }?.get(1)

    companion object {
        const val kind = 30022

        fun create(
            relays: List<String>,
            signer: NostrSigner,
            createdAt: Long = TimeUtils.now(),
            onReady: (RelaySetEvent) -> Unit
        ) {
            val tags = mutableListOf<List<String>>()
            relays.forEach {
                tags.add(listOf("r", it))
            }

            signer.sign(createdAt, kind, tags, "", onReady)
        }
    }
}
