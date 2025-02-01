import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.firebaseauth.model.Chat
import com.example.firebaseauth.model.ChatMessage
import com.example.firebaseauth.model.Message
import com.example.firebaseauth.model.User
import com.example.firebaseauth.utils.addToCopyNoDuplicates
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatViewModel(private val authViewModel: AuthViewModel) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private var messagesListener: ListenerRegistration? = null
    private var chatInfoListener: ListenerRegistration? = null

    // Mensagens
    private val _messages = mutableStateOf<List<ChatMessage>>(emptyList())
    val messages: State<List<ChatMessage>> = _messages

    // Informações do Chat (usando o modelo de Chat do seu código)
    private val _chatInfo = mutableStateOf(Chat())
    val chatInfo: State<Chat> = _chatInfo


    fun listenForMessages(chatId: String) {
        // Remover listeners antigos
        messagesListener?.remove()
        chatInfoListener?.remove()

        // Escutando as informações do chat (quem está digitando)
        chatInfoListener = db.collection("chats").document(chatId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    val userIds = it.get("userIds") as? List<String> ?: emptyList()
                    val userTyping = it.get("userTyping") as? List<String> ?: emptyList()

                    _chatInfo.value = Chat(
                        chatId = chatId,
                        userIds = userIds,
                        messages = _messages.value,
                        userTyping = userTyping
                    )
                }
            }

        // Escutando as mensagens do chat
        messagesListener = db.collection("chats").document(chatId)
            .collection("messages")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    viewModelScope.launch {
                        _messages.value = it.documents
                            .mapNotNull { doc ->
                                val message = doc.toObject(Message::class.java)?.copy(messageId = doc.id)
                                val user = message?.senderId?.let { getUserInfo(it) }
                                val selfOwner = message?.senderId == authViewModel.currentUser?.uid
                                val usersInChat = chatInfo.value.userIds.size
                                if (message != null && user != null) {
                                    ChatMessage(message, user, selfOwner, usersInChat)
                                } else null
                            }
                            // Ordena as mensagens pela timestamp
                            .sortedBy { it.message.timestamp } // Ordena por timestamp
                    }
                }
            }


    }

    private suspend fun getUserInfo(userId: String): User? {
        val userDoc = db.collection("users").document(userId).get().await()
        return userDoc.toObject(User::class.java)
    }

    suspend fun sendMessage(chatId: String, messageText: String) {
        val senderId = authViewModel.currentUser?.uid ?: return
        val messageId = UUID.randomUUID().toString()
        val user = getUserInfo(senderId)
        val newMessage = Message(
            messageId = messageId,
            text = messageText,
            senderId = senderId,
            sent = false,
            timestamp = System.currentTimeMillis()
        )

        // Adiciona a mensagem à lista local para exibição imediata
        _messages.value += ChatMessage(newMessage, user ?: User(), true)

        // Tenta enviar a mensagem de forma assíncrona até que seja bem-sucedido
        viewModelScope.launch {
            var messageSent = false
            while (!messageSent) {
                try {
                    withContext(Dispatchers.IO) {
                        db.collection("chats").document(chatId)
                            .collection("messages").document(messageId)
                            .set(newMessage)
                            .await()

                        db.collection("chats").document(chatId)
                            .collection("messages").document(messageId)
                            .update("sent", true).await()
                    }
                    messageSent = true
                } catch (e: Exception) {
                    kotlinx.coroutines.delay(1000)
                }
            }
        }
    }

    fun markMessageAsRead(messageIndex: Int, chatId: String) {
        val userId = authViewModel.currentUser?.uid ?: return
        val message = _messages.value[messageIndex].message
        val newRead: List<String>

        try {
            newRead = message.read.addToCopyNoDuplicates(userId)
        }catch (_:IllegalArgumentException){
            return
        }

        viewModelScope.launch {
            try {
                db.collection("chats").document(chatId)
                    .collection("messages").document(message.messageId)
                    .update("read", newRead)
            } catch (_: Exception) {}
        }
    }

    fun startTyping(chatId: String) {
        val userId = authViewModel.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val chatDoc = db.collection("chats").document(chatId).get().await()
                val currentUserTypingList = chatDoc.get("userTyping") as? List<String> ?: emptyList()

                val updatedUserTyping = currentUserTypingList.addToCopyNoDuplicates(userId)

                db.collection("chats").document(chatId).update("userTyping", updatedUserTyping)
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }

    fun stopTyping(chatId: String) {
        val userId = authViewModel.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val chatDoc = db.collection("chats").document(chatId).get().await()
                val currentUserTypingList = chatDoc.get("userTyping") as? List<String> ?: emptyList()

                val updatedUserTyping = currentUserTypingList.filterNot { it == userId }

                db.collection("chats").document(chatId).update("userTyping", updatedUserTyping)
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }

    fun checkMessageChanged(index: Int, message: ChatMessage): ChatMessage? {
        val isDiff = !(_messages.value[index].message.messageId == message.message.messageId &&
                    _messages.value[index].message.text == message.message.text &&
                    _messages.value[index].message.senderId == message.message.senderId &&
                    _messages.value[index].message.sent == message.message.sent &&
                    _messages.value[index].message.read == message.message.read &&
                    _messages.value[index].message.timestamp == message.message.timestamp &&
                    _messages.value[index].user == message.user &&
                    _messages.value[index].selfOwner == message.selfOwner &&
                    _messages.value[index].usersInChat == message.usersInChat)
        if(!isDiff)
            return null
        return _messages.value[index]
    }

    suspend fun getUserName(userId: String): String{
        val user = getUserInfo(userId)
        return user?.name ?: "USER"
    }

    override fun onCleared() {
        super.onCleared()
        messagesListener?.remove()
        chatInfoListener?.remove()
    }

    class ChatViewModelFactory(
        private val authViewModel: AuthViewModel
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                return ChatViewModel(authViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
