package com.tomatix.app.ui.controls

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tomatix.app.ui.theme.Blue100
import com.tomatix.app.ui.theme.Blue500
import com.tomatix.app.ui.theme.Blue600
import com.tomatix.app.ui.theme.Gray100
import com.tomatix.app.ui.theme.Gray200
import com.tomatix.app.ui.theme.Gray300
import com.tomatix.app.ui.theme.Gray400
import com.tomatix.app.ui.theme.Gray500
import com.tomatix.app.ui.theme.Gray600
import com.tomatix.app.ui.theme.Gray700
import com.tomatix.app.ui.theme.Gray800
import com.tomatix.app.ui.theme.Green500
import com.tomatix.app.ui.theme.Green600
import com.tomatix.app.ui.theme.Indigo500
import com.tomatix.app.ui.theme.Indigo600
import com.tomatix.app.ui.theme.Orange500
import com.tomatix.app.ui.theme.Orange600
import com.tomatix.app.ui.theme.Purple500
import com.tomatix.app.ui.theme.Purple600
import com.tomatix.app.ui.theme.Red500
import com.tomatix.app.ui.theme.Red600
import com.tomatix.app.ui.theme.White
import com.tomatix.app.ui.theme.Yellow500
import com.tomatix.app.ui.theme.Yellow600

private val cardShape = RoundedCornerShape(16.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlsScreen(
    viewModel: ControlsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            HeaderSection()
        }

        item {
            ManualModeAlert(
                isManual = uiState.manualMode,
                onToggle = viewModel::toggleManualMode
            )
        }

        item {
            ChemicalDistributionCard(
                uiState = uiState,
                viewModel = viewModel
            )
        }

        item {
            CameraModuleCard(
                uiState = uiState,
                viewModel = viewModel
            )
        }

        item {
            WaterPumpCard(
                isOn = uiState.pumpStatus,
                speed = uiState.pumpSpeed,
                onToggle = viewModel::togglePump,
                onSpeedChange = viewModel::setPumpSpeed
            )
        }

        item {
            IrrigationCard(
                isOn = uiState.irrigationStatus,
                onToggle = viewModel::toggleIrrigation,
                onCycle = viewModel::triggerIrrigationCycle
            )
        }

        item {
            ExhaustFanCard(
                isOn = uiState.fanStatus,
                speed = uiState.fanSpeed,
                onToggle = viewModel::toggleFan,
                onSpeedChange = viewModel::setFanSpeed
            )
        }

        item {
            GrowLightsCard(
                currentMode = uiState.growLightMode,
                onModeChange = viewModel::setGrowLightMode
            )
        }

        item {
            EmergencyControlsCard(
                onStopAll = viewModel::stopAll,
                onResetDefault = viewModel::resetToDefault
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun HeaderSection() {
    Column {
        Text(
            text = "Manual Controls",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Gray800
        )
        Text(
            text = "Manage your irrigation and ventilation systems",
            fontSize = 14.sp,
            color = Gray500,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ManualModeAlert(
    isManual: Boolean,
    onToggle: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isManual) Yellow500 else Gray200,
        label = "manualModeBg"
    )
    val contentColor = if (isManual) Gray800 else Gray600

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Manual Mode",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = contentColor
                    )
                    Text(
                        text = if (isManual) "Override active" else "System auto-managed",
                        fontSize = 12.sp,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
            Switch(
                checked = isManual,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = White,
                    checkedTrackColor = Green600,
                    uncheckedThumbColor = White,
                    uncheckedTrackColor = Gray400
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChemicalDistributionCard(
    uiState: ControlsUiState,
    viewModel: ControlsViewModel
) {
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CardHeader(
                title = "Chemical Distribution",
                subtitle = "Mix and distribute chemicals",
                iconColor = Orange500
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Chemical type dropdown
            Text("Chemical Type", fontSize = 14.sp, color = Gray600, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            ChemicalTypeDropdown(
                selected = uiState.chemicalType,
                onSelect = viewModel::setChemicalType
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mixing time input
            Text("Mixing Time (minutes)", fontSize = 14.sp, color = Gray600, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.mixingTime.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { viewModel.setMixingTime(it) }
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange500,
                    focusedTextColor = Gray800,
                    cursorColor = Orange500
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Concentration slider
            Text("Concentration: ${uiState.concentration}%", fontSize = 14.sp, color = Gray600, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = uiState.concentration.toFloat(),
                onValueChange = { viewModel.setConcentration(it.toInt()) },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Orange500,
                    activeTrackColor = Orange500,
                    inactiveTrackColor = Gray200
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0%", fontSize = 12.sp, color = Gray400)
                Text("100%", fontSize = 12.sp, color = Gray400)
            }

            // Timer display when mixing
            if (uiState.isMixing || uiState.isMixingPaused) {
                Spacer(modifier = Modifier.height(16.dp))
                MixingTimer(
                    remainingSeconds = uiState.mixingTimeRemaining,
                    totalSeconds = uiState.mixingTime * 60,
                    isPaused = uiState.isMixingPaused
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = viewModel::startMixing,
                    enabled = !uiState.isMixing,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Start Mix", fontSize = 13.sp)
                }
                Button(
                    onClick = viewModel::pauseMixing,
                    enabled = uiState.isMixing && !uiState.isMixingPaused,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Yellow500),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Pause", color = Gray800, fontSize = 13.sp)
                }
                Button(
                    onClick = viewModel::resumeMixing,
                    enabled = uiState.isMixingPaused,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Green500),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Resume", fontSize = 13.sp)
                }
                Button(
                    onClick = viewModel::stopMixing,
                    enabled = uiState.isMixing || uiState.isMixingPaused,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Red500),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Reset", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Distribution toggle
            DistributionToggle(
                isOn = uiState.chemicalDistributionStatus,
                onToggle = viewModel::toggleChemicalDistribution
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Safety guidelines alert
            SafetyGuidelinesAlert()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChemicalTypeDropdown(
    selected: String,
    onSelect: (String) -> Unit
) {
    val options = listOf("Fertilizer", "Pesticide", "Herbicide", "Fungicide")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Orange500,
                focusedTextColor = Gray800,
                cursorColor = Orange500
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun MixingTimer(
    remainingSeconds: Int,
    totalSeconds: Int,
    isPaused: Boolean
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val progress = if (totalSeconds > 0) {
        (totalSeconds - remainingSeconds).toFloat() / totalSeconds
    } else 0f

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPaused) Yellow500.copy(alpha = 0.1f) else Orange500.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "%02d:%02d".format(minutes, seconds),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPaused) Yellow600 else Orange600
            )
            Text(
                text = if (isPaused) "Mixing Paused" else "Mixing...",
                fontSize = 14.sp,
                color = Gray600
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isPaused) Yellow500 else Orange500,
                trackColor = Gray200
            )
        }
    }
}

@Composable
private fun DistributionToggle(
    isOn: Boolean,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOn) Orange500.copy(alpha = 0.1f) else Gray100
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = if (isOn) Orange500 else Gray400
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Chemical Distribution",
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Gray700
                )
            }
            Switch(
                checked = isOn,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = White,
                    checkedTrackColor = Orange500,
                    uncheckedThumbColor = White,
                    uncheckedTrackColor = Gray300
                )
            )
        }
    }
}

@Composable
private fun SafetyGuidelinesAlert() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Red500.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = Red500,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Safety Guidelines",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Red600
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Always wear protective equipment. Ensure proper ventilation. Follow chemical handling protocols.",
                    fontSize = 13.sp,
                    color = Gray600,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun CameraModuleCard(
    uiState: ControlsUiState,
    viewModel: ControlsViewModel
) {
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CardHeader(
                title = "Camera Module",
                subtitle = "Monitor your farm in real-time",
                iconColor = Indigo500
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Camera feed placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Gray800),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = null,
                        tint = Gray400,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Camera Feed",
                        color = Gray400,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Zoom slider
            Text("Zoom: ${uiState.cameraZoom}%", fontSize = 14.sp, color = Gray600, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = uiState.cameraZoom.toFloat(),
                onValueChange = { viewModel.setCameraZoom(it.toInt()) },
                valueRange = 50f..400f,
                colors = SliderDefaults.colors(
                    thumbColor = Indigo500,
                    activeTrackColor = Indigo500,
                    inactiveTrackColor = Gray200
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("50%", fontSize = 12.sp, color = Gray400)
                Text("400%", fontSize = 12.sp, color = Gray400)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Snapshot and refresh buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = viewModel::takeSnapshot,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Snapshot")
                }
                Button(
                    onClick = viewModel::refreshCamera,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refresh")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Camera position controls (3x3 grid)
            Text("Camera Position", fontSize = 14.sp, color = Gray600, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(12.dp))
            CameraPositionGrid(onMove = viewModel::moveCamera)
        }
    }
}

@Composable
private fun CameraPositionGrid(onMove: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: NW, N, NE
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DirectionButton(Icons.Filled.Explore, "NW") { onMove("NW") }
            DirectionButton(Icons.Filled.Explore, "N") { onMove("N") }
            DirectionButton(Icons.Filled.Explore, "NE") { onMove("NE") }
        }
        // Row 2: W, Center, E
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DirectionButton(Icons.Filled.Explore, "W") { onMove("W") }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Indigo500),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Explore,
                    contentDescription = "Center",
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }
            DirectionButton(Icons.Filled.Explore, "E") { onMove("E") }
        }
        // Row 3: SW, S, SE
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DirectionButton(Icons.Filled.Explore, "SW") { onMove("SW") }
            DirectionButton(Icons.Filled.Explore, "S") { onMove("S") }
            DirectionButton(Icons.Filled.Explore, "SE") { onMove("SE") }
        }
    }
}

@Composable
private fun DirectionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Gray100)
            .border(1.dp, Gray200, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Gray600,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun WaterPumpCard(
    isOn: Boolean,
    speed: Int,
    onToggle: () -> Unit,
    onSpeedChange: (Int) -> Unit
) {
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CardHeader(
                title = "Water Pump",
                subtitle = "Control water supply",
                iconColor = Blue500
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isOn) "Pump Active" else "Pump Off",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isOn) Green600 else Gray500
                )
                Switch(
                    checked = isOn,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = White,
                        checkedTrackColor = Blue500,
                        uncheckedThumbColor = White,
                        uncheckedTrackColor = Gray300
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Speed: $speed%", fontSize = 14.sp, color = Gray600, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = speed.toFloat(),
                onValueChange = { onSpeedChange(it.toInt()) },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Blue500,
                    activeTrackColor = Blue500,
                    inactiveTrackColor = Gray200
                )
            )
        }
    }
}

@Composable
private fun IrrigationCard(
    isOn: Boolean,
    onToggle: () -> Unit,
    onCycle: (String) -> Unit
) {
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CardHeader(
                title = "Irrigation",
                subtitle = "Quick irrigation cycles",
                iconColor = Green500
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isOn) "Irrigating" else "Irrigation Off",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isOn) Green600 else Gray500
                )
                Switch(
                    checked = isOn,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = White,
                        checkedTrackColor = Green500,
                        uncheckedThumbColor = White,
                        uncheckedTrackColor = Gray300
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CycleButton("Quick", Green500) { onCycle("quick") }
                CycleButton("Medium", Blue500) { onCycle("medium") }
                CycleButton("Deep", Indigo500) { onCycle("deep") }
            }
        }
    }
}

@Composable
private fun RowScope.CycleButton(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(label, fontSize = 13.sp)
    }
}

@Composable
private fun ExhaustFanCard(
    isOn: Boolean,
    speed: Int,
    onToggle: () -> Unit,
    onSpeedChange: (Int) -> Unit
) {
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CardHeader(
                title = "Exhaust Fan",
                subtitle = "Ventilation control",
                iconColor = Purple500
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isOn) "Fan Running" else "Fan Off",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isOn) Purple600 else Gray500
                )
                Switch(
                    checked = isOn,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = White,
                        checkedTrackColor = Purple500,
                        uncheckedThumbColor = White,
                        uncheckedTrackColor = Gray300
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Speed: $speed%", fontSize = 14.sp, color = Gray600, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = speed.toFloat(),
                onValueChange = { onSpeedChange(it.toInt()) },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Purple500,
                    activeTrackColor = Purple500,
                    inactiveTrackColor = Gray200
                )
            )
        }
    }
}

@Composable
private fun GrowLightsCard(
    currentMode: String,
    onModeChange: (String) -> Unit
) {
    val modes = listOf("Sunrise", "Full Sun", "Sunset", "Night")
    val modeColors = listOf(Yellow500, Orange500, Orange600, Blue500)

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CardHeader(
                title = "Grow Lights",
                subtitle = "Adjust lighting mode",
                iconColor = Yellow500
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                modes.forEachIndexed { index, mode ->
                    val isActive = currentMode == mode
                    val color = modeColors[index]

                    Button(
                        onClick = { onModeChange(mode) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) color else Gray100,
                            contentColor = if (isActive) White else Gray600
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = mode,
                            fontSize = 12.sp,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmergencyControlsCard(
    onStopAll: () -> Unit,
    onResetDefault: () -> Unit
) {
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Red500),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Emergency Controls",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Immediately stop or reset all systems",
                fontSize = 13.sp,
                color = White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onStopAll,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Red600),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Stop All", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Button(
                    onClick = onResetDefault,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "Reset to Default",
                        color = Red600,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CardHeader(
    title: String,
    subtitle: String,
    iconColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (title) {
                    "Chemical Distribution" -> Icons.Filled.CheckCircle
                    "Camera Module" -> Icons.Filled.CameraAlt
                    "Water Pump" -> Icons.Filled.Explore
                    "Irrigation" -> Icons.Filled.CheckCircle
                    "Exhaust Fan" -> Icons.Filled.Explore
                    "Grow Lights" -> Icons.Filled.CheckCircle
                    else -> Icons.Filled.CheckCircle
                },
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = Gray800
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Gray500
            )
        }
    }
}
