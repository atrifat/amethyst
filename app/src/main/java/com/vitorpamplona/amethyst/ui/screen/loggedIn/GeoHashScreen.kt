package com.vitorpamplona.amethyst.ui.screen.loggedIn

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fonfon.kgeohash.toGeoHash
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.service.NostrGeohashDataSource
import com.vitorpamplona.amethyst.service.ReverseGeoLocationUtil
import com.vitorpamplona.amethyst.ui.screen.NostrGeoHashFeedViewModel
import com.vitorpamplona.amethyst.ui.screen.RefresheableFeedView
import com.vitorpamplona.amethyst.ui.theme.StdPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun GeoHashScreen(tag: String?, accountViewModel: AccountViewModel, nav: (String) -> Unit) {
    if (tag == null) return

    PrepareViewModelsGeoHashScreen(tag, accountViewModel, nav)
}

@Composable
fun PrepareViewModelsGeoHashScreen(tag: String, accountViewModel: AccountViewModel, nav: (String) -> Unit) {
    val followsFeedViewModel: NostrGeoHashFeedViewModel = viewModel(
        key = tag + "GeoHashFeedViewModel",
        factory = NostrGeoHashFeedViewModel.Factory(
            tag,
            accountViewModel.account
        )
    )

    GeoHashScreen(tag, followsFeedViewModel, accountViewModel, nav)
}

@Composable
fun GeoHashScreen(tag: String, feedViewModel: NostrGeoHashFeedViewModel, accountViewModel: AccountViewModel, nav: (String) -> Unit) {
    val lifeCycleOwner = LocalLifecycleOwner.current

    NostrGeohashDataSource.loadHashtag(tag)

    DisposableEffect(tag) {
        NostrGeohashDataSource.start()
        feedViewModel.invalidateData()
        onDispose {
            NostrGeohashDataSource.loadHashtag(null)
            NostrGeohashDataSource.stop()
        }
    }

    DisposableEffect(lifeCycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                println("Hashtag Start")
                NostrGeohashDataSource.loadHashtag(tag)
                NostrGeohashDataSource.start()
                feedViewModel.invalidateData()
            }
            if (event == Lifecycle.Event.ON_PAUSE) {
                println("Hashtag Stop")
                NostrGeohashDataSource.loadHashtag(null)
                NostrGeohashDataSource.stop()
            }
        }

        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(Modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier.padding(vertical = 0.dp)
        ) {
            RefresheableFeedView(
                feedViewModel,
                null,
                accountViewModel = accountViewModel,
                nav = nav
            )
        }
    }
}

@Composable
fun GeoHashHeader(tag: String, modifier: Modifier = StdPadding, account: AccountViewModel, onClick: () -> Unit = { }) {
    Column(
        Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = modifier) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                DislayGeoTagHeader(tag, remember { Modifier.weight(1f) })

                GeoHashActionOptions(tag, account)
            }
        }

        Divider(
            thickness = 0.25.dp
        )
    }
}

@Composable
fun DislayGeoTagHeader(geohash: String, modifier: Modifier) {
    val context = LocalContext.current

    var cityName by remember(geohash) {
        mutableStateOf<String>(geohash)
    }

    LaunchedEffect(key1 = geohash) {
        launch(Dispatchers.IO) {
            val newCityName = ReverseGeoLocationUtil().execute(geohash.toGeoHash().toLocation(), context)?.ifBlank { null }
            if (newCityName != null && newCityName != cityName) {
                cityName = "$newCityName ($geohash)"
            }
        }
    }

    Text(
        cityName,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
fun GeoHashActionOptions(
    tag: String,
    accountViewModel: AccountViewModel
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val userState by accountViewModel.userProfile().live().follows.observeAsState()
    val isFollowingTag by remember(userState) {
        derivedStateOf {
            userState?.user?.isFollowingGeohashCached(tag) ?: false
        }
    }

    if (isFollowingTag) {
        UnfollowButton {
            if (!accountViewModel.isWriteable()) {
                if (accountViewModel.loggedInWithExternalSigner()) {
                    scope.launch(Dispatchers.IO) {
                        accountViewModel.account.unfollowGeohash(tag)
                    }
                } else {
                    scope.launch {
                        Toast
                            .makeText(
                                context,
                                context.getString(R.string.login_with_a_private_key_to_be_able_to_unfollow),
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
                }
            } else {
                scope.launch(Dispatchers.IO) {
                    accountViewModel.account.unfollowGeohash(tag)
                }
            }
        }
    } else {
        FollowButton {
            if (!accountViewModel.isWriteable()) {
                if (accountViewModel.loggedInWithExternalSigner()) {
                    scope.launch(Dispatchers.IO) {
                        accountViewModel.account.followGeohash(tag)
                    }
                } else {
                    scope.launch {
                        Toast
                            .makeText(
                                context,
                                context.getString(R.string.login_with_a_private_key_to_be_able_to_follow),
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
                }
            } else {
                scope.launch(Dispatchers.IO) {
                    accountViewModel.account.followGeohash(tag)
                }
            }
        }
    }
}
