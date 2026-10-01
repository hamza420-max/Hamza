package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.ProductEntity
import com.example.ui.CmtScreen
import com.example.ui.CmtViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(viewModel: CmtViewModel) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val wallet by viewModel.currentWallet.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val wishlist by viewModel.currentWishlist.collectAsStateWithLifecycle()
    val notifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val settings by viewModel.storeSettings.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedSort by viewModel.selectedSortFilter.collectAsStateWithLifecycle()

    val wishlistIds = remember(wishlist) { wishlist.map { it.productId }.toSet() }
    val unreadCount = remember(notifications, user) {
        val myId = user?.cmtId ?: ""
        notifications.count { !it.isRead && (it.targetCmtId == "ALL" || it.targetCmtId == myId || (user?.role == "OWNER" && it.targetCmtId == "OWNER")) }
    }

    val approvedProducts = remember(allProducts, searchQuery, selectedCategory, selectedSort) {
        val filtered = allProducts.filter { prod ->
            prod.status == "APPROVED" &&
                    (selectedCategory == "All" || prod.category.equals(selectedCategory, ignoreCase = true) || prod.subcategory.equals(selectedCategory, ignoreCase = true)) &&
                    (searchQuery.isBlank() ||
                            prod.name.contains(searchQuery, ignoreCase = true) ||
                            prod.category.contains(searchQuery, ignoreCase = true) ||
                            prod.subcategory.contains(searchQuery, ignoreCase = true) ||
                            prod.shopName.contains(searchQuery, ignoreCase = true) ||
                            prod.description.contains(searchQuery, ignoreCase = true))
        }
        when (selectedSort) {
            "Newest" -> filtered.sortedByDescending { it.createdAt }
            "PriceLow" -> filtered.sortedBy { it.coinPrice * (100 - it.discountPercent) / 100 }
            "Discount" -> filtered.sortedByDescending { it.discountPercent }
            "Rating" -> filtered.sortedByDescending { it.rating }
            else -> filtered.sortedWith(compareByDescending<ProductEntity> { it.isFeatured }.thenByDescending { it.reviewCount })
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Top Header with CMT Logo, Coin Balance Button, Notifications, and Profile/Me
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CmtLogoBadge(size = 42.dp, showSubtitle = true)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Prominent Coin Balance Button
                            CoinBalanceChip(
                                coins = wallet?.currentCoins ?: 0L,
                                onClick = { viewModel.navigateTo(CmtScreen.CoinWallet) }
                            )

                            // Notifications Button
                            IconButton(
                                onClick = { viewModel.navigateTo(CmtScreen.Notifications) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("home_notifications_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadCount > 0) {
                                            Badge(containerColor = CmtCoralError) {
                                                Text("$unreadCount", color = Color.White)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Profile / Me Button
                            IconButton(
                                onClick = { viewModel.navigateTo(CmtScreen.Me) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("home_profile_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = CmtCyanContainer,
                                    border = BorderStroke(1.dp, CmtCyanBright),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Me / Profile",
                                            tint = CmtCyanBright,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search mobile covers, fast chargers, shops, brands...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_bar")
                    )
                }
            }
        }

        // 2. Quick Wallet / Reseller / Owner Action Strip
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { viewModel.navigateTo(CmtScreen.Deposit) },
                    label = { Text("Deposit Coins") },
                    leadingIcon = {
                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = CmtEmeraldSuccess, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.testTag("home_deposit_chip")
                )
                AssistChip(
                    onClick = {
                        if (user?.role == "RESELLER" || user?.role == "SELLER" || user?.role == "OWNER") {
                            viewModel.navigateTo(CmtScreen.ResellerDashboard())
                        } else {
                            viewModel.navigateTo(CmtScreen.ResellerApply)
                        }
                    },
                    label = {
                        Text(if (user?.role == "RESELLER") "Reseller Hub" else "Reseller / Add Product")
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = CmtGoldBright, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.testTag("home_reseller_chip")
                )
                AssistChip(
                    onClick = { viewModel.navigateTo(CmtScreen.OwnerAdminPanel()) },
                    label = { Text("Owner Panel") },
                    leadingIcon = {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = CmtCyanBright, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.testTag("home_owner_chip")
                )
            }
        }

        // 3. Hero Banner & Announcement
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(175.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, CmtGoldPrimary.copy(alpha = 0.6f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "CMT Mobile Accessories Marketplace Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        CmtNavyBg.copy(alpha = 0.92f),
                                        CmtNavyBg.copy(alpha = 0.68f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.76f)
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Surface(
                                color = CmtGoldPrimary,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "CHAUHAN MOBILE TRADERS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "A-to-Z Mobile Accessories Marketplace",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = settings.announcementBanner,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE2E8F0),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.navigateTo(CmtScreen.ReferralHub) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CmtCyanSecondary,
                                    contentColor = Color(0xFF0F172A)
                                )
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Invite & Earn Points", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 4. Categories Strip
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mobile Accessory Categories (${categories.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { viewModel.navigateTo(CmtScreen.Categories) }) {
                    Text("View All", color = CmtGoldBright)
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == "All",
                        onClick = { viewModel.setSelectedCategory("All") },
                        label = { Text("All Accessories") },
                        leadingIcon = {
                            Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory.equals(cat.name, ignoreCase = true),
                        onClick = { viewModel.setSelectedCategory(cat.name) },
                        label = { Text(cat.name) },
                        leadingIcon = {
                            Icon(categoryIconFor(cat.iconKey), contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }
        }

        // 5. Sort & Filter Strip
        item {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val sorts = listOf(
                    "Popular" to "Popular",
                    "Newest" to "Newest",
                    "PriceLow" to "Lowest Coins",
                    "Discount" to "Top Discount",
                    "Rating" to "Highest Rated"
                )
                items(sorts) { (key, label) ->
                    InputChip(
                        selected = selectedSort == key,
                        onClick = { viewModel.setSelectedSortFilter(key) },
                        label = { Text(label) },
                        leadingIcon = if (selectedSort == key) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp)) }
                        } else null
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 6. Product Grid (2 columns per row)
        if (approvedProducts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = CmtGoldBright, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No matching mobile accessories found", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(onClick = {
                            viewModel.setSearchQuery("")
                            viewModel.setSelectedCategory("All")
                        }) {
                            Text("Reset Search & Category Filters")
                        }
                    }
                }
            }
        } else {
            val chunked = approvedProducts.chunked(2)
            items(chunked) { rowProducts ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (prod in rowProducts) {
                        CmtProductCard(
                            product = prod,
                            isWishlisted = wishlistIds.contains(prod.id),
                            onCardClick = { viewModel.navigateTo(CmtScreen.ProductDetail(prod.id)) },
                            onAddToCart = { viewModel.addToCart(prod, buyNow = false) },
                            onBuyNow = { viewModel.addToCart(prod, buyNow = true) },
                            onToggleWishlist = { viewModel.toggleWishlist(prod) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowProducts.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CategoriesScreen(viewModel: CmtViewModel) {
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val wishlist by viewModel.currentWishlist.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val wishlistIds = remember(wishlist) { wishlist.map { it.productId }.toSet() }

    val approvedProducts = remember(allProducts) { allProducts.filter { it.status == "APPROVED" } }
    val filteredProducts = remember(approvedProducts, selectedCategory) {
        if (selectedCategory == "All") approvedProducts
        else approvedProducts.filter {
            it.category.equals(selectedCategory, ignoreCase = true) ||
                    it.subcategory.equals(selectedCategory, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CmtTopBar(
            title = "CMT Accessory Categories",
            subtitle = "${categories.size} Categories & Subcategories"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Browse All Mobile Accessory Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Grid of Category Cards
                val catRows = (listOf(null) + categories).chunked(3)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    catRows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { cat ->
                                val name = cat?.name ?: "All"
                                val isSelected = selectedCategory.equals(name, ignoreCase = true)
                                val count = if (cat == null) approvedProducts.size else approvedProducts.count {
                                    it.category.equals(name, ignoreCase = true) || it.subcategory.equals(name, ignoreCase = true)
                                }
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setSelectedCategory(name) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) CmtGoldContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) CmtGoldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = categoryIconFor(cat?.iconKey ?: "apps"),
                                            contentDescription = name,
                                            tint = if (isSelected) CmtGoldBright else CmtCyanBright,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "$count items",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            repeat(3 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Products in '$selectedCategory' (${filteredProducts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CmtGoldBright
                )
            }

            items(filteredProducts.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (prod in pair) {
                        CmtProductCard(
                            product = prod,
                            isWishlisted = wishlistIds.contains(prod.id),
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

@Composable
fun ProductDetailScreen(productId: Long, viewModel: CmtViewModel) {
    BackHandler { viewModel.navigateBack() }
    val context = LocalContext.current

    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val wishlist by viewModel.currentWishlist.collectAsStateWithLifecycle()
    val reviews by viewModel.allReviews.collectAsStateWithLifecycle()

    val product = allProducts.find { it.id == productId } ?: allProducts.firstOrNull()
    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Product not found")
        }
        return
    }

    val isWishlisted = wishlist.any { it.productId == product.id }
    val productReviews = reviews.filter { it.productId == product.id && it.isApproved }
    val variationsList = remember(product.variations) {
        product.variations.split(",", "|").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { listOf("Standard") }
    }
    var selectedVariation by remember(variationsList) { mutableStateOf(variationsList.first()) }
    var quantity by remember { mutableIntStateOf(1) }
    var newRating by remember { mutableIntStateOf(5) }
    var newReviewComment by remember { mutableStateOf("") }

    val effectiveCoins = (product.coinPrice * (100 - product.discountPercent) / 100L).coerceAtLeast(1L)
    val shareLink = "https://cmtstore.com/product/${product.productCode}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        CmtTopBar(
            title = product.name,
            subtitle = "${product.category} • ${product.productCode}",
            onBack = { viewModel.navigateBack() },
            rightContent = {
                IconButton(onClick = { viewModel.toggleWishlist(product) }) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (isWishlisted) CmtCoralError else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = {
                    shareText(
                        context,
                        "Check out ${product.name} on CMT",
                        "${product.name} - Only ${formatNumber(effectiveCoins)} Coins at Chauhan Mobile Traders (CMT)! Link: $shareLink"
                    )
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Share Product")
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Product Hero Image
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ProductVisualBox(
                            imageKey = product.imageKey,
                            customImageUri = product.customImageUri,
                            category = product.category,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (product.discountPercent > 0) {
                            Surface(
                                color = CmtCoralError,
                                shape = RoundedCornerShape(bottomEnd = 12.dp),
                                modifier = Modifier.align(Alignment.TopStart)
                            ) {
                                Text(
                                    text = "SAVE ${product.discountPercent}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Title, Price, Coin Price, Stock & Rating
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(if (product.stock > 0) "IN STOCK: ${product.stock}" else "OUT OF STOCK")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = CmtGoldBright, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${product.rating} (${product.reviewCount} reviews)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CmtGoldBright
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = CmtGoldBright, modifier = Modifier.size(26.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${formatNumber(effectiveCoins)} Coins",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = CmtGoldBright,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            if (product.discountPercent > 0) {
                                Text(
                                    text = "${formatNumber(product.coinPrice)} Coins",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }
                        Text(
                            text = "Standard Retail Value: PKR ${formatNumber(product.pricePkr)} • Share Link: $shareLink",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Seller / Shop Card + Contact Seller + Public Shop Link
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CmtCyanContainer.copy(alpha = 0.45f)),
                    border = BorderStroke(1.dp, CmtCyanSecondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.navigateTo(CmtScreen.ShopProfile(product.sellerCmtId)) }
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = CmtCyanSecondary,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF0F172A))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = product.shopName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Seller ID: ${product.sellerCmtId} • Tap to Visit Shop",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CmtCyanBright
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.openChatWith(
                                    partnerCmtId = product.sellerCmtId,
                                    sharedProductId = product.id,
                                    sharedProductName = product.name
                                )
                            },
                            border = BorderStroke(1.dp, CmtGoldPrimary),
                            modifier = Modifier.testTag("contact_seller_button")
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = CmtGoldBright, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Contact Seller", color = CmtGoldBright)
                        }
                    }
                }
            }

            // Variations & Quantity Selector
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Select Variation", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(variationsList) { v ->
                                FilterChip(
                                    selected = selectedVariation == v,
                                    onClick = { selectedVariation = v },
                                    label = { Text(v) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Quantity", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (quantity > 1) quantity-- }) {
                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease quantity")
                                }
                                Text(
                                    text = "$quantity",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                IconButton(onClick = { if (quantity < product.stock) quantity++ }) {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase quantity")
                                }
                            }
                        }
                    }
                }
            }

            // Description, Specifications & Shipping
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
                        Text("Product Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CmtGoldBright)
                        Text(product.description, style = MaterialTheme.typography.bodyMedium)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text("Technical Specifications", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CmtCyanBright)
                        Text(product.specifications, style = MaterialTheme.typography.bodySmall)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text("Shipping & Delivery", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(product.shippingInfo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Reviews & Ratings Section
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
                        Text("Customer Reviews (${productReviews.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        // Submit Review Form
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Your Rating: ", style = MaterialTheme.typography.bodySmall)
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = { newRating = star },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "$star Stars",
                                        tint = if (star <= newRating) CmtGoldBright else CmtTextMuted
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newReviewComment,
                                onValueChange = { newReviewComment = it },
                                placeholder = { Text("Write your review for this accessory...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    viewModel.addProductReview(product.id, newRating, newReviewComment)
                                    newReviewComment = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CmtGoldPrimary, contentColor = Color(0xFF0F172A))
                            ) {
                                Text("Post", fontWeight = FontWeight.Bold)
                            }
                        }

                        productReviews.forEach { rev ->
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(rev.userName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                        Text("★".repeat(rev.rating), color = CmtGoldBright, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(rev.comment, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Sticky Action Bar: Add to Cart & Buy Now
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.addToCart(product, selectedVariation, quantity, buyNow = false) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("detail_add_to_cart"),
                    border = BorderStroke(1.5.dp, CmtCyanBright)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = CmtCyanBright)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to Cart", color = CmtCyanBright, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.addToCart(product, selectedVariation, quantity, buyNow = true) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("detail_buy_now"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CmtGoldPrimary,
                        contentColor = Color(0xFF0F172A)
                    )
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buy Now (${formatNumber(effectiveCoins * quantity)} Coins)", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun ShopProfileScreen(ownerCmtId: String, viewModel: CmtViewModel) {
    BackHandler { viewModel.navigateBack() }
    val shops by viewModel.allShops.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val wishlist by viewModel.currentWishlist.collectAsStateWithLifecycle()
    val wishlistIds = remember(wishlist) { wishlist.map { it.productId }.toSet() }

    val shop = shops.find { it.ownerCmtId == ownerCmtId }
    val shopProducts = allProducts.filter { it.sellerCmtId == ownerCmtId && it.status == "APPROVED" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        CmtTopBar(
            title = shop?.shopName ?: "Reseller Shop Profile",
            subtitle = "CMT Verified Partner ($ownerCmtId)",
            onBack = { viewModel.navigateBack() }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, CmtGoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = CmtGoldContainer,
                                border = BorderStroke(2.dp, CmtGoldPrimary),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = CmtGoldBright, modifier = Modifier.size(34.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = shop?.shopName ?: "CMT Partner Shop",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Location: ${shop?.location ?: "Pakistan"} • Rating: ★ ${shop?.rating ?: 4.9}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CmtGoldBright
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = shop?.shopDescription ?: "Official CMT Mobile Accessories Reseller Shop.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Contact: ${shop?.contactInfo ?: ownerCmtId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CmtCyanBright
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.openChatWith(ownerCmtId) },
                            colors = ButtonDefaults.buttonColors(containerColor = CmtCyanSecondary, contentColor = Color(0xFF0F172A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Message ${shop?.shopName ?: "Shop"}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Shop Products (${shopProducts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(shopProducts.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (prod in pair) {
                        CmtProductCard(
                            product = prod,
                            isWishlisted = wishlistIds.contains(prod.id),
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
