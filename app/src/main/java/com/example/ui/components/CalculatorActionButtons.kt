package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorActionButtons(
    isSaved: Boolean,
    onSave: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Exact 4-Button Grid (2 rows x 2 columns, beautifully balanced and modern)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Save & Copy
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionButtonItem(
                icon = if (isSaved) Icons.Default.Check else Icons.Default.BookmarkBorder,
                label = if (isSaved) "Saved" else "Save",
                bengaliLabel = if (isSaved) "সংরক্ষিত" else "সংরক্ষণ",
                containerColor = if (isSaved) Color(0xFF059669) else Color(0xFF0284C7),
                testTag = "action_btn_save",
                modifier = Modifier.weight(1f),
                onClick = onSave
            )

            ActionButtonItem(
                icon = Icons.Default.ContentCopy,
                label = "Copy",
                bengaliLabel = "কপি",
                containerColor = Color(0xFF0284C7),
                testTag = "action_btn_copy",
                modifier = Modifier.weight(1f),
                onClick = onCopy
            )
        }

        // Row 2: Share & PDF Report
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionButtonItem(
                icon = Icons.Default.Share,
                label = "Share",
                bengaliLabel = "শেয়ার",
                containerColor = Color(0xFF0284C7),
                testTag = "action_btn_share",
                modifier = Modifier.weight(1f),
                onClick = onShare
            )

            ActionButtonItem(
                icon = Icons.Default.PictureAsPdf,
                label = "PDF Report",
                bengaliLabel = "পিডিএফ ও প্রিন্ট",
                containerColor = Color(0xFFDC2626), // Professional PDF Crimson
                testTag = "action_btn_pdf",
                modifier = Modifier.weight(1f),
                onClick = onPdf
            )
        }
    }
}

@Composable
private fun ActionButtonItem(
    icon: ImageVector,
    label: String,
    bengaliLabel: String,
    containerColor: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 15.sp
                )
                Text(
                    text = bengaliLabel,
                    fontSize = 9.5.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 11.sp
                )
            }
        }
    }
}
