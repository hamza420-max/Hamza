package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CmtDao {

    // --- USERS ---
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE cmtId = :cmtId LIMIT 1")
    fun observeUserByCmtId(cmtId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE cmtId = :cmtId LIMIT 1")
    suspend fun getUserByCmtId(cmtId: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:identifier) OR LOWER(cmtId) = LOWER(:identifier) OR LOWER(email) = LOWER(:identifier) LIMIT 1")
    suspend fun findUserForLogin(identifier: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    // --- WALLETS ---
    @Query("SELECT * FROM wallets WHERE userCmtId = :cmtId LIMIT 1")
    fun observeWallet(cmtId: String): Flow<WalletEntity?>

    @Query("SELECT * FROM wallets WHERE userCmtId = :cmtId LIMIT 1")
    suspend fun getWallet(cmtId: String): WalletEntity?

    @Query("SELECT * FROM wallets")
    fun observeAllWallets(): Flow<List<WalletEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWallet(wallet: WalletEntity)

    // --- CATEGORIES ---
    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun observeAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: Long)

    // --- SHOPS ---
    @Query("SELECT * FROM shops ORDER BY createdAt DESC")
    fun observeAllShops(): Flow<List<ShopEntity>>

    @Query("SELECT * FROM shops WHERE ownerCmtId = :ownerCmtId LIMIT 1")
    fun observeShopByOwner(ownerCmtId: String): Flow<ShopEntity?>

    @Query("SELECT * FROM shops WHERE ownerCmtId = :ownerCmtId LIMIT 1")
    suspend fun getShopByOwner(ownerCmtId: String): ShopEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateShop(shop: ShopEntity): Long

    // --- PRODUCTS ---
    @Query("SELECT * FROM products ORDER BY isFeatured DESC, createdAt DESC")
    fun observeAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Long)

    // --- CART ---
    @Query("SELECT * FROM cart_items WHERE userCmtId = :cmtId ORDER BY addedAt DESC")
    fun observeCartItems(cmtId: String): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE userCmtId = :cmtId AND productId = :productId LIMIT 1")
    suspend fun getCartItem(cmtId: String, productId: Long): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: Long)

    @Query("DELETE FROM cart_items WHERE userCmtId = :cmtId")
    suspend fun clearCart(cmtId: String)

    // --- WISHLIST ---
    @Query("SELECT * FROM wishlist_items WHERE userCmtId = :cmtId ORDER BY addedAt DESC")
    fun observeWishlist(cmtId: String): Flow<List<WishlistEntity>>

    @Query("SELECT * FROM wishlist_items WHERE userCmtId = :cmtId AND productId = :productId LIMIT 1")
    suspend fun getWishlistItem(cmtId: String, productId: Long): WishlistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(item: WishlistEntity)

    @Query("DELETE FROM wishlist_items WHERE userCmtId = :cmtId AND productId = :productId")
    suspend fun removeWishlistItem(cmtId: String, productId: Long)

    // --- ORDERS ---
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Long): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)

    // --- WALLET TRANSACTIONS (DEPOSITS, WITHDRAWALS, PURCHASES, SECURITY DEPOSITS) ---
    @Query("SELECT * FROM wallet_transactions ORDER BY createdAt DESC")
    fun observeAllTransactions(): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): WalletTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: WalletTransactionEntity): Long

    @Update
    suspend fun updateTransaction(tx: WalletTransactionEntity)

    // --- RESELLER APPLICATIONS ---
    @Query("SELECT * FROM reseller_applications ORDER BY submittedAt DESC")
    fun observeAllResellerApplications(): Flow<List<ResellerApplicationEntity>>

    @Query("SELECT * FROM reseller_applications WHERE id = :id LIMIT 1")
    suspend fun getResellerApplicationById(id: Long): ResellerApplicationEntity?

    @Query("SELECT * FROM reseller_applications WHERE applicantCmtId = :cmtId ORDER BY submittedAt DESC LIMIT 1")
    suspend fun getLatestResellerAppForUser(cmtId: String): ResellerApplicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResellerApplication(app: ResellerApplicationEntity): Long

    @Update
    suspend fun updateResellerApplication(app: ResellerApplicationEntity)

    // --- REFERRALS ---
    @Query("SELECT * FROM referrals ORDER BY registrationDate DESC")
    fun observeAllReferrals(): Flow<List<ReferralEntity>>

    @Query("SELECT * FROM referrals WHERE newMemberCmtId = :newMemberCmtId LIMIT 1")
    suspend fun getReferralByNewMember(newMemberCmtId: String): ReferralEntity?

    @Query("SELECT COUNT(*) FROM referrals WHERE referrerCmtId = :referrerCmtId AND status = 'SUCCESSFUL'")
    suspend fun getSuccessfulReferralCount(referrerCmtId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferral(referral: ReferralEntity): Long

    @Update
    suspend fun updateReferral(referral: ReferralEntity)

    // --- REVIEWS ---
    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun observeAllReviews(): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity): Long

    @Update
    suspend fun updateReview(review: ReviewEntity)

    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun deleteReview(id: Long)

    // --- CHAT MESSAGES ---
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun observeAllChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(msg: ChatMessageEntity): Long

    @Query("UPDATE chat_messages SET isRead = 1 WHERE conversationId = :conversationId AND receiverCmtId = :viewerCmtId")
    suspend fun markConversationRead(conversationId: String, viewerCmtId: String)

    // --- NOTIFICATIONS ---
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun observeAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE targetCmtId = :cmtId OR targetCmtId = 'ALL'")
    suspend fun markNotificationsRead(cmtId: String)

    // --- STORE SETTINGS ---
    @Query("SELECT * FROM store_settings WHERE id = 1 LIMIT 1")
    fun observeStoreSettings(): Flow<StoreSettingsEntity?>

    @Query("SELECT * FROM store_settings WHERE id = 1 LIMIT 1")
    suspend fun getStoreSettings(): StoreSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStoreSettings(settings: StoreSettingsEntity)

    // --- PROMOTIONS ---
    @Query("SELECT * FROM promotions ORDER BY createdAt DESC")
    fun observeAllPromotions(): Flow<List<PromotionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromotion(promo: PromotionEntity)

    @Query("DELETE FROM promotions WHERE id = :id")
    suspend fun deletePromotion(id: Long)

    // --- AUDIT LOGS ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun observeAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)
}
