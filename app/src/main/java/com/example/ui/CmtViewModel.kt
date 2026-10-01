package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class CmtScreen {
    data object Splash : CmtScreen()
    data object Login : CmtScreen()
    data object Register : CmtScreen()
    data object Home : CmtScreen()
    data object Categories : CmtScreen()
    data object Chat : CmtScreen()
    data object Cart : CmtScreen()
    data object Me : CmtScreen()
    data class ProductDetail(val productId: Long) : CmtScreen()
    data class ShopProfile(val ownerCmtId: String) : CmtScreen()
    data object CoinWallet : CmtScreen()
    data object Deposit : CmtScreen()
    data object Withdrawal : CmtScreen()
    data object Orders : CmtScreen()
    data object Wishlist : CmtScreen()
    data object ReferralHub : CmtScreen()
    data object ResellerApply : CmtScreen()
    data class ResellerDashboard(val initialTab: String = "Dashboard") : CmtScreen()
    data class OwnerAdminPanel(val initialTab: String = "Dashboard") : CmtScreen()
    data object Notifications : CmtScreen()
    data object SettingsAndSupport : CmtScreen()
}

@OptIn(ExperimentalCoroutinesApi::class)
class CmtViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("cmt_auth_prefs", Context.MODE_PRIVATE)
    private val db = CmtDatabase.getDatabase(application)
    val repository = CmtRepository(db.cmtDao())

    private val _currentUserCmtId = MutableStateFlow<String?>(prefs.getString("logged_in_cmt_id", null))
    val currentUserCmtId: StateFlow<String?> = _currentUserCmtId.asStateFlow()

    private val _screenStack = MutableStateFlow<List<CmtScreen>>(
        listOf(if (_currentUserCmtId.value != null) CmtScreen.Home else CmtScreen.Splash)
    )
    val currentScreen: StateFlow<CmtScreen> = _screenStack
        .map { it.lastOrNull() ?: CmtScreen.Splash }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _screenStack.value.last())

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedSortFilter = MutableStateFlow("Popular") // Popular, Newest, PriceLow, Discount, Rating
    val selectedSortFilter: StateFlow<String> = _selectedSortFilter.asStateFlow()

    private val _activeChatPartnerCmtId = MutableStateFlow("CMT100001")
    val activeChatPartnerCmtId: StateFlow<String> = _activeChatPartnerCmtId.asStateFlow()

    private val _darkThemeEnabled = MutableStateFlow(true)
    val darkThemeEnabled: StateFlow<Boolean> = _darkThemeEnabled.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDatabase()
        }
    }

    // Reactive Flows
    val currentUser: StateFlow<UserEntity?> = _currentUserCmtId
        .flatMapLatest { id -> if (id != null) repository.observeUser(id) else flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentWallet: StateFlow<WalletEntity?> = _currentUserCmtId
        .flatMapLatest { id -> if (id != null) repository.observeWallet(id) else flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentCart: StateFlow<List<CartItemEntity>> = _currentUserCmtId
        .flatMapLatest { id -> if (id != null) repository.observeCart(id) else flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentWishlist: StateFlow<List<WishlistEntity>> = _currentUserCmtId
        .flatMapLatest { id -> if (id != null) repository.observeWishlist(id) else flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWallets: StateFlow<List<WalletEntity>> = repository.allWallets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allShops: StateFlow<List<ShopEntity>> = repository.allShops
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<WalletTransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allResellerApplications: StateFlow<List<ResellerApplicationEntity>> = repository.allResellerApplications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReferrals: StateFlow<List<ReferralEntity>> = repository.allReferrals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReviews: StateFlow<List<ReviewEntity>> = repository.allReviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChatMessages: StateFlow<List<ChatMessageEntity>> = repository.allChatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val storeSettings: StateFlow<StoreSettingsEntity> = repository.storeSettings
        .map { it ?: StoreSettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StoreSettingsEntity())

    val allPromotions: StateFlow<List<PromotionEntity>> = repository.allPromotions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation helpers
    fun navigateTo(screen: CmtScreen, clearStack: Boolean = false) {
        if (clearStack) {
            _screenStack.value = listOf(screen)
        } else {
            val current = _screenStack.value
            if (current.lastOrNull() != screen) {
                _screenStack.value = current + screen
            }
        }
    }

    fun navigateBack(): Boolean {
        val current = _screenStack.value
        return if (current.size > 1) {
            _screenStack.value = current.dropLast(1)
            true
        } else if (current.lastOrNull() !is CmtScreen.Home && _currentUserCmtId.value != null) {
            _screenStack.value = listOf(CmtScreen.Home)
            true
        } else {
            false
        }
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearMessage() {
        _snackbarMessage.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun setSelectedSortFilter(filter: String) {
        _selectedSortFilter.value = filter
    }

    fun toggleTheme() {
        _darkThemeEnabled.value = !_darkThemeEnabled.value
    }

    fun openChatWith(partnerCmtId: String, sharedProductId: Long? = null, sharedProductName: String? = null) {
        val myId = _currentUserCmtId.value ?: return
        val target = if (partnerCmtId == myId) "CMT100000" else partnerCmtId
        _activeChatPartnerCmtId.value = target
        if (sharedProductId != null && sharedProductName != null) {
            viewModelScope.launch {
                repository.sendChatMessage(
                    senderCmtId = myId,
                    receiverCmtId = target,
                    messageText = "Hi, I'm interested in '$sharedProductName'. Is it available?",
                    sharedProductId = sharedProductId,
                    sharedProductName = sharedProductName
                )
            }
        }
        navigateTo(CmtScreen.Chat)
    }

    // Auth actions
    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            val res = repository.login(identifier, password)
            res.onSuccess { user ->
                prefs.edit().putString("logged_in_cmt_id", user.cmtId).apply()
                _currentUserCmtId.value = user.cmtId
                showMessage("Welcome back, ${user.fullName} (${user.cmtId})!")
                navigateTo(CmtScreen.Home, clearStack = true)
            }.onFailure { err ->
                showMessage(err.message ?: "Login failed.")
            }
        }
    }

    fun quickSwitchAccount(cmtId: String) {
        prefs.edit().putString("logged_in_cmt_id", cmtId).apply()
        _currentUserCmtId.value = cmtId
        showMessage("Switched active account to $cmtId")
        navigateTo(CmtScreen.Home, clearStack = true)
    }

    fun register(
        fullName: String,
        username: String,
        mobile: String,
        email: String,
        password: String,
        confirmPassword: String,
        referralCode: String
    ) {
        if (password.length < 4) {
            showMessage("Password must be at least 4 characters.")
            return
        }
        if (password != confirmPassword) {
            showMessage("Password and Confirm Password do not match.")
            return
        }
        viewModelScope.launch {
            val res = repository.registerAccount(
                fullName = fullName,
                username = username,
                mobile = mobile,
                email = email,
                password = password,
                referralCodeInput = referralCode
            )
            res.onSuccess { user ->
                prefs.edit().putString("logged_in_cmt_id", user.cmtId).apply()
                _currentUserCmtId.value = user.cmtId
                showMessage("Account Created! Your unique CMT ID is ${user.cmtId}")
                navigateTo(CmtScreen.Home, clearStack = true)
            }.onFailure { err ->
                showMessage(err.message ?: "Registration failed.")
            }
        }
    }

    fun continueWithGoogle(googleName: String, googleEmail: String, referralCode: String? = null) {
        viewModelScope.launch {
            val res = repository.continueWithGoogle(googleName, googleEmail, referralCode)
            res.onSuccess { user ->
                prefs.edit().putString("logged_in_cmt_id", user.cmtId).apply()
                _currentUserCmtId.value = user.cmtId
                showMessage("Signed in with Google as ${user.fullName} (${user.cmtId})")
                navigateTo(CmtScreen.Home, clearStack = true)
            }.onFailure { err ->
                showMessage(err.message ?: "Google Sign-In failed.")
            }
        }
    }

    fun logout() {
        prefs.edit().remove("logged_in_cmt_id").apply()
        _currentUserCmtId.value = null
        showMessage("Logged out securely.")
        navigateTo(CmtScreen.Splash, clearStack = true)
    }

    // Cart & Wishlist actions
    fun addToCart(product: ProductEntity, variation: String = "Standard", qty: Int = 1, buyNow: Boolean = false) {
        val cmtId = _currentUserCmtId.value ?: return
        viewModelScope.launch {
            val chosenVar = variation.ifBlank {
                product.variations.split(",", "|").firstOrNull()?.trim() ?: "Standard"
            }
            repository.addToCart(cmtId, product.id, chosenVar, qty)
            if (buyNow) {
                navigateTo(CmtScreen.Cart)
            } else {
                showMessage("Added '${product.name}' to Cart!")
            }
        }
    }

    fun updateCartQty(item: CartItemEntity, newQty: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(item, newQty)
        }
    }

    fun toggleWishlist(product: ProductEntity) {
        val cmtId = _currentUserCmtId.value ?: return
        viewModelScope.launch {
            val added = repository.toggleWishlist(cmtId, product.id)
            showMessage(if (added) "Saved '${product.name}' to Wishlist" else "Removed from Wishlist")
        }
    }

    // Checkout
    fun checkoutWithCoins(
        buyerName: String,
        buyerPhone: String,
        buyerAddress: String,
        buyerCity: String,
        deliveryInfo: String,
        orderNotes: String,
        cartPairs: List<Pair<CartItemEntity, ProductEntity>>
    ) {
        val cmtId = _currentUserCmtId.value ?: return
        viewModelScope.launch {
            val res = repository.placeOrderWithCoins(
                buyerCmtId = cmtId,
                buyerName = buyerName,
                buyerPhone = buyerPhone,
                buyerAddress = buyerAddress,
                buyerCity = buyerCity,
                deliveryInfo = deliveryInfo,
                orderNotes = orderNotes,
                cartProducts = cartPairs
            )
            res.onSuccess { order ->
                showMessage("Order Placed! Order ID: ${order.orderNumber} (${order.totalCoinsPaid} Coins)")
                navigateTo(CmtScreen.Orders)
            }.onFailure { err ->
                showMessage(err.message ?: "Checkout failed.")
            }
        }
    }

    // Wallet Deposit & Withdrawal
    fun submitDeposit(amountPkr: Long, referenceId: String, proofUri: String?) {
        val cmtId = _currentUserCmtId.value ?: return
        viewModelScope.launch {
            val res = repository.submitDepositRequest(cmtId, amountPkr, referenceId, proofUri)
            res.onSuccess { tx ->
                showMessage("Deposit ${tx.transactionCode} (${tx.coins} Coins) submitted! Status: PENDING Admin verification.")
                navigateTo(CmtScreen.CoinWallet)
            }.onFailure { err ->
                showMessage(err.message ?: "Deposit submission failed.")
            }
        }
    }

    fun submitWithdrawal(coins: Long, method: String, accountName: String, accountNumber: String) {
        val cmtId = _currentUserCmtId.value ?: return
        viewModelScope.launch {
            val res = repository.submitWithdrawalRequest(cmtId, coins, method, accountName, accountNumber)
            res.onSuccess { tx ->
                showMessage("Withdrawal ${tx.transactionCode} ($coins Coins) requested! Status: PENDING Admin review.")
                navigateTo(CmtScreen.CoinWallet)
            }.onFailure { err ->
                showMessage(err.message ?: "Withdrawal failed.")
            }
        }
    }

    // Reseller Application & Add Product
    fun handleAddProductClick() {
        val user = currentUser.value ?: return
        if (user.role == "RESELLER" || user.role == "SELLER" || user.role == "OWNER") {
            navigateTo(CmtScreen.ResellerDashboard(initialTab = "AddProduct"))
        } else {
            showMessage("Approved Reseller status required to publish products. Apply for Reseller below!")
            navigateTo(CmtScreen.ResellerApply)
        }
    }

    fun submitResellerApplication(
        shopName: String,
        shopDesc: String,
        city: String,
        depositPkr: Long,
        referenceId: String,
        proofUri: String?
    ) {
        val cmtId = _currentUserCmtId.value ?: return
        viewModelScope.launch {
            val res = repository.submitResellerApplication(
                userCmtId = cmtId,
                proposedShopName = shopName,
                shopDescription = shopDesc,
                businessCity = city,
                depositAmountPkr = depositPkr,
                referenceId = referenceId,
                proofImageUri = proofUri
            )
            res.onSuccess {
                showMessage("Reseller Application & $depositPkr PKR Security Deposit submitted! Status: Pending Admin Approval.")
            }.onFailure { err ->
                showMessage(err.message ?: "Application failed.")
            }
        }
    }

    fun submitNewProduct(
        name: String,
        category: String,
        subcategory: String,
        pricePkr: Long,
        coinPrice: Long,
        discountPercent: Int,
        stock: Int,
        description: String,
        specifications: String,
        variations: String,
        shippingInfo: String,
        imageKey: String,
        customImageUri: String?,
        onSuccessDone: () -> Unit = {}
    ) {
        val cmtId = _currentUserCmtId.value ?: return
        viewModelScope.launch {
            val res = repository.addProduct(
                actorCmtId = cmtId,
                name = name,
                category = category,
                subcategory = subcategory,
                pricePkr = pricePkr,
                coinPrice = coinPrice,
                discountPercent = discountPercent,
                stock = stock,
                description = description,
                specifications = specifications,
                variations = variations,
                shippingInfo = shippingInfo,
                imageKey = imageKey,
                customImageUri = customImageUri
            )
            res.onSuccess { prod ->
                showMessage(
                    if (prod.status == "APPROVED") "Product '${prod.name}' published!"
                    else "Product '${prod.name}' submitted for Admin Review!"
                )
                onSuccessDone()
            }.onFailure { err ->
                showMessage(err.message ?: "Could not add product.")
            }
        }
    }

    fun updateShopProfile(shopName: String, description: String, contact: String, location: String) {
        val cmtId = _currentUserCmtId.value ?: return
        viewModelScope.launch {
            repository.updateShopProfile(cmtId, shopName, description, contact, location)
            showMessage("Shop Profile updated successfully!")
        }
    }

    // Chat & Reviews
    fun sendChatMessage(receiverCmtId: String, text: String, sharedOrderNo: String? = null) {
        val senderId = _currentUserCmtId.value ?: return
        viewModelScope.launch {
            repository.sendChatMessage(
                senderCmtId = senderId,
                receiverCmtId = receiverCmtId,
                messageText = text,
                sharedOrderNumber = sharedOrderNo
            )
        }
    }

    fun addProductReview(productId: Long, rating: Int, comment: String) {
        val cmtId = _currentUserCmtId.value ?: return
        viewModelScope.launch {
            repository.submitReview(cmtId, productId, rating, comment)
            showMessage("Thank you! Your ${rating}-star review has been posted.")
        }
    }

    // Admin / Owner Actions
    fun adminApproveOrRejectDeposit(txId: Long, approve: Boolean, note: String = "") {
        val adminId = _currentUserCmtId.value ?: "CMT100000"
        viewModelScope.launch {
            repository.adminReviewDeposit(adminId, txId, approve, note)
                .onSuccess {
                    showMessage(if (approve) "Deposit verified & Coins credited!" else "Deposit rejected.")
                }
                .onFailure { showMessage(it.message ?: "Action failed.") }
        }
    }

    fun adminUpdateWithdrawal(txId: Long, newStatus: String, note: String = "") {
        val adminId = _currentUserCmtId.value ?: "CMT100000"
        viewModelScope.launch {
            repository.adminUpdateWithdrawalStatus(adminId, txId, newStatus, note)
                .onSuccess { showMessage("Withdrawal updated to $newStatus") }
                .onFailure { showMessage(it.message ?: "Action failed.") }
        }
    }

    fun adminReviewResellerApp(appId: Long, newStatus: String, note: String = "") {
        val adminId = _currentUserCmtId.value ?: "CMT100000"
        viewModelScope.launch {
            repository.adminUpdateResellerApplication(adminId, appId, newStatus, note)
                .onSuccess { showMessage("Reseller application updated to $newStatus") }
                .onFailure { showMessage(it.message ?: "Action failed.") }
        }
    }

    fun adminUpdateProductStatus(product: ProductEntity, status: String, featured: Boolean = product.isFeatured) {
        val adminId = _currentUserCmtId.value ?: "CMT100000"
        viewModelScope.launch {
            repository.adminUpdateProductStatus(adminId, product, status, featured)
            showMessage("Product '${product.name}' marked $status")
        }
    }

    fun adminEditProduct(updated: ProductEntity) {
        val adminId = _currentUserCmtId.value ?: "CMT100000"
        viewModelScope.launch {
            repository.updateProductDetails(adminId, updated)
            showMessage("Product '${updated.name}' updated!")
        }
    }

    fun adminDeleteProduct(product: ProductEntity) {
        val adminId = _currentUserCmtId.value ?: "CMT100000"
        viewModelScope.launch {
            repository.deleteProduct(adminId, product)
            showMessage("Deleted '${product.name}'")
        }
    }

    fun adminAddCategory(name: String, parentName: String?, iconKey: String) {
        val adminId = _currentUserCmtId.value ?: "CMT100000"
        viewModelScope.launch {
            repository.addCategory(adminId, name, parentName, iconKey)
            showMessage("Category '$name' created!")
        }
    }

    fun updateOrderStatus(order: OrderEntity, newStatus: String) {
        val actorId = _currentUserCmtId.value ?: "CMT100000"
        viewModelScope.launch {
            repository.updateOrderStatus(actorId, order, newStatus)
            showMessage("Order ${order.orderNumber} updated to $newStatus")
        }
    }

    fun adminUpdateUser(user: UserEntity, role: String, status: String) {
        val adminId = _currentUserCmtId.value ?: "CMT100000"
        viewModelScope.launch {
            repository.adminUpdateUserRoleAndStatus(adminId, user, role, status)
            showMessage("Updated ${user.cmtId}: Role=$role, Status=$status")
        }
    }

    fun updateMyProfile(fullName: String, mobile: String, email: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateUserProfile(user, fullName, mobile, email)
            showMessage("Profile saved!")
        }
    }

    fun adminSaveStoreSettings(settings: StoreSettingsEntity) {
        val adminId = _currentUserCmtId.value ?: "CMT100000"
        viewModelScope.launch {
            repository.adminUpdateStoreSettings(adminId, settings)
            showMessage("CMT Store, Payment, Coin & Referral Settings saved!")
        }
    }

    fun adminBroadcastAnnouncement(title: String, message: String) {
        val adminId = _currentUserCmtId.value ?: "CMT100000"
        viewModelScope.launch {
            repository.adminSendBroadcastAnnouncement(adminId, title, message)
            showMessage("Announcement broadcasted to all CMT members!")
        }
    }

    fun adminDeleteReview(reviewId: Long) {
        val adminId = _currentUserCmtId.value ?: "CMT100000"
        viewModelScope.launch {
            repository.deleteReview(adminId, reviewId)
            showMessage("Review removed.")
        }
    }

    fun markNotificationsRead() {
        val cmtId = _currentUserCmtId.value ?: return
        viewModelScope.launch {
            repository.markAllNotificationsRead(cmtId)
        }
    }
}
