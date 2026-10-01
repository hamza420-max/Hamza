package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CartItemEntity
import com.example.data.ProductEntity
import com.example.ui.CmtScreen
import com.example.ui.CmtViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun CartScreen(viewModel: CmtViewModel) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val wallet by viewModel.currentWallet.collectAsStateWithLifecycle()
    val cartItems by viewModel.currentCart.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()

    val cartPairs: List<Pair<CartItemEntity, ProductEntity>> = remember(cartItems, allProducts) {
        cartItems.mapNotNull { item ->
            val p = allProducts.find { it.id == item.productId }
            if (p != null) item to p else null
        }
    }

    var buyerName by remember(user) { mutableStateOf(user?.fullName ?: "") }
    var buyerPhone by remember(user) { mutableStateOf(user?.mobile ?: "") }
    var buyerAddress by remember { mutableStateOf("Main Boulevard, Gulberg III") }
    var buyerCity by remember { mutableStateOf("Lahore") }
    var deliveryInfo by remember { mutableStateOf("CMT Express Tracked Courier (2-3 Days)") }
    var orderNotes by remember { mutableStateOf("") }

    val totalOriginalCoins = cartPairs.sumOf { (ci, p) -> p.coinPrice * ci.quantity }
    val totalPayableCoins = cartPairs.sumOf { (ci, p) ->
        val discounted = (p.coinPrice * (100 - p.discountPercent) / 100L).coerceAtLeast(1L)
        discounted * ci.quantity
    }
    val totalSavings = (totalOriginalCoins - totalPayableCoins).coerceAtLeast(0L)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CmtTopBar(
            title = "Shopping Cart & Checkout",
            subtitle = "${cartPairs.sumOf { it.first.quantity }} item(s) • Available: ${formatNumber(wallet?.currentCoins ?: 0L)} Coins",
            rightContent = {
                CoinBalanceChip(
                    coins = wallet?.currentCoins ?: 0L,
                    onClick = { viewModel.navigateTo(CmtScreen.Deposit) }
                )
            }
        )

        if (cartPairs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCartCheckout,
                        contentDescription = null,
                        tint = CmtGoldBright,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Your CMT Cart is Empty", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Explore 22+ categories of mobile accessories and pay seamlessly with CMT Coins.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.navigateTo(CmtScreen.Home) },
                        colors = ButtonDefaults.buttonColors(containerColor = CmtGoldPrimary, contentColor = Color(0xFF0F172A))
                    ) {
                        Text("Browse Accessories", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartPairs) { (cartItem, product) ->
                    val unitCoins = (product.coinPrice * (100 - product.discountPercent) / 100L).coerceAtLeast(1L)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductVisualBox(
                                imageKey = product.imageKey,
                                customImageUri = product.customImageUri,
                                category = product.category,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Variation: ${cartItem.selectedVariation} • Shop: ${product.shopName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${formatNumber(unitCoins * cartItem.quantity)} Coins (${formatNumber(unitCoins)} ea)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CmtGoldBright,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.updateCartQty(cartItem, cartItem.quantity - 1) }) {
                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease")
                                }
                                Text("${cartItem.quantity}", fontWeight = FontWeight.Bold)
                                IconButton(onClick = { viewModel.updateCartQty(cartItem, cartItem.quantity + 1) }) {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase")
                                }
                            }
                        }
                    }
                }

                // Checkout Delivery & Order Summary Card
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, CmtGoldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Checkout & Delivery Information",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = CmtGoldBright
                            )

                            OutlinedTextField(
                                value = buyerName,
                                onValueChange = { buyerName = it },
                                label = { Text("Customer Full Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = buyerPhone,
                                onValueChange = { buyerPhone = it },
                                label = { Text("Phone Number") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = buyerAddress,
                                onValueChange = { buyerAddress = it },
                                label = { Text("Street Address") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = buyerCity,
                                    onValueChange = { buyerCity = it },
                                    label = { Text("City") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = deliveryInfo,
                                    onValueChange = { deliveryInfo = it },
                                    label = { Text("Delivery Method") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            OutlinedTextField(
                                value = orderNotes,
                                onValueChange = { orderNotes = it },
                                label = { Text("Order Notes (Optional)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // Order Summary Breakdown
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal Coins:")
                                Text("${formatNumber(totalOriginalCoins)} Coins")
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Discount Savings:", color = CmtEmeraldSuccess)
                                Text("-${formatNumber(totalSavings)} Coins", color = CmtEmeraldSuccess, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Payable Coins:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${formatNumber(totalPayableCoins)} Coins",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = CmtGoldBright,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Your Wallet Balance:", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = "${formatNumber(wallet?.currentCoins ?: 0L)} Coins",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if ((wallet?.currentCoins ?: 0L) >= totalPayableCoins) CmtEmeraldSuccess else CmtCoralError,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if ((wallet?.currentCoins ?: 0L) < totalPayableCoins) {
                                OutlinedButton(
                                    onClick = { viewModel.navigateTo(CmtScreen.Deposit) },
                                    border = BorderStroke(1.dp, CmtGoldPrimary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = CmtGoldBright)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Insufficient Coins — Tap to Deposit via EasyPaisa", color = CmtGoldBright)
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.checkoutWithCoins(
                                        buyerName = buyerName,
                                        buyerPhone = buyerPhone,
                                        buyerAddress = buyerAddress,
                                        buyerCity = buyerCity,
                                        deliveryInfo = deliveryInfo,
                                        orderNotes = orderNotes,
                                        cartPairs = cartPairs
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("checkout_submit_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CmtGoldPrimary,
                                    contentColor = Color(0xFF0F172A)
                                )
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("PLACE ORDER (${formatNumber(totalPayableCoins)} COINS)", fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrdersScreen(viewModel: CmtViewModel) {
    BackHandler { viewModel.navigateBack() }
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()

    val myOrders = remember(allOrders, user) {
        val myId = user?.cmtId ?: ""
        allOrders.filter { it.buyerCmtId == myId || it.sellerCmtId == myId }
    }

    val lifecycleSteps = listOf("PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED", "COMPLETED")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        CmtTopBar(
            title = "My CMT Orders & Tracking",
            subtitle = "${myOrders.size} Order(s)",
            onBack = { viewModel.navigateBack() }
        )

        if (myOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No orders placed yet. Shop accessories from the Home page!")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(myOrders) { order ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                                    text = order.orderNumber,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CmtGoldBright,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                StatusBadge(order.status)
                            }

                            Text(
                                text = order.itemsSummary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Shop: ${order.shopName} (${order.sellerCmtId}) • Total Paid: ${formatNumber(order.totalCoinsPaid)} Coins",
                                style = MaterialTheme.typography.bodySmall,
                                color = CmtCyanBright
                            )
                            Text(
                                text = "Delivery: ${order.buyerName} (${order.buyerPhone}) — ${order.buyerAddress}, ${order.buyerCity}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Visual Order Step Tracker
                            val stepIdx = lifecycleSteps.indexOf(order.status.uppercase())
                            if (stepIdx >= 0) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(lifecycleSteps.size) { idx ->
                                        val stepName = lifecycleSteps[idx]
                                        val reached = idx <= stepIdx
                                        Surface(
                                            color = if (reached) CmtEmeraldSuccess.copy(alpha = 0.2f) else CmtCardDark,
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, if (reached) CmtEmeraldSuccess else CmtBorderSubtle)
                                        ) {
                                            Text(
                                                text = "${idx + 1}. $stepName",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (reached) CmtEmeraldSuccess else CmtTextMuted,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatTimestamp(order.createdAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedButton(
                                    onClick = {
                                        viewModel.sendChatMessage(
                                            receiverCmtId = order.sellerCmtId,
                                            text = "Hi, checking status for Order ${order.orderNumber}.",
                                            sharedOrderNo = order.orderNumber
                                        )
                                        viewModel.openChatWith(order.sellerCmtId)
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Chat About Order", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WishlistScreen(viewModel: CmtViewModel) {
    BackHandler { viewModel.navigateBack() }
    val wishlist by viewModel.currentWishlist.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()

    val savedProducts = remember(wishlist, allProducts) {
        val ids = wishlist.map { it.productId }.toSet()
        allProducts.filter { ids.contains(it.id) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        CmtTopBar(
            title = "Saved Wishlist",
            subtitle = "${savedProducts.size} Saved Accessories",
            onBack = { viewModel.navigateBack() }
        )

        if (savedProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your Wishlist is empty. Tap the heart icon on any accessory to save it!")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedProducts.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (prod in pair) {
                            CmtProductCard(
                                product = prod,
                                isWishlisted = true,
                                onCardClick = { viewModel.navigateTo(CmtScreen.ProductDetail(prod.id)) },
                                onAddToCart = { viewModel.addToCart(prod, buyNow = false) },
                                onBuyNow = { viewModel.addToCart(prod, buyNow = true) },
                                onToggleWishlist = { viewModel.toggleWishlist(prod) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatScreen(viewModel: CmtViewModel) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allMessages by viewModel.allChatMessages.collectAsStateWithLifecycle()
    val activePartnerId by viewModel.activeChatPartnerCmtId.collectAsStateWithLifecycle()

    val myId = user?.cmtId ?: "CMT100002"
    val chatContacts = remember(allUsers, myId) {
        allUsers.filter { it.cmtId != myId }
    }
    val activePartner = chatContacts.find { it.cmtId == activePartnerId } ?: chatContacts.firstOrNull()

    val conversationMessages = remember(allMessages, myId, activePartner) {
        val partnerId = activePartner?.cmtId ?: ""
        val convId = listOf(myId, partnerId).sorted().joinToString("_")
        allMessages.filter { it.conversationId == convId }
    }

    var messageInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CmtTopBar(
            title = "CMT Real-Time Chat",
            subtitle = "Chat with Sellers, Resellers, Customers & Support"
        )

        // Contact Selector Strip
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatContacts) { contact ->
                val isSelected = contact.cmtId == activePartner?.cmtId
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.openChatWith(contact.cmtId) },
                    label = {
                        Text("${contact.fullName} (${contact.role})")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when (contact.role) {
                                "OWNER" -> Icons.Default.SupportAgent
                                "RESELLER", "SELLER" -> Icons.Default.Storefront
                                else -> Icons.Default.Person
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }

        HorizontalDivider()

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(conversationMessages) { msg ->
                val isMe = msg.senderCmtId == myId
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    Surface(
                        color = if (isMe) CmtGoldContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isMe) CmtGoldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.widthIn(max = 310.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${msg.senderName} (${msg.senderRole})",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isMe) CmtGoldBright else CmtCyanBright,
                                fontWeight = FontWeight.Bold
                            )
                            if (!msg.sharedProductName.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = Color.Black.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable {
                                        msg.sharedProductId?.let { viewModel.navigateTo(CmtScreen.ProductDetail(it)) }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = CmtGoldBright, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Product: ${msg.sharedProductName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CmtGoldBright
                                        )
                                    }
                                }
                            }
                            if (!msg.sharedOrderNumber.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = Color.Black.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = CmtEmeraldSuccess, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Order Ref: ${msg.sharedOrderNumber}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CmtEmeraldSuccess
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.messageText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatTimestamp(msg.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Message Input Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = { Text("Message ${activePartner?.fullName ?: "Seller"}...") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input")
                )
                IconButton(
                    onClick = {
                        val target = activePartner?.cmtId ?: "CMT100001"
                        viewModel.sendChatMessage(target, messageInput)
                        messageInput = ""
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(CmtGoldPrimary, CircleShape)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = Color(0xFF0F172A)
                    )
                }
            }
        }
    }
}
