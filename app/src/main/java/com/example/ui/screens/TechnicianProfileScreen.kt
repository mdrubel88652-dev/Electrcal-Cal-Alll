package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.io.File
import com.example.ElectricalApp
import com.example.data.datastore.TechnicianReportProfile
import com.example.ui.theme.ElectricBluePrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicianProfileScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val app = ElectricalApp.getApp(context)

    val savedProfile by app.settingsManager.technicianProfileFlow.collectAsState(initial = TechnicianReportProfile())

    var name by remember(savedProfile) { mutableStateOf(savedProfile.name) }
    var designation by remember(savedProfile) { mutableStateOf(savedProfile.designation) }
    var licenseName by remember(savedProfile) { mutableStateOf(savedProfile.licenseName) }
    var licenseNumber by remember(savedProfile) { mutableStateOf(savedProfile.licenseNumber) }
    var contactPhone by remember(savedProfile) { mutableStateOf(savedProfile.contactPhone) }
    var email by remember(savedProfile) { mutableStateOf(savedProfile.email) }
    var officialAddress by remember(savedProfile) { mutableStateOf(savedProfile.officialAddress) }
    var photoUri by remember(savedProfile) { mutableStateOf(savedProfile.photoUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.filesDir, "technician_photo_${System.currentTimeMillis()}.jpg")
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                photoUri = file.absolutePath
                Toast.makeText(context, "Photo selected successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                photoUri = uri.toString()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("technician_profile_screen")
    ) {
        // Form Fields
        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Technician Photo Upload Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(92.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                                    .border(2.dp, ElectricBluePrimary, CircleShape)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (photoUri.isNotBlank()) {
                                    AsyncImage(
                                        model = photoUri,
                                        contentDescription = "Technician Photo",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddAPhoto,
                                            contentDescription = "Upload Photo",
                                            tint = ElectricBluePrimary,
                                            modifier = Modifier.size(30.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "ছবি যোগ করুন",
                                            fontSize = 9.5.sp,
                                            color = ElectricBluePrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (photoUri.isNotBlank()) Icons.Default.Edit else Icons.Default.AddAPhoto,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = ElectricBluePrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (photoUri.isNotBlank()) "Change Photo" else "Upload Photo",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricBluePrimary
                                    )
                                }

                                if (photoUri.isNotBlank()) {
                                    TextButton(
                                        onClick = {
                                            photoUri = ""
                                            Toast.makeText(context, "Photo removed", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color(0xFFDC2626)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Remove",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDC2626)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "এই ছবি প্রিন্ট রিপোর্টে টেকনিশিয়ান বিবরণের পাশে প্রদর্শিত হবে।",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        placeholder = { Text("Technician Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ElectricBluePrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tech_input_name"),
                        shape = RoundedCornerShape(6.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBluePrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Designation (Job Title)
                    OutlinedTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("Designation (Job Title)") },
                        placeholder = { Text("e.g. Lead Electrical Technician / Engineer") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ElectricBluePrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tech_input_designation"),
                        shape = RoundedCornerShape(6.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBluePrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2b. Government License Name (Optional)
                    OutlinedTextField(
                        value = licenseName,
                        onValueChange = { licenseName = it },
                        label = { Text("Govt License Name (Optional)") },
                        placeholder = { Text("e.g. Electrical Licensing Board") },
                        leadingIcon = { Icon(Icons.Default.Save, contentDescription = null, tint = ElectricBluePrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tech_input_license_name"),
                        shape = RoundedCornerShape(6.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBluePrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2c. Government License Number (Optional)
                    OutlinedTextField(
                        value = licenseNumber,
                        onValueChange = { licenseNumber = it },
                        label = { Text("License Number (Optional)") },
                        placeholder = { Text("e.g. ELEC-987654") },
                        leadingIcon = { Icon(Icons.Default.Save, contentDescription = null, tint = ElectricBluePrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tech_input_license_num"),
                        shape = RoundedCornerShape(6.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBluePrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Contact Phone
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Contact Phone") },
                        placeholder = { Text("+880 1XXXXXXXXX") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = ElectricBluePrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tech_input_phone"),
                        shape = RoundedCornerShape(6.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBluePrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        placeholder = { Text("technician@example.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ElectricBluePrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tech_input_email"),
                        shape = RoundedCornerShape(6.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBluePrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. Official Address (Multi-line)
                    OutlinedTextField(
                        value = officialAddress,
                        onValueChange = { officialAddress = it },
                        label = { Text("Official Address") },
                        placeholder = { Text("Official Address (Building, Road, City, Country)") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = ElectricBluePrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tech_input_address"),
                        shape = RoundedCornerShape(6.dp),
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBluePrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Buttons: SAVE and RESET
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // RESET Button
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            name = ""
                            designation = ""
                            licenseName = ""
                            licenseNumber = ""
                            contactPhone = ""
                            email = ""
                            officialAddress = ""
                            photoUri = ""
                            app.settingsManager.resetTechnicianProfile()
                            Toast.makeText(context, "Technician profile reset", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_reset_technician"),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RESET", fontWeight = FontWeight.Bold)
                }

                // SAVE Button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val newProfile = TechnicianReportProfile(
                                name = name.trim(),
                                designation = designation.trim(),
                                licenseName = licenseName.trim(),
                                licenseNumber = licenseNumber.trim(),
                                contactPhone = contactPhone.trim(),
                                email = email.trim(),
                                officialAddress = officialAddress.trim(),
                                photoUri = photoUri.trim()
                            )
                            app.settingsManager.updateTechnicianProfile(newProfile)
                            Toast.makeText(context, "Technician profile saved successfully", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_save_technician"),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SAVE", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
}
