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
    var company by remember(savedProfile) { mutableStateOf(savedProfile.company) }
    var contactPhone by remember(savedProfile) { mutableStateOf(savedProfile.contactPhone) }
    var email by remember(savedProfile) { mutableStateOf(savedProfile.email) }
    var officialAddress by remember(savedProfile) { mutableStateOf(savedProfile.officialAddress) }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(2.dp),
                title = {
                    Text(
                        text = "Technician Report Profile",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("tech_profile_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("technician_profile_screen")
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Technician Report Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBluePrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Save technician information for calculation and engineering reports. This information will appear near the top of printed and PDF reports.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Form Fields
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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

                    // 2. Company
                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("Company") },
                        placeholder = { Text("Company / Organisation") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = ElectricBluePrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tech_input_company"),
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
                            company = ""
                            contactPhone = ""
                            email = ""
                            officialAddress = ""
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
                                company = company.trim(),
                                contactPhone = contactPhone.trim(),
                                email = email.trim(),
                                officialAddress = officialAddress.trim()
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

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
