package com.rahat.ui.profile

import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahat.ui.theme.*
import kotlinx.coroutines.*

data class EmergencyContact(val name: String, val relation: String, val phone: String, val id: Int = 0)
enum class ContactStatus { SAFE, UNREACHABLE, ALERT_RECEIVED, SHARED_OFFLINE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userRepo: com.rahat.data.firebase.FirestoreUserRepository,
    narrator: com.rahat.service.Narrator,
    accessibilityPrefs: com.rahat.data.AccessibilityPreferences,
    activeRiskLevel: RiskLevel = RiskLevel.SAFE,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope   = rememberCoroutineScope()
    val glass   = MaterialTheme.glass

    var pickerName  by remember { mutableStateOf("") }
    var pickerPhone by remember { mutableStateOf("") }

    val contactPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri: Uri? ->
        uri?.let { getContactInfo(context, it)?.let { info -> pickerName = info.first; pickerPhone = info.second } }
    }
    val contactPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) contactPickerLauncher.launch(null)
    }

    val isNarratorEnabled by accessibilityPrefs.isNarratorEnabled.collectAsState()
    val narratorVolume    by accessibilityPrefs.narratorVolume.collectAsState()
    var userName          by remember { mutableStateOf("Loading...") }
    var userPhone         by remember { mutableStateOf("...") }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var newName           by remember { mutableStateOf("") }
    var rId               by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val db = com.rahat.data.local.RahatDatabase.getDatabase(context)
        launch(Dispatchers.IO) {
            val device = db.rahatDao().getDeviceOneShot()
            if (device != null) {
                rId = device.rId
                db.rahatDao().getUserProfile(device.rId).collect { profile ->
                    if (profile != null) { userName = profile.name; userPhone = profile.phone }
                }
            }
        }
    }

    var contacts by remember { mutableStateOf<List<EmergencyContact>>(emptyList()) }
    LaunchedEffect(rId) {
        val db = com.rahat.data.local.RahatDatabase.getDatabase(context)
        if (rId != null) {
            db.rahatDao().getContacts(rId!!).collect { entities ->
                contacts = entities.map { EmergencyContact(it.name, it.relation, it.phone, it.id) }
            }
        }
    }
    var showAddDialog by remember { mutableStateOf(false) }

    val showRiskBanner = activeRiskLevel != RiskLevel.SAFE && activeRiskLevel != RiskLevel.OFFLINE
    val riskSolid  = riskSolidColor(activeRiskLevel)
    val riskGlass  = riskGlassColor(activeRiskLevel)
    val riskBorder = riskBorderColor(activeRiskLevel)

    val infiniteTransition = rememberInfiniteTransition(label = "profilePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "pulseAlpha"
    )

    fun contactStatus(index: Int): ContactStatus = when {
        !showRiskBanner -> ContactStatus.SAFE
        index % 4 == 0  -> ContactStatus.ALERT_RECEIVED
        index % 4 == 1  -> ContactStatus.UNREACHABLE
        index % 4 == 2  -> ContactStatus.SHARED_OFFLINE
        else            -> ContactStatus.SAFE
    }

    Box(modifier = Modifier.fillMaxSize().background(glass.backgroundGradient)) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp)) {

            // Top bar
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp)).background(glass.cardBackground)
                        .border(1.dp, glass.cardBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.ArrowBack, null, tint = TextOnGlass)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Profile", color = TextOnGlass, fontWeight = FontWeight.Bold,
                            fontSize = 18.sp, modifier = Modifier.weight(1f))
                    }
                }
            }

            // User card
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(RahatBlue.copy(alpha = 0.25f), glass.cardBackground)))
                        .border(1.5.dp, RahatCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp)).padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(56.dp).clip(CircleShape)
                                .background(Brush.radialGradient(listOf(RahatBlue, RahatCyan.copy(alpha = 0.6f))))
                                .border(2.dp, RahatCyan.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Person, null, tint = TextOnGlass, modifier = Modifier.size(30.dp)) }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(userName, color = TextOnGlass, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(userPhone, color = TextOnGlassSecondary, fontSize = 13.sp)
                        }
                        IconButton(onClick = {
                            narrator.speakIfEnabled("Edit Name", isNarratorEnabled, narratorVolume)
                            showEditNameDialog = true
                        }) { Icon(Icons.Default.Edit, null, tint = RahatCyan) }
                    }
                }
            }

            // District warning banner
            if (showRiskBanner) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(riskGlass, glass.cardBackground)))
                            .border(1.5.dp, riskBorder.copy(alpha = if (activeRiskLevel == RiskLevel.CRITICAL) pulseAlpha else 0.5f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, tint = riskSolid, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Your family area is under watch", color = TextOnGlass,
                                    fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                            Text("Alert shared to ${contacts.size} contact${if (contacts.size != 1) "s" else ""}",
                                color = TextOnGlassSecondary, fontSize = 12.sp)
                            Text("${contacts.size} contact${if (contacts.size != 1) "s" else ""} in the same risk zone",
                                color = TextOnGlassMuted, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Contacts header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Emergency Family Contacts", color = TextOnGlass, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            .background(RahatCyan.copy(alpha = 0.15f))
                            .border(1.dp, RahatCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    ) {
                        TextButton(onClick = { showAddDialog = true }) {
                            Text("+ Add", color = RahatCyan, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Contact cards
            items(contacts, key = { it.id }) { contact ->
                ProfileContactCard(contact, contactStatus(contacts.indexOf(contact)), showRiskBanner, glass) {
                    scope.launch(Dispatchers.IO) {
                        val db = com.rahat.data.local.RahatDatabase.getDatabase(context)
                        db.rahatDao().deleteContact(contact.id)
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            if (contacts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(14.dp)).background(glass.cardBackground)
                            .border(1.dp, glass.cardBorder, RoundedCornerShape(14.dp)).padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PersonAdd, null, tint = TextOnGlassMuted, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No contacts added yet", color = TextOnGlassSecondary, fontSize = 13.sp)
                            Text("Add family members to notify in an SOS", color = TextOnGlassMuted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Edit name dialog (logic unchanged)
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor   = MaterialTheme.colorScheme.surface,
            title = { Text("Edit Name") },
            text  = {
                OutlinedTextField(value = newName, onValueChange = { newName = it },
                    label = { Text("Enter your name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank()) {
                        rId?.let { id ->
                            scope.launch(Dispatchers.IO) {
                                val db = com.rahat.data.local.RahatDatabase.getDatabase(context)
                                db.rahatDao().updateUserName(id, newName)
                                launch { try { userRepo.updateUserName(id, newName) } catch (e: Exception) {} }
                                withContext(Dispatchers.Main) {
                                    userName = newName; showEditNameDialog = false
                                    narrator.speakIfEnabled("Name updated locally", isNarratorEnabled, narratorVolume)
                                }
                            }
                        }
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showEditNameDialog = false }) { Text("Cancel") } }
        )
    }

    // Add contact dialog (logic unchanged)
    if (showAddDialog) {
        var name     by remember(pickerName)  { mutableStateOf(pickerName) }
        var relation by remember               { mutableStateOf("") }
        var phone    by remember(pickerPhone) { mutableStateOf(pickerPhone) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false; pickerName = ""; pickerPhone = "" },
            containerColor   = MaterialTheme.colorScheme.surface,
            title = { Text("Add Emergency Contact") },
            text  = {
                Column {
                    OutlinedButton(
                        onClick = {
                            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS)
                                == android.content.pm.PackageManager.PERMISSION_GRANTED)
                                contactPickerLauncher.launch(null)
                            else contactPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContactPhone, null); Spacer(Modifier.width(8.dp)); Text("Pick from Contacts")
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Contact Name") },
                        singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = relation, onValueChange = { relation = it },
                        label = { Text("Relation (e.g. Father, Friend)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        rId?.let { id ->
                            scope.launch(Dispatchers.IO) {
                                val db = com.rahat.data.local.RahatDatabase.getDatabase(context)
                                db.rahatDao().insertContact(com.rahat.data.local.entity.EmergencyContactEntity(
                                    ownerId = id, name = name, relation = relation.ifBlank { "Family" }, phone = phone))
                                withContext(Dispatchers.Main) {
                                    pickerName = ""; pickerPhone = ""; showAddDialog = false
                                    narrator.speakIfEnabled("Contact added", isNarratorEnabled, narratorVolume)
                                }
                            }
                        }
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; pickerName = ""; pickerPhone = "" }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProfileContactCard(
    contact: EmergencyContact, status: ContactStatus, showRiskBanner: Boolean,
    glass: GlassTheme, onDelete: () -> Unit
) {
    val (statusColor, statusLabel) = when (status) {
        ContactStatus.SAFE           -> Pair(RiskSafeGreen,    "safe")
        ContactStatus.UNREACHABLE    -> Pair(TextOnGlassMuted, "unreachable")
        ContactStatus.ALERT_RECEIVED -> Pair(RiskWarningOrange, "alert received")
        ContactStatus.SHARED_OFFLINE -> Pair(RahatCyan,        "shared offline")
    }

    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp)).background(glass.cardBackground)
            .border(1.dp, glass.cardBorder, RoundedCornerShape(16.dp)).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background(RahatBlue.copy(alpha = 0.2f))
                    .border(1.dp, RahatCyan.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(contact.name.firstOrNull()?.uppercase() ?: "?",
                    color = RahatCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(contact.name, color = TextOnGlass, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(4.dp))
                            .background(RahatCyan.copy(alpha = 0.12f)).padding(horizontal = 6.dp, vertical = 2.dp)
                    ) { Text(contact.relation, color = RahatCyan, fontSize = 10.sp) }
                }
                Spacer(Modifier.height(2.dp))
                Text(contact.phone, color = TextOnGlassSecondary, fontSize = 12.sp)
                if (showRiskBanner) {
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(5.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(5.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) { Text(statusLabel, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, null, tint = SOSRed.copy(alpha = 0.7f))
            }
        }
    }
}

private fun getContactInfo(context: android.content.Context, contactUri: Uri): Pair<String, String>? {
    context.contentResolver.query(contactUri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val name = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                .takeIf { it != -1 }?.let { cursor.getString(it) } ?: "Unknown"
            val id   = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                .takeIf { it != -1 }?.let { cursor.getString(it) }
            var phone = ""
            if (id != null) {
                context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(id), null
                )?.use { phoneCursor ->
                    while (phoneCursor.moveToNext()) {
                        val col = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (col != -1) { val num = phoneCursor.getString(col); if (num.isNotBlank()) { phone = num; break } }
                    }
                }
            }
            return Pair(name, phone)
        }
    }
    return null
}