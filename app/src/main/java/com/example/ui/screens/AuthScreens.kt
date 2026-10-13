package com.example.ui.screens

import android.app.Activity
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.R
import com.example.data.model.User
import com.example.ui.components.GoogleSignInButton
import com.example.ui.components.bounceClick
import com.example.ui.illustrations.AccessDeniedIllustration
import com.example.ui.illustrations.WaitingApprovalIllustration
import com.example.ui.illustrations.WelcomeDeskIllustration
import com.example.ui.theme.*
import com.example.util.ModerationUtils
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun WelcomeScreen(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isLoadingGoogle by remember { mutableStateOf(false) }

  val auth = Firebase.auth
  val credentialManager = remember { CredentialManager.create(context) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AcademicBgSecondary)
      .verticalScroll(rememberScrollState())
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    WelcomeDeskIllustration(modifier = Modifier.padding(bottom = 20.dp))

    Text(
      text = "Academia • Class 11A",
      style = MaterialTheme.typography.labelLarge,
      color = AcademicPrimary,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = "Welcome to Academia",
      style = MaterialTheme.typography.headlineMedium,
      color = AcademicTextPrimary,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "Urgency-ranked homework and live collaborative assignments for your class.",
      style = MaterialTheme.typography.bodyMedium,
      color = AcademicTextSecondary,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(horizontal = 16.dp)
    )

    Spacer(modifier = Modifier.height(32.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = AcademicBgPrimary),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Text(
          text = "Get Started",
          style = MaterialTheme.typography.titleMedium,
          color = AcademicTextPrimary,
          fontWeight = FontWeight.Bold
        )

        Text(
          text = "Sign in securely with your Google account to join your classroom roster.",
          style = MaterialTheme.typography.bodySmall,
          color = AcademicTextSecondary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Official High-Contrast Google Sign-In Button with iOS spring bounce
        GoogleSignInButton(
          onClick = {
            coroutineScope.launch {
              isLoadingGoogle = true
              errorMessage = null
              try {
                val webClientId = context.getString(R.string.default_web_client_id)
                val signInOption = GetSignInWithGoogleOption.Builder(webClientId)
                  .build()

                val request = GetCredentialRequest.Builder()
                  .addCredentialOption(signInOption)
                  .build()

                val result = credentialManager.getCredential(
                  request = request,
                  context = context as Activity
                )

                val credential = result.credential
                if (credential is CustomCredential &&
                  credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                  val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                  val firebaseCred = GoogleAuthProvider.getCredential(googleIdToken, null)
                  auth.signInWithCredential(firebaseCred).await()
                }
              } catch (e: Exception) {
                Log.w("WelcomeScreen", "Google Sign-In: ${e.message}")
                if (!e.message.isNullOrBlank() && !e.message!!.contains("CancellationException", ignoreCase = true)) {
                  errorMessage = e.localizedMessage ?: "Sign-in cancelled or unavailable. Please try again."
                }
              } finally {
                isLoadingGoogle = false
              }
            }
          },
          isLoading = isLoadingGoogle
        )

        if (errorMessage != null) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = AcademicErrorBg,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = errorMessage!!,
              color = AcademicError,
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.padding(10.dp),
              textAlign = TextAlign.Center
            )
          }
        }
      }
    }
  }
}

/**
 * Screen shown ONLY after Google Sign-in to prompt for the student's real name.
 * Prevents name-only entry without Google authentication.
 */
@Composable
fun EnterStudentNameScreen(
  googleUser: FirebaseUser?,
  onSubmitName: (String) -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  var nameInput by remember {
    mutableStateOf(googleUser?.displayName ?: "")
  }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AcademicBgSecondary)
      .verticalScroll(rememberScrollState())
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(68.dp)
        .clip(CircleShape)
        .background(AcademicPaleBlue),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.School,
        contentDescription = null,
        tint = AcademicPrimary,
        modifier = Modifier.size(36.dp)
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "Student Profile",
      style = MaterialTheme.typography.labelLarge,
      color = AcademicPrimary,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = "What is your name?",
      style = MaterialTheme.typography.headlineMedium,
      color = AcademicTextPrimary,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "Enter your full name as registered in Class 11A so the instructor can verify your request.",
      style = MaterialTheme.typography.bodyMedium,
      color = AcademicTextSecondary,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(horizontal = 16.dp)
    )

    Spacer(modifier = Modifier.height(28.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = AcademicBgPrimary),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Account indicator
        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = AcademicPaleBlue,
          shape = RoundedCornerShape(12.dp)
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AccountCircle,
              contentDescription = null,
              tint = AcademicPrimary
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Signed in as",
                style = MaterialTheme.typography.labelSmall,
                color = AcademicTextSecondary
              )
              Text(
                text = googleUser?.email ?: "Google Account",
                style = MaterialTheme.typography.titleSmall,
                color = AcademicTextPrimary,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        Text(
          text = "Full Student Name",
          style = MaterialTheme.typography.titleSmall,
          color = AcademicTextPrimary,
          fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
          value = nameInput,
          onValueChange = {
            nameInput = it
            errorMessage = null
          },
          placeholder = { Text("e.g. Alex Rivera") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("student_name_input"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AcademicPrimary,
            unfocusedBorderColor = AcademicBorder
          )
        )

        if (errorMessage != null) {
          Text(
            text = errorMessage!!,
            color = AcademicError,
            style = MaterialTheme.typography.bodySmall
          )
        }

        // Tactile iOS bounce join button
        Button(
          onClick = {
            val (valid, error) = ModerationUtils.validateName(nameInput)
            if (!valid) {
              errorMessage = error
            } else {
              onSubmitName(nameInput)
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .bounceClick {
              val (valid, error) = ModerationUtils.validateName(nameInput)
              if (!valid) {
                errorMessage = error
              } else {
                onSubmitName(nameInput)
              }
            }
            .testTag("join_class_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = AcademicPrimary,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(14.dp)
        ) {
          Text("Join Class", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        TextButton(
          onClick = onSignOut,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Switch Account", color = AcademicTextSecondary)
        }
      }
    }
  }
}

@Composable
fun WaitingApprovalScreen(
  user: User,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AcademicBgSecondary)
      .padding(28.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    WaitingApprovalIllustration(modifier = Modifier.padding(bottom = 24.dp))

    Text(
      text = "Waiting for Approval",
      style = MaterialTheme.typography.headlineSmall,
      color = AcademicTextPrimary,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
      text = "Hi ${user.name}! Your request to join the class has been submitted. The class teacher will review and approve your account shortly.",
      style = MaterialTheme.typography.bodyMedium,
      color = AcademicTextSecondary,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(horizontal = 16.dp)
    )

    Spacer(modifier = Modifier.height(20.dp))

    Surface(
      shape = RoundedCornerShape(16.dp),
      color = AcademicWarningBg,
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE0A3))
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        CircularProgressIndicator(
          modifier = Modifier.size(16.dp),
          color = AcademicWarning,
          strokeWidth = 2.dp
        )
        Text(
          text = "Real-time updates active. You'll enter automatically once approved.",
          color = AcademicWarning,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    Spacer(modifier = Modifier.height(32.dp))

    OutlinedButton(
      onClick = onSignOut,
      shape = RoundedCornerShape(12.dp)
    ) {
      Text("Sign Out", color = AcademicTextSecondary)
    }
  }
}

@Composable
fun AccessDeniedScreen(
  user: User,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AcademicBgSecondary)
      .padding(28.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    AccessDeniedIllustration(modifier = Modifier.padding(bottom = 24.dp))

    Text(
      text = "Access Restricted",
      style = MaterialTheme.typography.headlineSmall,
      color = AcademicError,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
      text = "Your request was declined or blocked by the class administrator. Please contact your instructor for access.",
      style = MaterialTheme.typography.bodyMedium,
      color = AcademicTextSecondary,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(horizontal = 16.dp)
    )

    Spacer(modifier = Modifier.height(32.dp))

    Button(
      onClick = onSignOut,
      colors = ButtonDefaults.buttonColors(containerColor = AcademicPrimaryDark),
      shape = RoundedCornerShape(12.dp)
    ) {
      Text("Return to Login")
    }
  }
}

@Composable
fun AuthLoadingScreen(
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(AcademicBgSecondary),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      CircularProgressIndicator(
        color = AcademicPrimary,
        strokeWidth = 3.dp
      )
      Text(
        text = "Loading Academia...",
        style = MaterialTheme.typography.bodyMedium,
        color = AcademicTextSecondary
      )
    }
  }
}
