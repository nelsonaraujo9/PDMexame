package com.example.firebaseauth.pages

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.firebaseauth.navigation.AppPages
import com.example.firebaseauth.viewmodels.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext
import androidx.activity.result.contract.ActivityResultContracts
import com.example.firebaseauth.R
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

@Composable
fun LoginPage(navController: NavController) {
    val loginViewModel: LoginViewModel = viewModel()

    // Só executa quando o loginSuccess muda de valor
    LaunchedEffect(loginViewModel.loginSuccess) {
        if (loginViewModel.loginSuccess) {
            navController.navigate(AppPages.HomePage.route) {
                popUpTo(AppPages.LoginPage.route) { inclusive = true }
            }
        }
    }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    // Configuração do Google Sign-In
    val googleSignInClient = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.web_client_id))
            .requestEmail()
            .build()
    )

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

                task.addOnCompleteListener { taskResult ->
                    if (taskResult.isSuccessful) {
                        val account = taskResult.result
                        account?.let { signInAccount: GoogleSignInAccount ->
                            val idToken = signInAccount.idToken
                            Log.d("LoginPage", "ID Token: $idToken")  // Log do ID Token

                            if (idToken != null) {
                                loginViewModel.loginWithGoogle(idToken) { success ->
                                    if (success) {
                                        navController.navigate(AppPages.HomePage.route) {
                                            popUpTo(AppPages.LoginPage.route) { inclusive = true }
                                        }
                                    } else {
                                        Log.w("LoginPage", "Google login failed after Firebase authentication")
                                    }
                                }
                            } else {
                                Log.w("LoginPage", "Google Sign-In ID Token is null")
                            }
                        }
                    } else {
                        Log.e("LoginPage", "Google login failed: ${taskResult.exception?.message}") // Log detalhado do erro
                        taskResult.exception?.printStackTrace() // Imprime o stack trace para depuração
                    }
                }
            } else {
                Log.w("LoginPage", "Google login failed 2: ${result.resultCode}")  // Log para capturar o código do resultado
            }
        }
    )





    // Função para iniciar o processo de login com Google
    fun handleGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = loginViewModel.email,
            onValueChange = { loginViewModel.email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = loginViewModel.password,
            onValueChange = { loginViewModel.password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )
        Button(
            onClick = { loginViewModel.login() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        if (loginViewModel.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
        if (loginViewModel.errorMessage != null) {
            Text(
                text = loginViewModel.errorMessage ?: "",
                color = androidx.compose.ui.graphics.Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        Button(
            onClick = { navController.navigate(AppPages.SignupPage.route) },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Sign Up")
        }

        // Botão para login com Google
        Button(
            onClick = { handleGoogleSignIn() },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Login with Google")
        }
    }
}
