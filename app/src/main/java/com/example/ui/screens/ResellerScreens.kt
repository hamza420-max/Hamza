package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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

@Composable
fun ResellerApplyScreen(viewModel: CmtViewModel) {
    BackHandler { viewModel.navigateBack() }
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val settings by viewModel.storeSettings.collectAsStateWithLifecycle()
    val allApps by viewModel.allResellerApplications.collectAsStateWithLifecycle()

    val myApp = remember(allApps, user) {
        allApps.firstOrNull { it.applicantCmtId == user?.cmtId }
    }

    var shopName by remember { mutableStateOf("") }
    var shopDesc by remember { mutableStateOf("Wholesale & Retail Mobile Accessories Partner") }
    var businessCity by remember { mutableStateOf("Lahore") }
    var depositAmountInput by remember(settings) { mutableStateOf(settings.resellerSecurityDepositPkr.toString()) }
    var referenceId by remember { mutableStateOf("") }
    var proofUri by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            proofUri = uri.toString()
            viewModel.showMessage("Security deposit screenshot attached!")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        CmtTopBar(
            title = "Apply for CMT Reseller",
            subtitle = "Required Security Deposit: ${formatNumber(settings.resellerSecurityDepositPkr)} PKR",
            onBack = { viewModel.navigateBack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Existing Application Status Banner (if already submitted)
            if (myApp != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CmtGoldContainer),
                    border = BorderStroke(1.5.dp, CmtGoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Shop: ${myApp.proposedShopName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            StatusBadge(myApp.status)
                        }
                        Text(
                            text = "Security Deposit: ${formatNumber(myApp.securityDepositPkr)} PKR via ${myApp.paymentMethod} • Ref: ${myApp.referenceId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CmtGoldBright
                        )
                        Text(
                            text = "Admin Note: ${myApp.adminNotes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                        if (myApp.status == "PENDING_APPROVAL") {
                            Button(
                                onClick = { viewModel.navigateTo(CmtScreen.OwnerAdminPanel("Resellers")) },
                                colors = ButtonDefaults.buttonColors(containerColor = CmtCyanSecondary, contentColor = Color(0xFF0F172A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open Owner Panel to Verify & Approve Application", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Reseller Benefits & Security Deposit Rules Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, CmtCyanSecondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = CmtGoldBright)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CMT Official Reseller Benefits & Requirements",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text("• Unlock 'Add Product' to publish mobile accessories across 22+ categories", style = MaterialTheme.typography.bodySmall)
                    Text("• Dedicated Reseller Shop Profile page with custom Shop Logo & Banner", style = MaterialTheme.typography.bodySmall)
                    Text("• Direct 1-to-1 Chat with customers & full Order/Inventory Dashboard", style = MaterialTheme.typography.bodySmall)
                    Text("• Required Security Deposit: ${formatNumber(settings.resellerSecurityDepositPkr)} PKR (Recorded separately as Security Deposit Balance: ${formatNumber(settings.resellerSecurityDepositPkr)} Coins/PKR equivalent)", style = MaterialTheme.typography.bodySmall, color = CmtCyanBright, fontWeight = FontWeight.Bold)
                }
            }

            // Official EasyPaisa Payment Card (Hamza Akram — 0327-8704237)
            EasyPaisaPaymentCard(
                settings = settings,
                onCopied = { viewModel.showMessage(it) }
            )

            // Reseller Application Form
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Reseller Application & Security Deposit Form",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CmtGoldBright
                    )

                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("Proposed Shop Name (e.g., Hamza Mobile Accessories)") },
                        leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reseller_shop_name_input")
                    )

                    OutlinedTextField(
                        value = shopDesc,
                        onValueChange = { shopDesc = it },
                        label = { Text("Shop Description & Specialty Categories") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = businessCity,
                        onValueChange = { businessCity = it },
                        label = { Text("City / Market Location") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = depositAmountInput,
                        onValueChange = { depositAmountInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Security Deposit Amount (PKR)") },
                        leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reseller_deposit_amount_input")
                    )

                    OutlinedTextField(
                        value = referenceId,
                        onValueChange = { referenceId = it },
                        label = { Text("${settings.paymentMethodName} Transaction / Reference ID") },
                        leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reseller_reference_input")
                    )

                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, CmtCyanBright)
                    ) {
                        Icon(
                            imageVector = if (proofUri != null) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = if (proofUri != null) CmtEmeraldSuccess else CmtCyanBright
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (proofUri != null) "Payment Proof Screenshot Attached" else "Upload Security Deposit Proof Screenshot")
                    }

                    Button(
                        onClick = {
                            val dep = depositAmountInput.toLongOrNull() ?: settings.resellerSecurityDepositPkr
                            viewModel.submitResellerApplication(
                                shopName = shopName,
                                shopDesc = shopDesc,
                                city = businessCity,
                                depositPkr = dep,
                                referenceId = referenceId,
                                proofUri = proofUri
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_reseller_app_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CmtGoldPrimary,
                            contentColor = Color(0xFF0F172A)
                        )
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SUBMIT RESELLER APPLICATION", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
fun ResellerDashboardScreen(initialTab: String = "Dashboard", viewModel: CmtViewModel) {
    BackHandler { viewModel.navigateBack() }
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val wallet by viewModel.currentWallet.collectAsStateWithLifecycle()
    val allShops by viewModel.allShops.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()

    // Enforce Role Permission: Only RESELLER, SELLER, or OWNER can access Reseller Panel
    if (user?.role != "RESELLER" && user?.role != "SELLER" && user?.role != "OWNER") {
        ResellerApplyScreen(viewModel)
        return
    }

    val myId = user?.cmtId ?: "CMT100001"
    val myShop = allShops.find { it.ownerCmtId == myId }
    val myProducts = remember(allProducts, myId) { allProducts.filter { it.sellerCmtId == myId } }
    val myOrders = remember(allOrders, myId) { allOrders.filter { it.sellerCmtId == myId } }
    val myTransactions = remember(allTransactions, myId) { allTransactions.filter { it.userCmtId == myId } }

    var activeTab by remember(initialTab) { mutableStateOf(initialTab) }
    val resellerTabs = listOf(
        "Dashboard" to "Dashboard",
        "Products" to "Products",
        "AddProduct" to "Add Product",
        "Orders" to "Orders",
        "Customers" to "Customers",
        "Profile" to "Shop Profile"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        CmtTopBar(
            title = myShop?.shopName ?: "CMT Reseller Dashboard",
            subtitle = "Reseller ID: $myId • Security Deposit: ${formatNumber(wallet?.securityDepositPkr ?: 20000L)} PKR",
            onBack = { viewModel.navigateBack() }
        )

        // Reseller Navigation Strip: Dashboard | Products | Add Product | Orders | Customers | Chat | Wallet | Profile
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(resellerTabs) { (key, label) ->
                FilterChip(
                    selected = activeTab == key,
                    onClick = { activeTab = key },
                    label = { Text(label) }
                )
            }
            item {
                AssistChip(
                    onClick = { viewModel.navigateTo(CmtScreen.Chat) },
                    label = { Text("Chat") },
                    leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
            item {
                AssistChip(
                    onClick = { viewModel.navigateTo(CmtScreen.CoinWallet) },
                    label = { Text("Wallet") },
                    leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        HorizontalDivider()

        when (activeTab) {
            "Dashboard" -> {
                val approvedCount = myProducts.count { it.status == "APPROVED" }
                val pendingCount = myProducts.count { it.status == "PENDING_REVIEW" }
                val rejectedCount = myProducts.count { it.status == "REJECTED" }
                val totalSalesCoins = myOrders.sumOf { it.totalCoinsPaid }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Shop Banner Card
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.5.dp, CmtGoldPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = myShop?.shopName ?: "${user?.fullName}'s Shop",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = myShop?.location ?: "Pakistan",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CmtCyanBright
                                        )
                                    }
                                    StatusBadge("RESELLER ACTIVE")
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { activeTab = "AddProduct" },
                                        colors = ButtonDefaults.buttonColors(containerColor = CmtGoldPrimary, contentColor = Color(0xFF0F172A)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.AddBox, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Add Product", fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.navigateTo(CmtScreen.ShopProfile(myId)) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Public Shop Page")
                                    }
                                }
                            }
                        }
                    }

                    // Reseller KPI Grid
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                KpiTile("Total Products", "${myProducts.size}", CmtCyanBright, Modifier.weight(1f))
                                KpiTile("Approved", "$approvedCount", CmtEmeraldSuccess, Modifier.weight(1f))
                                KpiTile("Pending Review", "$pendingCount", CmtAmberWarning, Modifier.weight(1f))
                                KpiTile("Rejected", "$rejectedCount", CmtCoralError, Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                KpiTile("Orders", "${myOrders.size}", CmtGoldBright, Modifier.weight(1f))
                                KpiTile("Total Sales", "${formatNumber(totalSalesCoins)} Coins", CmtEmeraldSuccess, Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                KpiTile("Wallet Coins", "${formatNumber(wallet?.currentCoins ?: 0L)}", CmtGoldBright, Modifier.weight(1f))
                                KpiTile("Security Deposit", "${formatNumber(wallet?.securityDepositPkr ?: 20000L)} PKR", CmtCyanBright, Modifier.weight(1f))
                            }
                        }
                    }

                    // Recent Reseller Transactions
                    item {
                        Text("Recent Shop Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(myTransactions.take(6)) { tx ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                                    Text("${tx.transactionCode} • ${tx.type}", fontWeight = FontWeight.Bold, color = CmtGoldBright)
                                    Text(tx.notes, style = MaterialTheme.typography.bodySmall)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    StatusBadge(tx.status)
                                    Text("${formatNumber(tx.coins)} Coins", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            "Products" -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("My Products (${myProducts.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Button(
                                onClick = { activeTab = "AddProduct" },
                                colors = ButtonDefaults.buttonColors(containerColor = CmtGoldPrimary, contentColor = Color(0xFF0F172A))
                            ) {
                                Text("+ Add Product", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    items(myProducts) { prod ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${prod.category} (${prod.subcategory}) • Stock: ${prod.stock}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${formatNumber(prod.coinPrice)} Coins (PKR ${formatNumber(prod.pricePkr)}) • Discount: ${prod.discountPercent}%",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = CmtGoldBright
                                    )
                                }
                                StatusBadge(prod.status)
                            }
                        }
                    }
                }
            }

            "AddProduct" -> {
                var pName by remember { mutableStateOf("") }
                var pCategory by remember { mutableStateOf(categories.firstOrNull()?.name ?: "Fast Chargers") }
                var pSubcategory by remember { mutableStateOf("Premium Series") }
                var pPricePkr by remember { mutableStateOf("1800") }
                var pCoinPrice by remember { mutableStateOf("1800") }
                var pDiscount by remember { mutableStateOf("10") }
                var pStock by remember { mutableStateOf("35") }
                var pDesc by remember { mutableStateOf("") }
                var pSpecs by remember { mutableStateOf("Fast Charging | 6-Month CMT Warranty") }
                var pVariations by remember { mutableStateOf("Matte Black, Pearl White") }
                var pShipping by remember { mutableStateOf("Express Delivery (2-3 Days)") }
                var pImageKey by remember { mutableStateOf("fast_charger") }
                var customImgUri by remember { mutableStateOf<String?>(null) }

                val prodImagePicker = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) { uri ->
                    if (uri != null) {
                        customImgUri = uri.toString()
                        viewModel.showMessage("Product image selected!")
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Add Mobile Accessory Product",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CmtGoldBright
                    )
                    Text(
                        text = "Workflow: Submit Details → Pending Review → Admin Approval → Published on CMT Marketplace",
                        style = MaterialTheme.typography.bodySmall,
                        color = CmtCyanBright
                    )

                    OutlinedTextField(
                        value = pName,
                        onValueChange = { pName = it },
                        label = { Text("Product Name (e.g., 45W Super Fast Type-C Charger)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_product_name_input")
                    )

                    Text("Select Category:", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = pCategory == cat.name,
                                onClick = {
                                    pCategory = cat.name
                                    pImageKey = cat.iconKey
                                },
                                label = { Text(cat.name) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = pSubcategory,
                        onValueChange = { pSubcategory = it },
                        label = { Text("Subcategory") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pPricePkr,
                            onValueChange = {
                                val digits = it.filter { c -> c.isDigit() }
                                pPricePkr = digits
                                pCoinPrice = digits
                            },
                            label = { Text("Price (PKR)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = pCoinPrice,
                            onValueChange = { pCoinPrice = it.filter { c -> c.isDigit() } },
                            label = { Text("Coin Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pDiscount,
                            onValueChange = { pDiscount = it.filter { c -> c.isDigit() } },
                            label = { Text("Discount %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = pStock,
                            onValueChange = { pStock = it.filter { c -> c.isDigit() } },
                            label = { Text("Stock Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = pVariations,
                        onValueChange = { pVariations = it },
                        label = { Text("Variations (comma separated, e.g. Black, White)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pDesc,
                        onValueChange = { pDesc = it },
                        label = { Text("Product Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pSpecs,
                        onValueChange = { pSpecs = it },
                        label = { Text("Specifications") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pShipping,
                        onValueChange = { pShipping = it },
                        label = { Text("Shipping Information") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedButton(
                        onClick = {
                            prodImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (customImgUri != null) "Custom Product Photo Selected" else "Select Product Photo from Gallery (Optional)")
                    }

                    Button(
                        onClick = {
                            viewModel.submitNewProduct(
                                name = pName,
                                category = pCategory,
                                subcategory = pSubcategory,
                                pricePkr = pPricePkr.toLongOrNull() ?: 1000L,
                                coinPrice = pCoinPrice.toLongOrNull() ?: 1000L,
                                discountPercent = pDiscount.toIntOrNull() ?: 0,
                                stock = pStock.toIntOrNull() ?: 20,
                                description = pDesc,
                                specifications = pSpecs,
                                variations = pVariations,
                                shippingInfo = pShipping,
                                imageKey = pImageKey,
                                customImageUri = customImgUri,
                                onSuccessDone = { activeTab = "Products" }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_new_product_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CmtGoldPrimary,
                            contentColor = Color(0xFF0F172A)
                        )
                    ) {
                        Icon(Icons.Default.Publish, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SUBMIT PRODUCT FOR APPROVAL", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            "Orders" -> {
                val statuses = listOf("CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED", "COMPLETED", "CANCELLED")
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Reseller Orders (${myOrders.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    items(myOrders) { order ->
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
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(order.orderNumber, fontWeight = FontWeight.Bold, color = CmtGoldBright)
                                    StatusBadge(order.status)
                                }
                                Text(order.itemsSummary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("Customer: ${order.buyerName} (${order.buyerPhone}) — ${order.buyerAddress}, ${order.buyerCity}", style = MaterialTheme.typography.bodySmall)
                                Text("Order Value: ${formatNumber(order.totalCoinsPaid)} Coins", color = CmtEmeraldSuccess, fontWeight = FontWeight.Bold)

                                Text("Update Order Status:", style = MaterialTheme.typography.labelSmall)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(statuses) { st ->
                                        FilterChip(
                                            selected = order.status == st,
                                            onClick = { viewModel.updateOrderStatus(order, st) },
                                            label = { Text(st) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "Customers" -> {
                val uniqueBuyers = myOrders.distinctBy { it.buyerCmtId }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text("Shop Customers (${uniqueBuyers.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    items(uniqueBuyers) { o ->
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
                                Column {
                                    Text("${o.buyerName} (${o.buyerCmtId})", fontWeight = FontWeight.Bold)
                                    Text("${o.buyerPhone} • ${o.buyerCity}", style = MaterialTheme.typography.bodySmall)
                                }
                                OutlinedButton(onClick = { viewModel.openChatWith(o.buyerCmtId) }) {
                                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Message")
                                }
                            }
                        }
                    }
                }
            }

            "Profile" -> {
                var editShopName by remember(myShop) { mutableStateOf(myShop?.shopName ?: "${user?.fullName}'s Shop") }
                var editShopDesc by remember(myShop) { mutableStateOf(myShop?.shopDescription ?: "") }
                var editContact by remember(myShop) { mutableStateOf(myShop?.contactInfo ?: "${user?.mobile} | ${user?.email}") }
                var editLocation by remember(myShop) { mutableStateOf(myShop?.location ?: "Lahore") }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Edit Reseller Shop Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = editShopName,
                        onValueChange = { editShopName = it },
                        label = { Text("Shop Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editShopDesc,
                        onValueChange = { editShopDesc = it },
                        label = { Text("Shop Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editContact,
                        onValueChange = { editContact = it },
                        label = { Text("Contact Information") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editLocation,
                        onValueChange = { editLocation = it },
                        label = { Text("Shop Location") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            viewModel.updateShopProfile(editShopName, editShopDesc, editContact, editLocation)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CmtGoldPrimary, contentColor = Color(0xFF0F172A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Shop Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun KpiTile(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.ExtraBold)
        }
    }
}
