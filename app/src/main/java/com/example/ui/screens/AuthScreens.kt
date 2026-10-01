package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.CmtScreen
import com.example.ui.CmtViewModel
import com.example.ui.components.CmtTopBar
import com.example.ui.theme.*

@Composable
fun SplashScreen(viewModel: CmtViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CmtNavyBg)
    ) {
        // Subtle Mobile-Accessories Visual Background
        Image(
            painter = painterResource(id = R.drawable.img_splash_bg),
            contentDescription = "Mobile Accessories Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.45f
        )

        // Gradient overlay for crisp readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CmtNavyBg.copy(alpha = 0.6f),
                            CmtNavyBg.copy(alpha = 0.88f),
                            CmtNavyBg
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Center Branding: CMT Logo, CMT, Chauhan Mobile Traders
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = CmtSurfaceDark,
                    border = BorderStroke(2.5.dp, CmtGoldPrimary),
                    shadowElevation = 16.dp,
                    modifier = Modifier.size(128.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_cmt_logo),
                        contentDescription = "CMT Official Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "CMT",
                    style = MaterialTheme.typography.displayLarge,
                    color = CmtGoldBright,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Chauhan Mobile Traders",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = CmtCyanContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, CmtCyanSecondary)
                ) {
                    Text(
                        text = "A-to-Z Mobile Accessories • Coin Wallet • Reseller Hub",
                        style = MaterialTheme.typography.labelMedium,
                        color = CmtCyanBright,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Subtle accessory category icons strip
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        Icons.Default.Bolt to "Fast Chargers",
                        Icons.Default.Headphones to "ANC Earbuds",
                        Icons.Default.BatteryChargingFull to "Power Banks",
                        Icons.Default.Watch to "Smart Watches"
                    ).forEach { (icon, label) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                color = CmtCardDark.copy(alpha = 0.85f),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, CmtBorderSubtle),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = CmtGoldBright,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = CmtTextMuted
                            )
                        }
                    }
                }
            }

            // Bottom Action Buttons: LOGIN and REGISTER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(CmtScreen.Login) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("splash_login_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CmtGoldPrimary,
                        contentColor = Color(0xFF0F172A)
                    )
                ) {
                    Icon(Icons.Default.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LOGIN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.navigateTo(CmtScreen.Register) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("splash_register_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, CmtCyanBright),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = CmtCyanBright)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REGISTER",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: CmtViewModel) {
    BackHandler { viewModel.navigateBack() }

    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showGoogleDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        CmtTopBar(
            title = "Login to CMT",
            subtitle = "Chauhan Mobile Traders",
            onBack = { viewModel.navigateBack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // CMT Logo Header
            Image(
                painter = painterResource(id = R.drawable.img_cmt_logo),
                contentDescription = "CMT Logo",
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, CmtGoldPrimary, RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Chauhan Mobile Traders",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Sign in with your CMT User ID or Username",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = identifier,
                        onValueChange = { identifier = it },
                        label = { Text("CMT User ID or Username (e.g., CMT100002)") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_identifier_input")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    Button(
                        onClick = { viewModel.login(identifier, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CmtGoldPrimary,
                            contentColor = Color(0xFF0F172A)
                        )
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("LOGIN TO CMT", fontWeight = FontWeight.ExtraBold)
                    }

                    OutlinedButton(
                        onClick = { showGoogleDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_login_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CmtCyanBright)
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = CmtCyanBright)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Continue with Google", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instant Role Account Switcher for seamless inspection of Member, Reseller & Owner flows
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CmtSurfaceDark),
                border = BorderStroke(1.dp, CmtBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = CmtGoldBright)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "One-Tap Role Login (Password: 123456)",
                            style = MaterialTheme.typography.titleSmall,
                            color = CmtGoldBright,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap any pre-configured CMT account below to log in immediately and test Customer, Reseller, or Owner/Admin Panel workflows:",
                        style = MaterialTheme.typography.bodySmall,
                        color = CmtTextMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { viewModel.quickSwitchAccount("CMT100002") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_login_member")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Member", fontWeight = FontWeight.Bold)
                                Text("CMT100002", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        FilledTonalButton(
                            onClick = { viewModel.quickSwitchAccount("CMT100001") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_login_reseller")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Reseller", fontWeight = FontWeight.Bold)
                                Text("CMT100001", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        FilledTonalButton(
                            onClick = { viewModel.quickSwitchAccount("CMT100000") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_login_owner")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Owner", fontWeight = FontWeight.Bold)
                                Text("CMT100000", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            TextButton(onClick = { viewModel.navigateTo(CmtScreen.Register) }) {
                Text(
                    text = "Don't have an account? Register for a new CMT ID",
                    color = CmtCyanBright,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (showGoogleDialog) {
        GoogleAccountSheetDialog(
            onDismiss = { showGoogleDialog = false },
            onContinue = { name, email, ref ->
                showGoogleDialog = false
                viewModel.continueWithGoogle(name, email, ref)
            }
        )
    }
}

@Composable
fun RegisterScreen(viewModel: CmtViewModel) {
    BackHandler { viewModel.navigateBack() }

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var referralLinkOrId by remember { mutableStateOf("") }
    var showGoogleDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        CmtTopBar(
            title = "Register CMT Account",
            subtitle = "Auto-creates CMT ID, Coin Wallet & Referral Link",
            onBack = { viewModel.navigateBack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.img_cmt_logo),
                    contentDescription = "CMT Logo",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.5.dp, CmtGoldPrimary, RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Join Chauhan Mobile Traders",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Receive your unique CMT ID (e.g. CMT100005)",
                        style = MaterialTheme.typography.bodySmall,
                        color = CmtGoldBright
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_fullname_input")
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_username_input")
                    )

                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("Mobile Number (e.g. 0300-1234567)") },
                        leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_mobile_input")
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_email_input")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_password_input")
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_confirm_password_input")
                    )

                    OutlinedTextField(
                        value = referralLinkOrId,
                        onValueChange = { referralLinkOrId = it },
                        label = { Text("Referral Link or CMT ID (Optional, e.g. CMT100001)") },
                        leadingIcon = { Icon(Icons.Default.CardGiftcard, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_referral_input")
                    )

                    Button(
                        onClick = {
                            viewModel.register(
                                fullName = fullName,
                                username = username,
                                mobile = mobile,
                                email = email,
                                password = password,
                                confirmPassword = confirmPassword,
                                referralCode = referralLinkOrId
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("reg_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CmtGoldPrimary,
                            contentColor = Color(0xFF0F172A)
                        )
                    ) {
                        Icon(Icons.Default.HowToReg, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CREATE CMT ACCOUNT", fontWeight = FontWeight.ExtraBold)
                    }

                    OutlinedButton(
                        onClick = { showGoogleDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("reg_google_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CmtCyanBright)
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = CmtCyanBright)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Continue with Google", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = { viewModel.navigateTo(CmtScreen.Login) }) {
                Text("Already registered? Login with your CMT ID", color = CmtCyanBright)
            }
        }
    }

    if (showGoogleDialog) {
        GoogleAccountSheetDialog(
            onDismiss = { showGoogleDialog = false },
            onContinue = { name, gEmail, ref ->
                showGoogleDialog = false
                viewModel.continueWithGoogle(name, gEmail, ref)
            }
        )
    }
}

@Composable
private fun GoogleAccountSheetDialog(
    onDismiss: () -> Unit,
    onContinue: (String, String, String?) -> Unit
) {
    var googleName by remember { mutableStateOf("Zain Chauhan") }
    var googleEmail by remember { mutableStateOf("zain.chauhan@gmail.com") }
    var referralId by remember { mutableStateOf("CMT100001") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = CmtCyanBright)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue with Google")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Select or enter your Google Account to register/sign in to Chauhan Mobile Traders (CMT):",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = googleName,
                    onValueChange = { googleName = it },
                    label = { Text("Google Account Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = googleEmail,
                    onValueChange = { googleEmail = it },
                    label = { Text("Google Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = referralId,
                    onValueChange = { referralId = it },
                    label = { Text("Referral Code (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onContinue(googleName, googleEmail, referralId.takeIf { it.isNotBlank() }) },
                colors = ButtonDefaults.buttonColors(containerColor = CmtGoldPrimary, contentColor = Color(0xFF0F172A))
            ) {
                Text("Sign In with Google", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
