package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.ProductEntity
import com.example.data.StoreSettingsEntity
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatNumber(value: Long): String {
    return NumberFormat.getNumberInstance(Locale.US).format(value)
}

fun formatTimestamp(ts: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
    return sdf.format(Date(ts))
}

fun copyToClipboard(context: Context, label: String, text: String, onCopied: (String) -> Unit) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    onCopied("Copied $label: $text")
}

fun shareText(context: Context, subject: String, body: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    context.startActivity(Intent.createChooser(intent, "Share via CMT"))
}

@Composable
fun CmtLogoBadge(
    size: Dp = 44.dp,
    showSubtitle: Boolean = true,
    subtitleOverride: String? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = R.drawable.img_cmt_logo),
            contentDescription = "CMT Chauhan Mobile Traders Logo",
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp))
                .border(1.5.dp, CmtGoldPrimary, RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        if (showSubtitle) {
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CMT",
                        style = MaterialTheme.typography.titleLarge,
                        color = CmtGoldBright,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = CmtCyanSecondary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "VERIFIED",
                            style = MaterialTheme.typography.labelSmall,
                            color = CmtCyanBright,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = subtitleOverride ?: "Chauhan Mobile Traders",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CmtTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    rightContent: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            Image(
                painter = painterResource(id = R.drawable.img_cmt_logo),
                contentDescription = "CMT Brand Logo",
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, CmtGoldPrimary, RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                content = rightContent
            )
        }
    }
}

@Composable
fun CoinBalanceChip(
    coins: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = CmtGoldContainer,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, CmtGoldPrimary),
        modifier = modifier
            .defaultMinSize(minHeight = 38.dp)
            .testTag("coin_balance_button")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MonetizationOn,
                contentDescription = "Coin Balance",
                tint = CmtGoldBright,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Coins: ${formatNumber(coins)}",
                style = MaterialTheme.typography.labelLarge,
                color = CmtGoldBright,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val upper = status.uppercase()
    val (bg, fg) = when {
        upper.contains("APPROVED") || upper.contains("COMPLETED") || upper.contains("ACTIVE") || upper.contains("SUCCESSFUL") || upper.contains("DELIVERED") ->
            CmtEmeraldSuccess.copy(alpha = 0.18f) to CmtEmeraldSuccess
        upper.contains("PENDING") || upper.contains("PROCESSING") || upper.contains("CONFIRMED") || upper.contains("SHIPPED") || upper.contains("INFO") ->
            CmtGoldPrimary.copy(alpha = 0.2f) to CmtGoldBright
        upper.contains("REJECTED") || upper.contains("CANCELLED") || upper.contains("SUSPENDED") || upper.contains("BLOCKED") ->
            CmtCoralError.copy(alpha = 0.2f) to CmtCoralError
        else -> CmtCyanSecondary.copy(alpha = 0.2f) to CmtCyanBright
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, fg.copy(alpha = 0.5f))
    ) {
        Text(
            text = status.replace("_", " "),
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

fun categoryIconFor(key: String): ImageVector {
    val lower = key.lowercase()
    return when {
        lower.contains("fast") || lower.contains("charger") -> Icons.Default.Bolt
        lower.contains("earbud") || lower.contains("earphone") -> Icons.Default.Headphones
        lower.contains("power") || lower.contains("bank") -> Icons.Default.BatteryChargingFull
        lower.contains("cover") || lower.contains("case") -> Icons.Default.PhoneAndroid
        lower.contains("screen") || lower.contains("glass") -> Icons.Default.Security
        lower.contains("watch") -> Icons.Default.Watch
        lower.contains("speaker") -> Icons.Default.Speaker
        lower.contains("car") || lower.contains("holder") || lower.contains("stand") -> Icons.Default.DirectionsCar
        lower.contains("gaming") -> Icons.Default.SportsEsports
        lower.contains("sd") || lower.contains("memory") || lower.contains("reader") -> Icons.Default.SdStorage
        lower.contains("clean") -> Icons.Default.CleaningServices
        lower.contains("cable") || lower.contains("usb") || lower.contains("type_c") || lower.contains("lightning") || lower.contains("otg") -> Icons.Default.Cable
        else -> Icons.Default.DevicesOther
    }
}

@Composable
fun ProductVisualBox(
    imageKey: String,
    customImageUri: String?,
    category: String,
    modifier: Modifier = Modifier
) {
    val gradientColors = when {
        imageKey.contains("charger") -> listOf(Color(0xFF0F172A), Color(0xFF1E3A8A), Color(0xFF0284C7))
        imageKey.contains("earbud") -> listOf(Color(0xFF1E1B4B), Color(0xFF311042), Color(0xFF7C3AED))
        imageKey.contains("power") -> listOf(Color(0xFF064E3B), Color(0xFF0F766E), Color(0xFF10B981))
        imageKey.contains("watch") -> listOf(Color(0xFF451A03), Color(0xFF78350F), Color(0xFFD97706))
        imageKey.contains("gaming") -> listOf(Color(0xFF3B0764), Color(0xFF701A75), Color(0xFFEC4899))
        else -> listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
    }

    Box(
        modifier = modifier
            .background(Brush.linearGradient(gradientColors)),
        contentAlignment = Alignment.Center
    ) {
        if (!customImageUri.isNullOrBlank()) {
            AsyncImage(
                model = customImageUri,
                contentDescription = category,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = categoryIconFor(imageKey.ifBlank { category }),
                            contentDescription = category,
                            tint = CmtGoldBright,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CmtProductCard(
    product: ProductEntity,
    isWishlisted: Boolean,
    onCardClick: () -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit,
    onToggleWishlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effectiveCoins = (product.coinPrice * (100 - product.discountPercent) / 100L).coerceAtLeast(1L)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
            ) {
                ProductVisualBox(
                    imageKey = product.imageKey,
                    customImageUri = product.customImageUri,
                    category = product.category,
                    modifier = Modifier.fillMaxSize()
                )

                // Discount Badge
                if (product.discountPercent > 0) {
                    Surface(
                        color = CmtCoralError,
                        shape = RoundedCornerShape(bottomEnd = 10.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "-${product.discountPercent}% OFF",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Wishlist Button
                IconButton(
                    onClick = onToggleWishlist,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(42.dp)
                        .testTag("wishlist_btn_${product.id}")
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Toggle Wishlist",
                        tint = if (isWishlisted) CmtCoralError else Color.White
                    )
                }

                // Stock pill
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(topStart = 8.dp),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(
                        text = if (product.stock > 0) "Stock: ${product.stock}" else "Out of Stock",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (product.stock > 0) CmtEmeraldSuccess else CmtCoralError,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Shop name & rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = CmtCyanBright,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = product.shopName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = CmtGoldBright,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${product.rating}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CmtGoldBright,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Price in Coins
                Row(verticalAlignment = Alignment.Bottom) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = CmtGoldBright,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${formatNumber(effectiveCoins)} Coins",
                        style = MaterialTheme.typography.titleMedium,
                        color = CmtGoldBright,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (product.discountPercent > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${formatNumber(product.coinPrice)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Add to Cart & Buy Now Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("add_cart_${product.id}"),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        border = BorderStroke(1.dp, CmtCyanSecondary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = "Add to Cart",
                            tint = CmtCyanBright,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Cart",
                            style = MaterialTheme.typography.labelMedium,
                            color = CmtCyanBright
                        )
                    }
                    Button(
                        onClick = onBuyNow,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("buy_now_${product.id}"),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CmtGoldPrimary,
                            contentColor = Color(0xFF0F172A)
                        )
                    ) {
                        Text(
                            text = "Buy Now",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EasyPaisaPaymentCard(
    settings: StoreSettingsEntity,
    onCopied: (String) -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CmtEmeraldDark.copy(alpha = 0.65f)),
        border = BorderStroke(1.5.dp, CmtEmeraldSuccess)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = CmtEmeraldSuccess,
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "EasyPaisa",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Supported Payment Method",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA7F3D0)
                        )
                        Text(
                            text = settings.paymentMethodName,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                StatusBadge("OFFICIAL CMT ACCOUNT")
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = CmtEmeraldSuccess.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Account Name",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA7F3D0)
                    )
                    Text(
                        text = settings.paymentAccountName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${settings.paymentMethodName} Number",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA7F3D0)
                    )
                    Text(
                        text = settings.paymentAccountNumber,
                        style = MaterialTheme.typography.titleMedium,
                        color = CmtGoldBright,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = {
                    copyToClipboard(
                        context,
                        "${settings.paymentMethodName} Number",
                        settings.paymentAccountNumber,
                        onCopied
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("copy_easypaisa_number_btn"),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy EasyPaisa Number",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy ${settings.paymentMethodName} Number (${settings.paymentAccountNumber})")
            }
        }
    }
}
