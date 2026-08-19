package com.tomatix.app.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tomatix.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val tabs = listOf(
        Triple("Thresholds", Icons.Filled.Bolt, "Thresholds"),
        Triple("Analytics", Icons.Filled.GraphicEq, "Analytics"),
        Triple("Notifications", Icons.Filled.NotificationsActive, "Notifications"),
        Triple("Logs", Icons.Filled.Smartphone, "Logs")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Gray800
            )
            Text(
                text = "Configure your Tomatix greenhouse system",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500
            )
        }

        TabRow(
            selectedTabIndex = viewModel.selectedTab,
            containerColor = White,
            contentColor = Green600
        ) {
            tabs.forEachIndexed { index, (title, icon, _) ->
                Tab(
                    selected = viewModel.selectedTab == index,
                    onClick = { viewModel.onTabSelected(index) },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(title)
                        }
                    }
                )
            }
        }

        when (viewModel.selectedTab) {
            0 -> ThresholdsTab(viewModel)
            1 -> AnalyticsTab(viewModel)
            2 -> NotificationsTab(viewModel)
            3 -> LogsTab(viewModel)
        }
    }
}

@Composable
private fun ThresholdsTab(viewModel: SettingsViewModel) {
    var tempMin by remember { mutableStateOf(viewModel.thresholds.tempMin.toString()) }
    var tempMax by remember { mutableStateOf(viewModel.thresholds.tempMax.toString()) }
    var humidityMin by remember { mutableStateOf(viewModel.thresholds.humidityMin.toString()) }
    var humidityMax by remember { mutableStateOf(viewModel.thresholds.humidityMax.toString()) }
    var soilMin by remember { mutableStateOf(viewModel.thresholds.soilMoistureMin.toString()) }
    var soilMax by remember { mutableStateOf(viewModel.thresholds.soilMoistureMax.toString()) }
    var lightMin by remember { mutableStateOf(viewModel.thresholds.lightIntensityMin.toString()) }

    Column(modifier = Modifier.padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = CardDefaults.outlinedCardBorder(enabled = true)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Environmental Thresholds",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray800
                )
                Text(
                    text = "Set optimal ranges for sensor readings",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
                Spacer(modifier = Modifier.height(16.dp))

                ThresholdSection(
                    title = "Temperature",
                    icon = Icons.Filled.Thermostat,
                    bgColor = Orange50,
                    accentColor = Orange500,
                    contentColor = Orange600
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NumberInput(
                            label = "Min",
                            value = tempMin,
                            onValueChange = { tempMin = it },
                            modifier = Modifier.weight(1f)
                        )
                        NumberInput(
                            label = "Max",
                            value = tempMax,
                            onValueChange = { tempMax = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ThresholdSection(
                    title = "Humidity",
                    icon = Icons.Filled.WaterDrop,
                    bgColor = Blue50,
                    accentColor = Blue500,
                    contentColor = Blue600
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NumberInput(
                            label = "Min",
                            value = humidityMin,
                            onValueChange = { humidityMin = it },
                            modifier = Modifier.weight(1f)
                        )
                        NumberInput(
                            label = "Max",
                            value = humidityMax,
                            onValueChange = { humidityMax = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ThresholdSection(
                    title = "Soil Moisture",
                    icon = Icons.Filled.WaterDrop,
                    bgColor = Green50,
                    accentColor = Green500,
                    contentColor = Green600
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NumberInput(
                            label = "Min",
                            value = soilMin,
                            onValueChange = { soilMin = it },
                            modifier = Modifier.weight(1f)
                        )
                        NumberInput(
                            label = "Max",
                            value = soilMax,
                            onValueChange = { soilMax = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ThresholdSection(
                    title = "Sunlight Intensity",
                    icon = Icons.Filled.Bolt,
                    bgColor = Yellow50,
                    accentColor = Yellow500,
                    contentColor = Yellow600
                ) {
                    NumberInput(
                        label = "Min",
                        value = lightMin,
                        onValueChange = { lightMin = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.updateThresholds(
                        viewModel.thresholds.copy(
                            tempMin = tempMin.toDoubleOrNull() ?: viewModel.thresholds.tempMin,
                            tempMax = tempMax.toDoubleOrNull() ?: viewModel.thresholds.tempMax,
                            humidityMin = humidityMin.toDoubleOrNull() ?: viewModel.thresholds.humidityMin,
                            humidityMax = humidityMax.toDoubleOrNull() ?: viewModel.thresholds.humidityMax,
                            soilMoistureMin = soilMin.toDoubleOrNull() ?: viewModel.thresholds.soilMoistureMin,
                            soilMoistureMax = soilMax.toDoubleOrNull() ?: viewModel.thresholds.soilMoistureMax,
                            lightIntensityMin = lightMin.toDoubleOrNull() ?: viewModel.thresholds.lightIntensityMin
                        )
                    )
                    viewModel.saveThresholds()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Green600)
            ) {
                Text("Save Changes", color = White)
            }
            OutlinedButton(
                onClick = {
                    viewModel.resetThresholds()
                    tempMin = viewModel.thresholds.tempMin.toString()
                    tempMax = viewModel.thresholds.tempMax.toString()
                    humidityMin = viewModel.thresholds.humidityMin.toString()
                    humidityMax = viewModel.thresholds.humidityMax.toString()
                    soilMin = viewModel.thresholds.soilMoistureMin.toString()
                    soilMax = viewModel.thresholds.soilMoistureMax.toString()
                    lightMin = viewModel.thresholds.lightIntensityMin.toString()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset to Default")
            }
        }
    }
}

@Composable
private fun ThresholdSection(
    title: String,
    icon: ImageVector,
    bgColor: Color,
    accentColor: Color,
    contentColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun NumberInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnalyticsTab(viewModel: SettingsViewModel) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("day", "week", "month", "year").forEach { range ->
                FilterChip(
                    selected = viewModel.analyticsRange == range,
                    onClick = { viewModel.onRangeSelected(range) },
                    label = { Text(range.replaceFirstChar { it.uppercase() }) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Green100,
                        selectedLabelColor = Green700
                    )
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(viewModel.analyticsDate.toString())
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val analytics = listOf(
            Triple("Temperature", viewModel.temperatureAnalytics, Orange500),
            Triple("Humidity", viewModel.humidityAnalytics, Blue500),
            Triple("Soil Moisture", viewModel.soilMoistureAnalytics, Green500),
            Triple("Sunlight Intensity", viewModel.lightIntensityAnalytics, Yellow500)
        )

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val columns = if (maxWidth < 400.dp) 1 else 2
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (row in analytics.chunked(columns)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { (name, data, color) ->
                            SensorAnalyticsCard(
                                name = name,
                                analytics = data,
                                color = color,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(columns - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val context = LocalContext.current
        val csvLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("text/csv")
        ) { uri ->
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(viewModel.buildSensorCsv().toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Sensor data exported.", Toast.LENGTH_SHORT).show()
            }
        }

        Button(
            onClick = {
                csvLauncher.launch(
                    "tomatix_sensors_${System.currentTimeMillis()}.csv"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Green600)
        ) {
            Icon(
                imageVector = Icons.Filled.Download,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Data", color = White)
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = java.time.Instant.ofEpochMilli(millis)
                        val date = java.time.LocalDate.ofInstant(instant, java.time.ZoneId.systemDefault())
                        viewModel.onDateSelected(date)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SensorAnalyticsCard(
    name: String,
    analytics: SensorAnalytics,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (name) {
                        "Temperature" -> Icons.Filled.Thermostat
                        "Humidity" -> Icons.Filled.WaterDrop
                        "Soil Moisture" -> Icons.Filled.WaterDrop
                        else -> Icons.Filled.Bolt
                    },
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val hasData = analytics.chartData.isNotEmpty()
                StatItem("Avg", if (hasData) String.format("%.1f", analytics.avg) else "--")
                StatItem("Max", if (hasData) String.format("%.1f", analytics.max) else "--")
                StatItem("Min", if (hasData) String.format("%.1f", analytics.min) else "--")
            }
            Spacer(modifier = Modifier.height(8.dp))
            ChartPlaceholder(
                data = analytics.chartData,
                color = color,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Gray500
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Gray800
        )
    }
}

@Composable
private fun ChartPlaceholder(
    data: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = 0.3f), color.copy(alpha = 0.1f))
                )
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (data.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val maxVal = data.max()
                data.takeLast(12).forEach { value ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp * (value / maxVal))
                            .padding(horizontal = 1.dp)
                            .background(
                                color = color.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                            )
                    )
                }
            }
        } else {
            Text(
                text = "No data",
                style = MaterialTheme.typography.bodySmall,
                color = Gray400
            )
        }
    }
}

@Composable
private fun NotificationsTab(viewModel: SettingsViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Notification Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray800
                )
                Text(
                    text = "Choose how you want to receive alerts",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
                Spacer(modifier = Modifier.height(16.dp))

                NotificationToggle(
                    title = "Email Notifications",
                    subtitle = "Receive alerts via email",
                    icon = Icons.Filled.Email,
                    checked = viewModel.notifications.email,
                    onCheckedChange = {
                        viewModel.updateNotifications(viewModel.notifications.copy(email = it))
                    }
                )
                NotificationToggle(
                    title = "Push Notifications",
                    subtitle = "Receive push notifications on your device",
                    icon = Icons.Filled.Smartphone,
                    checked = viewModel.notifications.push,
                    onCheckedChange = {
                        viewModel.updateNotifications(viewModel.notifications.copy(push = it))
                    }
                )
                NotificationToggle(
                    title = "SMS Notifications",
                    subtitle = "Receive alerts via SMS",
                    icon = Icons.Filled.Sms,
                    checked = viewModel.notifications.sms,
                    onCheckedChange = {
                        viewModel.updateNotifications(viewModel.notifications.copy(sms = it))
                    }
                )
                NotificationToggle(
                    title = "Critical Only",
                    subtitle = "Only receive critical system alerts",
                    icon = Icons.Filled.Notifications,
                    checked = viewModel.notifications.criticalOnly,
                    onCheckedChange = {
                        viewModel.updateNotifications(viewModel.notifications.copy(criticalOnly = it))
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.saveNotifications() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Green600)
        ) {
            Text("Save Preferences", color = White)
        }
    }
}

@Composable
private fun NotificationToggle(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) Green600 else Gray400,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Gray800
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
        }
        androidx.compose.material3.Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedTrackColor = Green500,
                checkedThumbColor = White
            )
        )
    }
}

@Composable
private fun LogsTab(viewModel: SettingsViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "System Logs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray800
                )
                Text(
                    text = "${viewModel.mockLogs.size} entries",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (viewModel.mockLogs.isEmpty()) {
                    Text(
                        text = "No records yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500
                    )
                } else {
                    viewModel.mockLogs.forEach { log ->
                        LogEntry(log)
                        if (log != viewModel.mockLogs.last()) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.exportLogs() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Export Logs")
            }
            OutlinedButton(
                onClick = { viewModel.clearLogs() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Red500)
            ) {
                Text("Clear History")
            }
        }
    }
}

@Composable
private fun LogEntry(log: com.tomatix.app.data.model.SystemLog) {
    val (bgColor, iconColor, icon) = when (log.type) {
        "success" -> Triple(Green50, Green500, Icons.Filled.CheckCircle)
        "warning" -> Triple(Yellow50, Yellow500, Icons.Filled.Warning)
        "error" -> Triple(Red50, Red500, Icons.Filled.Error)
        else -> Triple(Blue50, Blue500, Icons.Filled.Sensors)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.event,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray800
                )
            }
            Text(
                text = log.time,
                style = MaterialTheme.typography.labelSmall,
                color = Gray500
            )
        }
    }
}
