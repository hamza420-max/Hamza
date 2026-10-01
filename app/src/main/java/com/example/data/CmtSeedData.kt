package com.example.data

object CmtSeedData {

    suspend fun seedIfNeeded(dao: CmtDao) {
        if (dao.getUserCount() > 0) return

        val now = System.currentTimeMillis()
        val defaultPass = CmtDatabase.hashPassword("123456")

        // 1. Store Settings (Centralized Payment, Coin & Referral Config)
        dao.saveStoreSettings(
            StoreSettingsEntity(
                id = 1,
                storeName = "Chauhan Mobile Traders",
                shortBrand = "CMT",
                paymentMethodName = "EasyPaisa",
                paymentAccountName = "Hamza Akram",
                paymentAccountNumber = "0327-8704237",
                pkrToCoinRate = 1.0,
                minDepositPkr = 100L,
                maxDepositPkr = 500000L,
                minWithdrawalCoins = 500L,
                maxWithdrawalCoins = 250000L,
                resellerSecurityDepositPkr = 20000L,
                treatSecurityDepositAsSpendable = false,
                pointsPerQualifyingReferral = 1,
                referralQualifyingCondition = "First Approved Deposit or Completed Order",
                milestone5Bonus = 5,
                milestone10Bonus = 10,
                milestone20Bonus = 20,
                enableGoogleAuth = true,
                preventSelfAndDuplicateReferral = true,
                announcementBanner = "Official CMT Marketplace • Deposit via EasyPaisa (Hamza Akram - 0327-8704237) • 1 PKR = 1 Coin"
            )
        )

        // 2. Initial Users across all Roles (Owner, Reseller, Seller, Member)
        val ownerUser = UserEntity(
            cmtId = "CMT100000",
            fullName = "Hamza Akram (CMT Owner)",
            username = "admin",
            mobile = "0327-8704237",
            email = "owner@cmtstore.com",
            passwordHash = defaultPass,
            authProvider = "PASSWORD",
            role = "OWNER",
            accountStatus = "ACTIVE",
            referralLink = "cmtstore.com/register?ref=CMT100000",
            createdAt = now - 86_400_000L * 30
        )
        val resellerUser = UserEntity(
            cmtId = "CMT100001",
            fullName = "Hamza Mobile Accessories",
            username = "hamza_reseller",
            mobile = "0327-8704237",
            email = "hamza@cmtstore.com",
            passwordHash = defaultPass,
            authProvider = "PASSWORD",
            role = "RESELLER",
            accountStatus = "ACTIVE",
            referralLink = "cmtstore.com/register?ref=CMT100001",
            referredByCmtId = "CMT100000",
            createdAt = now - 86_400_000L * 20
        )
        val memberUser = UserEntity(
            cmtId = "CMT100002",
            fullName = "Ali Raza Chauhan",
            username = "aliraza",
            mobile = "0300-4519823",
            email = "ali.raza@gmail.com",
            passwordHash = defaultPass,
            authProvider = "PASSWORD",
            role = "MEMBER",
            accountStatus = "ACTIVE",
            referralLink = "cmtstore.com/register?ref=CMT100002",
            referredByCmtId = "CMT100001",
            createdAt = now - 86_400_000L * 10
        )
        val applicantUser = UserEntity(
            cmtId = "CMT100003",
            fullName = "Usman Tariq Traders",
            username = "usman_mobile",
            mobile = "0321-9988112",
            email = "usman@traders.pk",
            passwordHash = defaultPass,
            authProvider = "GOOGLE",
            role = "MEMBER",
            accountStatus = "ACTIVE",
            referralLink = "cmtstore.com/register?ref=CMT100003",
            referredByCmtId = "CMT100001",
            createdAt = now - 86_400_000L * 4
        )

        dao.insertUser(ownerUser)
        dao.insertUser(resellerUser)
        dao.insertUser(memberUser)
        dao.insertUser(applicantUser)

        // 3. Wallets (Demonstrating separate Security Deposit vs Spendable Coin Balance)
        dao.insertOrUpdateWallet(
            WalletEntity(
                userCmtId = "CMT100000",
                currentCoins = 250000L,
                totalDepositedPkr = 250000L,
                totalSpentCoins = 12000L,
                securityDepositPkr = 20000L,
                referralPoints = 12,
                totalEarningsCoins = 185000L
            )
        )
        dao.insertOrUpdateWallet(
            WalletEntity(
                userCmtId = "CMT100001",
                currentCoins = 18500L,
                totalDepositedPkr = 25000L,
                totalSpentCoins = 6500L,
                pendingWithdrawalCoins = 2500L,
                securityDepositPkr = 20000L, // Held separately as Reseller Security Deposit!
                referralPoints = 4,
                totalEarningsCoins = 42800L
            )
        )
        dao.insertOrUpdateWallet(
            WalletEntity(
                userCmtId = "CMT100002",
                currentCoins = 5000L,
                totalDepositedPkr = 6500L,
                totalSpentCoins = 1500L,
                pendingDepositCoins = 5000L,
                securityDepositPkr = 0L,
                referralPoints = 1,
                totalEarningsCoins = 0L
            )
        )
        dao.insertOrUpdateWallet(
            WalletEntity(
                userCmtId = "CMT100003",
                currentCoins = 1200L,
                totalDepositedPkr = 1200L,
                totalSpentCoins = 0L,
                pendingDepositCoins = 0L,
                securityDepositPkr = 0L,
                referralPoints = 0,
                totalEarningsCoins = 0L
            )
        )

        // 4. All 22+ Mobile Accessories Categories & Subcategories
        val categories = listOf(
            CategoryEntity(name = "Mobile Covers", parentCategoryName = null, iconKey = "cover"),
            CategoryEntity(name = "Screen Protectors", parentCategoryName = null, iconKey = "screen"),
            CategoryEntity(name = "Chargers", parentCategoryName = null, iconKey = "charger"),
            CategoryEntity(name = "Fast Chargers", parentCategoryName = "Chargers", iconKey = "fast_charger"),
            CategoryEntity(name = "Charging Cables", parentCategoryName = null, iconKey = "cable"),
            CategoryEntity(name = "USB Cables", parentCategoryName = "Charging Cables", iconKey = "usb"),
            CategoryEntity(name = "Type-C Accessories", parentCategoryName = null, iconKey = "type_c"),
            CategoryEntity(name = "Lightning Accessories", parentCategoryName = null, iconKey = "lightning"),
            CategoryEntity(name = "Power Banks", parentCategoryName = null, iconKey = "power_bank"),
            CategoryEntity(name = "Earphones", parentCategoryName = null, iconKey = "earphones"),
            CategoryEntity(name = "Wireless Earbuds", parentCategoryName = null, iconKey = "earbuds"),
            CategoryEntity(name = "Bluetooth Speakers", parentCategoryName = null, iconKey = "speaker"),
            CategoryEntity(name = "Mobile Stands", parentCategoryName = null, iconKey = "stand"),
            CategoryEntity(name = "Car Holders", parentCategoryName = null, iconKey = "car_holder"),
            CategoryEntity(name = "Mobile Holders", parentCategoryName = "Mobile Stands", iconKey = "holder"),
            CategoryEntity(name = "OTG Accessories", parentCategoryName = null, iconKey = "otg"),
            CategoryEntity(name = "Memory Cards", parentCategoryName = null, iconKey = "sd_card"),
            CategoryEntity(name = "Card Readers", parentCategoryName = "Memory Cards", iconKey = "reader"),
            CategoryEntity(name = "Smart Watches", parentCategoryName = null, iconKey = "watch"),
            CategoryEntity(name = "Gaming Accessories", parentCategoryName = null, iconKey = "gaming"),
            CategoryEntity(name = "Phone Cleaning Accessories", parentCategoryName = null, iconKey = "cleaning"),
            CategoryEntity(name = "Other Mobile Accessories", parentCategoryName = null, iconKey = "other")
        )
        dao.insertCategories(categories)

        // 5. Reseller Shop Profile
        dao.insertOrUpdateShop(
            ShopEntity(
                ownerCmtId = "CMT100001",
                shopName = "Hamza Mobile Accessories",
                shopDescription = "Verified CMT Wholesale & Retail Partner specializing in 65W/100W GaN Fast Chargers, ANC Earbuds, MagSafe Covers & Braided Type-C Cables.",
                contactInfo = "0327-8704237 | hamza@cmtstore.com",
                location = "Hall Road Mobile Market, Lahore",
                rating = 4.9,
                totalSalesCount = 148,
                isVerified = true
            )
        )
        dao.insertOrUpdateShop(
            ShopEntity(
                ownerCmtId = "CMT100000",
                shopName = "CMT Official Flagship Store",
                shopDescription = "Official Chauhan Mobile Traders Flagship Inventory with 100% authentic warranty and direct Coin checkout.",
                contactInfo = "0327-8704237 | owner@cmtstore.com",
                location = "CMT Headquarters, Lahore",
                rating = 5.0,
                totalSalesCount = 520,
                isVerified = true
            )
        )

        // 6. Rich Mobile Accessories Products (Approved + 1 Pending Review for Admin Demo)
        val products = listOf(
            ProductEntity(
                productCode = "CMT-PRD-101",
                name = "65W GaN Ultra Fast Charger Dual Type-C + USB",
                category = "Fast Chargers",
                subcategory = "GaN Tech",
                sellerCmtId = "CMT100001",
                shopName = "Hamza Mobile Accessories",
                pricePkr = 1500L,
                coinPrice = 1500L,
                discountPercent = 15,
                stock = 45,
                description = "Next-generation 65W Gallium Nitride (GaN) PD 3.0 & QC 4.0+ wall charger. Charges smartphones from 0% to 80% in 28 minutes with smart thermal protection.",
                specifications = "Output: 65W Max | Ports: 2x USB-C, 1x USB-A | Protocol: PD3.0, PPS, QC4+ | Plug: UK/PK 3-Pin",
                variations = "Matte Black, Arctic White | 45W, 65W, 100W",
                imageKey = "fast_charger",
                rating = 4.9,
                reviewCount = 38,
                status = "APPROVED",
                isFeatured = true
            ),
            ProductEntity(
                productCode = "CMT-PRD-102",
                name = "ANC Pro Wireless Earbuds with LED Coin Display",
                category = "Wireless Earbuds",
                subcategory = "Active Noise Cancelling",
                sellerCmtId = "CMT100001",
                shopName = "Hamza Mobile Accessories",
                pricePkr = 3200L,
                coinPrice = 3200L,
                discountPercent = 20,
                stock = 30,
                description = "38dB Active Noise Cancellation wireless earbuds with 36-hour total battery life, low-latency 45ms gaming mode, and crystal-clear quad-mic ENC calls.",
                specifications = "Bluetooth: 5.4 | ANC: -38dB | Battery: 36 Hours | Charging: Type-C & Wireless",
                variations = "Obsidian Black, Cyber Silver, Champagne Gold",
                imageKey = "earbuds",
                rating = 4.8,
                reviewCount = 54,
                status = "APPROVED",
                isFeatured = true
            ),
            ProductEntity(
                productCode = "CMT-PRD-103",
                name = "20,000mAh 22.5W SuperCharge Power Bank Digital LED",
                category = "Power Banks",
                subcategory = "High Capacity",
                sellerCmtId = "CMT100000",
                shopName = "CMT Official Flagship Store",
                pricePkr = 4200L,
                coinPrice = 4200L,
                discountPercent = 10,
                stock = 60,
                description = "Airline-approved 20,000mAh lithium-polymer power bank with built-in braided Type-C & Lightning cables, 22.5W bi-directional fast charging, and precision LED percentage screen.",
                specifications = "Capacity: 20,000mAh | Output: 22.5W SCP + 20W PD | Display: Digital % LED | Weight: 340g",
                variations = "Space Gray, Midnight Navy | 10000mAh, 20000mAh",
                imageKey = "power_bank",
                rating = 4.9,
                reviewCount = 62,
                status = "APPROVED",
                isFeatured = true
            ),
            ProductEntity(
                productCode = "CMT-PRD-104",
                name = "Kevlar Carbon MagSafe Armor Mobile Cover",
                category = "Mobile Covers",
                subcategory = "MagSafe Shockproof",
                sellerCmtId = "CMT100001",
                shopName = "Hamza Mobile Accessories",
                pricePkr = 1250L,
                coinPrice = 1250L,
                discountPercent = 12,
                stock = 85,
                description = "Aerospace aramid carbon-fiber texture case with N52 neodymium MagSafe ring, raised alloy camera bezel guard, and 10ft military drop certification.",
                specifications = "Material: Aramid + TPU | Magnet: 38x N52 Ring | Drop Protection: MIL-STD-810G",
                variations = "iPhone 15/16 Pro Max, Samsung S24 Ultra, Xiaomi 14 | Carbon Black, Titanium Blue",
                imageKey = "cover",
                rating = 4.7,
                reviewCount = 29,
                status = "APPROVED",
                isFeatured = false
            ),
            ProductEntity(
                productCode = "CMT-PRD-105",
                name = "100W Braided Nylon Type-C to Type-C E-Marker Cable (2m)",
                category = "Type-C Accessories",
                subcategory = "Braided Cables",
                sellerCmtId = "CMT100001",
                shopName = "Hamza Mobile Accessories",
                pricePkr = 650L,
                coinPrice = 650L,
                discountPercent = 5,
                stock = 120,
                description = "Smart E-Marker chip 100W (20V/5A) fast-charging cable with zinc-alloy connectors and real-time wattage digital display.",
                specifications = "Power: 100W (5A) | Data: 480Mbps | Length: 2 Meters | Jacket: Double-Braided Nylon",
                variations = "1 Meter, 2 Meter | Black/Gold, Cyan/Black",
                imageKey = "type_c",
                rating = 4.8,
                reviewCount = 41,
                status = "APPROVED",
                isFeatured = false
            ),
            ProductEntity(
                productCode = "CMT-PRD-106",
                name = "9H Sapphire Privacy Tempered Glass Screen Protector",
                category = "Screen Protectors",
                subcategory = "Privacy Glass",
                sellerCmtId = "CMT100000",
                shopName = "CMT Official Flagship Store",
                pricePkr = 550L,
                coinPrice = 550L,
                discountPercent = 0,
                stock = 200,
                description = "28-degree anti-spy privacy tempered glass with auto-alignment installation frame and oleophobic anti-fingerprint coating.",
                specifications = "Hardness: 9H | Thickness: 0.33mm | Coating: Hydrophobic & Oleophobic | Edge: 3D Curved",
                variations = "iPhone Series, Samsung Galaxy S Series, Infinix/Tecno | Clear HD, Privacy Matte",
                imageKey = "screen",
                rating = 4.7,
                reviewCount = 19,
                status = "APPROVED",
                isFeatured = false
            ),
            ProductEntity(
                productCode = "CMT-PRD-107",
                name = "CMT Watch Ultra AMOLED Smart Watch with Bluetooth Calling",
                category = "Smart Watches",
                subcategory = "AMOLED Calling",
                sellerCmtId = "CMT100000",
                shopName = "CMT Official Flagship Store",
                pricePkr = 5800L,
                coinPrice = 5800L,
                discountPercent = 18,
                stock = 22,
                description = "2.1-inch Always-On Super AMOLED display, titanium-finish alloy chassis, rotary crown, heart-rate/SpO2 sensors, and 7-day battery with 3 straps included.",
                specifications = "Display: 2.1\" AMOLED 485x520 | Water Resistance: IP68 | Battery: 450mAh",
                variations = "Titanium Orange, Stealth Black, Starlight Silver",
                imageKey = "watch",
                rating = 4.9,
                reviewCount = 33,
                status = "APPROVED",
                isFeatured = true
            ),
            ProductEntity(
                productCode = "CMT-PRD-108",
                name = "15W MagSafe Auto-Clamping Dashboard & Vent Car Holder",
                category = "Car Holders",
                subcategory = "Wireless Car Mount",
                sellerCmtId = "CMT100001",
                shopName = "Hamza Mobile Accessories",
                pricePkr = 2100L,
                coinPrice = 2100L,
                discountPercent = 10,
                stock = 40,
                description = "360-degree rotatable magnetic wireless charging car mount with blue ambient ring light and super-suction telescopic arm.",
                specifications = "Wireless Output: 15W/10W/7.5W | Mount: Vent + Dashboard Arm | Rotation: 360°",
                variations = "Magnetic Ring Mount, Infrared Auto-Clamp",
                imageKey = "car_holder",
                rating = 4.8,
                reviewCount = 24,
                status = "APPROVED",
                isFeatured = false
            ),
            ProductEntity(
                productCode = "CMT-PRD-109",
                name = "Peltier Semiconductor Mobile Gaming Cooler Fan RGB",
                category = "Gaming Accessories",
                subcategory = "Phone Coolers",
                sellerCmtId = "CMT100001",
                shopName = "Hamza Mobile Accessories",
                pricePkr = 2400L,
                coinPrice = 2400L,
                discountPercent = 15,
                stock = 18,
                description = "Instant semiconductor cooling drop up to -15°C in 10 seconds for PUBG/CODM 120FPS gaming sessions with whisper-quiet 29dB fan and live temperature display.",
                specifications = "Cooling: TEC Peltier 18W | Mount: Magnetic + Back Clip | Noise: <29dB",
                variations = "Clip-On Pro, MagSafe RGB Pro",
                imageKey = "gaming",
                rating = 4.9,
                reviewCount = 31,
                status = "APPROVED",
                isFeatured = false
            ),
            ProductEntity(
                productCode = "CMT-PRD-110",
                name = "20-in-1 Multi-Tool AirPods & Phone Cleaning Kit",
                category = "Phone Cleaning Accessories",
                subcategory = "Maintenance Kits",
                sellerCmtId = "CMT100000",
                shopName = "CMT Official Flagship Store",
                pricePkr = 850L,
                coinPrice = 850L,
                discountPercent = 0,
                stock = 95,
                description = "Complete precision cleaning cylinder with flocking sponge, high-density brush, metal nib, microfiber screen spray, and SIM ejector tool.",
                specifications = "Tools Included: 20 Pieces | Spray Bottle: 10ml Alcohol-Free | Case: ABS Cylinder",
                variations = "White/Gold, Matte Black",
                imageKey = "cleaning",
                rating = 4.6,
                reviewCount = 15,
                status = "APPROVED",
                isFeatured = false
            ),
            ProductEntity(
                productCode = "CMT-PRD-111",
                name = "128GB A2 UHS-I V30 MicroSD Memory Card + USB 3.0 Reader",
                category = "Memory Cards",
                subcategory = "High Speed MicroSD",
                sellerCmtId = "CMT100000",
                shopName = "CMT Official Flagship Store",
                pricePkr = 2600L,
                coinPrice = 2600L,
                discountPercent = 8,
                stock = 50,
                description = "Up to 160MB/s read speed for 4K UHD recording and rapid app loading, bundled with an aluminum OTG & USB 3.0 card reader.",
                specifications = "Capacity: 128GB | Speed Class: U3, V30, A2 | Read: 160MB/s",
                variations = "64GB, 128GB, 256GB",
                imageKey = "sd_card",
                rating = 4.8,
                reviewCount = 27,
                status = "APPROVED",
                isFeatured = false
            ),
            ProductEntity(
                productCode = "CMT-PRD-112",
                name = "MFi Certified PD 27W Braided Lightning to Type-C Cable",
                category = "Lightning Accessories",
                subcategory = "iPhone Fast Cables",
                sellerCmtId = "CMT100001",
                shopName = "Hamza Mobile Accessories",
                pricePkr = 950L,
                coinPrice = 950L,
                discountPercent = 10,
                stock = 70,
                description = "Reinforced Rhodium-plated connector Lightning cable supporting 27W PD fast charge with zero pop-up warnings.",
                specifications = "Length: 1.5m | Power: 27W PD | Bend Lifespan: 25,000+ Bends",
                variations = "1m Silver, 1.5m Gold, 2m Space Black",
                imageKey = "lightning",
                rating = 4.9,
                reviewCount = 14,
                status = "PENDING_REVIEW", // Ready for Admin Review in Owner Panel!
                isFeatured = false
            )
        )
        dao.insertProducts(products)

        // 7. Sample Wallet Transactions (Approved + Pending Deposit + Pending Withdrawal + Security Deposit)
        dao.insertTransaction(
            WalletTransactionEntity(
                transactionCode = "TXN-10001",
                userCmtId = "CMT100001",
                userName = "Hamza Mobile Accessories",
                type = "RESELLER_SECURITY_DEPOSIT",
                amountPkr = 20000L,
                coins = 20000L,
                status = "COMPLETED",
                paymentMethod = "EasyPaisa",
                accountName = "Hamza Akram",
                accountNumber = "0327-8704237",
                referenceId = "EP-9823410021",
                notes = "Verified Reseller Security Deposit (Held in Security Deposit Balance)",
                createdAt = now - 86_400_000L * 19
            )
        )
        dao.insertTransaction(
            WalletTransactionEntity(
                transactionCode = "TXN-10002",
                userCmtId = "CMT100002",
                userName = "Ali Raza Chauhan",
                type = "DEPOSIT",
                amountPkr = 6500L,
                coins = 6500L,
                status = "COMPLETED",
                paymentMethod = "EasyPaisa",
                accountName = "Hamza Akram",
                accountNumber = "0327-8704237",
                referenceId = "EP-7731904422",
                notes = "EasyPaisa Deposit Approved by Admin (+6,500 Coins)",
                createdAt = now - 86_400_000L * 7
            )
        )
        dao.insertTransaction(
            WalletTransactionEntity(
                transactionCode = "TXN-10003",
                userCmtId = "CMT100002",
                userName = "Ali Raza Chauhan",
                type = "PURCHASE",
                amountPkr = 1500L,
                coins = 1500L,
                status = "COMPLETED",
                paymentMethod = "CMT Coin Wallet",
                referenceId = "ORD-CMT-8001",
                notes = "Purchased 65W GaN Ultra Fast Charger",
                createdAt = now - 86_400_000L * 5
            )
        )
        dao.insertTransaction(
            WalletTransactionEntity(
                transactionCode = "TXN-10004",
                userCmtId = "CMT100002",
                userName = "Ali Raza Chauhan",
                type = "DEPOSIT",
                amountPkr = 5000L,
                coins = 5000L,
                status = "PENDING",
                paymentMethod = "EasyPaisa",
                accountName = "Hamza Akram",
                accountNumber = "0327-8704237",
                referenceId = "EP-8845120098",
                notes = "Awaiting Admin verification before crediting 5,000 Coins",
                createdAt = now - 3_600_000L * 2
            )
        )
        dao.insertTransaction(
            WalletTransactionEntity(
                transactionCode = "TXN-10005",
                userCmtId = "CMT100001",
                userName = "Hamza Mobile Accessories",
                type = "WITHDRAWAL",
                amountPkr = 2500L,
                coins = 2500L,
                status = "PENDING",
                paymentMethod = "EasyPaisa",
                accountName = "Hamza Mobile Accessories",
                accountNumber = "0327-8704237",
                referenceId = "WD-REQ-301",
                notes = "Reseller earnings withdrawal request awaiting Admin approval",
                createdAt = now - 3_600_000L * 4
            )
        )

        // 8. Sample Reseller Applications (1 Approved, 1 Pending Approval with 20,000 PKR Security Deposit)
        dao.insertResellerApplication(
            ResellerApplicationEntity(
                applicantCmtId = "CMT100001",
                applicantName = "Hamza Mobile Accessories",
                applicantMobile = "0327-8704237",
                proposedShopName = "Hamza Mobile Accessories",
                shopDescription = "Wholesale fast chargers, cables, and earbuds dealer.",
                businessCity = "Lahore",
                securityDepositPkr = 20000L,
                paymentMethod = "EasyPaisa",
                paymentAccountName = "Hamza Akram",
                paymentAccountNumber = "0327-8704237",
                referenceId = "EP-9823410021",
                status = "ACTIVE",
                adminNotes = "Security deposit verified. Reseller panel activated.",
                submittedAt = now - 86_400_000L * 20,
                reviewedAt = now - 86_400_000L * 19
            )
        )
        dao.insertResellerApplication(
            ResellerApplicationEntity(
                applicantCmtId = "CMT100003",
                applicantName = "Usman Tariq Traders",
                applicantMobile = "0321-9988112",
                proposedShopName = "Usman Smart Accessories Hub",
                shopDescription = "Importer of smart watches, gaming coolers, and tempered glass.",
                businessCity = "Faisalabad",
                securityDepositPkr = 20000L,
                paymentMethod = "EasyPaisa",
                paymentAccountName = "Hamza Akram",
                paymentAccountNumber = "0327-8704237",
                referenceId = "EP-5510928374",
                status = "PENDING_APPROVAL",
                adminNotes = "20,000 PKR EasyPaisa deposit proof submitted; ready for Admin review.",
                submittedAt = now - 3_600_000L * 5
            )
        )

        // 9. Sample Order
        dao.insertOrder(
            OrderEntity(
                orderNumber = "ORD-CMT-8001",
                buyerCmtId = "CMT100002",
                buyerName = "Ali Raza Chauhan",
                buyerPhone = "0300-4519823",
                buyerAddress = "House 42, Block B, Model Town",
                buyerCity = "Lahore",
                deliveryInfo = "Express Courier (Trackable)",
                orderNotes = "Please pack the 65W charger with bubble wrap.",
                sellerCmtId = "CMT100001",
                shopName = "Hamza Mobile Accessories",
                itemsSummary = "1x 65W GaN Ultra Fast Charger Dual Type-C + USB (Matte Black)",
                productIdsCsv = "1",
                totalQuantity = 1,
                totalPricePkr = 1500L,
                totalCoinsPaid = 1500L,
                status = "DELIVERED",
                createdAt = now - 86_400_000L * 5,
                updatedAt = now - 86_400_000L * 2
            )
        )

        // 10. Sample Referrals
        dao.insertReferral(
            ReferralEntity(
                referrerCmtId = "CMT100001",
                newMemberCmtId = "CMT100002",
                newMemberName = "Ali Raza Chauhan",
                registrationDate = now - 86_400_000L * 10,
                qualifyingCondition = "First Approved Deposit or Completed Order",
                status = "SUCCESSFUL",
                pointsAwarded = 1
            )
        )
        dao.insertReferral(
            ReferralEntity(
                referrerCmtId = "CMT100001",
                newMemberCmtId = "CMT100003",
                newMemberName = "Usman Tariq Traders",
                registrationDate = now - 86_400_000L * 4,
                qualifyingCondition = "First Approved Deposit or Completed Order",
                status = "PENDING",
                pointsAwarded = 0
            )
        )

        // 11. Sample Reviews
        dao.insertReview(
            ReviewEntity(
                productId = 1L,
                userCmtId = "CMT100002",
                userName = "Ali Raza Chauhan",
                rating = 5,
                comment = "Super fast charging! My S24 Ultra triggers Super Fast Charging 2.0 immediately. Authentic CMT reseller product.",
                isApproved = true,
                createdAt = now - 86_400_000L * 2
            )
        )
        dao.insertReview(
            ReviewEntity(
                productId = 2L,
                userCmtId = "CMT100002",
                userName = "Ali Raza Chauhan",
                rating = 5,
                comment = "ANC blocks out traffic noise completely and battery lasts all week.",
                isApproved = true,
                createdAt = now - 86_400_000L * 1
            )
        )

        // 12. Sample Real-Time Chat Messages
        val conv1 = "CMT100001_CMT100002"
        dao.insertChatMessage(
            ChatMessageEntity(
                conversationId = conv1,
                senderCmtId = "CMT100002",
                senderName = "Ali Raza Chauhan",
                senderRole = "MEMBER",
                receiverCmtId = "CMT100001",
                receiverName = "Hamza Mobile Accessories",
                receiverRole = "RESELLER",
                messageText = "Salam! Does the 65W GaN Charger support Samsung 45W PPS fast charging?",
                sharedProductId = 1L,
                sharedProductName = "65W GaN Ultra Fast Charger Dual Type-C + USB",
                isRead = true,
                timestamp = now - 3_600_000L * 12
            )
        )
        dao.insertChatMessage(
            ChatMessageEntity(
                conversationId = conv1,
                senderCmtId = "CMT100001",
                senderName = "Hamza Mobile Accessories",
                senderRole = "RESELLER",
                receiverCmtId = "CMT100002",
                receiverName = "Ali Raza Chauhan",
                receiverRole = "MEMBER",
                messageText = "Walaikum Assalam! Yes, Port C1 supports full PPS 45W & 65W PD. Ready in stock for instant dispatch!",
                sharedOrderNumber = "ORD-CMT-8001",
                isRead = false,
                timestamp = now - 3_600_000L * 11
            )
        )

        val convSupport = "CMT100000_CMT100002"
        dao.insertChatMessage(
            ChatMessageEntity(
                conversationId = convSupport,
                senderCmtId = "CMT100000",
                senderName = "CMT Support & Owner",
                senderRole = "OWNER",
                receiverCmtId = "CMT100002",
                receiverName = "Ali Raza Chauhan",
                receiverRole = "MEMBER",
                messageText = "Welcome to Chauhan Mobile Traders (CMT)! Message us anytime for deposit verification or order support.",
                isRead = false,
                timestamp = now - 3_600_000L * 24
            )
        )

        // 13. Notifications
        dao.insertNotification(
            NotificationEntity(
                targetCmtId = "ALL",
                title = "Welcome to Chauhan Mobile Traders (CMT)",
                message = "Shop 22+ categories of mobile accessories using CMT Coins. Deposit via EasyPaisa (Hamza Akram - 0327-8704237).",
                type = "ANNOUNCEMENT",
                timestamp = now - 86_400_000L * 3
            )
        )
        dao.insertNotification(
            NotificationEntity(
                targetCmtId = "CMT100002",
                title = "Deposit Submitted (Pending Verification)",
                message = "Your 5,000 PKR EasyPaisa deposit (Ref: EP-8845120098) is pending Admin review.",
                type = "DEPOSIT",
                timestamp = now - 3_600_000L * 2
            )
        )
        dao.insertNotification(
            NotificationEntity(
                targetCmtId = "OWNER",
                title = "New Reseller Application & 20,000 PKR Deposit",
                message = "Usman Tariq Traders (CMT100003) submitted a Reseller Application with 20,000 PKR EasyPaisa proof.",
                type = "RESELLER",
                timestamp = now - 3_600_000L * 5
            )
        )

        // 14. Promotions
        dao.insertPromotion(
            PromotionEntity(
                title = "Mega Fast Charger & Cable Fest",
                subtitle = "Up to 20% Coin Discount on GaN Chargers & Braided Type-C Cables",
                promoCode = "CMTFAST20",
                discountPercent = 20,
                isActive = true
            )
        )

        // 15. Audit Logs
        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = "CMT100000",
                actorRole = "OWNER",
                actionType = "RESELLER_APPROVED",
                targetInfo = "CMT100001 (Hamza Mobile Accessories)",
                details = "Verified 20,000 PKR EasyPaisa security deposit (Ref: EP-9823410021) and activated Reseller Panel.",
                timestamp = now - 86_400_000L * 19
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                actorCmtId = "CMT100000",
                actorRole = "OWNER",
                actionType = "DEPOSIT_APPROVED",
                targetInfo = "TXN-10002 -> CMT100002",
                details = "Approved 6,500 PKR EasyPaisa deposit and credited +6,500 Coins.",
                timestamp = now - 86_400_000L * 7
            )
        )
    }
}
