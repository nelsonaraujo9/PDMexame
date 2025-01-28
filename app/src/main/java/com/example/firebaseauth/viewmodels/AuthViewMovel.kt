import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var isAuthenticated by mutableStateOf(false)
        private set

    var currentUser by mutableStateOf<FirebaseUser?>(null)
        private set

    init {
        checkUserAuthStatus()
    }

    private fun checkUserAuthStatus() {

        auth.addAuthStateListener { authState ->
            val user = authState.currentUser

            if (user != currentUser) {
                currentUser = user
                isAuthenticated = user != null
            }
        }
    }

    fun logout() {
        auth.signOut()
        currentUser = null
        isAuthenticated = false
    }
}
