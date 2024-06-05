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
package com.vitorpamplona.amethyst.ui.note

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.model.FeatureSetType
import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.model.User
import com.vitorpamplona.amethyst.ui.components.CreateTextWithEmoji
import com.vitorpamplona.amethyst.ui.components.SensitivityWarning
import com.vitorpamplona.amethyst.ui.components.TranslatableRichTextViewer
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.amethyst.ui.theme.ChatBubbleMaxSizeModifier
import com.vitorpamplona.amethyst.ui.theme.ChatBubbleShapeMe
import com.vitorpamplona.amethyst.ui.theme.ChatBubbleShapeThem
import com.vitorpamplona.amethyst.ui.theme.ChatPaddingInnerQuoteModifier
import com.vitorpamplona.amethyst.ui.theme.ChatPaddingModifier
import com.vitorpamplona.amethyst.ui.theme.DoubleHorzSpacer
import com.vitorpamplona.amethyst.ui.theme.Font12SP
import com.vitorpamplona.amethyst.ui.theme.HalfTopPadding
import com.vitorpamplona.amethyst.ui.theme.ReactionRowHeightChat
import com.vitorpamplona.amethyst.ui.theme.RowColSpacing
import com.vitorpamplona.amethyst.ui.theme.RowColSpacing5dp
import com.vitorpamplona.amethyst.ui.theme.Size15Modifier
import com.vitorpamplona.amethyst.ui.theme.Size20dp
import com.vitorpamplona.amethyst.ui.theme.Size5dp
import com.vitorpamplona.amethyst.ui.theme.StdHorzSpacer
import com.vitorpamplona.amethyst.ui.theme.StdTopPadding
import com.vitorpamplona.amethyst.ui.theme.chatAuthorBox
import com.vitorpamplona.amethyst.ui.theme.incognitoIconModifier
import com.vitorpamplona.amethyst.ui.theme.mediumImportanceLink
import com.vitorpamplona.amethyst.ui.theme.messageBubbleLimits
import com.vitorpamplona.amethyst.ui.theme.placeholderText
import com.vitorpamplona.amethyst.ui.theme.subtleBorder
import com.vitorpamplona.quartz.events.ChannelCreateEvent
import com.vitorpamplona.quartz.events.ChannelMetadataEvent
import com.vitorpamplona.quartz.events.ChatMessageEvent
import com.vitorpamplona.quartz.events.DraftEvent
import com.vitorpamplona.quartz.events.EmptyTagList
import com.vitorpamplona.quartz.events.ImmutableListOfLists
import com.vitorpamplona.quartz.events.PrivateDmEvent
import com.vitorpamplona.quartz.events.toImmutableListOfLists

@Composable
fun ChatroomMessageCompose(
    baseNote: Note,
    routeForLastRead: String?,
    innerQuote: Boolean = false,
    parentBackgroundColor: MutableState<Color>? = null,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
    onWantsToReply: (Note) -> Unit,
    onWantsToEditDraft: (Note) -> Unit,
) {
    WatchNoteEvent(baseNote = baseNote, accountViewModel = accountViewModel) {
        WatchBlockAndReport(
            note = baseNote,
            showHiddenWarning = innerQuote,
            modifier = Modifier.fillMaxWidth(),
            accountViewModel = accountViewModel,
            nav = nav,
        ) { canPreview ->
            NormalChatNote(
                baseNote,
                routeForLastRead,
                innerQuote,
                canPreview,
                parentBackgroundColor,
                accountViewModel,
                nav,
                onWantsToReply,
                onWantsToEditDraft,
            )
        }
    }
}

@Composable
fun NormalChatNote(
    note: Note,
    routeForLastRead: String?,
    innerQuote: Boolean = false,
    canPreview: Boolean = true,
    parentBackgroundColor: MutableState<Color>? = null,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
    onWantsToReply: (Note) -> Unit,
    onWantsToEditDraft: (Note) -> Unit,
) {
    val isLoggedInUser =
        remember(note.author) {
            accountViewModel.isLoggedUser(note.author)
        }

    if (routeForLastRead != null) {
        LaunchedEffect(key1 = routeForLastRead) {
            accountViewModel.loadAndMarkAsRead(routeForLastRead, note.createdAt())
        }
    }

    val drawAuthorInfo by
        remember(note) {
            derivedStateOf {
                val noteEvent = note.event
                if (accountViewModel.isLoggedUser(note.author)) {
                    false // never shows the user's pictures
                } else if (noteEvent is PrivateDmEvent) {
                    false // one-on-one, never shows it.
                } else if (noteEvent is ChatMessageEvent) {
                    // only shows in a group chat.
                    noteEvent.chatroomKey(accountViewModel.userProfile().pubkeyHex).users.size > 1
                } else {
                    true
                }
            }
        }

    ChatBubbleLayout(
        isLoggedInUser = isLoggedInUser,
        innerQuote = innerQuote,
        isSimplified = accountViewModel.settings.featureSet == FeatureSetType.SIMPLIFIED,
        hasDetailsToShow = note.zaps.isNotEmpty() || note.zapPayments.isNotEmpty() || note.reactions.isNotEmpty(),
        drawAuthorInfo = drawAuthorInfo,
        parentBackgroundColor = parentBackgroundColor,
        onClick = {
            if (note.event is ChannelCreateEvent) {
                nav("Channel/${note.idHex}")
                true
            } else {
                false
            }
        },
        onAuthorClick = {
            note.author?.let {
                nav("User/${it.pubkeyHex}")
            }
        },
        actionMenu = { popupExpanded, onDismiss ->
            NoteQuickActionMenu(
                note = note,
                popupExpanded = popupExpanded,
                onDismiss = onDismiss,
                onWantsToEditDraft = { onWantsToEditDraft(note) },
                accountViewModel = accountViewModel,
                nav = nav,
            )
        },
        drawAuthorLine = {
            DrawAuthorInfo(
                note,
                accountViewModel,
                nav,
            )
        },
        detailRow = {
            if (note.isDraft()) {
                DisplayDraftChat()
            }
            IncognitoBadge(note)
            ChatTimeAgo(note)
            RelayBadgesHorizontal(note, accountViewModel, nav = nav)
            Spacer(modifier = DoubleHorzSpacer)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = RowColSpacing) {
                ReplyReaction(
                    baseNote = note,
                    grayTint = MaterialTheme.colorScheme.placeholderText,
                    accountViewModel = accountViewModel,
                    showCounter = false,
                    iconSizeModifier = Size15Modifier,
                ) {
                    onWantsToReply(note)
                }
                Spacer(modifier = StdHorzSpacer)
                LikeReaction(note, MaterialTheme.colorScheme.placeholderText, accountViewModel, nav)

                ZapReaction(note, MaterialTheme.colorScheme.placeholderText, accountViewModel, nav = nav)
            }
        },
    ) { backgroundBubbleColor ->
        MessageBubbleLines(
            note,
            innerQuote,
            backgroundBubbleColor,
            onWantsToReply,
            onWantsToEditDraft,
            canPreview,
            accountViewModel,
            nav,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubbleLayout(
    isLoggedInUser: Boolean,
    innerQuote: Boolean,
    isSimplified: Boolean,
    hasDetailsToShow: Boolean,
    drawAuthorInfo: Boolean,
    parentBackgroundColor: MutableState<Color>? = null,
    onClick: () -> Boolean,
    onAuthorClick: () -> Unit,
    actionMenu: @Composable (popupExpanded: Boolean, onDismiss: () -> Unit) -> Unit,
    detailRow: @Composable () -> Unit,
    drawAuthorLine: @Composable () -> Unit,
    inner: @Composable (MutableState<Color>) -> Unit,
) {
    val loggedInColors = MaterialTheme.colorScheme.mediumImportanceLink
    val otherColors = MaterialTheme.colorScheme.subtleBorder
    val defaultBackground = MaterialTheme.colorScheme.background

    val backgroundBubbleColor =
        remember {
            if (isLoggedInUser) {
                mutableStateOf(
                    loggedInColors.compositeOver(parentBackgroundColor?.value ?: defaultBackground),
                )
            } else {
                mutableStateOf(otherColors.compositeOver(parentBackgroundColor?.value ?: defaultBackground))
            }
        }

    val alignment: Arrangement.Horizontal =
        if (isLoggedInUser) {
            Arrangement.End
        } else {
            Arrangement.Start
        }

    val shape: Shape =
        if (isLoggedInUser) {
            ChatBubbleShapeMe
        } else {
            ChatBubbleShapeThem
        }

    Row(
        modifier = if (innerQuote) ChatPaddingInnerQuoteModifier else ChatPaddingModifier,
        horizontalArrangement = alignment,
    ) {
        var popupExpanded by remember { mutableStateOf(false) }

        val modif2 = if (innerQuote) Modifier else ChatBubbleMaxSizeModifier

        val showDetails =
            remember {
                mutableStateOf(
                    if (isSimplified) {
                        hasDetailsToShow
                    } else {
                        true
                    },
                )
            }

        val clickableModifier =
            remember {
                Modifier.combinedClickable(
                    onClick = {
                        if (!onClick()) {
                            if (isSimplified) {
                                showDetails.value = !showDetails.value
                            }
                        }
                    },
                    onLongClick = { popupExpanded = true },
                )
            }

        Row(
            horizontalArrangement = alignment,
            modifier = modif2,
        ) {
            Surface(
                color = backgroundBubbleColor.value,
                shape = shape,
                modifier = clickableModifier,
            ) {
                Column(modifier = messageBubbleLimits, verticalArrangement = RowColSpacing5dp) {
                    if (drawAuthorInfo) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = alignment,
                            modifier = StdTopPadding.then(Modifier.clickable(onClick = onAuthorClick)),
                        ) {
                            drawAuthorLine()
                        }
                    }

                    inner(backgroundBubbleColor)

                    if (showDetails.value) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = ReactionRowHeightChat,
                        ) {
                            detailRow()
                        }
                    }
                }
            }
        }

        actionMenu(popupExpanded) {
            popupExpanded = false
        }
    }
}

@Preview
@Composable
private fun BubblePreview() {
    val backgroundBubbleColor =
        remember {
            mutableStateOf<Color>(Color.Transparent)
        }

    Column {
        ChatBubbleLayout(
            isLoggedInUser = false,
            innerQuote = false,
            isSimplified = false,
            hasDetailsToShow = true,
            drawAuthorInfo = true,
            parentBackgroundColor = backgroundBubbleColor,
            onClick = { false },
            onAuthorClick = {},
            actionMenu = { popupExpanded, onDismiss ->
            },
            drawAuthorLine = {
                UserDisplayNameLayout(
                    picture = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(Size20dp).clip(CircleShape),
                        )
                    },
                    name = {
                        Text("Me", fontWeight = FontWeight.Bold)
                    },
                )
            },
            detailRow = { Text("Relays and Actions") },
        ) { backgroundBubbleColor ->
            Text("This is my note")
        }

        ChatBubbleLayout(
            isLoggedInUser = true,
            innerQuote = false,
            isSimplified = false,
            hasDetailsToShow = true,
            drawAuthorInfo = true,
            parentBackgroundColor = backgroundBubbleColor,
            onClick = { false },
            onAuthorClick = {},
            actionMenu = { popupExpanded, onDismiss ->
            },
            drawAuthorLine = {
                UserDisplayNameLayout(
                    picture = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(Size20dp).clip(CircleShape),
                        )
                    },
                    name = {
                        Text("Me", fontWeight = FontWeight.Bold)
                    },
                )
            },
            detailRow = { Text("Relays and Actions") },
        ) { backgroundBubbleColor ->
            Text("This is a very long long loong note")
        }
    }
}

@Composable
private fun MessageBubbleLines(
    baseNote: Note,
    innerQuote: Boolean,
    backgroundBubbleColor: MutableState<Color>,
    onWantsToReply: (Note) -> Unit,
    onWantsToEditDraft: (Note) -> Unit,
    canPreview: Boolean,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
) {
    if (baseNote.event !is DraftEvent) {
        RenderReplyRow(
            note = baseNote,
            innerQuote = innerQuote,
            backgroundBubbleColor = backgroundBubbleColor,
            accountViewModel = accountViewModel,
            nav = nav,
            onWantsToReply = onWantsToReply,
            onWantsToEditDraft = onWantsToEditDraft,
        )
    }

    NoteRow(
        note = baseNote,
        canPreview = canPreview,
        innerQuote = innerQuote,
        onWantsToReply = onWantsToReply,
        onWantsToEditDraft = onWantsToEditDraft,
        backgroundBubbleColor = backgroundBubbleColor,
        accountViewModel = accountViewModel,
        nav = nav,
    )
}

@Composable
private fun RenderReplyRow(
    note: Note,
    innerQuote: Boolean,
    backgroundBubbleColor: MutableState<Color>,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
    onWantsToReply: (Note) -> Unit,
    onWantsToEditDraft: (Note) -> Unit,
) {
    if (!innerQuote && note.replyTo?.lastOrNull() != null) {
        RenderReply(note, backgroundBubbleColor, accountViewModel, nav, onWantsToReply, onWantsToEditDraft)
    }
}

@Composable
private fun RenderReply(
    note: Note,
    backgroundBubbleColor: MutableState<Color>,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
    onWantsToReply: (Note) -> Unit,
    onWantsToEditDraft: (Note) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val replyTo =
            produceState(initialValue = note.replyTo?.lastOrNull()) {
                accountViewModel.unwrapIfNeeded(value) {
                    value = it
                }
            }

        replyTo.value?.let { note ->
            ChatroomMessageCompose(
                baseNote = note,
                routeForLastRead = null,
                innerQuote = true,
                parentBackgroundColor = backgroundBubbleColor,
                accountViewModel = accountViewModel,
                nav = nav,
                onWantsToReply = onWantsToReply,
                onWantsToEditDraft = onWantsToEditDraft,
            )
        }
    }
}

@Composable
private fun NoteRow(
    note: Note,
    canPreview: Boolean,
    innerQuote: Boolean,
    onWantsToReply: (Note) -> Unit,
    onWantsToEditDraft: (Note) -> Unit,
    backgroundBubbleColor: MutableState<Color>,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        when (note.event) {
            is ChannelCreateEvent -> RenderCreateChannelNote(note)
            is ChannelMetadataEvent -> RenderChangeChannelMetadataNote(note)
            is DraftEvent ->
                RenderDraftEvent(
                    note,
                    canPreview,
                    innerQuote,
                    onWantsToReply,
                    onWantsToEditDraft,
                    backgroundBubbleColor,
                    accountViewModel,
                    nav,
                )
            else ->
                RenderRegularTextNote(
                    note,
                    canPreview,
                    innerQuote,
                    backgroundBubbleColor,
                    accountViewModel,
                    nav,
                )
        }
    }
}

@Composable
private fun RenderDraftEvent(
    note: Note,
    canPreview: Boolean,
    innerQuote: Boolean,
    onWantsToReply: (Note) -> Unit,
    onWantsToEditDraft: (Note) -> Unit,
    backgroundBubbleColor: MutableState<Color>,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
) {
    ObserveDraftEvent(note, accountViewModel) {
        Column {
            RenderReplyRow(
                note = it,
                innerQuote = innerQuote,
                backgroundBubbleColor = backgroundBubbleColor,
                accountViewModel = accountViewModel,
                nav = nav,
                onWantsToReply = onWantsToReply,
                onWantsToEditDraft = onWantsToEditDraft,
            )

            NoteRow(
                note = it,
                canPreview = canPreview,
                innerQuote = innerQuote,
                onWantsToReply = onWantsToReply,
                onWantsToEditDraft = onWantsToEditDraft,
                backgroundBubbleColor = backgroundBubbleColor,
                accountViewModel = accountViewModel,
                nav = nav,
            )
        }
    }
}

@Composable
private fun ConstrainedStatusRow(
    firstColumn: @Composable () -> Unit,
    secondColumn: @Composable () -> Unit,
) {
}

@Composable
fun IncognitoBadge(baseNote: Note) {
    if (baseNote.event is ChatMessageEvent) {
        Icon(
            painter = painterResource(id = R.drawable.incognito),
            null,
            modifier = incognitoIconModifier,
            tint = MaterialTheme.colorScheme.placeholderText,
        )
        Spacer(modifier = StdHorzSpacer)
    } else if (baseNote.event is PrivateDmEvent) {
        Icon(
            painter = painterResource(id = R.drawable.incognito_off),
            null,
            modifier = incognitoIconModifier,
            tint = MaterialTheme.colorScheme.placeholderText,
        )
        Spacer(modifier = StdHorzSpacer)
    }
}

@Composable
fun ChatTimeAgo(baseNote: Note) {
    val nowStr = stringResource(id = R.string.now)

    val time by
        remember(baseNote) { derivedStateOf { timeAgoShort(baseNote.createdAt() ?: 0, nowStr) } }

    Text(
        text = time,
        color = MaterialTheme.colorScheme.placeholderText,
        fontSize = Font12SP,
        maxLines = 1,
    )
}

@Composable
private fun RenderRegularTextNote(
    note: Note,
    canPreview: Boolean,
    innerQuote: Boolean,
    backgroundBubbleColor: MutableState<Color>,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
) {
    LoadDecryptedContentOrNull(note = note, accountViewModel = accountViewModel) { eventContent ->
        if (eventContent != null) {
            SensitivityWarning(
                note = note,
                accountViewModel = accountViewModel,
            ) {
                val tags = remember(note.event) { note.event?.tags()?.toImmutableListOfLists() ?: EmptyTagList }

                TranslatableRichTextViewer(
                    content = eventContent,
                    canPreview = canPreview,
                    quotesLeft = if (innerQuote) 0 else 1,
                    modifier = HalfTopPadding,
                    tags = tags,
                    backgroundColor = backgroundBubbleColor,
                    id = note.idHex,
                    callbackUri = note.toNostrUri(),
                    accountViewModel = accountViewModel,
                    nav = nav,
                )
            }
        } else {
            TranslatableRichTextViewer(
                content = stringResource(id = R.string.could_not_decrypt_the_message),
                canPreview = true,
                quotesLeft = 0,
                modifier = HalfTopPadding,
                tags = EmptyTagList,
                backgroundColor = backgroundBubbleColor,
                id = note.idHex,
                callbackUri = note.toNostrUri(),
                accountViewModel = accountViewModel,
                nav = nav,
            )
        }
    }
}

@Composable
private fun RenderChangeChannelMetadataNote(note: Note) {
    val noteEvent = note.event as? ChannelMetadataEvent ?: return

    val channelInfo = noteEvent.channelInfo()
    val text =
        note.author?.toBestDisplayName().toString() +
            " ${stringResource(R.string.changed_chat_name_to)} '" +
            (channelInfo.name ?: "") +
            "', ${stringResource(R.string.description_to)} '" +
            (channelInfo.about ?: "") +
            "', ${stringResource(R.string.and_picture_to)} '" +
            (channelInfo.picture ?: "") +
            "'"

    CreateTextWithEmoji(
        text = text,
        tags = note.author?.info?.tags,
    )
}

@Composable
private fun RenderCreateChannelNote(note: Note) {
    val noteEvent = note.event as? ChannelCreateEvent ?: return
    val channelInfo = remember { noteEvent.channelInfo() }

    val text =
        note.author?.toBestDisplayName().toString() +
            " ${stringResource(R.string.created)} " +
            (channelInfo.name ?: "") +
            " ${stringResource(R.string.with_description_of)} '" +
            (channelInfo.about ?: "") +
            "', ${stringResource(R.string.and_picture)} '" +
            (channelInfo.picture ?: "") +
            "'"

    CreateTextWithEmoji(
        text = text,
        tags = note.author?.info?.tags,
    )
}

@Composable
private fun DrawAuthorInfo(
    baseNote: Note,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
) {
    baseNote.author?.let {
        WatchAndDisplayUser(it, accountViewModel, nav)
    }
}

@Composable
fun UserDisplayNameLayout(
    picture: @Composable () -> Unit,
    name: @Composable () -> Unit,
) {
    Box(chatAuthorBox, contentAlignment = Alignment.TopEnd) {
        picture()
    }

    Spacer(modifier = StdHorzSpacer)

    name()
}

@Composable
private fun WatchAndDisplayUser(
    author: User,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
) {
    val userState by author.live().userMetadataInfo.observeAsState()

    UserDisplayNameLayout(
        picture = {
            InnerUserPicture(
                userHex = author.pubkeyHex,
                userPicture = userState?.picture,
                userName = userState?.bestName(),
                size = Size20dp,
                modifier = Modifier,
                accountViewModel = accountViewModel,
            )

            ObserveAndDisplayFollowingMark(author.pubkeyHex, Size5dp, accountViewModel)
        },
        name = {
            if (userState != null) {
                DisplayMessageUsername(userState?.bestName() ?: author.pubkeyDisplayHex(), userState?.tags ?: EmptyTagList)
            } else {
                DisplayMessageUsername(author.pubkeyDisplayHex(), EmptyTagList)
            }
        },
    )
}

@Composable
private fun DisplayMessageUsername(
    userDisplayName: String,
    userTags: ImmutableListOfLists<String>,
) {
    CreateTextWithEmoji(
        text = userDisplayName,
        tags = userTags,
        maxLines = 1,
        fontWeight = FontWeight.Bold,
    )
}
