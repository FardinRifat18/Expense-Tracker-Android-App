package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurrencyConfig
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.CleanBorder
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletDark
import com.example.ui.theme.VioletGradientEnd
import com.example.ui.theme.VioletGradientStart
import com.example.ui.theme.VioletLight
import com.example.ui.theme.VioletPrimary
import com.example.viewmodel.ExpenseViewModel
import java.math.BigDecimal

@Composable
fun ProfileScreen(
    viewModel: ExpenseViewModel,
    onNavigateToCategories: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val currency by viewModel.currentCurrency.collectAsState()
    val isSyncing by viewModel.isBackendSyncing.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showOpeningBalanceDialog by remember { mutableStateOf(false) }
    var showServerConfigDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CleanBackground)
            .testTag("profile_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. User Profile Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_hero_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Avatar with Initials & Gradient Ring
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(VioletGradientStart, VioletGradientEnd)
                                        )
                                    )
                                    .clickable { showEditProfileDialog = true }
                                    .testTag("profile_avatar_box"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userProfile.name.take(2).uppercase().ifBlank { "EF" },
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = userProfile.name,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                if (userProfile.occupation.isNotBlank()) {
                                    Text(
                                        text = userProfile.occupation,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = VioletPrimary
                                    )
                                }
                                Text(
                                    text = userProfile.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Edit Profile Pill Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = VioletPrimary.copy(alpha = 0.12f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showEditProfileDialog = true }
                                .testTag("edit_profile_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = VioletPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Edit",
                                    color = VioletPrimary,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    // Personal Bio / Motto if provided
                    if (userProfile.bio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CleanBackground,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = VioletPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "\"${userProfile.bio}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Badges Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = VioletPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Currency: ${currency.name} (${currency.symbol})",
                                style = MaterialTheme.typography.labelSmall,
                                color = VioletPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (userProfile.phone.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = userProfile.phone,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Personal Details Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personal Details",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                TextButton(
                    onClick = { showEditProfileDialog = true },
                    modifier = Modifier.testTag("change_details_link")
                ) {
                    Text("Change Details", color = VioletPrimary, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    ProfileDetailRow(
                        icon = Icons.Default.Person,
                        label = "Full Name",
                        value = userProfile.name,
                        onClick = { showEditProfileDialog = true }
                    )
                    SettingsDivider()
                    ProfileDetailRow(
                        icon = Icons.Default.Email,
                        label = "Email Address",
                        value = userProfile.email,
                        onClick = { showEditProfileDialog = true }
                    )
                    SettingsDivider()
                    ProfileDetailRow(
                        icon = Icons.Default.Phone,
                        label = "Phone Number",
                        value = userProfile.phone.ifBlank { "Not provided (Tap to add)" },
                        onClick = { showEditProfileDialog = true }
                    )
                    SettingsDivider()
                    ProfileDetailRow(
                        icon = Icons.Default.Work,
                        label = "Occupation / Role",
                        value = userProfile.occupation.ifBlank { "Not provided (Tap to add)" },
                        onClick = { showEditProfileDialog = true }
                    )
                    SettingsDivider()
                    ProfileDetailRow(
                        icon = Icons.Default.Description,
                        label = "Bio & Financial Goal",
                        value = userProfile.bio.ifBlank { "Not provided (Tap to add)" },
                        onClick = { showEditProfileDialog = true }
                    )
                }
            }
        }

        // 3. Section: Preferences & Regional
        item {
            Text(
                text = "Preferences & Regional",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.Language,
                        title = "Currency Localization",
                        subtitle = "${currency.flag} ${currency.name} (${currency.symbol} ${currency.code})",
                        onClick = { showCurrencyDialog = true }
                    )

                    SettingsDivider()

                    SettingsRowItem(
                        icon = Icons.Default.AccountBalance,
                        title = "Opening Ledger Balance",
                        subtitle = "${currency.symbol} ${userProfile.openingBalance}",
                        onClick = { showOpeningBalanceDialog = true }
                    )

                    SettingsDivider()

                    SettingsRowItem(
                        icon = Icons.Default.Category,
                        title = "Categories Management",
                        subtitle = "Configure expense & income categories",
                        onClick = onNavigateToCategories
                    )
                }
            }
        }

        // 4. Section: Backend & Cloud Sync
        item {
            Text(
                text = "Django Backend & Cloud Sync",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.CloudSync,
                        title = "Django REST Backend URL",
                        subtitle = userProfile.serverBaseUrl,
                        onClick = { showServerConfigDialog = true }
                    )

                    SettingsDivider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.testBackendSync() }
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Test Server Connection",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Verify connectivity to local Django REST API",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }

                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = VioletPrimary)
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = VioletPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Test",
                                    color = VioletPrimary,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Section: Data & Backup
        item {
            Text(
                text = "Data Management",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.FileDownload,
                        title = "Export Transactions CSV",
                        subtitle = "Generate spreadsheet compatible CSV file",
                        onClick = {
                            val csv = viewModel.exportTransactionsCsv()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, csv)
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "ExpenseFlow_${currency.code}_Data.csv")
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Export Financial CSV"))
                        }
                    )

                    SettingsDivider()

                    SettingsRowItem(
                        icon = Icons.Default.Refresh,
                        title = "Reset Demo Data",
                        subtitle = "Reload initial sample Bangladeshi Taka records",
                        onClick = { showResetDialog = true }
                    )
                }
            }
        }

        // 6. Logout
        item {
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("logout_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralExpense.copy(alpha = 0.15f))
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = CoralExpense)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", color = CoralExpense, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }

    // ==========================================
    // Edit Profile Details Dialog
    // ==========================================
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(userProfile.name) }
        var editEmail by remember { mutableStateOf(userProfile.email) }
        var editPhone by remember { mutableStateOf(userProfile.phone) }
        var editOccupation by remember { mutableStateOf(userProfile.occupation) }
        var editBio by remember { mutableStateOf(userProfile.bio) }
        var validationError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(VioletPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = VioletPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Edit Profile & Details",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Update your display name, contact information, occupation, and financial motto.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    // Full Name Input
                    OutlinedTextField(
                        value = editName,
                        onValueChange = {
                            editName = it
                            validationError = null
                        },
                        label = { Text("Full Name *") },
                        placeholder = { Text("e.g. Fardin Hasan") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = VioletPrimary)
                        },
                        singleLine = true,
                        isError = editName.isBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletPrimary,
                            focusedLabelColor = VioletPrimary
                        )
                    )

                    // Email Address Input
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = {
                            editEmail = it
                            validationError = null
                        },
                        label = { Text("Email Address *") },
                        placeholder = { Text("e.g. yourname@example.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = VioletPrimary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = editEmail.isBlank() || !editEmail.contains("@"),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_email_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletPrimary,
                            focusedLabelColor = VioletPrimary
                        )
                    )

                    // Phone Number Input
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("+880 1712-345678") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = VioletPrimary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_phone_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletPrimary,
                            focusedLabelColor = VioletPrimary
                        )
                    )

                    // Occupation / Role Input
                    OutlinedTextField(
                        value = editOccupation,
                        onValueChange = { editOccupation = it },
                        label = { Text("Occupation / Role") },
                        placeholder = { Text("e.g. Software Engineer, Business Owner") },
                        leadingIcon = {
                            Icon(Icons.Default.Work, contentDescription = null, tint = VioletPrimary)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_occupation_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletPrimary,
                            focusedLabelColor = VioletPrimary
                        )
                    )

                    // Bio / Financial Motto Input
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio & Financial Motto") },
                        placeholder = { Text("e.g. Tracking expenses for early financial freedom") },
                        leadingIcon = {
                            Icon(Icons.Default.Description, contentDescription = null, tint = VioletPrimary)
                        },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_bio_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletPrimary,
                            focusedLabelColor = VioletPrimary
                        )
                    )

                    validationError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedName = editName.trim()
                        val trimmedEmail = editEmail.trim()
                        if (trimmedName.isBlank()) {
                            validationError = "Please enter your name"
                            return@Button
                        }
                        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
                            validationError = "Please enter a valid email address"
                            return@Button
                        }
                        viewModel.updateProfileDetails(
                            name = trimmedName,
                            email = trimmedEmail,
                            phone = editPhone.trim(),
                            occupation = editOccupation.trim(),
                            bio = editBio.trim()
                        )
                        showEditProfileDialog = false
                    },
                    modifier = Modifier.testTag("save_profile_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditProfileDialog = false },
                    modifier = Modifier.testTag("cancel_edit_profile_button")
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Currency Selector Dialog
    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Currency", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Choose your preferred primary currency. ExpenseFlow defaults to Bangladeshi Taka (৳).",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CurrencyConfig.SUPPORTED_CURRENCIES.forEach { curr ->
                        val isSelected = curr.code == currency.code
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.updateCurrency(curr.code)
                                    showCurrencyDialog = false
                                },
                            color = if (isSelected) VioletPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = curr.flag, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = curr.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = "${curr.symbol} (${curr.code})",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) VioletPrimary else TextPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Close", color = VioletPrimary)
                }
            }
        )
    }

    // Opening Balance Dialog
    if (showOpeningBalanceDialog) {
        var balanceText by remember { mutableStateOf(userProfile.openingBalance.toPlainString()) }
        var dialogError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showOpeningBalanceDialog = false },
            title = { Text("Set Opening Balance", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Total balance is computed strictly as: Opening Balance + All Income - All Expenses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                                balanceText = it
                                dialogError = null
                            }
                        },
                        label = { Text("Opening Balance (${currency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletPrimary,
                            focusedLabelColor = VioletPrimary
                        )
                    )
                    dialogError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val bal = balanceText.toBigDecimalOrNull()
                        if (bal == null || bal < BigDecimal.ZERO) {
                            dialogError = "Please enter a valid amount"
                            return@Button
                        }
                        viewModel.updateOpeningBalance(bal)
                        showOpeningBalanceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOpeningBalanceDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Django Server Configuration Dialog
    if (showServerConfigDialog) {
        var urlText by remember { mutableStateOf(userProfile.serverBaseUrl) }

        AlertDialog(
            onDismissRequest = { showServerConfigDialog = false },
            title = { Text("Django Backend Configuration", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Connect to Django REST Framework backend:\n• Android Emulator: http://10.0.2.2:8000/api/v1/\n• Physical Device: http://<YOUR_LAN_IP>:8000/api/v1/",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        label = { Text("API Base URL") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletPrimary,
                            focusedLabelColor = VioletPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateServerUrl(urlText)
                        showServerConfigDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save URL", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerConfigDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Reset Data Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Sample Data", color = TextPrimary) },
            text = { Text("Reset all transactions, budgets, savings goals, and profile details back to the initial sample state?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralExpense),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Reset", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun ProfileDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(VioletPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = VioletPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
            }
        }

        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit $label",
            tint = VioletPrimary.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun SettingsRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(VioletPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = VioletPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(CleanBorder)
    )
}
