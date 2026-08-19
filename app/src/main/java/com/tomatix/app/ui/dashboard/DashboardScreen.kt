package com.tomatix.app.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingFlat
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tomatix.app.data.model.DeviceStatus
import com.tomatix.app.data.model.SensorData
import com.tomatix.app.ui.theme.Blue100
import com.tomatix.app.ui.theme.Blue500
import com.tomatix.app.ui.theme.Green100
import com.tomatix.app.ui.theme.Green50
import com.tomatix.app.ui.theme.Green500
import com.tomatix.app.ui.theme.Green600
import com.tomatix.app.ui.theme.Gray200
import com.tomatix.app.ui.theme.Gray300
import com.tomatix.app.ui.theme.Gray400
import com.tomatix.app.ui.theme.Gray500
import com.tomatix.app.ui.theme.Gray600
import com.tomatix.app.ui.theme.Gray700
import com.tomatix.app.ui.theme.Orange100
import com.tomatix.app.ui.theme.Orange500
import com.tomatix.app.ui.theme.Red500
import com.tomatix.app.ui.theme.White
import com.tomatix.app.ui.theme.Yellow100
import com.tomatix.app.ui.theme.Yellow500

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val sensorData by viewModel.sensorData.collectAsStateWithLifecycle()
    val deviceStatus by viewModel.deviceStatus.collectAsStateWithLifecycle()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "alpha"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        item {
            Column(modifier = Modifier.fadeAlpha(alpha)) {
                Text(
                    text = "Welcome back, Farmer!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Gray700
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Here's what's happening in your greenhouse today.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500
                )
            }
        }

        item {
            Column(
                modifier = Modifier.fadeAlpha(alpha),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val columns = if (maxWidth >= 700.dp) 3 else 2
                    val items = listOf(
                        SensorItem(
                            title = "Temperature",
                            value = sensorData?.temperature?.let { String.format("%.1f", it) } ?: "--",
                            unit = "°C",
                            icon = Icons.Filled.DeviceThermostat,
                            iconBg = Orange100,
                            iconTint = Orange500,
                            idealRange = "Ideal: 21–27°C",
                            trend = viewModel.getTrend(sensorData?.temperature?.toFloat(), 21f, 27f)
                        ),
                        SensorItem(
                            title = "Humidity",
                            value = sensorData?.humidity?.let { String.format("%.1f", it) } ?: "--",
                            unit = "%",
                            icon = Icons.Filled.WaterDrop,
                            iconBg = Blue100,
                            iconTint = Blue500,
                            idealRange = "Ideal: 50–70%",
                            trend = viewModel.getTrend(sensorData?.humidity?.toFloat(), 50f, 70f),
                            progress = sensorData?.humidity?.let { (it.toFloat() / 100f).coerceIn(0f, 1f) }
                        ),
                        SensorItem(
                            title = "Incoming Sunlight",
                            value = sensorData?.lightIntensity?.let { String.format("%.1f", it / 1000.0) } ?: "--",
                            unit = "k lux",
                            icon = Icons.Filled.WbSunny,
                            iconBg = Yellow100,
                            iconTint = Yellow500,
                            idealRange = "6–10 k lux",
                            trend = viewModel.getTrend(sensorData?.lightIntensity?.toFloat(), 6000f, 10000f)
                        )
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items.chunked(columns).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                rowItems.forEach { item ->
                                    SensorCard(
                                        modifier = Modifier.weight(1f),
                                        item = item
                                    )
                                }
                                repeat(columns - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            SoilMoistureSection(
                viewModel = viewModel,
                sensorData = sensorData,
                alpha = alpha
            )
        }

        item {
            QuickControlsSection(
                deviceStatus = deviceStatus,
                alpha = alpha,
                onPumpToggle = viewModel::togglePump,
                onFanToggle = viewModel::toggleFan,
                onCameraToggle = viewModel::toggleCamera
            )
        }

        item {
            SystemStatusSection(deviceStatus = deviceStatus, alpha = alpha)
        }

        item {
            TrendsSection(viewModel = viewModel, alpha = alpha)
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

private fun Modifier.fadeAlpha(alpha: Float): Modifier = this.then(
    Modifier.drawWithContent {
        drawIntoCanvas { canvas ->
            val paint = androidx.compose.ui.graphics.Paint().apply {
                this.alpha = alpha
            }
            canvas.saveLayer(Rect(0f, 0f, size.width, size.height), paint)
            drawContent()
            canvas.restore()
        }
    }
)

private data class SensorItem(
    val title: String,
    val value: String,
    val unit: String,
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val idealRange: String,
    val trend: TrendDirection,
    val progress: Float? = null
)

@Composable
private fun SensorCard(
    modifier: Modifier = Modifier,
    item: SensorItem
) {
    Card(
        modifier = modifier
            .border(1.dp, Green100, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(item.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = item.value,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Gray700
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = item.unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray400,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = item.idealRange,
                style = MaterialTheme.typography.bodySmall,
                color = Gray400
            )

            if (item.progress != null) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { item.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = item.iconTint,
                    trackColor = item.iconBg,
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val (trendIcon, trendColor) = when (item.trend) {
                    TrendDirection.UP -> Icons.Outlined.TrendingUp to Green500
                    TrendDirection.DOWN -> Icons.Outlined.TrendingDown to Red500
                    TrendDirection.STABLE -> Icons.Outlined.TrendingFlat to Gray400
                }
                Icon(
                    imageVector = trendIcon,
                    contentDescription = null,
                    tint = trendColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                val badgeText = when (item.trend) {
                    TrendDirection.UP -> "High"
                    TrendDirection.DOWN -> "Low"
                    TrendDirection.STABLE -> "Normal"
                }
                val badgeBg = when (item.trend) {
                    TrendDirection.UP -> Green100
                    TrendDirection.DOWN -> Red500.copy(alpha = 0.12f)
                    TrendDirection.STABLE -> Gray200
                }
                val badgeFg = when (item.trend) {
                    TrendDirection.UP -> Green600
                    TrendDirection.DOWN -> Red500
                    TrendDirection.STABLE -> Gray500
                }
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeFg,
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun SoilMoistureSection(viewModel: DashboardViewModel, sensorData: SensorData?, alpha: Float) {
    val sensors = sensorData?.soilSensors ?: emptyList()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fadeAlpha(alpha)
            .border(1.dp, Green100, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Soil Moisture Sensors",
                style = MaterialTheme.typography.titleLarge,
                color = Gray700
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "12 sensors grouped in 4 plots",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
            Spacer(Modifier.height(12.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val columns = if (maxWidth >= 600.dp) 2 else 1
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    (1..4).chunked(columns).forEach { rowPlots ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowPlots.forEach { plotIndex ->
                                PlotCard(
                                    modifier = Modifier.weight(1f),
                                    viewModel = viewModel,
                                    plotIndex = plotIndex,
                                    sensors = sensors
                                )
                            }
                            repeat(columns - rowPlots.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlotCard(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel,
    plotIndex: Int,
    sensors: List<Double>
) {
    Card(
        modifier = modifier.border(1.dp, Green100, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Green50),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Green100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Grass,
                        contentDescription = null,
                        tint = Green600,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Plot $plotIndex",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Green600
                )
            }
            Spacer(Modifier.height(10.dp))
            for (i in 0 until 3) {
                val index = (plotIndex - 1) * 3 + i
                val value = sensors.getOrNull(index)
                SoilSensorBox(
                    sensorName = "sensor${index + 1}",
                    value = value,
                    trend = viewModel.getTrend(value?.toFloat(), 45f, 65f)
                )
                if (i < 2) {
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SoilSensorBox(sensorName: String, value: Double?, trend: TrendDirection) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Green100, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Green100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Grass,
                        contentDescription = sensorName,
                        tint = Green600,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = sensorName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value?.let { String.format("%.1f", it) } ?: "--",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (value != null) Gray700 else Gray400
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray400,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Ideal: 45–65%",
                style = MaterialTheme.typography.bodySmall,
                color = Gray400
            )

            if (value != null) {
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (value.toFloat() / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Green500,
                    trackColor = Green100
                )
            }

            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val (trendIcon, trendColor) = when (trend) {
                    TrendDirection.UP -> Icons.Outlined.TrendingUp to Green500
                    TrendDirection.DOWN -> Icons.Outlined.TrendingDown to Red500
                    TrendDirection.STABLE -> Icons.Outlined.TrendingFlat to Gray400
                }
                Icon(
                    imageVector = trendIcon,
                    contentDescription = null,
                    tint = trendColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                val badgeText = when (trend) {
                    TrendDirection.UP -> "High"
                    TrendDirection.DOWN -> "Low"
                    TrendDirection.STABLE -> "Normal"
                }
                val badgeBg = when (trend) {
                    TrendDirection.UP -> Green100
                    TrendDirection.DOWN -> Red500.copy(alpha = 0.12f)
                    TrendDirection.STABLE -> Gray200
                }
                val badgeFg = when (trend) {
                    TrendDirection.UP -> Green600
                    TrendDirection.DOWN -> Red500
                    TrendDirection.STABLE -> Gray500
                }
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeFg,
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickControlsSection(
    deviceStatus: DeviceStatus?,
    alpha: Float,
    onPumpToggle: () -> Unit,
    onFanToggle: () -> Unit,
    onCameraToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fadeAlpha(alpha)
            .border(1.dp, Green100, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quick Controls",
                style = MaterialTheme.typography.titleLarge,
                color = Gray700
            )
            Spacer(Modifier.height(12.dp))
            QuickControlRow(
                icon = Icons.Filled.WaterDrop,
                label = "Water Pump",
                checked = deviceStatus?.pumpStatus == true,
                backgroundColor = Blue100,
                accentColor = Blue500,
                onToggle = onPumpToggle
            )
            Spacer(Modifier.height(8.dp))
            QuickControlRow(
                icon = Icons.Filled.Grass,
                label = "Exhaust Fan",
                checked = deviceStatus?.fanStatus == true,
                backgroundColor = Green100,
                accentColor = Green500,
                onToggle = onFanToggle
            )
            Spacer(Modifier.height(8.dp))
            QuickControlRow(
                icon = Icons.Filled.Camera,
                label = "Camera Module",
                checked = deviceStatus?.cameraStatus == true,
                backgroundColor = Yellow100,
                accentColor = Yellow500,
                onToggle = onCameraToggle
            )
        }
    }
}

@Composable
private fun QuickControlRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    backgroundColor: Color,
    accentColor: Color,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Gray700,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = White,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = White,
                uncheckedTrackColor = Gray300
            )
        )
    }
}

@Composable
private fun SystemStatusSection(deviceStatus: DeviceStatus?, alpha: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fadeAlpha(alpha)
            .border(1.dp, Green100, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "System Status",
                style = MaterialTheme.typography.titleLarge,
                color = Gray700
            )
            Spacer(Modifier.height(12.dp))
            StatusItem(icon = Icons.Filled.WaterDrop, label = "Water Pump", active = deviceStatus?.pumpStatus)
            Spacer(Modifier.height(8.dp))
            StatusItem(icon = Icons.Filled.ElectricBolt, label = "Irrigation", active = deviceStatus?.irrigationStatus)
            Spacer(Modifier.height(8.dp))
            StatusItem(icon = Icons.Filled.Grass, label = "Exhaust Fan", active = deviceStatus?.fanStatus)
            Spacer(Modifier.height(8.dp))
            StatusItem(icon = Icons.Filled.Camera, label = "Camera", active = deviceStatus?.cameraStatus)
        }
    }
}

@Composable
private fun StatusItem(icon: ImageVector, label: String, active: Boolean?) {
    val dotColor = when (active) {
        true -> Green500
        false -> Gray300
        null -> Gray200
    }
    val statusText = when (active) {
        true -> "Active"
        false -> "Inactive"
        null -> "--"
    }
    val statusColor = when (active) {
        true -> Green600
        false -> Gray400
        null -> Gray400
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(12.dp))
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Gray500,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Gray700,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyMedium,
            color = statusColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TrendsSection(viewModel: DashboardViewModel, alpha: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fadeAlpha(alpha)
            .border(1.dp, Green100, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "24-Hour Trends",
                style = MaterialTheme.typography.titleLarge,
                color = Gray700
            )
            Spacer(Modifier.height(12.dp))

            val tempData = remember { viewModel.getTemperatureHistory() }
            val humidData = remember { viewModel.getHumidityHistory() }
            val soilData = remember { viewModel.getSoilMoistureHistory() }

            if (tempData.isEmpty() && humidData.isEmpty() && soilData.isEmpty()) {
                Text(
                    text = "No data available yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500
                )
            } else {
                MiniLineChart(
                    data = tempData,
                    lineColor = Orange500,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ChartLegend(color = Orange500, label = "Temp")
                    ChartLegend(color = Blue500, label = "Humidity")
                    ChartLegend(color = Green500, label = "Soil")
                }
                Spacer(Modifier.height(8.dp))
                MiniLineChart(
                    data = humidData,
                    lineColor = Blue500,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
                Spacer(Modifier.height(8.dp))
                MiniLineChart(
                    data = soilData,
                    lineColor = Green500,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
            }
        }
    }
}

@Composable
private fun MiniLineChart(
    data: List<DataPoint>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val values = data.map { it.value }
        val minVal = values.min()
        val maxVal = values.max()
        val range = (maxVal - minVal).coerceAtLeast(0.01f)

        val path = Path()
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)

        data.forEachIndexed { index, point ->
            val x = index * stepX
            val y = size.height - ((point.value - minVal) / range * size.height)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = lineColor.copy(alpha = 0.15f),
            style = Stroke(width = size.height * 0.3f, cap = StrokeCap.Round)
        )
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        data.lastOrNull()?.let { last ->
            val lastX = (data.size - 1) * stepX
            val lastY = size.height - ((last.value - minVal) / range * size.height)
            drawCircle(color = lineColor, radius = 4.dp.toPx(), center = Offset(lastX, lastY))
        }
    }
}

@Composable
private fun ChartLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Gray500
        )
    }
}
