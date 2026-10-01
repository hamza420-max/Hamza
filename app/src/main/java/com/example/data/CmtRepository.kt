package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlin.math.roundToLong

class CmtRepository(private val dao: CmtDao) {

    val allUsers: Flow<List<UserEntity>> = dao.observeAllUsers()
    val allWallets: Flow<List<WalletEntity>> = dao.observeAllWallets()
    val allCategories: Flow<List<CategoryEntity>> = dao.observeAllCategories()
    val allShops: Flow<List<ShopEntity>> = dao.observeAllShops()
    val allProducts: Flow<List<ProductEntity>> = dao.observeAllProducts()
    val allOrders: Flow<List<OrderEntity>> = dao.observeAllOrders()
    val allTransactions: Flow<List<WalletTransactionEntity>> = dao.observeAllTransactions()
    val allResellerApplications: Flow<List<ResellerApplicationEntity>> = dao.observeAllResellerApplications()
    val allReferrals: Flow<List<ReferralEntity>> = dao.observeAllReferrals()
    val allReviews: Flow<List<ReviewEntity>> = dao.observeAllReviews()
    val allChatMessages: Flow<List<ChatMessageEntity>> = dao.observeAllChatMessages()
    val allNotifications: Flow<List<NotificationEntity>> = dao.observeAllNotifications()
    val storeSettings: Flow<StoreSettingsEntity?> = dao.observeStoreSettings()
    val allPromotions: Flow<List<PromotionEntity>> = dao.observeAllPromotions()
    val allAuditLogs: Flow<List<AuditLogEntity>> = dao.observeAllAuditLogs()

    fun observeUser(cmtId: String): Flow<UserEntity?> = dao.observeUserByCmtId(cmtId)
    fun observeWallet(cmtId: String): Flow<WalletEntity?> = dao.observeWallet(cmtId)
    fun observeCart(cmtId: String): Flow<List<CartItemEntity>> = dao.observeCartItems(cmtId)
    fun observeWishlist(cmtId: String): Flow<List<WishlistEntity>> = dao.observeWishlist(cmtId)
    fun observeShop(ownerCmtId: String): Flow<ShopEntity?> = dao.observeShopByOwner(ownerCmtId)

    suspend fun initializeDatabase() {
        CmtSeedData.seedIfNeeded(dao)
    }

    // --- AUTHENTICATION & REGISTRATION ---
    suspend fun registerAccount(
        fullName: String,
        username: String,
        mobile: String,
        email: String,
        password: String,
        referralCodeInput: String?,
        authProvider: String = "PASSWORD"
    ): Result<UserEntity> {
        val cleanUsername = username.trim().lowercase()
        if (fullName.isBlank() || cleanUsername.isBlank() || mobile.isBlank() || email.isBlank()) {
            return Result.failure(IllegalArgumentException("All registration fields are required."))
        }
        val existing = dao.findUserForLogin(cleanUsername) ?: dao.findUserForLogin(email.trim())
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Username or Email is already registered."))
        }

        val count = dao.getUserCount()
        val uniqueCmtId = "CMT${100000 + count + 1}"
        val referralLink = "cmtstore.com/register?ref=$uniqueCmtId"
        val settings = dao.getStoreSettings() ?: StoreSettingsEntity()

        // Extract referrer CMT ID if a link or CMT ID was provided
        val parsedReferrer = referralCodeInput?.trim()?.let { raw ->
            if (raw.contains("ref=", ignoreCase = true)) {
                raw.substringAfter("ref=").substringBefore("&").trim().uppercase()
            } else {
                raw.uppercase()
            }
        }?.takeIf { it.isNotBlank() && it != uniqueCmtId }

        var validReferrerId: String? = null
        if (parsedReferrer != null) {
            val refUser = dao.getUserByCmtId(parsedReferrer)
            // Anti-self-referral & duplicate phone/email check
            if (refUser != null && (!settings.preventSelfAndDuplicateReferral ||
                        (refUser.mobile != mobile.trim() && !refUser.email.equals(email.trim(), ignoreCase = true)))
            ) {
                validReferrerId = refUser.cmtId
            }
        }

        val newUser = UserEntity(
            cmtId = uniqueCmtId,
            fullName = fullName.trim(),
            username = cleanUsername,
            mobile = mobile.trim(),
            email = email.trim(),
            passwordHash = CmtDatabase.hashPassword(password),
            authProvider = authProvider,
            role = "MEMBER",
            accountStatus = "ACTIVE",
            referralLink = referralLink,
            referredByCmtId = validReferrerId
        )
        dao.insertUser(newUser)

        // Automatically create user's Wallet & Coin Balance (0 Coins initially - never fake balance)
        dao.insertOrUpdateWallet(
            WalletEntity(
                userCmtId = uniqueCmtId,
                currentCoins = 0L,
                totalDepositedPkr = 0L,
                totalSpentCoins = 0L,
                pendingDepositCoins = 0L,
                pendingWithdrawalCoins = 0L,
                securityDepositPkr = 0L,
                referralPoints = 0,
                totalEarningsCoins = 0L
            )
        )

        // Record Referral (Pending until qualifying condition is completed!)
        if (validReferrerId != null) {
            dao.insertReferral(
                ReferralEntity(
                    referrerCmtId = validReferrerId,
                    newMemberCmtId = uniqueCmtId,
                    newMemberName = newUser.fullName,
                    qualifyingCondition = settings.referralQualifyingCondition,
                    status = "PENDING",
                    pointsAwarded = 0
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    targetCmtId = validReferrerId,
                    title = "New Referral Registration: $uniqueCmtId",
                    message = "${newUser.fullName} registered using your referral link! Points will be credited once they complete: ${settings.referralQualifyingCondition}.",
                    type = "REFERRAL"
                )
            )
        }

        dao.insertNotification(
            NotificationEntity(
                targetCmtId = uniqueCmtId,
                title = "Welcome to CMT! Your ID is $uniqueCmtId",
                message = "Your Chauhan Mobile Traders account, Coin Wallet, and Referral Link ($referralLink) are active.",
                type = "AUTH"
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = uniqueCmtId,
                actorRole = "MEMBER",
                actionType = "USER_REGISTERED",
                targetInfo = "$uniqueCmtId (${newUser.username})",
                details = "New account registered via $authProvider. Referrer: ${validReferrerId ?: "None"}"
            )
        )

        return Result.success(newUser)
    }

    suspend fun login(identifier: String, password: String): Result<UserEntity> {
        val user = dao.findUserForLogin(identifier.trim())
            ?: return Result.failure(IllegalArgumentException("No CMT account found for '$identifier'."))

        if (user.accountStatus == "BLOCKED" || user.accountStatus == "SUSPENDED") {
            return Result.failure(IllegalStateException("Account ${user.cmtId} is currently ${user.accountStatus}. Contact CMT Support."))
        }

        val inputHash = CmtDatabase.hashPassword(password)
        if (user.passwordHash != inputHash) {
            return Result.failure(IllegalArgumentException("Incorrect password for ${user.cmtId}."))
        }

        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = user.cmtId,
                actorRole = user.role,
                actionType = "USER_LOGIN",
                targetInfo = user.cmtId,
                details = "User logged in successfully."
            )
        )
        return Result.success(user)
    }

    suspend fun continueWithGoogle(googleName: String, googleEmail: String, referralCode: String? = null): Result<UserEntity> {
        val settings = dao.getStoreSettings() ?: StoreSettingsEntity()
        if (!settings.enableGoogleAuth) {
            return Result.failure(IllegalStateException("Google Authentication is currently disabled by Owner."))
        }
        val existing = dao.findUserForLogin(googleEmail.trim())
        if (existing != null) {
            if (existing.accountStatus != "ACTIVE") {
                return Result.failure(IllegalStateException("Account ${existing.cmtId} is ${existing.accountStatus}."))
            }
            return Result.success(existing)
        }
        val baseUsername = googleEmail.substringBefore("@").replace("[^a-zA-Z0-9_]".toRegex(), "").lowercase()
        return registerAccount(
            fullName = googleName,
            username = "${baseUsername}_${(100..999).random()}",
            mobile = "0300-${(1000000..9999999).random()}",
            email = googleEmail,
            password = "GOOGLE_OAUTH_TOKEN_${System.currentTimeMillis()}",
            referralCodeInput = referralCode,
            authProvider = "GOOGLE"
        )
    }

    // --- DEPOSIT SYSTEM (PENDING UNTIL ADMIN VERIFICATION) ---
    suspend fun submitDepositRequest(
        userCmtId: String,
        amountPkr: Long,
        referenceId: String,
        proofImageUri: String?
    ): Result<WalletTransactionEntity> {
        val settings = dao.getStoreSettings() ?: StoreSettingsEntity()
        if (amountPkr < settings.minDepositPkr || amountPkr > settings.maxDepositPkr) {
            return Result.failure(
                IllegalArgumentException("Deposit must be between ${settings.minDepositPkr} and ${settings.maxDepositPkr} PKR.")
            )
        }
        if (referenceId.isBlank()) {
            return Result.failure(IllegalArgumentException("EasyPaisa Payment / Reference ID is required."))
        }

        val user = dao.getUserByCmtId(userCmtId)
            ?: return Result.failure(IllegalStateException("User not found."))
        val wallet = dao.getWallet(userCmtId) ?: WalletEntity(userCmtId = userCmtId)

        val expectedCoins = (amountPkr * settings.pkrToCoinRate).roundToLong()
        val txCode = "TXN-${(10000..99999).random()}"

        val tx = WalletTransactionEntity(
            transactionCode = txCode,
            userCmtId = userCmtId,
            userName = user.fullName,
            type = "DEPOSIT",
            amountPkr = amountPkr,
            coins = expectedCoins,
            status = "PENDING",
            paymentMethod = settings.paymentMethodName,
            accountName = settings.paymentAccountName,
            accountNumber = settings.paymentAccountNumber,
            referenceId = referenceId.trim(),
            proofImageUri = proofImageUri,
            notes = "Submitted ${settings.paymentMethodName} deposit ($amountPkr PKR -> $expectedCoins Coins). Pending Admin Verification."
        )
        dao.insertTransaction(tx)

        // Update only pendingDepositCoins — NEVER credit currentCoins before Admin approval!
        dao.insertOrUpdateWallet(
            wallet.copy(
                pendingDepositCoins = wallet.pendingDepositCoins + expectedCoins,
                updatedAt = System.currentTimeMillis()
            )
        )

        dao.insertNotification(
            NotificationEntity(
                targetCmtId = userCmtId,
                title = "Deposit Submitted ($txCode)",
                message = "Your $amountPkr PKR ($expectedCoins Coins) EasyPaisa deposit is PENDING verification.",
                type = "DEPOSIT"
            )
        )
        dao.insertNotification(
            NotificationEntity(
                targetCmtId = "OWNER",
                title = "New Pending Deposit ($txCode)",
                message = "${user.fullName} ($userCmtId) submitted $amountPkr PKR via ${settings.paymentMethodName} (Ref: $referenceId).",
                type = "DEPOSIT"
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = userCmtId,
                actorRole = user.role,
                actionType = "DEPOSIT_SUBMITTED",
                targetInfo = txCode,
                details = "Deposit $amountPkr PKR ($expectedCoins Coins) Ref: $referenceId submitted as PENDING."
            )
        )

        return Result.success(tx)
    }

    suspend fun adminReviewDeposit(
        adminCmtId: String,
        transactionId: Long,
        approve: Boolean,
        adminNote: String = ""
    ): Result<Unit> {
        val tx = dao.getTransactionById(transactionId)
            ?: return Result.failure(IllegalArgumentException("Transaction not found."))
        if (tx.status != "PENDING") {
            return Result.failure(IllegalStateException("Transaction is already ${tx.status}."))
        }

        val wallet = dao.getWallet(tx.userCmtId) ?: WalletEntity(userCmtId = tx.userCmtId)
        val reducedPending = (wallet.pendingDepositCoins - tx.coins).coerceAtLeast(0L)

        if (approve) {
            dao.updateTransaction(
                tx.copy(
                    status = "COMPLETED",
                    notes = if (adminNote.isNotBlank()) adminNote else "Verified & Approved by Owner/Admin (+${tx.coins} Coins credited)"
                )
            )
            dao.insertOrUpdateWallet(
                wallet.copy(
                    currentCoins = wallet.currentCoins + tx.coins,
                    totalDepositedPkr = wallet.totalDepositedPkr + tx.amountPkr,
                    pendingDepositCoins = reducedPending,
                    updatedAt = System.currentTimeMillis()
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    targetCmtId = tx.userCmtId,
                    title = "Deposit Approved! +${tx.coins} Coins Credited",
                    message = "Your ${tx.amountPkr} PKR deposit (${tx.transactionCode}) has been verified. +${tx.coins} Coins added to your CMT Wallet.",
                    type = "DEPOSIT"
                )
            )
            // Trigger Referral Qualification check!
            checkAndRewardQualifyingReferral(tx.userCmtId)
        } else {
            dao.updateTransaction(
                tx.copy(
                    status = "REJECTED",
                    notes = if (adminNote.isNotBlank()) adminNote else "Rejected by Owner/Admin (Payment reference could not be verified)"
                )
            )
            dao.insertOrUpdateWallet(
                wallet.copy(
                    pendingDepositCoins = reducedPending,
                    updatedAt = System.currentTimeMillis()
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    targetCmtId = tx.userCmtId,
                    title = "Deposit Rejected (${tx.transactionCode})",
                    message = "Your ${tx.amountPkr} PKR deposit request was rejected. Note: $adminNote",
                    type = "DEPOSIT"
                )
            )
        }

        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = adminCmtId,
                actorRole = "OWNER",
                actionType = if (approve) "DEPOSIT_APPROVED" else "DEPOSIT_REJECTED",
                targetInfo = "${tx.transactionCode} (${tx.userCmtId})",
                details = "${if (approve) "Credited +${tx.coins} Coins" else "Rejected deposit"} for ${tx.amountPkr} PKR."
            )
        )
        return Result.success(Unit)
    }

    // --- WITHDRAWAL SYSTEM ---
    suspend fun submitWithdrawalRequest(
        userCmtId: String,
        coinsToWithdraw: Long,
        paymentMethod: String,
        recipientAccountName: String,
        recipientAccountNumber: String
    ): Result<WalletTransactionEntity> {
        val settings = dao.getStoreSettings() ?: StoreSettingsEntity()
        if (coinsToWithdraw < settings.minWithdrawalCoins || coinsToWithdraw > settings.maxWithdrawalCoins) {
            return Result.failure(
                IllegalArgumentException("Withdrawal must be between ${settings.minWithdrawalCoins} and ${settings.maxWithdrawalCoins} Coins.")
            )
        }
        if (recipientAccountName.isBlank() || recipientAccountNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("Account Name and Account Number are required."))
        }

        val user = dao.getUserByCmtId(userCmtId)
            ?: return Result.failure(IllegalStateException("User not found."))
        val wallet = dao.getWallet(userCmtId) ?: WalletEntity(userCmtId = userCmtId)

        if (wallet.currentCoins < coinsToWithdraw) {
            return Result.failure(IllegalArgumentException("Insufficient spendable Coin balance (${wallet.currentCoins} Coins available). Note: Security deposit is held separately."))
        }

        val pkrEquivalent = (coinsToWithdraw / settings.pkrToCoinRate.coerceAtLeast(0.01)).roundToLong()
        val txCode = "WD-${(10000..99999).random()}"

        // Reserve coins into pendingWithdrawalCoins while awaiting Admin confirmation
        dao.insertOrUpdateWallet(
            wallet.copy(
                currentCoins = wallet.currentCoins - coinsToWithdraw,
                pendingWithdrawalCoins = wallet.pendingWithdrawalCoins + coinsToWithdraw,
                updatedAt = System.currentTimeMillis()
            )
        )

        val tx = WalletTransactionEntity(
            transactionCode = txCode,
            userCmtId = userCmtId,
            userName = user.fullName,
            type = "WITHDRAWAL",
            amountPkr = pkrEquivalent,
            coins = coinsToWithdraw,
            status = "PENDING",
            paymentMethod = paymentMethod,
            accountName = recipientAccountName.trim(),
            accountNumber = recipientAccountNumber.trim(),
            referenceId = txCode,
            notes = "Withdrawal request to $paymentMethod ($recipientAccountName - $recipientAccountNumber)"
        )
        dao.insertTransaction(tx)

        dao.insertNotification(
            NotificationEntity(
                targetCmtId = userCmtId,
                title = "Withdrawal Request Submitted ($txCode)",
                message = "Your withdrawal of $coinsToWithdraw Coins ($pkrEquivalent PKR) is PENDING Admin review.",
                type = "WITHDRAWAL"
            )
        )
        dao.insertNotification(
            NotificationEntity(
                targetCmtId = "OWNER",
                title = "Withdrawal Request ($txCode)",
                message = "${user.fullName} ($userCmtId) requested withdrawal of $coinsToWithdraw Coins ($pkrEquivalent PKR) to $paymentMethod ($recipientAccountNumber).",
                type = "WITHDRAWAL"
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = userCmtId,
                actorRole = user.role,
                actionType = "WITHDRAWAL_REQUESTED",
                targetInfo = txCode,
                details = "Requested $coinsToWithdraw Coins ($pkrEquivalent PKR) to $paymentMethod $recipientAccountNumber"
            )
        )
        return Result.success(tx)
    }

    suspend fun adminUpdateWithdrawalStatus(
        adminCmtId: String,
        transactionId: Long,
        newStatus: String, // PROCESSING, APPROVED, COMPLETED, REJECTED
        adminNote: String = ""
    ): Result<Unit> {
        val tx = dao.getTransactionById(transactionId)
            ?: return Result.failure(IllegalArgumentException("Withdrawal transaction not found."))
        val wallet = dao.getWallet(tx.userCmtId) ?: WalletEntity(userCmtId = tx.userCmtId)

        if (tx.status == "COMPLETED" || tx.status == "REJECTED") {
            return Result.failure(IllegalStateException("Withdrawal is already finalized (${tx.status})."))
        }

        dao.updateTransaction(
            tx.copy(
                status = newStatus,
                notes = if (adminNote.isNotBlank()) adminNote else "Withdrawal status updated to $newStatus by Admin"
            )
        )

        if (newStatus == "COMPLETED") {
            dao.insertOrUpdateWallet(
                wallet.copy(
                    pendingWithdrawalCoins = (wallet.pendingWithdrawalCoins - tx.coins).coerceAtLeast(0L),
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else if (newStatus == "REJECTED") {
            // Refund reserved coins back to currentCoins
            dao.insertOrUpdateWallet(
                wallet.copy(
                    currentCoins = wallet.currentCoins + tx.coins,
                    pendingWithdrawalCoins = (wallet.pendingWithdrawalCoins - tx.coins).coerceAtLeast(0L),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        dao.insertNotification(
            NotificationEntity(
                targetCmtId = tx.userCmtId,
                title = "Withdrawal ${tx.transactionCode}: $newStatus",
                message = "Your withdrawal request for ${tx.coins} Coins (${tx.amountPkr} PKR) is now $newStatus.",
                type = "WITHDRAWAL"
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = adminCmtId,
                actorRole = "OWNER",
                actionType = "WITHDRAWAL_$newStatus",
                targetInfo = "${tx.transactionCode} (${tx.userCmtId})",
                details = "Updated withdrawal of ${tx.coins} Coins to $newStatus."
            )
        )
        return Result.success(Unit)
    }

    // --- RESELLER APPLICATION & 20,000 PKR SECURITY DEPOSIT ---
    suspend fun submitResellerApplication(
        userCmtId: String,
        proposedShopName: String,
        shopDescription: String,
        businessCity: String,
        depositAmountPkr: Long,
        referenceId: String,
        proofImageUri: String?
    ): Result<ResellerApplicationEntity> {
        val settings = dao.getStoreSettings() ?: StoreSettingsEntity()
        if (proposedShopName.isBlank() || businessCity.isBlank() || referenceId.isBlank()) {
            return Result.failure(IllegalArgumentException("Shop Name, City, and EasyPaisa Reference ID are required."))
        }
        if (depositAmountPkr < settings.resellerSecurityDepositPkr) {
            return Result.failure(
                IllegalArgumentException("Reseller Security Deposit requires at least ${settings.resellerSecurityDepositPkr} PKR.")
            )
        }

        val user = dao.getUserByCmtId(userCmtId)
            ?: return Result.failure(IllegalStateException("User not found."))

        val app = ResellerApplicationEntity(
            applicantCmtId = userCmtId,
            applicantName = user.fullName,
            applicantMobile = user.mobile,
            proposedShopName = proposedShopName.trim(),
            shopDescription = shopDescription.trim().ifBlank { "Verified CMT Mobile Accessories Reseller" },
            businessCity = businessCity.trim(),
            securityDepositPkr = depositAmountPkr,
            paymentMethod = settings.paymentMethodName,
            paymentAccountName = settings.paymentAccountName,
            paymentAccountNumber = settings.paymentAccountNumber,
            referenceId = referenceId.trim(),
            proofImageUri = proofImageUri,
            status = "PENDING_APPROVAL",
            adminNotes = "Submitted ${depositAmountPkr} PKR security deposit via ${settings.paymentMethodName} (Ref: $referenceId)."
        )
        dao.insertResellerApplication(app)

        // Also record as a distinct RESELLER_SECURITY_DEPOSIT transaction in Pending status
        val txCode = "SEC-${(10000..99999).random()}"
        dao.insertTransaction(
            WalletTransactionEntity(
                transactionCode = txCode,
                userCmtId = userCmtId,
                userName = user.fullName,
                type = "RESELLER_SECURITY_DEPOSIT",
                amountPkr = depositAmountPkr,
                coins = depositAmountPkr,
                status = "PENDING",
                paymentMethod = settings.paymentMethodName,
                accountName = settings.paymentAccountName,
                accountNumber = settings.paymentAccountNumber,
                referenceId = referenceId.trim(),
                proofImageUri = proofImageUri,
                notes = "Reseller Security Deposit ($depositAmountPkr PKR) for shop '${proposedShopName.trim()}'"
            )
        )

        dao.insertNotification(
            NotificationEntity(
                targetCmtId = userCmtId,
                title = "Reseller Application Submitted",
                message = "Your application for '${proposedShopName.trim()}' with $depositAmountPkr PKR security deposit is Pending Admin Approval.",
                type = "RESELLER"
            )
        )
        dao.insertNotification(
            NotificationEntity(
                targetCmtId = "OWNER",
                title = "New Reseller Application: ${proposedShopName.trim()}",
                message = "${user.fullName} ($userCmtId) applied for Reseller with $depositAmountPkr PKR EasyPaisa Ref: $referenceId.",
                type = "RESELLER"
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = userCmtId,
                actorRole = user.role,
                actionType = "RESELLER_APPLIED",
                targetInfo = "$userCmtId (${proposedShopName.trim()})",
                details = "Submitted $depositAmountPkr PKR Security Deposit Ref: $referenceId"
            )
        )
        return Result.success(app)
    }

    suspend fun adminUpdateResellerApplication(
        adminCmtId: String,
        applicationId: Long,
        newStatus: String, // APPROVED, REJECTED, INFO_REQUESTED, SUSPENDED, ACTIVE
        adminNote: String
    ): Result<Unit> {
        val app = dao.getResellerApplicationById(applicationId)
            ?: return Result.failure(IllegalArgumentException("Application not found."))
        val user = dao.getUserByCmtId(app.applicantCmtId)
            ?: return Result.failure(IllegalArgumentException("Applicant user not found."))
        val wallet = dao.getWallet(user.cmtId) ?: WalletEntity(userCmtId = user.cmtId)
        val settings = dao.getStoreSettings() ?: StoreSettingsEntity()

        val effectiveStatus = if (newStatus == "APPROVED") "ACTIVE" else newStatus
        dao.updateResellerApplication(
            app.copy(
                status = effectiveStatus,
                adminNotes = adminNote.ifBlank { "Status updated to $effectiveStatus by Admin" },
                reviewedAt = System.currentTimeMillis()
            )
        )

        if (effectiveStatus == "ACTIVE") {
            // Upgrade user role to RESELLER
            dao.updateUser(user.copy(role = "RESELLER"))

            // Credit Security Deposit separately (or also to spendable if Owner configured treatSecurityDepositAsSpendable)
            val newSecurityDeposit = wallet.securityDepositPkr.coerceAtLeast(app.securityDepositPkr)
            val addedSpendable = if (settings.treatSecurityDepositAsSpendable && wallet.securityDepositPkr == 0L) {
                app.securityDepositPkr
            } else 0L

            dao.insertOrUpdateWallet(
                wallet.copy(
                    securityDepositPkr = newSecurityDeposit,
                    currentCoins = wallet.currentCoins + addedSpendable,
                    updatedAt = System.currentTimeMillis()
                )
            )

            // Ensure Shop Profile exists
            val existingShop = dao.getShopByOwner(user.cmtId)
            if (existingShop == null) {
                dao.insertOrUpdateShop(
                    ShopEntity(
                        ownerCmtId = user.cmtId,
                        shopName = app.proposedShopName,
                        shopDescription = app.shopDescription,
                        contactInfo = "${user.mobile} | ${user.email}",
                        location = app.businessCity,
                        isVerified = true
                    )
                )
            }

            dao.insertNotification(
                NotificationEntity(
                    targetCmtId = user.cmtId,
                    title = "Congratulations! Reseller Panel Activated",
                    message = "Your ${app.securityDepositPkr} PKR security deposit is verified. Your role is now RESELLER and Add Product is unlocked!",
                    type = "RESELLER"
                )
            )
        } else if (effectiveStatus == "SUSPENDED" || effectiveStatus == "REJECTED") {
            if (user.role == "RESELLER") {
                dao.updateUser(user.copy(role = "MEMBER"))
            }
            dao.insertNotification(
                NotificationEntity(
                    targetCmtId = user.cmtId,
                    title = "Reseller Status: $effectiveStatus",
                    message = "Admin update on your Reseller status: $adminNote",
                    type = "RESELLER"
                )
            )
        } else {
            dao.insertNotification(
                NotificationEntity(
                    targetCmtId = user.cmtId,
                    title = "Reseller Application Update: $effectiveStatus",
                    message = "Admin note: $adminNote",
                    type = "RESELLER"
                )
            )
        }

        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = adminCmtId,
                actorRole = "OWNER",
                actionType = "RESELLER_STATUS_$effectiveStatus",
                targetInfo = "${user.cmtId} (${app.proposedShopName})",
                details = "Updated reseller application to $effectiveStatus. Note: $adminNote"
            )
        )
        return Result.success(Unit)
    }

    // --- SHOP PROFILE UPDATE ---
    suspend fun updateShopProfile(
        ownerCmtId: String,
        shopName: String,
        shopDescription: String,
        contactInfo: String,
        location: String,
        logoUri: String? = null,
        bannerUri: String? = null
    ): Result<Unit> {
        val existing = dao.getShopByOwner(ownerCmtId)
        val updated = if (existing != null) {
            existing.copy(
                shopName = shopName.trim(),
                shopDescription = shopDescription.trim(),
                contactInfo = contactInfo.trim(),
                location = location.trim(),
                shopLogoUri = logoUri ?: existing.shopLogoUri,
                shopBannerUri = bannerUri ?: existing.shopBannerUri
            )
        } else {
            ShopEntity(
                ownerCmtId = ownerCmtId,
                shopName = shopName.trim(),
                shopDescription = shopDescription.trim(),
                contactInfo = contactInfo.trim(),
                location = location.trim(),
                shopLogoUri = logoUri,
                shopBannerUri = bannerUri
            )
        }
        dao.insertOrUpdateShop(updated)
        return Result.success(Unit)
    }

    // --- PRODUCT MANAGEMENT (RESELLER ADD PRODUCT -> ADMIN APPROVAL) ---
    suspend fun addProduct(
        actorCmtId: String,
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
        customImageUri: String? = null
    ): Result<ProductEntity> {
        val user = dao.getUserByCmtId(actorCmtId)
            ?: return Result.failure(IllegalStateException("User not found."))

        if (user.role != "RESELLER" && user.role != "SELLER" && user.role != "OWNER") {
            return Result.failure(
                IllegalAccessException("Only approved Resellers, Sellers, or Owner can add products. Please Apply for Reseller first.")
            )
        }
        if (name.isBlank() || category.isBlank() || pricePkr <= 0 || coinPrice <= 0) {
            return Result.failure(IllegalArgumentException("Product Name, Category, Price, and Coin Price are required."))
        }

        val shop = dao.getShopByOwner(actorCmtId)
        val shopName = shop?.shopName ?: if (user.role == "OWNER") "CMT Official Flagship Store" else user.fullName
        val initialStatus = if (user.role == "OWNER") "APPROVED" else "PENDING_REVIEW"

        val product = ProductEntity(
            productCode = "CMT-PRD-${(1000..9999).random()}",
            name = name.trim(),
            category = category.trim(),
            subcategory = subcategory.trim().ifBlank { "General" },
            sellerCmtId = actorCmtId,
            shopName = shopName,
            pricePkr = pricePkr,
            coinPrice = coinPrice,
            discountPercent = discountPercent.coerceIn(0, 90),
            stock = stock.coerceAtLeast(0),
            description = description.trim().ifBlank { "High-quality CMT mobile accessory." },
            specifications = specifications.trim().ifBlank { "Quality Checked | CMT Warranty" },
            variations = variations.trim().ifBlank { "Standard Edition" },
            shippingInfo = shippingInfo.trim().ifBlank { "Fast Nationwide Delivery (2-3 Days)" },
            imageKey = imageKey,
            customImageUri = customImageUri,
            status = initialStatus,
            isFeatured = user.role == "OWNER"
        )
        dao.insertProduct(product)

        if (initialStatus == "PENDING_REVIEW") {
            dao.insertNotification(
                NotificationEntity(
                    targetCmtId = "OWNER",
                    title = "New Reseller Product Pending Review",
                    message = "$shopName ($actorCmtId) submitted '${product.name}' (${product.coinPrice} Coins) for approval.",
                    type = "PRODUCT"
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    targetCmtId = actorCmtId,
                    title = "Product Submitted for Review",
                    message = "'${product.name}' is now Pending Admin Approval before publishing.",
                    type = "PRODUCT"
                )
            )
        }

        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = actorCmtId,
                actorRole = user.role,
                actionType = "PRODUCT_CREATED",
                targetInfo = "${product.productCode} (${product.name})",
                details = "Created product in $category at ${product.coinPrice} Coins. Status: $initialStatus"
            )
        )
        return Result.success(product)
    }

    suspend fun adminUpdateProductStatus(
        adminCmtId: String,
        product: ProductEntity,
        newStatus: String, // APPROVED, REJECTED, HIDDEN
        isFeatured: Boolean = product.isFeatured
    ) {
        val updated = product.copy(status = newStatus, isFeatured = isFeatured)
        dao.updateProduct(updated)
        dao.insertNotification(
            NotificationEntity(
                targetCmtId = product.sellerCmtId,
                title = "Product ${product.name}: $newStatus",
                message = "Your product '${product.name}' (${product.productCode}) status is now $newStatus.",
                type = "PRODUCT"
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = adminCmtId,
                actorRole = "OWNER",
                actionType = "PRODUCT_$newStatus",
                targetInfo = "${product.productCode} (${product.name})",
                details = "Updated status to $newStatus (Featured: $isFeatured)"
            )
        )
    }

    suspend fun updateProductDetails(actorCmtId: String, updated: ProductEntity) {
        dao.updateProduct(updated)
        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = actorCmtId,
                actorRole = "OWNER",
                actionType = "PRODUCT_EDITED",
                targetInfo = "${updated.productCode} (${updated.name})",
                details = "Updated price=${updated.coinPrice} Coins, stock=${updated.stock}, discount=${updated.discountPercent}%"
            )
        )
    }

    suspend fun deleteProduct(actorCmtId: String, product: ProductEntity) {
        dao.deleteProduct(product.id)
        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = actorCmtId,
                actorRole = "OWNER",
                actionType = "PRODUCT_DELETED",
                targetInfo = "${product.productCode} (${product.name})",
                details = "Deleted product from store."
            )
        )
    }

    // --- CATEGORIES ---
    suspend fun addCategory(adminCmtId: String, name: String, parentCategoryName: String?, iconKey: String = "devices") {
        if (name.isBlank()) return
        dao.insertCategory(
            CategoryEntity(
                name = name.trim(),
                parentCategoryName = parentCategoryName?.takeIf { it.isNotBlank() },
                iconKey = iconKey
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = adminCmtId,
                actorRole = "OWNER",
                actionType = "CATEGORY_ADDED",
                targetInfo = name.trim(),
                details = "Added category '${name.trim()}' (Parent: ${parentCategoryName ?: "Top-Level"})"
            )
        )
    }

    // --- CART & WISHLIST ---
    suspend fun addToCart(userCmtId: String, productId: Long, variation: String = "Standard", qty: Int = 1) {
        val existing = dao.getCartItem(userCmtId, productId)
        if (existing != null) {
            dao.updateCartItem(existing.copy(quantity = existing.quantity + qty, selectedVariation = variation))
        } else {
            dao.insertCartItem(
                CartItemEntity(
                    userCmtId = userCmtId,
                    productId = productId,
                    selectedVariation = variation,
                    quantity = qty
                )
            )
        }
    }

    suspend fun updateCartQuantity(item: CartItemEntity, newQty: Int) {
        if (newQty <= 0) {
            dao.deleteCartItem(item.id)
        } else {
            dao.updateCartItem(item.copy(quantity = newQty))
        }
    }

    suspend fun toggleWishlist(userCmtId: String, productId: Long): Boolean {
        val existing = dao.getWishlistItem(userCmtId, productId)
        return if (existing != null) {
            dao.removeWishlistItem(userCmtId, productId)
            false
        } else {
            dao.insertWishlistItem(WishlistEntity(userCmtId = userCmtId, productId = productId))
            true
        }
    }

    // --- CHECKOUT & ORDER MANAGEMENT ---
    suspend fun placeOrderWithCoins(
        buyerCmtId: String,
        buyerName: String,
        buyerPhone: String,
        buyerAddress: String,
        buyerCity: String,
        deliveryInfo: String,
        orderNotes: String,
        cartProducts: List<Pair<CartItemEntity, ProductEntity>>
    ): Result<OrderEntity> {
        if (cartProducts.isEmpty()) {
            return Result.failure(IllegalArgumentException("Your cart is empty."))
        }
        if (buyerName.isBlank() || buyerPhone.isBlank() || buyerAddress.isBlank() || buyerCity.isBlank()) {
            return Result.failure(IllegalArgumentException("Name, Phone, Address, and City are required for delivery."))
        }

        val wallet = dao.getWallet(buyerCmtId) ?: WalletEntity(userCmtId = buyerCmtId)

        var totalCoins = 0L
        var totalPkr = 0L
        var totalQty = 0
        cartProducts.forEach { (cartItem, product) ->
            if (product.stock < cartItem.quantity) {
                return Result.failure(IllegalStateException("Insufficient stock for '${product.name}' (${product.stock} left)."))
            }
            val effectiveCoins = (product.coinPrice * (100 - product.discountPercent) / 100L).coerceAtLeast(1L)
            val effectivePkr = (product.pricePkr * (100 - product.discountPercent) / 100L).coerceAtLeast(1L)
            totalCoins += effectiveCoins * cartItem.quantity
            totalPkr += effectivePkr * cartItem.quantity
            totalQty += cartItem.quantity
        }

        if (wallet.currentCoins < totalCoins) {
            return Result.failure(
                IllegalArgumentException("Insufficient Coin Wallet balance. Required: $totalCoins Coins, Available: ${wallet.currentCoins} Coins. Please Deposit Coins first.")
            )
        }

        // Deduct buyer coins
        dao.insertOrUpdateWallet(
            wallet.copy(
                currentCoins = wallet.currentCoins - totalCoins,
                totalSpentCoins = wallet.totalSpentCoins + totalCoins,
                updatedAt = System.currentTimeMillis()
            )
        )

        // Reduce product stock
        cartProducts.forEach { (cartItem, product) ->
            dao.updateProduct(product.copy(stock = (product.stock - cartItem.quantity).coerceAtLeast(0)))
        }

        val primaryProduct = cartProducts.first().second
        val orderNumber = "ORD-CMT-${(10000..99999).random()}"
        val summary = cartProducts.joinToString("; ") { (ci, p) ->
            "${ci.quantity}x ${p.name} (${ci.selectedVariation})"
        }
        val productIdsCsv = cartProducts.joinToString(",") { it.second.id.toString() }

        val order = OrderEntity(
            orderNumber = orderNumber,
            buyerCmtId = buyerCmtId,
            buyerName = buyerName.trim(),
            buyerPhone = buyerPhone.trim(),
            buyerAddress = buyerAddress.trim(),
            buyerCity = buyerCity.trim(),
            deliveryInfo = deliveryInfo.trim().ifBlank { "Standard Express Delivery" },
            orderNotes = orderNotes.trim(),
            sellerCmtId = primaryProduct.sellerCmtId,
            shopName = primaryProduct.shopName,
            itemsSummary = summary,
            productIdsCsv = productIdsCsv,
            totalQuantity = totalQty,
            totalPricePkr = totalPkr,
            totalCoinsPaid = totalCoins,
            status = "PENDING"
        )
        dao.insertOrder(order)

        // Record Wallet Purchase Transaction
        dao.insertTransaction(
            WalletTransactionEntity(
                transactionCode = "TXN-${(10000..99999).random()}",
                userCmtId = buyerCmtId,
                userName = buyerName.trim(),
                type = "PURCHASE",
                amountPkr = totalPkr,
                coins = totalCoins,
                status = "COMPLETED",
                paymentMethod = "CMT Coin Wallet",
                referenceId = orderNumber,
                notes = "Order $orderNumber: $summary"
            )
        )

        dao.clearCart(buyerCmtId)

        // Notify Buyer, Seller, and Owner
        dao.insertNotification(
            NotificationEntity(
                targetCmtId = buyerCmtId,
                title = "Order Confirmed ($orderNumber)",
                message = "Your order ($summary) for $totalCoins Coins has been placed!",
                type = "ORDER"
            )
        )
        dao.insertNotification(
            NotificationEntity(
                targetCmtId = primaryProduct.sellerCmtId,
                title = "New Customer Order ($orderNumber)",
                message = "${buyerName.trim()} ordered $summary ($totalCoins Coins).",
                type = "ORDER"
            )
        )

        // Check Referral Qualifying Condition
        checkAndRewardQualifyingReferral(buyerCmtId)

        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = buyerCmtId,
                actorRole = "MEMBER",
                actionType = "ORDER_PLACED",
                targetInfo = orderNumber,
                details = "Paid $totalCoins Coins for $summary"
            )
        )
        return Result.success(order)
    }

    suspend fun updateOrderStatus(actorCmtId: String, order: OrderEntity, newStatus: String) {
        val oldStatus = order.status
        if (oldStatus == newStatus) return
        val updated = order.copy(status = newStatus, updatedAt = System.currentTimeMillis())
        dao.updateOrder(updated)

        // If transitioning to COMPLETED for the first time, credit seller earnings!
        if (newStatus == "COMPLETED" && oldStatus != "COMPLETED") {
            val sellerWallet = dao.getWallet(order.sellerCmtId) ?: WalletEntity(userCmtId = order.sellerCmtId)
            dao.insertOrUpdateWallet(
                sellerWallet.copy(
                    currentCoins = sellerWallet.currentCoins + order.totalCoinsPaid,
                    totalEarningsCoins = sellerWallet.totalEarningsCoins + order.totalCoinsPaid,
                    updatedAt = System.currentTimeMillis()
                )
            )
            dao.insertTransaction(
                WalletTransactionEntity(
                    transactionCode = "ERN-${(10000..99999).random()}",
                    userCmtId = order.sellerCmtId,
                    userName = order.shopName,
                    type = "SALE_EARNING",
                    amountPkr = order.totalPricePkr,
                    coins = order.totalCoinsPaid,
                    status = "COMPLETED",
                    paymentMethod = "CMT Order Settlement",
                    referenceId = order.orderNumber,
                    notes = "Order ${order.orderNumber} completed. +${order.totalCoinsPaid} Coins credited to seller wallet."
                )
            )
        } else if (newStatus == "REFUNDED" && oldStatus != "REFUNDED") {
            val buyerWallet = dao.getWallet(order.buyerCmtId) ?: WalletEntity(userCmtId = order.buyerCmtId)
            dao.insertOrUpdateWallet(
                buyerWallet.copy(
                    currentCoins = buyerWallet.currentCoins + order.totalCoinsPaid,
                    updatedAt = System.currentTimeMillis()
                )
            )
            dao.insertTransaction(
                WalletTransactionEntity(
                    transactionCode = "RFD-${(10000..99999).random()}",
                    userCmtId = order.buyerCmtId,
                    userName = order.buyerName,
                    type = "REFUND",
                    amountPkr = order.totalPricePkr,
                    coins = order.totalCoinsPaid,
                    status = "COMPLETED",
                    paymentMethod = "CMT Coin Refund",
                    referenceId = order.orderNumber,
                    notes = "Refunded +${order.totalCoinsPaid} Coins for Order ${order.orderNumber}"
                )
            )
        }

        dao.insertNotification(
            NotificationEntity(
                targetCmtId = order.buyerCmtId,
                title = "Order ${order.orderNumber} Status: $newStatus",
                message = "Your order (${order.itemsSummary}) is now $newStatus.",
                type = "ORDER"
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = actorCmtId,
                actorRole = "RESELLER/OWNER",
                actionType = "ORDER_STATUS_$newStatus",
                targetInfo = order.orderNumber,
                details = "Updated order status from $oldStatus to $newStatus"
            )
        )
    }

    // --- REFERRAL QUALIFICATION & MILESTONE REWARDS ---
    private suspend fun checkAndRewardQualifyingReferral(memberCmtId: String) {
        val referral = dao.getReferralByNewMember(memberCmtId) ?: return
        if (referral.status == "SUCCESSFUL") return

        val settings = dao.getStoreSettings() ?: StoreSettingsEntity()
        val basePoints = settings.pointsPerQualifyingReferral.coerceAtLeast(1)

        val currentSuccessCount = dao.getSuccessfulReferralCount(referral.referrerCmtId) + 1
        val milestoneBonus = when (currentSuccessCount) {
            5 -> settings.milestone5Bonus
            10 -> settings.milestone10Bonus
            20 -> settings.milestone20Bonus
            else -> 0
        }
        val totalAwarded = basePoints + milestoneBonus

        dao.updateReferral(
            referral.copy(
                status = "SUCCESSFUL",
                pointsAwarded = totalAwarded
            )
        )

        val referrerWallet = dao.getWallet(referral.referrerCmtId)
            ?: WalletEntity(userCmtId = referral.referrerCmtId)
        dao.insertOrUpdateWallet(
            referrerWallet.copy(
                referralPoints = referrerWallet.referralPoints + totalAwarded,
                updatedAt = System.currentTimeMillis()
            )
        )

        dao.insertNotification(
            NotificationEntity(
                targetCmtId = referral.referrerCmtId,
                title = "Referral Reward! +$totalAwarded Referral Point(s)",
                message = "${referral.newMemberName} ($memberCmtId) completed their qualifying action! You earned +$totalAwarded Referral Point(s).",
                type = "REFERRAL"
            )
        )
    }

    // --- CHAT SYSTEM ---
    suspend fun sendChatMessage(
        senderCmtId: String,
        receiverCmtId: String,
        messageText: String,
        sharedProductId: Long? = null,
        sharedProductName: String? = null,
        sharedOrderNumber: String? = null
    ) {
        if (messageText.isBlank() && sharedProductName == null && sharedOrderNumber == null) return
        val sender = dao.getUserByCmtId(senderCmtId) ?: return
        val receiver = dao.getUserByCmtId(receiverCmtId) ?: return
        val convId = listOf(senderCmtId, receiverCmtId).sorted().joinToString("_")

        dao.insertChatMessage(
            ChatMessageEntity(
                conversationId = convId,
                senderCmtId = senderCmtId,
                senderName = sender.fullName,
                senderRole = sender.role,
                receiverCmtId = receiverCmtId,
                receiverName = receiver.fullName,
                receiverRole = receiver.role,
                messageText = messageText.trim(),
                sharedProductId = sharedProductId,
                sharedProductName = sharedProductName,
                sharedOrderNumber = sharedOrderNumber
            )
        )
        dao.insertNotification(
            NotificationEntity(
                targetCmtId = receiverCmtId,
                title = "New Message from ${sender.fullName}",
                message = messageText.trim().ifBlank { "Shared a product/order reference with you." },
                type = "CHAT"
            )
        )
    }

    // --- REVIEWS ---
    suspend fun submitReview(userCmtId: String, productId: Long, rating: Int, comment: String) {
        val user = dao.getUserByCmtId(userCmtId) ?: return
        if (comment.isBlank()) return
        dao.insertReview(
            ReviewEntity(
                productId = productId,
                userCmtId = userCmtId,
                userName = user.fullName,
                rating = rating.coerceIn(1, 5),
                comment = comment.trim(),
                isApproved = true
            )
        )
        val prod = dao.getProductById(productId)
        if (prod != null) {
            val newCount = prod.reviewCount + 1
            val newRating = ((prod.rating * prod.reviewCount) + rating) / newCount
            dao.updateProduct(prod.copy(reviewCount = newCount, rating = (newRating * 10).roundToLong() / 10.0))
        }
    }

    suspend fun deleteReview(adminCmtId: String, reviewId: Long) {
        dao.deleteReview(reviewId)
        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = adminCmtId,
                actorRole = "OWNER",
                actionType = "REVIEW_MODERATED",
                targetInfo = "Review #$reviewId",
                details = "Removed review during moderation."
            )
        )
    }

    // --- OWNER USER & SETTINGS MANAGEMENT ---
    suspend fun adminUpdateUserRoleAndStatus(
        adminCmtId: String,
        targetUser: UserEntity,
        newRole: String,
        newStatus: String
    ) {
        dao.updateUser(targetUser.copy(role = newRole, accountStatus = newStatus))
        dao.insertNotification(
            NotificationEntity(
                targetCmtId = targetUser.cmtId,
                title = "Account Updated by CMT Owner",
                message = "Your account role is now $newRole and status is $newStatus.",
                type = "AUTH"
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = adminCmtId,
                actorRole = "OWNER",
                actionType = "USER_UPDATED",
                targetInfo = "${targetUser.cmtId} (${targetUser.fullName})",
                details = "Changed role=${newRole}, status=${newStatus}"
            )
        )
    }

    suspend fun updateUserProfile(
        user: UserEntity,
        fullName: String,
        mobile: String,
        email: String
    ) {
        dao.updateUser(
            user.copy(
                fullName = fullName.trim().ifBlank { user.fullName },
                mobile = mobile.trim().ifBlank { user.mobile },
                email = email.trim().ifBlank { user.email }
            )
        )
    }

    suspend fun adminUpdateStoreSettings(adminCmtId: String, newSettings: StoreSettingsEntity) {
        dao.saveStoreSettings(newSettings.copy(id = 1))
        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = adminCmtId,
                actorRole = "OWNER",
                actionType = "SETTINGS_UPDATED",
                targetInfo = "StoreSettings",
                details = "Updated Payment (${newSettings.paymentMethodName}: ${newSettings.paymentAccountName} - ${newSettings.paymentAccountNumber}), Rate=${newSettings.pkrToCoinRate}, ResellerDeposit=${newSettings.resellerSecurityDepositPkr}"
            )
        )
    }

    suspend fun adminSendBroadcastAnnouncement(adminCmtId: String, title: String, message: String) {
        if (title.isBlank() || message.isBlank()) return
        dao.insertNotification(
            NotificationEntity(
                targetCmtId = "ALL",
                title = title.trim(),
                message = message.trim(),
                type = "ANNOUNCEMENT"
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = adminCmtId,
                actorRole = "OWNER",
                actionType = "BROADCAST_ANNOUNCEMENT",
                targetInfo = "ALL_USERS",
                details = "${title.trim()}: ${message.trim()}"
            )
        )
    }

    suspend fun markAllNotificationsRead(cmtId: String) {
        dao.markNotificationsRead(cmtId)
    }
}
