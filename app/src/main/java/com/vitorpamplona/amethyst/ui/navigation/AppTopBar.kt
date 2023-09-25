package com.vitorpamplona.amethyst.ui.navigation

import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import coil.Coil
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.model.Account
import com.vitorpamplona.amethyst.model.AddressableNote
import com.vitorpamplona.amethyst.model.GLOBAL_FOLLOWS
import com.vitorpamplona.amethyst.model.KIND3_FOLLOWS
import com.vitorpamplona.amethyst.model.LocalCache
import com.vitorpamplona.amethyst.service.NostrAccountDataSource
import com.vitorpamplona.amethyst.service.NostrChannelDataSource
import com.vitorpamplona.amethyst.service.NostrChatroomDataSource
import com.vitorpamplona.amethyst.service.NostrChatroomListDataSource
import com.vitorpamplona.amethyst.service.NostrCommunityDataSource
import com.vitorpamplona.amethyst.service.NostrDiscoveryDataSource
import com.vitorpamplona.amethyst.service.NostrGeohashDataSource
import com.vitorpamplona.amethyst.service.NostrHashtagDataSource
import com.vitorpamplona.amethyst.service.NostrHomeDataSource
import com.vitorpamplona.amethyst.service.NostrSearchEventOrUserDataSource
import com.vitorpamplona.amethyst.service.NostrSingleChannelDataSource
import com.vitorpamplona.amethyst.service.NostrSingleEventDataSource
import com.vitorpamplona.amethyst.service.NostrSingleUserDataSource
import com.vitorpamplona.amethyst.service.NostrThreadDataSource
import com.vitorpamplona.amethyst.service.NostrUserProfileDataSource
import com.vitorpamplona.amethyst.service.NostrVideoDataSource
import com.vitorpamplona.amethyst.service.checkNotInMainThread
import com.vitorpamplona.amethyst.service.relays.Client
import com.vitorpamplona.amethyst.service.relays.RelayPool
import com.vitorpamplona.amethyst.ui.components.RobohashAsyncImageProxy
import com.vitorpamplona.amethyst.ui.note.AmethystIcon
import com.vitorpamplona.amethyst.ui.note.ArrowBackIcon
import com.vitorpamplona.amethyst.ui.note.ClickableUserPicture
import com.vitorpamplona.amethyst.ui.note.LoadAddressableNote
import com.vitorpamplona.amethyst.ui.note.LoadChannel
import com.vitorpamplona.amethyst.ui.note.LoadCityName
import com.vitorpamplona.amethyst.ui.note.LoadUser
import com.vitorpamplona.amethyst.ui.note.LongCommunityHeader
import com.vitorpamplona.amethyst.ui.note.NonClickableUserPictures
import com.vitorpamplona.amethyst.ui.note.SearchIcon
import com.vitorpamplona.amethyst.ui.note.ShortCommunityHeader
import com.vitorpamplona.amethyst.ui.note.UserCompose
import com.vitorpamplona.amethyst.ui.note.UsernameDisplay
import com.vitorpamplona.amethyst.ui.screen.equalImmutableLists
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.amethyst.ui.screen.loggedIn.DislayGeoTagHeader
import com.vitorpamplona.amethyst.ui.screen.loggedIn.GeoHashActionOptions
import com.vitorpamplona.amethyst.ui.screen.loggedIn.HashtagActionOptions
import com.vitorpamplona.amethyst.ui.screen.loggedIn.LoadRoom
import com.vitorpamplona.amethyst.ui.screen.loggedIn.LoadRoomByAuthor
import com.vitorpamplona.amethyst.ui.screen.loggedIn.LongChannelHeader
import com.vitorpamplona.amethyst.ui.screen.loggedIn.LongRoomHeader
import com.vitorpamplona.amethyst.ui.screen.loggedIn.RoomNameOnlyDisplay
import com.vitorpamplona.amethyst.ui.screen.loggedIn.ShortChannelHeader
import com.vitorpamplona.amethyst.ui.screen.loggedIn.SpinnerSelectionDialog
import com.vitorpamplona.amethyst.ui.theme.BottomTopHeight
import com.vitorpamplona.amethyst.ui.theme.DoubleHorzSpacer
import com.vitorpamplona.amethyst.ui.theme.HalfVertSpacer
import com.vitorpamplona.amethyst.ui.theme.HeaderPictureModifier
import com.vitorpamplona.amethyst.ui.theme.Size22Modifier
import com.vitorpamplona.amethyst.ui.theme.Size34dp
import com.vitorpamplona.amethyst.ui.theme.Size40dp
import com.vitorpamplona.amethyst.ui.theme.placeholderText
import com.vitorpamplona.quartz.events.ChatroomKey
import com.vitorpamplona.quartz.events.ContactListEvent
import com.vitorpamplona.quartz.events.PeopleListEvent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Composable
fun AppTopBar(
    followLists: FollowListViewModel,
    navEntryState: State<NavBackStackEntry?>,
    scaffoldState: ScaffoldState,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
    navPopBack: () -> Unit
) {
    val currentRoute by remember(navEntryState.value) {
        derivedStateOf {
            navEntryState.value?.destination?.route?.substringBefore("?")
        }
    }

    val id by remember(navEntryState.value) {
        derivedStateOf {
            navEntryState.value?.arguments?.getString("id")
        }
    }

    RenderTopRouteBar(currentRoute, id, followLists, scaffoldState, accountViewModel, nav, navPopBack)
}

@Composable
private fun RenderTopRouteBar(
    currentRoute: String?,
    id: String?,
    followLists: FollowListViewModel,
    scaffoldState: ScaffoldState,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
    navPopBack: () -> Unit
) {
    when (currentRoute) {
        Route.Home.base -> HomeTopBar(followLists, scaffoldState, accountViewModel, nav)
        Route.Video.base -> StoriesTopBar(followLists, scaffoldState, accountViewModel, nav)
        Route.Discover.base -> DiscoveryTopBar(followLists, scaffoldState, accountViewModel, nav)
        Route.Notification.base -> NotificationTopBar(followLists, scaffoldState, accountViewModel, nav)
        Route.Settings.base -> TopBarWithBackButton(stringResource(id = R.string.application_preferences), navPopBack)
        else -> {
            if (id != null) {
                when (currentRoute) {
                    Route.Channel.base -> ChannelTopBar(id, accountViewModel, nav, navPopBack)
                    Route.RoomByAuthor.base -> RoomByAuthorTopBar(id, accountViewModel, nav, navPopBack)
                    Route.Room.base -> RoomTopBar(id, accountViewModel, nav, navPopBack)
                    Route.Community.base -> CommunityTopBar(id, accountViewModel, nav, navPopBack)
                    Route.Hashtag.base -> HashTagTopBar(id, accountViewModel, navPopBack)
                    Route.Geohash.base -> GeoHashTopBar(id, accountViewModel, navPopBack)
                    else -> MainTopBar(scaffoldState, accountViewModel, nav)
                }
            } else {
                MainTopBar(scaffoldState, accountViewModel, nav)
            }
        }
    }
}

@Composable
private fun GeoHashTopBar(
    tag: String,
    accountViewModel: AccountViewModel,
    navPopBack: () -> Unit
) {
    FlexibleTopBarWithBackButton(
        title = {
            DislayGeoTagHeader(tag, remember { Modifier.weight(1f) })
            GeoHashActionOptions(tag, accountViewModel)
        },
        popBack = navPopBack
    )
}

@Composable
private fun HashTagTopBar(
    tag: String,
    accountViewModel: AccountViewModel,
    navPopBack: () -> Unit
) {
    FlexibleTopBarWithBackButton(
        title = {
            Text(
                remember(tag) { "#$tag" },
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            HashtagActionOptions(tag, accountViewModel)
        },
        popBack = navPopBack
    )
}

@Composable
private fun CommunityTopBar(
    id: String,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
    navPopBack: () -> Unit
) {
    LoadAddressableNote(aTagHex = id, accountViewModel) { baseNote ->
        if (baseNote != null) {
            FlexibleTopBarWithBackButton(
                title = {
                    ShortCommunityHeader(baseNote, fontWeight = FontWeight.Medium, accountViewModel, nav)
                },
                extendableRow = {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        LongCommunityHeader(baseNote = baseNote, accountViewModel = accountViewModel, nav = nav)
                    }
                },
                popBack = navPopBack
            )
        } else {
            Spacer(BottomTopHeight)
        }
    }
}

@Composable
private fun RoomTopBar(
    id: String,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
    navPopBack: () -> Unit
) {
    LoadRoom(roomId = id, accountViewModel) { room ->
        if (room != null) {
            RenderRoomTopBar(room, accountViewModel, nav, navPopBack)
        } else {
            Spacer(BottomTopHeight)
        }
    }
}

@Composable
private fun RoomByAuthorTopBar(
    id: String,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
    navPopBack: () -> Unit
) {
    LoadRoomByAuthor(authorPubKeyHex = id, accountViewModel) { room ->
        if (room != null) {
            RenderRoomTopBar(room, accountViewModel, nav, navPopBack)
        } else {
            Spacer(BottomTopHeight)
        }
    }
}

@Composable
private fun RenderRoomTopBar(
    room: ChatroomKey,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
    navPopBack: () -> Unit
) {
    if (room.users.size == 1) {
        FlexibleTopBarWithBackButton(
            title = {
                LoadUser(baseUserHex = room.users.first(), accountViewModel) { baseUser ->
                    if (baseUser != null) {
                        ClickableUserPicture(
                            baseUser = baseUser,
                            accountViewModel = accountViewModel,
                            size = Size34dp
                        )

                        Spacer(modifier = DoubleHorzSpacer)

                        UsernameDisplay(baseUser, Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    }
                }
            },
            extendableRow = {
                LoadUser(baseUserHex = room.users.first(), accountViewModel) {
                    if (it != null) {
                        UserCompose(
                            baseUser = it,
                            accountViewModel = accountViewModel,
                            nav = nav
                        )
                    }
                }
            },
            popBack = navPopBack
        )
    } else {
        FlexibleTopBarWithBackButton(
            title = {
                NonClickableUserPictures(
                    users = room.users,
                    accountViewModel = accountViewModel,
                    size = Size34dp
                )

                RoomNameOnlyDisplay(
                    room,
                    Modifier
                        .padding(start = 10.dp)
                        .weight(1f),
                    fontWeight = FontWeight.Medium,
                    accountViewModel.userProfile()
                )
            },
            extendableRow = {
                LongRoomHeader(room = room, accountViewModel = accountViewModel, nav = nav)
            },
            popBack = navPopBack
        )
    }
}

@Composable
private fun ChannelTopBar(
    id: String,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
    navPopBack: () -> Unit
) {
    LoadChannel(baseChannelHex = id, accountViewModel) { baseChannel ->
        FlexibleTopBarWithBackButton(
            title = {
                ShortChannelHeader(
                    baseChannel = baseChannel,
                    accountViewModel = accountViewModel,
                    fontWeight = FontWeight.Medium,
                    nav = nav,
                    showFlag = true
                )
            },
            extendableRow = {
                LongChannelHeader(baseChannel = baseChannel, accountViewModel = accountViewModel, nav = nav)
            },
            popBack = navPopBack
        )
    }
}

@Composable
fun NoTopBar() {
}

@Composable
fun StoriesTopBar(followLists: FollowListViewModel, scaffoldState: ScaffoldState, accountViewModel: AccountViewModel, nav: (String) -> Unit) {
    GenericMainTopBar(scaffoldState, accountViewModel, nav) { accountViewModel ->
        val list by accountViewModel.storiesListLiveData.observeAsState(GLOBAL_FOLLOWS)

        FollowList(
            followListsModel = followLists,
            listName = list,
            withGlobal = true,
            withRoutes = false
        ) { listName ->
            accountViewModel.account.changeDefaultStoriesFollowList(listName.code)
        }
    }
}

@Composable
fun HomeTopBar(followLists: FollowListViewModel, scaffoldState: ScaffoldState, accountViewModel: AccountViewModel, nav: (String) -> Unit) {
    GenericMainTopBar(scaffoldState, accountViewModel, nav) { accountViewModel ->
        val list by accountViewModel.homeListLiveData.observeAsState(KIND3_FOLLOWS)

        FollowList(
            followListsModel = followLists,
            listName = list,
            withGlobal = true,
            withRoutes = true
        ) { listName ->
            if (listName.type == CodeNameType.ROUTE) {
                nav(listName.code)
            } else {
                accountViewModel.account.changeDefaultHomeFollowList(listName.code)
            }
        }
    }
}

@Composable
fun NotificationTopBar(followLists: FollowListViewModel, scaffoldState: ScaffoldState, accountViewModel: AccountViewModel, nav: (String) -> Unit) {
    GenericMainTopBar(scaffoldState, accountViewModel, nav) { accountViewModel ->
        val list by accountViewModel.notificationListLiveData.observeAsState(GLOBAL_FOLLOWS)

        FollowList(
            followListsModel = followLists,
            listName = list,
            withGlobal = true,
            withRoutes = false
        ) { listName ->
            accountViewModel.account.changeDefaultNotificationFollowList(listName.code)
        }
    }
}

@Composable
fun DiscoveryTopBar(followLists: FollowListViewModel, scaffoldState: ScaffoldState, accountViewModel: AccountViewModel, nav: (String) -> Unit) {
    GenericMainTopBar(scaffoldState, accountViewModel, nav) { accountViewModel ->
        val list by accountViewModel.discoveryListLiveData.observeAsState(GLOBAL_FOLLOWS)

        FollowList(
            followListsModel = followLists,
            listName = list,
            withGlobal = true,
            withRoutes = false
        ) { listName ->
            accountViewModel.account.changeDefaultDiscoveryFollowList(listName.code)
        }
    }
}

@Composable
fun MainTopBar(scaffoldState: ScaffoldState, accountViewModel: AccountViewModel, nav: (String) -> Unit) {
    GenericMainTopBar(scaffoldState, accountViewModel, nav) {
        AmethystClickableIcon()
    }
}

@OptIn(coil.annotation.ExperimentalCoilApi::class)
@Composable
fun GenericMainTopBar(
    scaffoldState: ScaffoldState,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
    content: @Composable (AccountViewModel) -> Unit
) {
    Column(modifier = BottomTopHeight) {
        MyTopAppBar(
            elevation = 0.dp,
            backgroundColor = MaterialTheme.colors.surface,
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(Modifier) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            content(accountViewModel)
                        }
                    }
                }
            },
            navigationIcon = {
                val coroutineScope = rememberCoroutineScope()
                LoggedInUserPictureDrawer(accountViewModel) {
                    coroutineScope.launch {
                        scaffoldState.drawerState.open()
                    }
                }
            },
            actions = {
                SearchButton() {
                    nav(Route.Search.route)
                }
            }
        )
        Divider(thickness = 0.25.dp)
    }
}

@Composable
private fun SearchButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick
    ) {
        SearchIcon(modifier = Size22Modifier, Color.Unspecified)
    }
}

@Composable
private fun LoggedInUserPictureDrawer(
    accountViewModel: AccountViewModel,
    onClick: () -> Unit
) {
    val profilePicture by accountViewModel.account.userProfile().live().profilePictureChanges.observeAsState()

    val pubkeyHex = remember { accountViewModel.userProfile().pubkeyHex }

    IconButton(
        onClick = onClick
    ) {
        RobohashAsyncImageProxy(
            robot = pubkeyHex,
            model = profilePicture,
            contentDescription = stringResource(id = R.string.profile_image),
            modifier = HeaderPictureModifier,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun FollowList(
    followListsModel: FollowListViewModel,
    listName: String,
    withGlobal: Boolean,
    withRoutes: Boolean,
    onChange: (CodeName) -> Unit
) {
    val context = LocalContext.current

    val kind3Follow = CodeName(KIND3_FOLLOWS, ResourceName(R.string.follow_list_kind3follows, context), CodeNameType.HARDCODED)
    val globalFollow = CodeName(GLOBAL_FOLLOWS, ResourceName(R.string.follow_list_global, context), CodeNameType.HARDCODED)

    val defaultOptions = if (withGlobal) listOf(kind3Follow, globalFollow) else listOf(kind3Follow)

    val followLists by followListsModel.peopleLists.collectAsState()
    val routeList by followListsModel.routes.collectAsState()

    val allLists = remember(followLists) {
        if (withRoutes) {
            (defaultOptions + followLists + routeList)
        } else {
            (defaultOptions + followLists)
        }
    }

    val followNames by remember(followLists) {
        derivedStateOf {
            allLists.map { it.name }.toImmutableList()
        }
    }

    SimpleTextSpinner(
        placeholder = allLists.firstOrNull { it.code == listName }?.name?.name() ?: "Select an Option",
        options = followNames,
        onSelect = {
            onChange(allLists.getOrNull(it) ?: kind3Follow)
        }
    )
}

enum class CodeNameType {
    HARDCODED, PEOPLE_LIST, ROUTE
}

abstract class Name {
    abstract fun name(): String
}

class GeoHashName(val geoHashTag: String) : Name() {
    override fun name() = "/g/$geoHashTag"
}
class HashtagName(val hashTag: String) : Name() {
    override fun name() = "#$hashTag"
}
class ResourceName(val resourceId: Int, val context: Context) : Name() {
    override fun name() = context.getString(resourceId)
}

class PeopleListName(val note: AddressableNote) : Name() {
    override fun name() = note.dTag() ?: ""
}
class CommunityName(val note: AddressableNote) : Name() {
    override fun name() = "/n/${(note.dTag() ?: "")}"
}

@Immutable
data class CodeName(val code: String, val name: Name, val type: CodeNameType)

@Stable
class FollowListViewModel(val account: Account) : ViewModel() {
    private var _peopleLists = MutableStateFlow<ImmutableList<CodeName>>(emptyList<CodeName>().toPersistentList())
    val peopleLists = _peopleLists.asStateFlow()

    private var _routes = MutableStateFlow<ImmutableList<CodeName>>(emptyList<CodeName>().toPersistentList())
    val routes = _routes.asStateFlow()

    fun refresh() {
        viewModelScope.launch(Dispatchers.Default) {
            refreshFollows()
        }
    }

    private suspend fun refreshFollows() {
        checkNotInMainThread()

        val newFollowLists = LocalCache.addressables.mapNotNull {
            val event = (it.value.event as? PeopleListEvent)
            // Has to have an list
            if (event != null &&
                event.pubKey == account.userProfile().pubkeyHex &&
                (event.tags.size > 1 || event.content.length > 50)
            ) {
                CodeName(event.address().toTag(), PeopleListName(it.value), CodeNameType.PEOPLE_LIST)
            } else {
                null
            }
        }.sortedBy { it.name.name() }.toImmutableList()

        if (!equalImmutableLists(_peopleLists.value, newFollowLists)) {
            _peopleLists.emit(newFollowLists)
        }

        val communities = account.userProfile().cachedFollowingCommunitiesSet().mapNotNull {
            LocalCache.checkGetOrCreateAddressableNote(it)?.let { communityNote ->
                CodeName("Community/${communityNote.idHex}", CommunityName(communityNote), CodeNameType.ROUTE)
            }
        }

        val hashtags = account.userProfile().cachedFollowingTagSet().map {
            CodeName("Hashtag/$it", HashtagName(it), CodeNameType.ROUTE)
        }

        val geotags = account.userProfile().cachedFollowingGeohashSet().map {
            CodeName("Geohash/$it", GeoHashName(it), CodeNameType.ROUTE)
        }

        val routeList = (communities + hashtags + geotags).sortedBy { it.name.name() }.toImmutableList()

        if (!equalImmutableLists(_routes.value, routeList)) {
            _routes.emit(routeList)
        }
    }

    var collectorJob: Job? = null

    init {
        Log.d("Init", "App Top Bar")
        refresh()
        collectorJob = viewModelScope.launch(Dispatchers.IO) {
            LocalCache.live.newEventBundles.collect { newNotes ->
                checkNotInMainThread()
                if (newNotes.any {
                    it.event?.pubKey() == account.userProfile().pubkeyHex && (it.event is PeopleListEvent || it.event is ContactListEvent)
                }
                ) {
                    refresh()
                }
            }
        }
    }

    override fun onCleared() {
        collectorJob?.cancel()
        super.onCleared()
    }

    class Factory(val account: Account) : ViewModelProvider.Factory {
        override fun <FollowListViewModel : ViewModel> create(modelClass: Class<FollowListViewModel>): FollowListViewModel {
            return FollowListViewModel(account) as FollowListViewModel
        }
    }
}

@Composable
fun SimpleTextSpinner(
    placeholder: String,
    options: ImmutableList<Name>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    var optionsShowing by remember { mutableStateOf(false) }
    var currentText by remember(placeholder) { mutableStateOf(placeholder) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.size(20.dp))
            Text(placeholder)
            Icon(
                imageVector = Icons.Default.ExpandMore,
                null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colors.placeholderText
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    optionsShowing = true
                }
        )
    }

    if (optionsShowing) {
        options.isNotEmpty().also {
            SpinnerSelectionDialog(
                options = options,
                onDismiss = { optionsShowing = false },
                onSelect = {
                    currentText = options[it].name()
                    optionsShowing = false
                    onSelect(it)
                }
            ) {
                RenderOption(it)
            }
        }
    }
}

@Composable
fun RenderOption(it: Name) {
    when (it) {
        is GeoHashName -> {
            LoadCityName(it.geoHashTag) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "/g/$it", color = MaterialTheme.colors.onSurface)
                }
            }
        }
        is HashtagName -> {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = it.name(), color = MaterialTheme.colors.onSurface)
            }
        }
        is ResourceName -> {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = it.resourceId), color = MaterialTheme.colors.onSurface)
            }
        }
        is PeopleListName -> {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = it.name(), color = MaterialTheme.colors.onSurface)
            }
        }
        is CommunityName -> {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                val name by it.note.live().metadata.map {
                    "/n/" + (it.note as? AddressableNote)?.dTag()
                }.observeAsState()

                Text(text = name ?: "", color = MaterialTheme.colors.onSurface)
            }
        }
    }
}

@Composable
fun TopBarWithBackButton(caption: String, popBack: () -> Unit) {
    Column(modifier = BottomTopHeight) {
        MyTopAppBar(
            elevation = 0.dp,
            title = { Text(caption) },
            navigationIcon = {
                IconButton(
                    onClick = popBack,
                    modifier = Modifier
                ) {
                    ArrowBackIcon()
                }
            },
            actions = {}
        )
        Divider(thickness = 0.25.dp)
    }
}

@Composable
fun FlexibleTopBarWithBackButton(
    prefixRow: (@Composable () -> Unit)? = null,
    title: @Composable RowScope.() -> Unit,
    extendableRow: (@Composable () -> Unit)? = null,
    popBack: () -> Unit
) {
    Column() {
        MyExtensibleTopAppBar(
            elevation = 0.dp,
            prefixRow = prefixRow,
            title = title,
            extendableRow = extendableRow,
            navigationIcon = {
                IconButton(
                    onClick = popBack,
                    modifier = Modifier
                ) {
                    ArrowBackIcon()
                }
            },
            actions = {}
        )
        Spacer(modifier = HalfVertSpacer)
        Divider(thickness = 0.25.dp)
    }
}

@Composable
fun AmethystClickableIcon() {
    val context = LocalContext.current

    IconButton(
        onClick = {
            debugState(context)
        }
    ) {
        AmethystIcon(Size40dp)
    }
}

fun debugState(context: Context) {
    Client.allSubscriptions().map {
        "$it ${
        Client.getSubscriptionFilters(it)
            .joinToString { it.filter.toJson() }
        }"
    }.forEach {
        Log.d("STATE DUMP", it)
    }

    NostrAccountDataSource.printCounter()
    NostrChannelDataSource.printCounter()
    NostrChatroomDataSource.printCounter()
    NostrChatroomListDataSource.printCounter()
    NostrCommunityDataSource.printCounter()
    NostrDiscoveryDataSource.printCounter()
    NostrHashtagDataSource.printCounter()
    NostrGeohashDataSource.printCounter()
    NostrHomeDataSource.printCounter()
    NostrSearchEventOrUserDataSource.printCounter()
    NostrSingleChannelDataSource.printCounter()
    NostrSingleEventDataSource.printCounter()
    NostrSingleUserDataSource.printCounter()
    NostrThreadDataSource.printCounter()
    NostrUserProfileDataSource.printCounter()
    NostrVideoDataSource.printCounter()

    Log.d("STATE DUMP", "Connected Relays: " + RelayPool.connectedRelays())

    val imageLoader = Coil.imageLoader(context)
    Log.d("STATE DUMP", "Image Disk Cache ${(imageLoader.diskCache?.size ?: 0) / (1024 * 1024)}/${(imageLoader.diskCache?.maxSize ?: 0) / (1024 * 1024)} MB")
    Log.d("STATE DUMP", "Image Memory Cache ${(imageLoader.memoryCache?.size ?: 0) / (1024 * 1024)}/${(imageLoader.memoryCache?.maxSize ?: 0) / (1024 * 1024)} MB")

    Log.d("STATE DUMP", "Notes: " + LocalCache.notes.filter { it.value.liveSet != null }.size + " / " + LocalCache.notes.filter { it.value.event != null }.size + " / " + LocalCache.notes.size)
    Log.d("STATE DUMP", "Addressables: " + LocalCache.addressables.filter { it.value.liveSet != null }.size + " / " + LocalCache.addressables.filter { it.value.event != null }.size + " / " + LocalCache.addressables.size)
    Log.d("STATE DUMP", "Users: " + LocalCache.users.filter { it.value.liveSet != null }.size + " / " + LocalCache.users.filter { it.value.info?.latestMetadata != null }.size + " / " + LocalCache.users.size)

    Log.d("STATE DUMP", "Memory used by Events: " + LocalCache.notes.values.sumOf { it.event?.countMemory() ?: 0 } / (1024 * 1024) + " MB")

    LocalCache.notes.values.groupBy { it.event?.kind() }.forEach {
        Log.d("STATE DUMP", "Kind ${it.key}: \t${it.value.size} elements ")
    }
    LocalCache.addressables.values.groupBy { it.event?.kind() }.forEach {
        Log.d("STATE DUMP", "Kind ${it.key}: \t${it.value.size} elements ")
    }
}

@Composable
fun MyTopAppBar(
    title: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = AppBarDefaults.TopAppBarElevation
) {
    Surface(
        contentColor = contentColor,
        elevation = elevation,
        modifier = modifier
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(AppBarDefaults.ContentPadding),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (navigationIcon == null) {
                    Spacer(TitleInsetWithoutIcon)
                } else {
                    Row(TitleIconModifier, verticalAlignment = Alignment.CenterVertically) {
                        CompositionLocalProvider(
                            LocalContentAlpha provides ContentAlpha.high,
                            content = navigationIcon
                        )
                    }
                }

                Row(
                    Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProvideTextStyle(MaterialTheme.typography.h6) {
                        title()
                    }
                }

                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        content = actions
                    )
                }
            }
        }
    }
}

@Composable
fun MyExtensibleTopAppBar(
    prefixRow: (@Composable () -> Unit)? = null,
    title: @Composable RowScope.() -> Unit,
    extendableRow: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = AppBarDefaults.TopAppBarElevation
) {
    val expanded = remember { mutableStateOf(false) }

    Surface(
        color = backgroundColor,
        contentColor = contentColor,
        elevation = elevation,
        modifier = modifier.clickable {
            expanded.value = !expanded.value
        }
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(AppBarDefaults.ContentPadding),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (navigationIcon == null) {
                        Spacer(TitleInsetWithoutIcon)
                    } else {
                        Row(TitleIconModifier, verticalAlignment = Alignment.CenterVertically) {
                            CompositionLocalProvider(
                                LocalContentAlpha provides ContentAlpha.high,
                                content = navigationIcon
                            )
                        }
                    }

                    Row(
                        Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProvideTextStyle(MaterialTheme.typography.h6) {
                            title()
                        }
                    }

                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                            content = actions
                        )
                    }
                }

                if (expanded.value && extendableRow != null) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            extendableRow()
                        }
                    }
                }

                if (prefixRow != null) {
                    prefixRow()
                }
            }
        }
    }
}

private val AppBarHeight = 50.dp

// TODO: this should probably be part of the touch target of the start and end icons, clarify this
private val AppBarHorizontalPadding = 4.dp

// Start inset for the title when there is no navigation icon provided
private val TitleInsetWithoutIcon = Modifier.width(16.dp - AppBarHorizontalPadding)

// Start inset for the title when there is a navigation icon provided
private val TitleIconModifier = Modifier.width(48.dp - AppBarHorizontalPadding)
