package com.tomatix.app.ui.chatbot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tomatix.app.data.model.ChatMessage
import com.tomatix.app.ui.theme.Green400
import com.tomatix.app.ui.theme.Green600
import com.tomatix.app.ui.theme.Green700
import com.tomatix.app.ui.theme.Gray200
import com.tomatix.app.ui.theme.Gray500
import com.tomatix.app.ui.theme.Gray600
import com.tomatix.app.ui.theme.Gray800
import com.tomatix.app.ui.theme.Red500
import com.tomatix.app.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatbotOverlay(
    viewModel: ChatbotViewModel = hiltViewModel()
) {
    val isOpen by viewModel.isOpen.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val inputValue by viewModel.inputValue.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val quickActions = viewModel.quickActions

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isOpen,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .padding(horizontal = 0.dp)
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ChatHeader(onClose = { viewModel.toggleOpen() })

                    if (messages.size <= 1) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickActions.forEach { action ->
                                QuickActionChip(
                                    label = action.label,
                                    onClick = {
                                        viewModel.updateInput(action.query)
                                        viewModel.sendMessage()
                                    }
                                )
                            }
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                    ) {
                        items(messages) { message ->
                            MessageBubble(message = message)
                        }
                        if (isTyping) {
                            item {
                                TypingIndicator()
                            }
                        }
                    }

                    ChatInput(
                        value = inputValue,
                        onValueChange = { viewModel.updateInput(it) },
                        onSend = { viewModel.sendMessage() }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !isOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            FloatingActionButton(
                onClick = { viewModel.toggleOpen() },
                modifier = Modifier
                    .padding(end = 16.dp, bottom = 16.dp)
                    .size(56.dp),
                containerColor = Color.Transparent,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Green400, Green600, Green700)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Chat",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Red500)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatHeader(onClose: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Green600, Green700)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Assistant",
                        color = White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Smart",
                        color = Green400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickActionChip(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Green400.copy(alpha = 0.1f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = Green600,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.sender == "user"
    val alignment: Alignment.Horizontal =
        if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .shadow(
                    elevation = if (isUser) 0.dp else 2.dp,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(if (isUser) Green600 else Gray200)
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = if (isUser) White else Gray800,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }

        if (message.recommendations.isNotEmpty() && !isUser) {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Green400.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Recommendations",
                        color = Green700,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    message.recommendations.forEach { rec ->
                        Text(
                            text = "• $rec",
                            color = Gray600,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = formatTimestamp(message.timestamp),
            color = Gray500,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Gray200,
            modifier = Modifier.shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) { index ->
                    val delay = index * 200
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = delay),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Gray500.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message...", color = Gray500, fontSize = 14.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Green600,
                unfocusedBorderColor = Gray200,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = Green600
            ),
            shape = RoundedCornerShape(24.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        FloatingActionButton(
            onClick = onSend,
            modifier = Modifier.size(48.dp),
            containerColor = Green600,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
