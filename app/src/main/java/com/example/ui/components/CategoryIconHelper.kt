package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.MobileFriendly
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SendToMobile
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {
    fun getIcon(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            "restaurant", "food", "dining" -> Icons.Default.Restaurant
            "directions_car", "transport", "car" -> Icons.Default.DirectionsCar
            "shopping_bag", "shopping", "mall" -> Icons.Default.ShoppingBag
            "receipt_long", "bills", "receipt" -> Icons.Default.ReceiptLong
            "movie", "entertainment", "film" -> Icons.Default.Movie
            "local_hospital", "health", "hospital" -> Icons.Default.LocalHospital
            "school", "education" -> Icons.Default.School
            "home", "rent", "housing" -> Icons.Default.Home
            "face", "personal", "beauty" -> Icons.Default.Face
            "payments", "salary" -> Icons.Default.Payments
            "laptop_mac", "freelance", "tech" -> Icons.Default.LaptopMac
            "business_center", "business" -> Icons.Default.BusinessCenter
            "trending_up", "investment", "growth" -> Icons.Default.TrendingUp
            "card_giftcard", "gift", "bonus" -> Icons.Default.CardGiftcard
            "credit_card", "card" -> Icons.Default.CreditCard
            "account_balance", "bank" -> Icons.Default.AccountBalance
            "account_balance_wallet", "wallet" -> Icons.Default.AccountBalanceWallet
            "mobile_friendly", "bkash" -> Icons.Default.MobileFriendly
            "phone_android", "upay" -> Icons.Default.PhoneAndroid
            "send_to_mobile", "rocket" -> Icons.Default.SendToMobile
            else -> Icons.Default.Category
        }
    }

    fun parseColor(hex: String, defaultColor: Color = Color(0xFF00A86B)): Color {
        return try {
            val cleanHex = if (hex.startsWith("#")) hex else "#$hex"
            Color(android.graphics.Color.parseColor(cleanHex))
        } catch (e: Exception) {
            defaultColor
        }
    }
}
