package com.vitorpamplona.amethyst.ui.actions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitorpamplona.amethyst.model.Account
import com.vitorpamplona.amethyst.model.RelaySetupInfo
import com.vitorpamplona.amethyst.service.Nip11CachedRetriever
import com.vitorpamplona.amethyst.service.relays.Constants
import com.vitorpamplona.amethyst.service.relays.FeedType
import com.vitorpamplona.amethyst.service.relays.RelayPool
import com.vitorpamplona.quartz.events.ContactListEvent
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewRelayListViewModel : ViewModel() {
    private lateinit var account: Account

    private val _relays = MutableStateFlow<List<RelaySetupInfo>>(emptyList())
    val relays = _relays.asStateFlow()

    fun load(account: Account) {
        this.account = account
        clear()
        loadRelayDocuments()
    }

    fun create() {
        relays.let {
            viewModelScope.launch(Dispatchers.IO) {
                account.saveRelayList(it.value)
            }
        }

        clear()
    }

    fun loadRelayDocuments() {
        viewModelScope.launch(Dispatchers.IO) {
            _relays.value.forEach { item ->
                Nip11CachedRetriever.loadRelayInfo(
                    dirtyUrl = item.url,
                    onInfo = {
                        togglePaidRelay(item, it.limitation?.payment_required ?: false)
                    },
                    onError = { url, errorCode, exceptionMessage ->
                    }
                )
            }
        }
    }

    fun clear() {
        _relays.update {
            var relayFile = account.userProfile().latestContactList?.relays()

            // Ugly, but forces nostr.band as the only search-supporting relay today.
            // TODO: Remove when search becomes more available.
            if (relayFile?.none { it.key.removeSuffix("/") in Constants.forcedRelaysForSearchSet } == true) {
                relayFile = relayFile + Constants.forcedRelayForSearch.map {
                    Pair(
                        it.url,
                        ContactListEvent.ReadWrite(it.read, it.write)
                    )
                }
            }

            if (relayFile != null) {
                relayFile.map {
                    val liveRelay = RelayPool.getRelay(it.key)
                    val localInfoFeedTypes = account.localRelays.filter { localRelay -> localRelay.url == it.key }.firstOrNull()?.feedTypes ?: FeedType.values().toSet().toImmutableSet()

                    val errorCounter = liveRelay?.errorCounter ?: 0
                    val eventDownloadCounter = liveRelay?.eventDownloadCounterInBytes ?: 0
                    val eventUploadCounter = liveRelay?.eventUploadCounterInBytes ?: 0
                    val spamCounter = liveRelay?.spamCounter ?: 0

                    RelaySetupInfo(it.key, it.value.read, it.value.write, errorCounter, eventDownloadCounter, eventUploadCounter, spamCounter, localInfoFeedTypes)
                }.sortedBy { it.downloadCountInBytes }.reversed()
            } else {
                account.localRelays.map {
                    val liveRelay = RelayPool.getRelay(it.url)

                    val errorCounter = liveRelay?.errorCounter ?: 0
                    val eventDownloadCounter = liveRelay?.eventDownloadCounterInBytes ?: 0
                    val eventUploadCounter = liveRelay?.eventUploadCounterInBytes ?: 0
                    val spamCounter = liveRelay?.spamCounter ?: 0

                    RelaySetupInfo(it.url, it.read, it.write, errorCounter, eventDownloadCounter, eventUploadCounter, spamCounter, it.feedTypes)
                }.sortedBy { it.downloadCountInBytes }.reversed()
            }
        }
    }

    fun addRelay(relay: RelaySetupInfo) {
        if (relays.value.any { it.url == relay.url }) return

        _relays.update {
            it.plus(relay)
        }
    }

    fun deleteRelay(relay: RelaySetupInfo) {
        _relays.update {
            it.minus(relay)
        }
    }

    fun deleteAll() {
        _relays.update { relays ->
            relays.dropWhile { relays.isNotEmpty() }
        }
    }

    fun toggleDownload(relay: RelaySetupInfo) {
        _relays.update {
            it.updated(relay, relay.copy(read = !relay.read))
        }
    }

    fun toggleUpload(relay: RelaySetupInfo) {
        _relays.update {
            it.updated(relay, relay.copy(write = !relay.write))
        }
    }

    fun toggleFollows(relay: RelaySetupInfo) {
        val newTypes = togglePresenceInSet(relay.feedTypes, FeedType.FOLLOWS)
        _relays.update {
            it.updated(relay, relay.copy(feedTypes = newTypes))
        }
    }

    fun toggleMessages(relay: RelaySetupInfo) {
        val newTypes = togglePresenceInSet(relay.feedTypes, FeedType.PRIVATE_DMS)
        _relays.update {
            it.updated(relay, relay.copy(feedTypes = newTypes))
        }
    }

    fun togglePublicChats(relay: RelaySetupInfo) {
        val newTypes = togglePresenceInSet(relay.feedTypes, FeedType.PUBLIC_CHATS)
        _relays.update {
            it.updated(relay, relay.copy(feedTypes = newTypes))
        }
    }

    fun toggleGlobal(relay: RelaySetupInfo) {
        val newTypes = togglePresenceInSet(relay.feedTypes, FeedType.GLOBAL)
        _relays.update {
            it.updated(relay, relay.copy(feedTypes = newTypes))
        }
    }

    fun toggleSearch(relay: RelaySetupInfo) {
        val newTypes = togglePresenceInSet(relay.feedTypes, FeedType.SEARCH)
        _relays.update {
            it.updated(relay, relay.copy(feedTypes = newTypes))
        }
    }

    fun togglePaidRelay(relay: RelaySetupInfo, paid: Boolean) {
        _relays.update {
            it.updated(relay, relay.copy(paidRelay = paid))
        }
    }
}

fun <T> Iterable<T>.updated(old: T, new: T): List<T> = map { if (it == old) new else it }

fun <T> togglePresenceInSet(set: Set<T>, item: T): Set<T> {
    return if (set.contains(item)) set.minus(item) else set.plus(item)
}
