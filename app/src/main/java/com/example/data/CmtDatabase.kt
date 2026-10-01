package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.security.MessageDigest

@Database(
    entities = [
        UserEntity::class,
        WalletEntity::class,
        CategoryEntity::class,
        ShopEntity::class,
        ProductEntity::class,
        CartItemEntity::class,
        WishlistEntity::class,
        OrderEntity::class,
        WalletTransactionEntity::class,
        ResellerApplicationEntity::class,
        ReferralEntity::class,
        ReviewEntity::class,
        ChatMessageEntity::class,
        NotificationEntity::class,
        StoreSettingsEntity::class,
        PromotionEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CmtDatabase : RoomDatabase() {
    abstract fun cmtDao(): CmtDao

    companion object {
        @Volatile
        private var INSTANCE: CmtDatabase? = null

        fun getDatabase(context: Context): CmtDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CmtDatabase::class.java,
                    "cmt_marketplace_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256")
                .digest(("CMT_SALT_2026_$password").toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
