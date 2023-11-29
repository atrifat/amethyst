package com.vitorpamplona.quartz.events

import androidx.compose.runtime.Immutable
import com.vitorpamplona.quartz.utils.TimeUtils
import com.vitorpamplona.quartz.encoders.toHexKey
import com.vitorpamplona.quartz.crypto.CryptoUtils
import com.vitorpamplona.quartz.encoders.ATag
import com.vitorpamplona.quartz.encoders.HexKey
import com.vitorpamplona.quartz.signers.NostrSigner

@Immutable
class CalendarRSVPEvent(
    id: HexKey,
    pubKey: HexKey,
    createdAt: Long,
    tags: List<List<String>>,
    content: String,
    sig: HexKey
) : BaseAddressableEvent(id, pubKey, createdAt, kind, tags, content, sig) {

    fun status() = tags.firstOrNull { it.size > 1 && it[0] == "location" }?.get(1)
    fun start() = tags.firstOrNull { it.size > 1 && it[0] == "start" }?.get(1)
    fun end() = tags.firstOrNull { it.size > 1 && it[0] == "end" }?.get(1)

    //    ["L", "status"],
    //    ["l", "<accepted/declined/tentative>", "status"],
    //    ["L", "freebusy"],
    //    ["l", "<free/busy>", "freebusy"]

    companion object {
        const val kind = 31925

        fun create(
            signer: NostrSigner,
            createdAt: Long = TimeUtils.now(),
            onReady: (CalendarRSVPEvent) -> Unit
        ) {
            val tags = mutableListOf<List<String>>()
            signer.sign(createdAt, kind, tags, "", onReady)
        }
    }
}
