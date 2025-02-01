import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.firebaseauth.componentes.ChatWriter
import com.example.firebaseauth.componentes.MessageBox
import com.example.firebaseauth.model.ChatMessage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ChatPage(navController: NavController, authViewModel: AuthViewModel, chatId: String) {
    val coroutineScope = rememberCoroutineScope()
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

    // Agrupa as mensagens por data, preservando o índice original.
    fun groupMessagesByDateWithIndex(messages: List<ChatMessage>): Map<String, List<Pair<Int, ChatMessage>>> {
        return messages.withIndex() // Converte para IndexedValue<ChatMessage>
            .groupBy { formatDate(it.value.message.timestamp) }
            .toSortedMap() // Ordena os grupos por data em ordem crescente
            .mapValues { entry ->
                // Ordena cada grupo por timestamp e mapeia para (índice original, ChatMessage)
                entry.value.sortedBy { it.value.message.timestamp }
                    .map { it.index to it.value }
            }
            .toList()
            .reversed() // Reverte a ordem dos grupos para que o mais recente apareça embaixo (ou conforme sua lógica)
            .toMap()
    }

    val groupedMessages = groupMessagesByDateWithIndex(chatViewModel.messages.value)

    Box(
        modifier = Modifier
            .padding(top = 30.dp, bottom = 45.dp)
            .fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            groupedMessages.forEach { (date, messagePairs) ->
                // Cabeçalho do grupo com a data
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
                // Para cada mensagem do grupo, usamos o índice original armazenado no Pair
                itemsIndexed(messagePairs) { _, pair ->
                    val (originalIndex, chatMessage) = pair
                    MessageBox(
                        chatMessageData = chatMessage,
                        markAsRead = { chatViewModel.markMessageAsRead(originalIndex, chatId) },
                        checkMessageUpdate = { message ->
                            chatViewModel.checkMessageChanged(originalIndex, message)
                        },
                    )
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
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
            startTyping = { chatViewModel.startTyping(chatId) },
            stopTyping = { chatViewModel.stopTyping(chatId) },
            usersTyping = chatViewModel.chatInfo.value.userTyping.filter { it != authViewModel.currentUser?.uid },
            getUserName = { userId -> chatViewModel.getUserName(userId) }
        )
    }
}

fun formatDate(messageDate: Long): String {
    val calendar = Calendar.getInstance()

    // Obtém a data de hoje e ajusta a hora para 00:00:00
    calendar.timeInMillis = System.currentTimeMillis()
    val today = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // Calcula o valor de "ontem"
    val yesterday = today - (24 * 60 * 60 * 1000)

    return when {
        messageDate >= today -> "Hoje"
        messageDate >= yesterday -> "Ontem"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(messageDate))
    }
}
