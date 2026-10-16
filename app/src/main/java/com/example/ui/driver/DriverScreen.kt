package com.example.ui.driver

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.BookingOrder
import com.example.data.models.Driver
import com.example.data.models.DriverStatus
import com.example.data.models.OrderStatus
import com.example.data.models.ServiceType
import com.example.data.repository.SwiftRideRepository
import com.example.ui.components.CityMapCanvas
import com.example.ui.components.MetricCard
import com.example.ui.components.RatingStarsRow
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanDark
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun DriverScreen(
    repository: SwiftRideRepository,
    modifier: Modifier = Modifier
) {
    val drivers by repository.drivers.collectAsState()
    val activeDriverId by repository.activeDriverId.collectAsState()
    val orders by repository.orders.collectAsState()

    val currentDriver = drivers.find { it.id == activeDriverId } ?: drivers.first()

    // Find any order currently assigned to this driver that is in progress
    val activeTrip = orders.find {
        it.assignedDriverId == currentDriver.id &&
                (it.status == OrderStatus.ACCEPTED ||
                        it.status == OrderStatus.ARRIVED_AT_PICKUP ||
                        it.status == OrderStatus.IN_TRANSIT)
    }

    // Find pending orders waiting for a driver (incoming job alerts)
    val incomingRequests = orders.filter { it.status == OrderStatus.SEARCHING_DRIVER }

    var showDriverPicker by remember { mutableStateOf(false) }
    var pinVerificationInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var showChatDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("driver_screen_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Driver Profile & Online/Offline Switcher
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Driver Info & Avatar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showDriverPicker = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(AmberPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TwoWheeler,
                                    contentDescription = null,
                                    tint = Slate900,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentDriver.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("▼", color = AmberPrimary, fontSize = 10.sp)
                                }
                                Text(
                                    text = "${currentDriver.bikeModel} • ${currentDriver.licensePlate}",
                                    color = Slate300,
                                    fontSize = 11.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RatingStarsRow(rating = currentDriver.rating)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("• ${currentDriver.totalTrips} trips", color = Slate300, fontSize = 11.sp)
                                }
                            }
                        }

                        // Online / Offline Switch
                        Column(horizontalAlignment = Alignment.End) {
                            Switch(
                                checked = currentDriver.isOnline,
                                onCheckedChange = { repository.driverToggleOnline(currentDriver.id) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = EmeraldSuccess,
                                    uncheckedThumbColor = Slate500,
                                    uncheckedTrackColor = Slate700
                                ),
                                modifier = Modifier.testTag("driver_online_toggle")
                            )
                            Text(
                                text = if (currentDriver.isOnline) "ONLINE" else "OFFLINE",
                                color = if (currentDriver.isOnline) EmeraldSuccess else Slate500,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Dropdown to switch demo drivers
                    DropdownMenu(
                        expanded = showDriverPicker,
                        onDismissRequest = { showDriverPicker = false }
                    ) {
                        drivers.forEach { d ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(d.name, fontWeight = FontWeight.Bold)
                                        Text("${d.bikeModel} (${d.licensePlate})", fontSize = 11.sp, color = Slate500)
                                    }
                                },
                                onClick = {
                                    repository.setActiveDriver(d.id)
                                    showDriverPicker = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // 2. Earnings & Shifts Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Today's Net",
                    value = String.format("$%.2f", currentDriver.todayEarnings),
                    subtitle = "82% net fare payout",
                    icon = Icons.Default.FlashOn,
                    iconTint = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Acceptance",
                    value = "${currentDriver.acceptanceRate}%",
                    subtitle = "Tier 1 Rider status",
                    icon = Icons.Default.CheckCircle,
                    iconTint = AmberDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Active Trip Navigation Workflow (If driver has accepted an order)
        if (activeTrip != null) {
            item {
                ActiveTripNavigationCard(
                    order = activeTrip,
                    onAdvanceStep = {
                        pinError = false
                        repository.driverAdvanceTripStep(activeTrip.id)
                    },
                    pinVerificationInput = pinVerificationInput,
                    onPinChange = { pinVerificationInput = it },
                    onVerifyPin = {
                        if (pinVerificationInput == activeTrip.verificationPin) {
                            pinError = false
                            repository.driverAdvanceTripStep(activeTrip.id)
                        } else {
                            pinError = true
                        }
                    },
                    pinError = pinError,
                    onChatClick = { showChatDialog = true }
                )
            }
        } else if (!currentDriver.isOnline) {
            // 4. Offline State prompt
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate100)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("You are currently Offline", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                        Text(
                            "Toggle the switch above to go Online and receive nearby bike rides and parcel delivery jobs.",
                            fontSize = 12.sp,
                            color = Slate700,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        } else {
            // 5. Online & Listening for Dispatches
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                color = EmeraldSuccess,
                                shape = CircleShape,
                                modifier = Modifier.size(10.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Radar Active • Ready for Requests",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = EmeraldSuccess
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Stay in busy hubs (Downtown, Metro Station) for faster dispatch.",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }
            }

            // Incoming Broadcast Requests
            if (incomingRequests.isNotEmpty()) {
                item {
                    Text(
                        "Incoming Job Radar (${incomingRequests.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Slate900
                    )
                }

                items(incomingRequests) { req ->
                    IncomingJobAlertCard(
                        order = req,
                        onAccept = { repository.assignDriverToOrder(req.id, currentDriver) },
                        onDecline = { repository.cancelOrder(req.id, com.example.data.models.UserRole.DRIVER, "Declined by driver") }
                    )
                }
            }
        }

        // 6. Recent Completed Jobs for this Driver
        val driverCompleted = orders.filter {
            it.assignedDriverId == currentDriver.id && it.status == OrderStatus.COMPLETED
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Completed Jobs Today", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
        }

        if (driverCompleted.isEmpty()) {
            item {
                Text(
                    "No completed trips today yet. Turn Online to start earning!",
                    fontSize = 12.sp,
                    color = Slate500,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(driverCompleted) { comp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (comp.serviceType == ServiceType.BIKE_RIDE) Icons.Default.DirectionsBike else Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = if (comp.serviceType == ServiceType.BIKE_RIDE) AmberDark else CyanDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(comp.id, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text("${comp.pickupAddress} ➔ ${comp.dropoffAddress}", fontSize = 12.sp, color = Slate700)
                            Text("${comp.distanceKm} km • Passenger: ${comp.customerName}", fontSize = 10.sp, color = Slate500)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            val driverPayout = Math.round(comp.fare * 0.82 * 100.0) / 100.0
                            Text(
                                "+$${String.format("%.2f", driverPayout)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = EmeraldSuccess
                            )
                            Text("Payout Net", fontSize = 10.sp, color = Slate500)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Driver Chat Dialog
    if (showChatDialog && activeTrip != null) {
        var driverMsg by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showChatDialog = false },
            title = { Text("Message Passenger / Sender", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Order: ${activeTrip.id} • ${activeTrip.customerName}", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    listOf(
                        "I am arriving in 2 minutes.",
                        "I am at the main entrance / pickup gate.",
                        "Heavy traffic delay around the corner."
                    ).forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Slate100,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { driverMsg = preset }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(preset, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = driverMsg,
                        onValueChange = { driverMsg = it },
                        label = { Text("Custom message") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showChatDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberDark)
                ) {
                    Text("Send Message")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChatDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun IncomingJobAlertCard(
    order: BookingOrder,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("incoming_job_card_${order.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (order.serviceType == ServiceType.BIKE_RIDE) AmberPrimary else CyanAccent,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.serviceType.label.uppercase(),
                        color = Slate900,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                val driverEarnings = Math.round(order.fare * 0.82 * 100.0) / 100.0
                Text(
                    text = "Earn +$${String.format("%.2f", driverEarnings)}",
                    color = EmeraldSuccess,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${order.pickupAddress} ➔ ${order.dropoffAddress}",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${order.distanceKm} km • ~${order.estimatedMinutes} mins • Customer: ${order.customerName}",
                color = Slate300,
                fontSize = 12.sp
            )

            if (order.serviceType == ServiceType.PARCEL_DELIVERY) {
                Text(
                    text = "Package: ${order.parcelCategory?.title ?: "Standard"} (${order.parcelWeightKg} kg)",
                    color = CyanAccent,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("decline_job_button_${order.id}"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300)
                ) {
                    Text("Decline")
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("accept_job_button_${order.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Text("Accept Request 🏍️", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ActiveTripNavigationCard(
    order: BookingOrder,
    onAdvanceStep: () -> Unit,
    pinVerificationInput: String,
    onPinChange: (String) -> Unit,
    onVerifyPin: () -> Unit,
    pinError: Boolean,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("active_trip_navigation_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Navigation, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ACTIVE DISPATCH", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                StatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step details
            val phaseTitle = when (order.status) {
                OrderStatus.ACCEPTED -> "1. Heading to Pickup Location"
                OrderStatus.ARRIVED_AT_PICKUP -> "2. Waiting at Pickup Point"
                OrderStatus.IN_TRANSIT -> "3. In Transit to Destination"
                else -> "Job Completed"
            }
            Text(phaseTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)

            val targetAddress = if (order.status == OrderStatus.ACCEPTED || order.status == OrderStatus.ARRIVED_AT_PICKUP) {
                order.pickupAddress
            } else {
                order.dropoffAddress
            }
            Text(targetAddress, color = AmberPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Contact & Chat
            Surface(
                color = Slate800,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(order.customerName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(order.customerPhone, color = Slate300, fontSize = 11.sp)
                    }
                    IconButton(
                        onClick = onChatClick,
                        modifier = Modifier.testTag("driver_chat_passenger_button")
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "Chat", tint = AmberPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Parcel PIN Verification Prompt if in Transit
            if (order.serviceType == ServiceType.PARCEL_DELIVERY && order.status == OrderStatus.IN_TRANSIT) {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Security Handover PIN Required", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Text(
                            "Ask recipient for their 4-digit verification PIN (Demo PIN: ${order.verificationPin})",
                            color = Slate300,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = pinVerificationInput,
                                onValueChange = onPinChange,
                                placeholder = { Text("4-digit PIN") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("driver_pin_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onVerifyPin,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                modifier = Modifier.testTag("verify_pin_button")
                            ) {
                                Text("Verify & Handover")
                            }
                        }
                        if (pinError) {
                            Text("Invalid PIN code. Please verify with recipient.", color = RoseError, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Primary Stepper Action Button
            val nextActionLabel = when (order.status) {
                OrderStatus.ACCEPTED -> "Tap: Arrived at Pickup Spot 📍"
                OrderStatus.ARRIVED_AT_PICKUP -> if (order.serviceType == ServiceType.BIKE_RIDE) "Passenger Onboard - Start Ride 🏍️" else "Parcel Collected - Start Transit 📦"
                OrderStatus.IN_TRANSIT -> if (order.serviceType == ServiceType.BIKE_RIDE) "Arrived at Destination & Complete Trip ✅" else "Arrived at Recipient Destination 📍"
                else -> "Complete"
            }

            Button(
                onClick = onAdvanceStep,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("driver_advance_step_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (order.status == OrderStatus.IN_TRANSIT) EmeraldSuccess else AmberDark
                )
            ) {
                Text(
                    text = nextActionLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}
