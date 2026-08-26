package com.example.veggielens.ui.profile

import android.widget.Button
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.veggielens.viewmodel.HistoryViewModel
import com.example.veggielens.viewmodel.ProfileViewModel
import com.google.android.datatransport.runtime.BuildConfig

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    historyViewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val stats by profileViewModel.stats.collectAsState()
    val apiKey by profileViewModel.apiKey.collectAsState()

    var showKeyDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var tempKey by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = "头像",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "VeggieLens",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "离线蔬菜识别与营养信息",
                        fontSize = 13.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "本地识别 · 数据不上传",
                            color = Color(0xFF2E7D32),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                StatBox(
                    icon = Icons.Default.Search,
                    value = stats.totalScans.toString(),
                    label =  "扫描次数",
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    icon = Icons.Default.Favorite,
                    value = stats.uniqueVegetables.toString(),
                    label = "发现品类",
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    icon = Icons.Default.Star,
                    value = stats.monthlyScans.toString(),
                    label = "本月扫描",
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    icon = Icons.Default.DateRange,
                    value = stats.streakDays.toString(),
                    label = "连续打卡",
                    modifier = Modifier.weight(1f)
                )
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column {
                    ProfileListItem(
                        icon = Icons.Default.Settings,
                        title = "DeepSeek API 密钥",
                        subtitle = if (apiKey.isBlank()) "去配置 (未启用云端 AI)" else "已配置 (云端 AI 活跃)",
                        onClick = {
                            tempKey = apiKey
                            showKeyDialog = true
                        }
                    )
                    HorizontalDivider(color = Color(0xFFF1F1F1), thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
                    ProfileListItem(
                        icon = Icons.Default.Delete,
                        title = "清除历史记录",
                        subtitle = "彻底清空本地扫描的记录",
                        onClick = { showClearHistoryDialog = true }
                    )
                    HorizontalDivider(color = Color(0xFFF1F1F1), thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
                    ProfileListItem(
                        icon = Icons.Default.Info,
                        title = "关于 VeggieLens",
                        subtitle = "版本 ${BuildConfig.VERSION_NAME}",
                        onClick = { showAboutDialog = true }
                    )
                }
            }
        }

        if (showKeyDialog) {
            AlertDialog(
                onDismissRequest = { showKeyDialog = false },
                title = { Text("配置 DeepSeek API 密钥", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "配置后将使用 deepseek-chat 生成简短科普；留空或请求失败时使用本地资料。密钥只加密保存在本机，但正式产品仍应通过自有服务端代理。",
                            fontSize = 13.sp,
                            color = Color(0xFF757575),
                            lineHeight = 18.sp
                        )
                        Spacer(modifier.height(16.dp))
                        OutlinedTextField(
                            value = tempKey,
                            onValueChange = { tempKey = it },
                            placeholder = { Text("输入 sk-... 格式密钥", fontSize = 14.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            profileViewModel.updateApiKey(tempKey)
                            showKeyDialog = false
                            Toast.makeText(context, "API 密钥已更新", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("保存")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showKeyDialog = false }) {
                        Text("取消", color = Color(0xFF757575))
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (showClearHistoryDialog) {
            AlertDialog(
                onDismissRequest = { showClearHistoryDialog = false },
                icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                title = { Text("清除全部历史记录？") },
                text = { Text("该操作会永久删除本机保存的识别记录，无法撤销。") },
                confirmButton = {
                    Button(
                        onClick = {
                            historyViewModel.clearAllHistory()
                            showClearHistoryDialog = false
                            Toast.makeText(context, "扫描历史已全部清空", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("确认清除")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearHistoryDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                icon = { Icon(Icons.Default.Info, contentDescription = null) },
                title = { Text("关于 VeggieLens") },
                text = {
                    Text(
                        "版本 ${BuildConfig.VERSION_NAME}\n\n" +
                                "使用 CameraX 与 MediaPipe 在设备本地完成图像分类，" +
                                "Room 保存识别历史。云端科普为可选功能，未配置密钥时不发送请求。\n\n" +
                                "识别和营养内容仅供学习与一般科普，不用于医疗或食品安全判断。"
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text("知道了")
                    }
                }
            )
        }
    }
}

@Composable
fun StatBox(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFF9E9E9E),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProfileListItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFF1F8E9), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1B1F)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "进入",
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(20.dp)
        )
    }
}