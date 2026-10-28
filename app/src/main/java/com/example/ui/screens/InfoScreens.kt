package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ElectricBluePrimary

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "App Icon",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Electrical Cal/Pro",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = ElectricBluePrimary
        )
        Text(
            text = "Professional Electrical Engineering & Estimation",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Version 1.0.0 (Production Release)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Features Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                FeatureItem("167 authoritative electrical engineering calculators")
                FeatureItem("4 Dedicated tiers: Main (167), Advance (55), Basic (61), Lower (51)")
                FeatureItem("Building Electrical Material Calculation (#167) without prices")
                FeatureItem("Real PDF generation & system PrintManager support")
                FeatureItem("Offline calculation history persistence via Room Database")
                FeatureItem("Standard IEEE / IEC / NEC electrical formulas")
            }
        }
    }
}

@Composable
fun DeveloperScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Main Developer Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header with Avatar & Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Developer",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Engineer Rubel",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE0F2FE)
                            ) {
                                Text(
                                    text = "VERIFIED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0369A1),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "Engineer & Developer",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0284C7)
                        )

                        Text(
                            text = "Industrial Electrical & Power Engineering",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Detail Items
                ProfileDetailItem(
                    icon = Icons.Default.Person,
                    label = "Full Name",
                    value = "Engineer Rubel"
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProfileDetailItem(
                    icon = Icons.Default.Info,
                    label = "Designation / Bio",
                    value = "Engineer & Developer"
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProfileDetailItem(
                    icon = Icons.Default.Shield,
                    label = "Company / Organization",
                    value = "Industrial Electrical & Power Engineering"
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProfileDetailItem(
                    icon = Icons.Default.CheckCircle,
                    label = "Division & Department",
                    value = "Power Systems Engineering Division"
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProfileDetailItem(
                    icon = Icons.Default.Info,
                    label = "Official Email",
                    value = "mdrubel88652@gmail.com"
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProfileDetailItem(
                    icon = Icons.Default.Shield,
                    label = "Office Address",
                    value = "Power Systems Engineering Division"
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Quick Action Button
                androidx.compose.material3.Button(
                    onClick = {
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                data = android.net.Uri.parse("mailto:mdrubel88652@gmail.com")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "mdrubel88652@gmail.com", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Email", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProfileDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF475569),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )
        }
    }
}

@Composable
fun PrivacyPolicyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = ElectricBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Privacy Policy", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                Text("Offline First Privacy", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("All calculation inputs, building projects, and history records are stored purely locally on your device within encrypted sandbox storage. No personal data or calculation numbers are transmitted to external servers without your explicit action.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun NotificationScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = ElectricBluePrimary, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Engineering Updates & Notifications", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text("You are up to date! All 167 electrical engineering calculators and the building electrical material engine are ready for offline operation.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
    }
}

@Composable
private fun FeatureItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
