package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.CmtScreen
import com.example.ui.CmtViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun MeScreen(viewModel: CmtViewModel) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val wallet by viewModel.currentWallet.collectAsStateWithLifecycle()
    val referrals by viewModel.allReferrals.collectAsStateWithLifecycle()

    val myReferrals = remember(referrals, user) {
        val myId = user?.cmtId ?: ""
        referrals.filter { it.referrerCmtId == myId }
    }
    val isApprovedReseller = user?.role == "RESELLER" || user?.role == "SELLER" || user?.role == "OWNER"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CmtTopBar(
            title = "Me / My CMT Account",
            subtitle = "${user?.cmtId ?: "Guest"} • Role: ${user?.role ?: "MEMBER"}",
            rightContent = {
                CoinBalanceChip(
                    coins = wallet?.currentCoins ?: 0L,
                    onClick = { viewModel.navigateTo(CmtScreen.CoinWallet) }
                )
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. User Profile Identity Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.5.dp, CmtGoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_cmt_logo),
                                contentDescription = "Profile Avatar",
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, CmtGoldPrimary, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = user?.fullName ?: "CMT Member",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "CMT ID: ${user?.cmtId ?: "CMT100002"} • @${user?.username ?: "member"}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CmtGoldBright,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${user?.mobile ?: ""} • ${user?.email ?: ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    StatusBadge("ROLE: ${user?.role ?: "MEMBER"}")
                                    StatusBadge("STATUS: ${user?.accountStatus ?: "ACTIVE"}")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // Wallet & Referral Quick Summary Strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { viewModel.navigateTo(CmtScreen.CoinWallet) }
                            ) {
                                Text("Spendable Coins", style = MaterialTheme.typography.labelSmall, color = CmtTextMuted)
                                Text(
                                    text = formatNumber(wallet?.currentCoins ?: 0L),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CmtGoldBright,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { viewModel.navigateTo(CmtScreen.CoinWallet) }
                            ) {
                                Text("Security Deposit", style = MaterialTheme.typography.labelSmall, color = CmtTextMuted)
                                Text(
                                    text = "${formatNumber(wallet?.securityDepositPkr ?: 0L)} PKR",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CmtCyanBright,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { viewModel.navigateTo(CmtScreen.ReferralHub) }
                            ) {
                                Text("Referral Points", style = MaterialTheme.typography.labelSmall, color = CmtTextMuted)
                                Text(
                                    text = "${wallet?.referralPoints ?: 0} Pts (${myReferrals.size} Refs)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CmtEmeraldSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 2. Reseller & Add Product Highlight Card (Enforces Role Permission!)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CmtCyanContainer.copy(alpha = 0.55f)),
                    border = BorderStroke(1.dp, CmtCyanSecondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = CmtGoldBright)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "RESELLER & SELLER CENTER",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            StatusBadge(if (isApprovedReseller) "APPROVED RESELLER" else "MEMBER")
                        }

                        Text(
                            text = if (isApprovedReseller)
                                "Your Reseller Panel is active! Manage your shop profile, add products, process orders, and track earnings."
                            else
                                "Add Product is exclusively available to approved Resellers. Apply for Reseller with the 20,000 PKR security deposit via EasyPaisa to unlock your shop.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (isApprovedReseller) {
                                        viewModel.navigateTo(CmtScreen.ResellerDashboard("Dashboard"))
                                    } else {
                                        viewModel.navigateTo(CmtScreen.ResellerApply)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("me_reseller_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CmtGoldPrimary,
                                    contentColor = Color(0xFF0F172A)
                                )
                            ) {
                                Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isApprovedReseller) "Reseller Panel" else "Apply for Reseller",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.handleAddProductClick() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("me_add_product_button"),
                                border = BorderStroke(1.dp, CmtCyanBright)
                            ) {
                                Icon(Icons.Default.AddBox, contentDescription = null, tint = CmtCyanBright, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Product", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 3. Complete Me Menu Grid (Wallet, Deposit, Withdrawal, Orders, Wishlist, My Products, My Shop, Referrals, Notifications, Settings, Support, Owner Panel)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        MeMenuRow(
                            icon = Icons.Default.AccountBalanceWallet,
                            title = "Coin Wallet",
                            subtitle = "Balance: ${formatNumber(wallet?.currentCoins ?: 0L)} Coins • History & Audit",
                            tint = CmtGoldBright,
                            onClick = { viewModel.navigateTo(CmtScreen.CoinWallet) }
                        )
                        MeMenuRow(
                            icon = Icons.Default.AddCard,
                            title = "Deposit (EasyPaisa)",
                            subtitle = "Hamza Akram (0327-8704237) • 1 PKR = 1 Coin",
                            tint = CmtEmeraldSuccess,
                            onClick = { viewModel.navigateTo(CmtScreen.Deposit) }
                        )
                        MeMenuRow(
                            icon = Icons.Default.Payments,
                            title = "Withdrawal",
                            subtitle = "Request Coin withdrawal to EasyPaisa / Bank",
                            tint = CmtCyanBright,
                            onClick = { viewModel.navigateTo(CmtScreen.Withdrawal) }
                        )
                        MeMenuRow(
                            icon = Icons.Default.LocalShipping,
                            title = "Orders",
                            subtitle = "Track your purchases and delivery status",
                            tint = CmtGoldBright,
                            onClick = { viewModel.navigateTo(CmtScreen.Orders) }
                        )
                        MeMenuRow(
                            icon = Icons.Default.Favorite,
                            title = "Wishlist",
                            subtitle = "Saved mobile accessories",
                            tint = CmtCoralError,
                            onClick = { viewModel.navigateTo(CmtScreen.Wishlist) }
                        )
                        MeMenuRow(
                            icon = Icons.Default.Inventory2,
                            title = "My Products",
                            subtitle = if (isApprovedReseller) "Manage your published & pending products" else "Apply for Reseller to publish products",
                            tint = CmtCyanBright,
                            onClick = {
                                if (isApprovedReseller) viewModel.navigateTo(CmtScreen.ResellerDashboard("Products"))
                                else viewModel.navigateTo(CmtScreen.ResellerApply)
                            }
                        )
                        MeMenuRow(
                            icon = Icons.Default.Store,
                            title = "My Shop",
                            subtitle = "Public Reseller Shop Profile & Banner",
                            tint = CmtGoldBright,
                            onClick = {
                                if (isApprovedReseller) viewModel.navigateTo(CmtScreen.ShopProfile(user?.cmtId ?: "CMT100001"))
                                else viewModel.navigateTo(CmtScreen.ResellerApply)
                            }
                        )
                        MeMenuRow(
                            icon = Icons.Default.Share,
                            title = "Referral / Invite Friends & Rewards",
                            subtitle = "Link: ${user?.referralLink ?: ""} • ${wallet?.referralPoints ?: 0} Points",
                            tint = CmtEmeraldSuccess,
                            onClick = { viewModel.navigateTo(CmtScreen.ReferralHub) }
                        )
                        MeMenuRow(
                            icon = Icons.Default.NotificationsActive,
                            title = "Notifications",
                            subtitle = "Deposits, orders, approvals & announcements",
                            tint = CmtGoldBright,
                            onClick = { viewModel.navigateTo(CmtScreen.Notifications) }
                        )
                        MeMenuRow(
                            icon = Icons.Default.Settings,
                            title = "Settings & Help / Support",
                            subtitle = "Profile settings, dark/light theme & CMT Support Chat",
                            tint = CmtCyanBright,
                            onClick = { viewModel.navigateTo(CmtScreen.SettingsAndSupport) }
                        )
                        MeMenuRow(
                            icon = Icons.Default.AdminPanelSettings,
                            title = "Owner / Admin Panel",
                            subtitle = "Complete CMT Store, Users, Resellers, Wallet & Settings Control",
                            tint = CmtGoldBright,
                            onClick = { viewModel.navigateTo(CmtScreen.OwnerAdminPanel()) }
                        )
                    }
                }
            }

            // 4. Role Account Switcher & Logout
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Switch Active CMT Account (Test Member / Reseller / Owner):",
                            style = MaterialTheme.typography.labelLarge,
                            color = CmtGoldBright
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.quickSwitchAccount("CMT100002") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Member", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = { viewModel.quickSwitchAccount("CMT100001") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reseller", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = { viewModel.quickSwitchAccount("CMT100000") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Owner", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Button(
                            onClick = { viewModel.logout() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("logout_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CmtCoralError,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("LOGOUT (${user?.cmtId ?: ""})", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MeMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = tint.copy(alpha = 0.16f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ReferralHubScreen(viewModel: CmtViewModel) {
    BackHandler { viewModel.navigateBack() }
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val wallet by viewModel.currentWallet.collectAsStateWithLifecycle()
    val allReferrals by viewModel.allReferrals.collectAsStateWithLifecycle()
    val settings by viewModel.storeSettings.collectAsStateWithLifecycle()

    val myId = user?.cmtId ?: "CMT100001"
    val referralLink = user?.referralLink ?: "cmtstore.com/register?ref=$myId"
    val myReferrals = remember(allReferrals, myId) {
        allReferrals.filter { it.referrerCmtId == myId }
    }
    val successfulCount = myReferrals.count { it.status == "SUCCESSFUL" }
    val pendingCount = myReferrals.count { it.status == "PENDING" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        CmtTopBar(
            title = "Referral & Invite Friends",
            subtitle = "Earn Referral Points • Anti-Abuse Protected",
            onBack = { viewModel.navigateBack() }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Unique Referral Link Card with Copy Link, Share Link, Invite Friends
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.5.dp, CmtGoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = CmtGoldBright, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Your Unique CMT Referral Link",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Referrer CMT ID: $myId",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CmtCyanBright
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CmtBorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = referralLink,
                                style = MaterialTheme.typography.titleMedium,
                                color = CmtGoldBright,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(14.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    copyToClipboard(context, "CMT Referral Link", referralLink) {
                                        viewModel.showMessage(it)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("copy_referral_link_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = CmtGoldPrimary, contentColor = Color(0xFF0F172A))
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Link", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    shareText(
                                        context,
                                        "Join Chauhan Mobile Traders (CMT)",
                                        "Join Chauhan Mobile Traders (CMT) using my official invite link: https://$referralLink (Referrer ID: $myId)"
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("share_referral_link_btn"),
                                border = BorderStroke(1.dp, CmtCyanBright)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = CmtCyanBright, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Link", color = CmtCyanBright, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                shareText(
                                    context,
                                    "Invite Friends to CMT",
                                    "Shop A-to-Z Mobile Accessories on CMT! Register with my link: https://$referralLink"
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("invite_friends_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = CmtEmeraldSuccess, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.GroupAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("INVITE FRIENDS NOW", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            // 2. Referral Statistics Grid (Total, Successful, Pending, Referral Points)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReferralMetricCard("Total Referrals", "${myReferrals.size}", CmtCyanBright, Modifier.weight(1f))
                    ReferralMetricCard("Successful", "$successfulCount", CmtEmeraldSuccess, Modifier.weight(1f))
                    ReferralMetricCard("Pending", "$pendingCount", CmtAmberWarning, Modifier.weight(1f))
                    ReferralMetricCard("Referral Points", "${wallet?.referralPoints ?: 0}", CmtGoldBright, Modifier.weight(1f))
                }
            }

            // 3. Qualifying Condition & Configurable Milestones
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Referral Points & Milestones",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CmtGoldBright
                        )
                        Text(
                            text = "Qualifying Rule: 1 Qualifying Referral = ${settings.pointsPerQualifyingReferral} Referral Point(s) when the invited friend completes: '${settings.referralQualifyingCondition}'. Clicking a link alone does not grant points.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        listOf(
                            "1 Qualifying Referral" to "${settings.pointsPerQualifyingReferral} Point",
                            "5 Referrals Milestone" to "+${settings.milestone5Bonus} Points",
                            "10 Referrals Milestone" to "+${settings.milestone10Bonus} Points",
                            "20 Referrals Milestone" to "+${settings.milestone20Bonus} Points"
                        ).forEach { (milestone, reward) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(milestone, style = MaterialTheme.typography.bodySmall)
                                Text(reward, style = MaterialTheme.typography.labelLarge, color = CmtEmeraldSuccess, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 4. Referral History
            item {
                Text(
                    text = "Referral History (${myReferrals.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (myReferrals.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No referrals recorded yet. Copy and share your referral link above!",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                items(myReferrals) { ref ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${ref.newMemberName} (${ref.newMemberCmtId})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Condition: ${ref.qualifyingCondition}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatTimestamp(ref.registrationDate),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                StatusBadge(ref.status)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "+${ref.pointsAwarded} Pt(s)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CmtGoldBright,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferralMetricCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun NotificationsScreen(viewModel: CmtViewModel) {
    BackHandler { viewModel.navigateBack() }
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val notifications by viewModel.allNotifications.collectAsStateWithLifecycle()

    val visibleNotifications = remember(notifications, user) {
        val myId = user?.cmtId ?: ""
        notifications.filter {
            it.targetCmtId == "ALL" || it.targetCmtId == myId || (user?.role == "OWNER" && it.targetCmtId == "OWNER")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.markNotificationsRead()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        CmtTopBar(
            title = "CMT Notifications",
            subtitle = "${visibleNotifications.size} Notification(s)",
            onBack = { viewModel.navigateBack() }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(visibleNotifications) { n ->
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
                            Text(n.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CmtGoldBright)
                            StatusBadge(n.type)
                        }
                        Text(n.message, style = MaterialTheme.typography.bodyMedium)
                        Text(formatTimestamp(n.timestamp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsAndSupportScreen(viewModel: CmtViewModel) {
    BackHandler { viewModel.navigateBack() }
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val darkTheme by viewModel.darkThemeEnabled.collectAsStateWithLifecycle()

    var fullName by remember(user) { mutableStateOf(user?.fullName ?: "") }
    var mobile by remember(user) { mutableStateOf(user?.mobile ?: "") }
    var email by remember(user) { mutableStateOf(user?.email ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        CmtTopBar(
            title = "Settings & Help / Support",
            subtitle = "Account Preferences & Official CMT Support",
            onBack = { viewModel.navigateBack() }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Edit Profile Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it },
                            label = { Text("Mobile Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { viewModel.updateMyProfile(fullName, mobile, email) },
                            colors = ButtonDefaults.buttonColors(containerColor = CmtGoldPrimary, contentColor = Color(0xFF0F172A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Profile Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Luxury Dark Marketplace Theme", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Toggle between CMT Dark & Light mode", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = darkTheme,
                            onCheckedChange = { viewModel.toggleTheme() }
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CmtCyanContainer.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, CmtCyanSecondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("CMT Help & Live Support Desk", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Need assistance with an EasyPaisa deposit, Reseller application, or order? Message CMT Owner/Support directly.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Button(
                            onClick = { viewModel.openChatWith("CMT100000") },
                            colors = ButtonDefaults.buttonColors(containerColor = CmtCyanSecondary, contentColor = Color(0xFF0F172A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Direct Chat with CMT Support", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
