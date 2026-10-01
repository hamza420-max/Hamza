package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["cmtId"], unique = true), Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cmtId: String, // e.g., CMT100001
    val fullName: String,
    val username: String,
    val mobile: String,
    val email: String,
    val passwordHash: String,
    val authProvider: String = "PASSWORD", // PASSWORD or GOOGLE
    val role: String = "MEMBER", // MEMBER, SELLER, RESELLER, OWNER
    val accountStatus: String = "ACTIVE", // ACTIVE, SUSPENDED, BLOCKED
    val referralLink: String, // e.g., cmtstore.com/register?ref=CMT100001
    val referredByCmtId: String? = null,
    val profilePictureUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val userCmtId: String,
    val currentCoins: Long = 0L,
    val totalDepositedPkr: Long = 0L,
    val totalSpentCoins: Long = 0L,
    val pendingDepositCoins: Long = 0L,
    val pendingWithdrawalCoins: Long = 0L,
    val securityDepositPkr: Long = 0L, // Held separately from normal spendable coins
    val referralPoints: Int = 0,
    val totalEarningsCoins: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val parentCategoryName: String? = null, // null = main category, non-null = subcategory
    val iconKey: String = "devices",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "shops",
    indices = [Index(value = ["ownerCmtId"], unique = true)]
)
data class ShopEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerCmtId: String,
    val shopName: String,
    val shopDescription: String,
    val contactInfo: String,
    val location: String,
    val shopLogoUri: String? = null,
    val shopProfileImageUri: String? = null,
    val shopBannerUri: String? = null,
    val rating: Double = 4.9,
    val totalSalesCount: Int = 0,
    val isVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productCode: String,
    val name: String,
    val category: String,
    val subcategory: String = "General",
    val sellerCmtId: String,
    val shopName: String,
    val pricePkr: Long,
    val coinPrice: Long,
    val discountPercent: Int = 0,
    val stock: Int = 25,
    val description: String,
    val specifications: String,
    val variations: String, // e.g. "Matte Black, Titanium Gray | 65W, 100W"
    val shippingInfo: String = "Fast Nationwide Delivery (2-3 Business Days)",
    val imageKey: String = "charger", // maps to high-detail icon/visual or custom URI
    val customImageUri: String? = null,
    val videoUri: String? = null,
    val rating: Double = 4.8,
    val reviewCount: Int = 12,
    val status: String = "APPROVED", // PENDING_REVIEW, APPROVED, REJECTED, HIDDEN
    val isFeatured: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userCmtId: String,
    val productId: Long,
    val selectedVariation: String = "Standard",
    val quantity: Int = 1,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wishlist_items")
data class WishlistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userCmtId: String,
    val productId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String, // e.g., ORD-CMT-1001
    val buyerCmtId: String,
    val buyerName: String,
    val buyerPhone: String,
    val buyerAddress: String,
    val buyerCity: String,
    val deliveryInfo: String,
    val orderNotes: String,
    val sellerCmtId: String,
    val shopName: String,
    val itemsSummary: String,
    val productIdsCsv: String,
    val totalQuantity: Int,
    val totalPricePkr: Long,
    val totalCoinsPaid: Long,
    val status: String = "PENDING", // PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, COMPLETED, CANCELLED, RETURNED, REFUNDED
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionCode: String, // e.g., TXN-50001
    val userCmtId: String,
    val userName: String = "",
    val type: String, // DEPOSIT, WITHDRAWAL, PURCHASE, SALE_EARNING, RESELLER_SECURITY_DEPOSIT, REFERRAL_REWARD, ADMIN_CREDIT, REFUND
    val amountPkr: Long,
    val coins: Long,
    val status: String, // PENDING, PROCESSING, APPROVED, COMPLETED, REJECTED, CANCELLED
    val paymentMethod: String = "EasyPaisa",
    val accountName: String = "",
    val accountNumber: String = "",
    val referenceId: String = "",
    val proofImageUri: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reseller_applications")
data class ResellerApplicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val applicantCmtId: String,
    val applicantName: String,
    val applicantMobile: String,
    val proposedShopName: String,
    val shopDescription: String,
    val businessCity: String,
    val securityDepositPkr: Long,
    val paymentMethod: String = "EasyPaisa",
    val paymentAccountName: String = "Hamza Akram",
    val paymentAccountNumber: String = "0327-8704237",
    val referenceId: String,
    val proofImageUri: String? = null,
    val status: String = "PENDING_APPROVAL", // PENDING_APPROVAL, APPROVED, REJECTED, INFO_REQUESTED, SUSPENDED, ACTIVE
    val adminNotes: String = "",
    val submittedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null
)

@Entity(tableName = "referrals")
data class ReferralEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referrerCmtId: String,
    val newMemberCmtId: String,
    val newMemberName: String,
    val registrationDate: Long = System.currentTimeMillis(),
    val qualifyingCondition: String = "Complete first approved deposit or order",
    val status: String = "PENDING", // PENDING, SUCCESSFUL, FLAGGED_ABUSE
    val pointsAwarded: Int = 0
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val userCmtId: String,
    val userName: String,
    val rating: Int, // 1..5
    val comment: String,
    val reviewImageUri: String? = null,
    val isApproved: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String, // sorted pair e.g. CMT100001_CMT100002
    val senderCmtId: String,
    val senderName: String,
    val senderRole: String,
    val receiverCmtId: String,
    val receiverName: String,
    val receiverRole: String,
    val messageText: String,
    val sharedProductId: Long? = null,
    val sharedProductName: String? = null,
    val sharedOrderNumber: String? = null,
    val attachmentUri: String? = null,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetCmtId: String, // specific cmtId, "ALL", or "OWNER"
    val title: String,
    val message: String,
    val type: String, // AUTH, DEPOSIT, WITHDRAWAL, RESELLER, PRODUCT, ORDER, CHAT, REFERRAL, ANNOUNCEMENT
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "store_settings")
data class StoreSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val storeName: String = "Chauhan Mobile Traders",
    val shortBrand: String = "CMT",
    val paymentMethodName: String = "EasyPaisa",
    val paymentAccountName: String = "Hamza Akram",
    val paymentAccountNumber: String = "0327-8704237",
    val pkrToCoinRate: Double = 1.0, // 1 PKR = 1 Coin
    val minDepositPkr: Long = 100L,
    val maxDepositPkr: Long = 500000L,
    val minWithdrawalCoins: Long = 500L,
    val maxWithdrawalCoins: Long = 250000L,
    val resellerSecurityDepositPkr: Long = 20000L,
    val treatSecurityDepositAsSpendable: Boolean = false,
    val pointsPerQualifyingReferral: Int = 1,
    val referralQualifyingCondition: String = "First Approved Deposit or Completed Order",
    val milestone5Bonus: Int = 5,
    val milestone10Bonus: Int = 10,
    val milestone20Bonus: Int = 20,
    val enableGoogleAuth: Boolean = true,
    val preventSelfAndDuplicateReferral: Boolean = true,
    val announcementBanner: String = "Welcome to CMT! Deposit via EasyPaisa (Hamza Akram - 0327-8704237) for 1:1 Coins or Apply for Reseller today!"
)

@Entity(tableName = "promotions")
data class PromotionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subtitle: String,
    val promoCode: String,
    val discountPercent: Int,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actorCmtId: String,
    val actorRole: String,
    val actionType: String,
    val targetInfo: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
