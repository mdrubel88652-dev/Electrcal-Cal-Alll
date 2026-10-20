package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * Clean, elegant horizontal action toolbar matching exact user screenshot
 * (Bookmark / Copy / Share / Red PDF Badge / Print)
 */
@Composable
fun CalculatorActionButtons(
    isSaved: Boolean,
    onSave: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onPdf: () -> Unit,
    modifier: Modifier = Modifier,
    onPrint: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Bookmark / Save
            IconButton(
                onClick = onSave,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("action_btn_save")
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Save",
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(24.dp)
                )
            }

            // 2. Copy
            IconButton(
                onClick = onCopy,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("action_btn_copy")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            // 3. Share
            IconButton(
                onClick = onShare,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("action_btn_share")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            // 4. PDF Report (Red PDF icon)
            IconButton(
                onClick = onPdf,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("action_btn_pdf")
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "PDF Report",
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(26.dp)
                )
            }

            if (onPrint != null) {
                // 5. Optional Print
                IconButton(
                    onClick = onPrint,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("action_btn_print")
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "Print",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
