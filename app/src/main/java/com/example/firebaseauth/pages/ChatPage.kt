import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.firebaseauth.componentes.ChatActionParams
import com.example.firebaseauth.componentes.ChatWriter
import com.example.firebaseauth.componentes.MessageBox
import com.example.firebaseauth.componentes.SelectUsersDialog
import com.example.firebaseauth.model.ChatMessage
import com.example.firebaseauth.navigation.AppPages
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPage(navController: NavController, authViewModel: AuthViewModel, chatId: String) {
    val coroutineScope = rememberCoroutineScope()
    val showSelectUsersDialog = remember { mutableStateOf(false) }
    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.ChatViewModelFactory(authViewModel)
    )
    LaunchedEffect(Unit) {
        chatViewModel.listenForMessages(chatId)
    }

    val listState = rememberLazyListState()

    LaunchedEffect(chatViewModel.messages.value) {
        if (chatViewModel.messages.value.isNotEmpty()) {
            listState.animateScrollToItem(chatViewModel.messages.value.size - 1)
        }
    }

    fun groupMessagesByDateWithIndex(messages: List<ChatMessage>): Map<String, List<Pair<Int, ChatMessage>>> {
        return messages.withIndex()
            .groupBy { formatDate(it.value.message.timestamp) }
            .toSortedMap()
            .mapValues { entry -> entry.value.sortedBy { it.value.message.timestamp }.map { it.index to it.value } }
            .toList()
            .reversed()
            .toMap()
    }

    val groupedMessages = groupMessagesByDateWithIndex(chatViewModel.messages.value)

    if (showSelectUsersDialog.value) {
        SelectUsersDialog(
            onDismiss = { showSelectUsersDialog.value = false },
            onUserSelected = { newChatId ->
                showSelectUsersDialog.value = false
                if(newChatId.isNotEmpty()){
                    navController.navigate(AppPages.ChatPage.route.replace("{chatId}", newChatId)) {
                        // Remove a página atual da pilha de navegação
                        popUpTo(navController.currentBackStackEntry?.destination?.route ?: "") {
                            inclusive = true
                        }
                    }
                }
            },
            userNotAllowed = chatViewModel.chatInfo.value.userIds,
            isMultipleSelection = true,
            chatActionParams = ChatActionParams(
                create = chatViewModel.chatInfo.value.userIds.size<=2,
                existingChat = chatViewModel.chatInfo.value.chatId,
                currentUsersInChat = chatViewModel.chatInfo.value.userIds
            )
        )
    }

    val chatParticipants = chatViewModel.chatInfo.value.userIds
    val chatTitle = if (chatParticipants.size > 2) {
        "Grupo de Chat"
    } else {
        val userName = chatParticipants.firstOrNull()?.let { userId ->
            // Chama a função suspensa dentro de uma corrotina
            runBlocking { chatViewModel.getUserName(userId) }
        }
        userName ?: "Chat"
    }

    Box(
        modifier = Modifier
            .padding(top = 30.dp, bottom = 45.dp)
            .fillMaxSize()
    ) {
        Column {
            TopAppBar(
                title = { Text(text = chatTitle) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { showSelectUsersDialog.value = true }) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = "Adicionar participante")
                    }
                }
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
            ) {
                groupedMessages.forEach { (date, messagePairs) ->
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Text(
                                text = date,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                    itemsIndexed(messagePairs) { _, pair ->
                        val (originalIndex, chatMessage) = pair
                        MessageBox(
                            chatMessageData = chatMessage,
                            markAsRead = { chatViewModel.markMessageAsRead(originalIndex, chatId) },
                            checkMessageUpdate = { message ->
                                chatViewModel.checkMessageChanged(originalIndex, message)
                            }
                        )
                    }
                }
            }


        }
        ChatWriter(
            senderId = authViewModel.currentUser?.uid.orEmpty(),
            onSendMessage = { message ->
                coroutineScope.launch {
                    chatViewModel.sendMessage(chatId, message)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            startTyping = { chatViewModel.startTyping(chatId) },
            stopTyping = { chatViewModel.stopTyping(chatId) },
            usersTyping = chatViewModel.chatInfo.value.userTyping.filter { it != authViewModel.currentUser?.uid },
            getUserName = { userId -> chatViewModel.getUserName(userId) }
        )
    }
}

fun formatDate(messageDate: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    val today = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val yesterday = today - (24 * 60 * 60 * 1000)
    return when {
        messageDate >= today -> "Hoje"
        messageDate >= yesterday -> "Ontem"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(messageDate))
    }
}
