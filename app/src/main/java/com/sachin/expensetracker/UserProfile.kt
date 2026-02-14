package com.sachin.expensetracker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.graphics.Color

@Composable
fun UserProfileIcon(
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Get first letter of email or 'U' for User
    val firstLetter = currentUser?.email?.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    Box {
        // Profile Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(HighlightGold, GoldAccent, MutedGold)
                    )
                )
                .clickable { expanded = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = firstLetter,
                color = DeepBlack,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Dropdown Menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(SoftBlack)
                .width(200.dp)
        ) {
            // User Email Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CharcoalBorder)
                    .padding(16.dp)
            ) {
                Text(
                    text = currentUser?.email ?: "User",
                    color = WhiteText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Divider(color = CharcoalBorder, thickness = 1.dp)

            // Reports
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Reports", color = WhiteText)
                    }
                },
                onClick = {
                    expanded = false
                    onNavigateToReports()
                }
            )

            // Settings
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Settings", color = WhiteText)
                    }
                },
                onClick = {
                    expanded = false
                    onNavigateToSettings()
                }
            )

            Divider(color = CharcoalBorder, thickness = 1.dp)

            // Logout
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = Color(0xFFCF6679),
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Logout", color = WhiteText)
                    }
                },
                onClick = {
                    expanded = false
                    onLogout()
                }
            )
        }
    }
}