package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String = "Electrical Calculation All",
    subtitle: String = "Electrical engineering solution",
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onNavigateToNotification: () -> Unit = {},
    onNavigateToInformation: () -> Unit = {},
    onOpenMenu: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!canNavigateBack) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFFF4B400).copy(alpha = 0.6f), CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = onNavigateToInformation
                            )
                            .testTag("top_bar_user_picture"),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "App Logo • Information",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    modifier = Modifier.padding(start = 2.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.1.sp,
                            fontSize = 15.sp,
                            lineHeight = 18.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 13.sp
                        ),
                        color = Color(0xFFB9E8FF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Information Icon with subtle electric yellow accent
                IconButton(
                    onClick = onNavigateToInformation,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("top_bar_information_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Information",
                        tint = Color(0xFFF4B400),
                        modifier = Modifier.size(23.dp)
                    )
                }
                // Notification Icon
                IconButton(
                    onClick = onNavigateToNotification,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("top_bar_notification_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notification",
                        tint = Color.White,
                        modifier = Modifier.size(23.dp)
                    )
                }
                // Three Lines Menu Icon
                IconButton(
                    onClick = onOpenMenu,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 4.dp)
                        .testTag("top_bar_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Technician Hub & Wallet Menu",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF082D63),
            scrolledContainerColor = Color(0xFF082D63),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}
