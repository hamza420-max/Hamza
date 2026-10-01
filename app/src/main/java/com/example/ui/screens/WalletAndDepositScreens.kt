package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CmtScreen
import com.example.ui.CmtViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlin.math.roundToLong

@Composable
fun CoinWalletScreen(viewModel: CmtViewModel) {
    BackHandler { viewModel.navigateBack() }
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val wallet by viewModel.currentWallet.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val settings by viewModel.storeSettings.collectAsStateWithLifecycle()

    val myTransactions = remember(allTransactions, user) {
        val myId = user?.cmtId ?: ""
        allTransactions.filter { it.userCmtId == myId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        CmtTopBar(
            title = "CMT Coin Wallet",
            subtitle = "Rate: 1 PKR = ${settings.pkrToCoinRate} Coin • ${user?.cmtId ?: ""}",
            onBack = { viewModel.navigateBack() }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Main Gold Wallet Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.5.dp, CmtGoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF0F172A),
                                        Color(0xFF451A03),
                                        Color(0xFF1E293B)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "CURRENT SPENDABLE BALANCE",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CmtGoldBright
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.MonetizationOn,
                                            contentDescription = null,
                                            tint = CmtGoldBright,
                                            modifier = Modifier.size(34.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${formatNumber(wallet?.currentCoins ?: 0L)} Coins",
                                            style = MaterialTheme.typography.displayMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                                StatusBadge(user?.role ?: "MEMBER")
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Security Deposit Balance (Clearly distinguished from normal wallet balance!)
                            Surface(
                                color = Color.Black.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CmtCyanSecondary.copy(alpha = 0.7f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Reseller Security Deposit Balance (Held Separately)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CmtCyanBright,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (settings.treatSecurityDepositAsSpendable)
                                                "Owner configured: Eligible for spend"
                                            else
                                                "Protected Reseller Security Deposit (Non-spendable)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CmtTextMuted
                                        )
                                    }
                                    Text(
                                        text = "${formatNumber(wallet?.securityDepositPkr ?: 0L)} PKR / Coins",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = CmtCyanBright,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 4 Wallet Metrics Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                WalletStatMiniCard(
                                    label = "Total Deposited",
                                    value = "${formatNumber(wallet?.totalDepositedPkr ?: 0L)} PKR",
                                    color = CmtEmeraldSuccess,
                                    modifier = Modifier.weight(1f)
                                )
                                WalletStatMiniCard(
                                    label = "Total Spent",
                                    value = "${formatNumber(wallet?.totalSpentCoins ?: 0L)} Coins",
                                    color = CmtGoldBright,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                WalletStatMiniCard(
                                    label = "Pending Deposits",
                                    value = "${formatNumber(wallet?.pendingDepositCoins ?: 0L)} Coins",
                                    color = CmtAmberWarning,
                                    modifier = Modifier.weight(1f)
                                )
                                WalletStatMiniCard(
                                    label = "Pending Withdrawals",
                                    value = "${formatNumber(wallet?.pendingWithdrawalCoins ?: 0L)} Coins",
                                    color = CmtCyanBright,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Deposit & Withdrawal Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.navigateTo(CmtScreen.Deposit) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("wallet_deposit_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CmtEmeraldSuccess,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Deposit Coins", fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.navigateTo(CmtScreen.Withdrawal) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("wallet_withdraw_button"),
                                    border = BorderStroke(1.5.dp, CmtGoldBright),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CmtGoldBright)
                                ) {
                                    Icon(Icons.Default.Payments, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Withdraw", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Transaction History Header
            item {
                Text(
                    text = "Transaction History (${myTransactions.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (myTransactions.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No wallet transactions yet. Tap 'Deposit Coins' to fund your CMT wallet via EasyPaisa.",
                            modifier = Modifier.padding(18.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(myTransactions) { tx ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${tx.transactionCode} • ${tx.type.replace("_", " ")}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CmtGoldBright
                                )
                                StatusBadge(tx.status)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Amount: PKR ${formatNumber(tx.amountPkr)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${if (tx.type == "PURCHASE" || tx.type == "WITHDRAWAL") "-" else "+"}${formatNumber(tx.coins)} Coins",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (tx.type == "PURCHASE" || tx.type == "WITHDRAWAL") CmtCoralError else CmtEmeraldSuccess,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            if (tx.referenceId.isNotBlank()) {
                                Text(
                                    text = "Method: ${tx.paymentMethod} • Ref ID: ${tx.referenceId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CmtCyanBright
                                )
                            }
                            if (tx.notes.isNotBlank()) {
                                Text(
                                    text = tx.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = formatTimestamp(tx.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletStatMiniCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Black.copy(alpha = 0.32f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = CmtTextMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DepositScreen(viewModel: CmtViewModel) {
    BackHandler { viewModel.navigateBack() }
    val settings by viewModel.storeSettings.collectAsStateWithLifecycle()

    var amountInput by remember { mutableStateOf("5000") }
    var referenceId by remember { mutableStateOf("") }
    var proofUri by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            proofUri = uri.toString()
            viewModel.showMessage("Payment screenshot attached!")
        }
    }

    val parsedPkr = amountInput.toLongOrNull() ?: 0L
    val expectedCoins = (parsedPkr * settings.pkrToCoinRate).roundToLong()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        CmtTopBar(
            title = "Deposit Coins (${settings.paymentMethodName})",
            subtitle = "Me → Wallet → Deposit",
            onBack = { viewModel.navigateBack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Official EasyPaisa Payment Card (Hamza Akram / 0327-8704237)
            EasyPaisaPaymentCard(
                settings = settings,
                onCopied = { viewModel.showMessage(it) }
            )

            // Quick Preset Amounts
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Select or Enter Deposit Amount (PKR)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(100L, 1000L, 5000L, 20000L).forEach { preset ->
                            FilterChip(
                                selected = parsedPkr == preset,
                                onClick = { amountInput = preset.toString() },
                                label = { Text("${formatNumber(preset)} PKR") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Deposit Amount (PKR)") },
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deposit_amount_input")
                    )

                    // Live Expected Coins Calculation Box
                    Surface(
                        color = CmtGoldContainer,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CmtGoldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Expected CMT Coins",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CmtGoldBright
                                )
                                Text(
                                    text = "Conversion Rate: 1 PKR = ${settings.pkrToCoinRate} Coin",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Text(
                                text = "${formatNumber(expectedCoins)} Coins",
                                style = MaterialTheme.typography.headlineMedium,
                                color = CmtGoldBright,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    OutlinedTextField(
                        value = referenceId,
                        onValueChange = { referenceId = it },
                        label = { Text("${settings.paymentMethodName} Transaction / Reference ID (e.g., EP-3849201)") },
                        leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deposit_reference_input")
                    )

                    // Screenshot Upload via Android Photo Picker
                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, CmtCyanBright)
                    ) {
                        Icon(
                            imageVector = if (proofUri != null) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                            contentDescription = "Upload Payment Screenshot",
                            tint = if (proofUri != null) CmtEmeraldSuccess else CmtCyanBright
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (proofUri != null) "Screenshot Attached (Tap to Change)" else "Upload Payment Screenshot / Proof (Optional)",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Verification Notice
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = CmtGoldBright)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Security Rule: Your deposit remains PENDING until verified by the CMT Owner/Admin. Once verified, +${formatNumber(expectedCoins)} Coins are automatically credited to your Coin Wallet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.submitDeposit(parsedPkr, referenceId, proofUri)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_deposit_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CmtEmeraldSuccess,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SUBMIT DEPOSIT (${formatNumber(parsedPkr)} PKR)", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
fun WithdrawalScreen(viewModel: CmtViewModel) {
    BackHandler { viewModel.navigateBack() }
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val wallet by viewModel.currentWallet.collectAsStateWithLifecycle()
    val settings by viewModel.storeSettings.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()

    var withdrawCoinsInput by remember { mutableStateOf("1000") }
    var paymentMethod by remember { mutableStateOf("EasyPaisa") }
    var accountName by remember(user) { mutableStateOf(user?.fullName ?: "") }
    var accountNumber by remember(user) { mutableStateOf(user?.mobile ?: "") }

    val myWithdrawals = remember(allTransactions, user) {
        val myId = user?.cmtId ?: ""
        allTransactions.filter { it.userCmtId == myId && it.type == "WITHDRAWAL" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        CmtTopBar(
            title = "Withdraw CMT Coins",
            subtitle = "Me → Wallet → Withdrawal",
            onBack = { viewModel.navigateBack() }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, CmtGoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Available Spendable Balance", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = "${formatNumber(wallet?.currentCoins ?: 0L)} Coins",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = CmtGoldBright,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            StatusBadge("Min: ${settings.minWithdrawalCoins} Coins")
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("EasyPaisa", "JazzCash", "Bank Transfer").forEach { method ->
                                FilterChip(
                                    selected = paymentMethod == method,
                                    onClick = { paymentMethod = method },
                                    label = { Text(method) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = withdrawCoinsInput,
                            onValueChange = { withdrawCoinsInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Withdrawal Amount (Coins)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("withdraw_amount_input")
                        )

                        OutlinedTextField(
                            value = accountName,
                            onValueChange = { accountName = it },
                            label = { Text("Recipient Account Title / Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = accountNumber,
                            onValueChange = { accountNumber = it },
                            label = { Text("Recipient Account / Mobile Number") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("withdraw_account_input")
                        )

                        Text(
                            text = "Note: Withdrawal requests start as PENDING and are reviewed by the Owner/Admin (Pending → Processing → Approved → Completed / Rejected).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                val coins = withdrawCoinsInput.toLongOrNull() ?: 0L
                                viewModel.submitWithdrawal(coins, paymentMethod, accountName, accountNumber)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_withdrawal_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CmtGoldPrimary,
                                contentColor = Color(0xFF0F172A)
                            )
                        ) {
                            Icon(Icons.Default.Output, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SUBMIT WITHDRAWAL REQUEST", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "My Withdrawal Requests (${myWithdrawals.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(myWithdrawals) { wd ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(wd.transactionCode, fontWeight = FontWeight.Bold, color = CmtGoldBright)
                            StatusBadge(wd.status)
                        }
                        Text("${formatNumber(wd.coins)} Coins (PKR ${formatNumber(wd.amountPkr)}) via ${wd.paymentMethod}")
                        Text("Account: ${wd.accountName} (${wd.accountNumber})", style = MaterialTheme.typography.bodySmall, color = CmtCyanBright)
                        Text(wd.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
