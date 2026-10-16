package com.example.ui.manager

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.models.TariffConfig
import com.example.data.models.UserRole
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ManagerScreen(
    repository: SwiftRideRepository,
    modifier: Modifier = Modifier
) {
    val drivers by repository.drivers.collectAsState()
    val orders by repository.orders.collectAsState()
    val tariffs by repository.tariffs.collectAsState()
    val auditLogs by repository.auditLogs.collectAsState()

    // Manager Section Tabs: 0 = Overview & Fleet, 1 = Orders Dispatch, 2 = Drivers, 3 = Tariffs & Logs
    var selectedManagerTab by remember { mutableIntStateOf(0) }

    // Dialog States
    var showAddDriverDialog by remember { mutableStateOf(false) }
    var reassignOrderTarget by remember { mutableStateOf<BookingOrder?>(null) }
    var showTariffEditDialog by remember { mutableStateOf(false) }

    // Quick calculations for KPIs
    val onlineDriversCount = drivers.count { it.isOnline }
    val activeOrdersCount = orders.count {
        it.status == OrderStatus.ACCEPTED || it.status == OrderStatus.ARRIVED_AT_PICKUP || it.status == OrderStatus.IN_TRANSIT
    }
    val totalRevenue = orders.filter { it.status == OrderStatus.COMPLETED }.sumOf { it.fare }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("manager_screen_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Operations Control Center",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Real-time dispatch & fleet monitoring",
                        fontSize = 12.sp,
                        color = Slate700
                    )
                }

                Surface(
                    color = EmeraldContainer,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(color = EmeraldSuccess, shape = CircleShape, modifier = Modifier.size(8.dp)) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SYSTEM HEALTHY",
                            color = OnEmeraldContainer,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Navigation Tabs
        item {
            TabRow(
                selectedTabIndex = selectedManagerTab,
                containerColor = Slate100,
                contentColor = Slate900,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("manager_tab_row")
            ) {
                val tabs = listOf("Overview", "Dispatch (${orders.size})", "Fleet (${drivers.size})", "Tariffs & Logs")
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedManagerTab == index,
                        onClick = { selectedManagerTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedManagerTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }
        }

        // TAB 0: OVERVIEW & FLEET MAP
        if (selectedManagerTab == 0) {
            // Metrics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Online Fleet",
                        value = "$onlineDriversCount / ${drivers.size}",
                        subtitle = "${drivers.count { it.currentStatus == DriverStatus.ON_TRIP }} on active trips",
                        icon = Icons.Default.TwoWheeler,
                        iconTint = EmeraldSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "In-Transit",
                        value = "$activeOrdersCount",
                        subtitle = "Pending: ${orders.count { it.status == OrderStatus.SEARCHING_DRIVER }}",
                        icon = Icons.Default.NearMe,
                        iconTint = AmberDark,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "GMV Revenue",
                        value = String.format("$%.2f", totalRevenue),
                        subtitle = "${orders.count { it.status == OrderStatus.COMPLETED }} completed orders",
                        icon = Icons.Default.TrendingUp,
                        iconTint = CyanDark,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Avg Dispatch",
                        value = "1.8 min",
                        subtitle = "Surge: ${tariffs.surgeMultiplier}x",
                        icon = Icons.Default.Speed,
                        iconTint = AmberPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Live City Fleet Map
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Live Fleet Tracking", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                            Text("Green=Available • Amber=Busy", fontSize = 11.sp, color = Slate500)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        CityMapCanvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp),
                            activeDrivers = drivers,
                            showAllDrivers = true,
                            mapLabel = "City Fleet Radar"
                        )
                    }
                }
            }

            // Quick Status Table
            item {
                Text("Driver Fleet Status Snapshot", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
            }

            items(drivers) { d ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = when {
                                    !d.isOnline -> Color(0xFF94A3B8)
                                    d.currentStatus == DriverStatus.AVAILABLE -> EmeraldSuccess
                                    else -> AmberPrimary
                                },
                                shape = CircleShape,
                                modifier = Modifier.size(10.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(d.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${d.bikeModel} • ${d.licensePlate}", fontSize = 11.sp, color = Slate500)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(d.currentStatus.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            Text("Earned: $${String.format("%.2f", d.todayEarnings)}", fontSize = 10.sp, color = Slate500)
                        }
                    }
                }
            }
        }

        // TAB 1: DISPATCH & ORDER INTERVENTIONS
        if (selectedManagerTab == 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("All System Dispatches", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                    Text("${orders.size} Total Orders", fontSize = 12.sp, color = Slate500)
                }
            }

            items(orders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manager_order_card_${order.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (order.serviceType == ServiceType.BIKE_RIDE) Icons.Default.DirectionsBike else Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = if (order.serviceType == ServiceType.BIKE_RIDE) AmberDark else CyanDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(order.id, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("• ${order.serviceType.label}", fontSize = 12.sp, color = Slate500)
                            }
                            StatusBadge(status = order.status)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${order.pickupAddress} ➔ ${order.dropoffAddress}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                        Text(
                            "Customer: ${order.customerName} (${order.customerPhone}) • Fare: ${String.format("$%.2f", order.fare)}",
                            fontSize = 11.sp,
                            color = Slate700
                        )

                        if (order.assignedDriverName != null) {
                            Text("Assigned Driver: ${order.assignedDriverName} (${order.assignedDriverBike})", fontSize = 11.sp, color = AmberDark, fontWeight = FontWeight.Medium)
                        }

                        if (order.serviceType == ServiceType.PARCEL_DELIVERY) {
                            Text("Recipient: ${order.recipientName} • PIN: ${order.verificationPin}", fontSize = 11.sp, color = CyanDark)
                        }

                        // Manager Interventions
                        if (order.status != OrderStatus.COMPLETED && order.status != OrderStatus.CANCELLED) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Slate200)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { reassignOrderTarget = order },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("reassign_driver_button_${order.id}"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reassign Driver", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        repository.cancelOrder(order.id, UserRole.MANAGER, "Manager override cancellation")
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("manager_cancel_order_button_${order.id}"),
                                    colors = ButtonDefaults.buttonColors(containerColor = RoseError),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Force Cancel", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 2: FLEET MANAGEMENT
        if (selectedManagerTab == 2) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Driver Fleet Directory", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                    Button(
                        onClick = { showAddDriverDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_new_driver_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Onboard Driver", fontSize = 12.sp)
                    }
                }
            }

            items(drivers) { driver ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_card_${driver.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(driver.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    RatingStarsRow(rating = driver.rating)
                                }
                                Text("${driver.bikeModel} • Plate: ${driver.licensePlate}", fontSize = 12.sp, color = Slate700)
                                Text("ID: ${driver.id} • ${driver.phone}", fontSize = 11.sp, color = Slate500)
                            }

                            Surface(
                                color = if (driver.isApproved) EmeraldContainer else RoseContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (driver.isApproved) "APPROVED" else "SUSPENDED",
                                    color = if (driver.isApproved) OnEmeraldContainer else RoseError,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Total Trips: ${driver.totalTrips} • Earnings: $${String.format("%.2f", driver.todayEarnings)}",
                                fontSize = 11.sp,
                                color = Slate700
                            )

                            OutlinedButton(
                                onClick = { repository.managerToggleDriverApproval(driver.id) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (driver.isApproved) RoseError else EmeraldSuccess
                                ),
                                modifier = Modifier.testTag("toggle_approval_${driver.id}")
                            ) {
                                Text(if (driver.isApproved) "Suspend Driver" else "Approve Driver", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // TAB 3: TARIFFS & AUDIT LOGS
        if (selectedManagerTab == 3) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = AmberDark)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tariffs & Surge Controls", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            IconButton(
                                onClick = { showTariffEditDialog = true },
                                modifier = Modifier.testTag("edit_tariffs_button")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AmberDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Bike Ride: $${tariffs.bikeBaseFare} base + $${tariffs.bikePerKm}/km", fontSize = 13.sp, color = Slate700)
                        Text("• Parcel Delivery: $${tariffs.parcelBaseFare} base + $${tariffs.parcelPerKm}/km", fontSize = 13.sp, color = Slate700)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Active Surge: ", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Surface(
                                color = if (tariffs.surgeMultiplier > 1.0) Color(0xFFFEF3C7) else EmeraldContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "${tariffs.surgeMultiplier}x (${tariffs.surgeReason})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tariffs.surgeMultiplier > 1.0) Color(0xFF92400E) else OnEmeraldContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Surge Toggles
            item {
                Text("Set Surge Condition Shortcut", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Pair(1.0, "Normal Traffic"),
                        Pair(1.25, "Rush Hour"),
                        Pair(1.5, "Rain Storm"),
                        Pair(2.0, "Severe Demand")
                    ).forEach { (mult, label) ->
                        val isCurrent = tariffs.surgeMultiplier == mult
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) AmberPrimary else Slate100,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    repository.managerUpdateTariffs(
                                        tariffs.copy(surgeMultiplier = mult, surgeReason = label)
                                    )
                                }
                                .testTag("surge_shortcut_${(mult * 10).toInt()}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("${mult}x", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                                Text(label.take(8), fontSize = 10.sp, color = Slate700)
                            }
                        }
                    }
                }
            }

            // System Audit Logs
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("System Audit Logs", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                    Icon(Icons.Default.ListAlt, contentDescription = null, tint = Slate500, modifier = Modifier.size(18.dp))
                }
            }

            items(auditLogs) { log ->
                val timeStr = remember(log.timestamp) {
                    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(log.action, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                            Text(timeStr, fontSize = 10.sp, color = Slate500)
                        }
                        Text(log.details, fontSize = 11.sp, color = Slate700)
                        Text("Category: ${log.category}", fontSize = 10.sp, color = AmberDark)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Reassign Driver Dialog
    if (reassignOrderTarget != null) {
        val order = reassignOrderTarget!!
        AlertDialog(
            onDismissRequest = { reassignOrderTarget = null },
            title = { Text("Reassign Order ${order.id}", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select a driver to take over this dispatch:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    drivers.filter { it.isOnline && it.isApproved }.forEach { d ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    repository.managerReassignDriver(order.id, d.id)
                                    reassignOrderTarget = null
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(d.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${d.bikeModel} (${d.licensePlate})", fontSize = 11.sp, color = Slate500)
                            }
                            Text("Select ➔", fontSize = 12.sp, color = AmberDark, fontWeight = FontWeight.Bold)
                        }
                        Divider(color = Slate100)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { reassignOrderTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Driver Dialog
    if (showAddDriverDialog) {
        var newName by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("") }
        var newBike by remember { mutableStateOf("") }
        var newPlate by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDriverDialog = false },
            title = { Text("Onboard New Driver", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.testTag("new_driver_name_field")
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.testTag("new_driver_phone_field")
                    )
                    OutlinedTextField(
                        value = newBike,
                        onValueChange = { newBike = it },
                        label = { Text("Bike Model (e.g. Yamaha R3)") },
                        singleLine = true,
                        modifier = Modifier.testTag("new_driver_bike_field")
                    )
                    OutlinedTextField(
                        value = newPlate,
                        onValueChange = { newPlate = it },
                        label = { Text("License Plate (e.g. NY-4011)") },
                        singleLine = true,
                        modifier = Modifier.testTag("new_driver_plate_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newBike.isNotBlank()) {
                            repository.managerRegisterDriver(newName, newPhone, newBike, newPlate)
                            showAddDriverDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberDark),
                    modifier = Modifier.testTag("confirm_add_driver_button")
                ) {
                    Text("Register Driver")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDriverDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Tariffs Dialog
    if (showTariffEditDialog) {
        var bikeBase by remember { mutableStateOf(tariffs.bikeBaseFare.toString()) }
        var bikePerKm by remember { mutableStateOf(tariffs.bikePerKm.toString()) }
        var parcelBase by remember { mutableStateOf(tariffs.parcelBaseFare.toString()) }
        var parcelPerKm by remember { mutableStateOf(tariffs.parcelPerKm.toString()) }

        AlertDialog(
            onDismissRequest = { showTariffEditDialog = false },
            title = { Text("Edit Tariff Configuration", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bikeBase,
                        onValueChange = { bikeBase = it },
                        label = { Text("Bike Ride Base Fare ($)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = bikePerKm,
                        onValueChange = { bikePerKm = it },
                        label = { Text("Bike Ride Per Km ($)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = parcelBase,
                        onValueChange = { parcelBase = it },
                        label = { Text("Parcel Courier Base Fare ($)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = parcelPerKm,
                        onValueChange = { parcelPerKm = it },
                        label = { Text("Parcel Courier Per Km ($)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newBikeB = bikeBase.toDoubleOrNull() ?: tariffs.bikeBaseFare
                        val newBikeKm = bikePerKm.toDoubleOrNull() ?: tariffs.bikePerKm
                        val newParB = parcelBase.toDoubleOrNull() ?: tariffs.parcelBaseFare
                        val newParKm = parcelPerKm.toDoubleOrNull() ?: tariffs.parcelPerKm

                        repository.managerUpdateTariffs(
                            tariffs.copy(
                                bikeBaseFare = newBikeB,
                                bikePerKm = newBikeKm,
                                parcelBaseFare = newParB,
                                parcelPerKm = newParKm
                            )
                        )
                        showTariffEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberDark)
                ) {
                    Text("Save Tariffs")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTariffEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
