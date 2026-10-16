package com.example.ui.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TakeoutDining
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.data.models.BikeTier
import com.example.data.models.BookingOrder
import com.example.data.models.CityLocation
import com.example.data.models.OrderStatus
import com.example.data.models.ParcelCategory
import com.example.data.models.ServiceType
import com.example.data.models.UserRole
import com.example.data.repository.SwiftRideRepository
import com.example.ui.components.CityMapCanvas
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(
    repository: SwiftRideRepository,
    modifier: Modifier = Modifier
) {
    val orders by repository.orders.collectAsState()
    val walletBalance by repository.walletBalance.collectAsState()
    val tariffs by repository.tariffs.collectAsState()

    // Service Selection: 0 = Bike Ride, 1 = Parcel Delivery
    var selectedServiceTab by remember { mutableIntStateOf(0) }

    // Predefined Locations
    val locations = repository.predefinedLocations
    var pickupLocation by remember { mutableStateOf(locations[0]) }
    var dropoffLocation by remember { mutableStateOf(locations[1]) }

    // Bike Booking State
    var selectedBikeTier by remember { mutableStateOf(BikeTier.STANDARD) }
    var paymentMethod by remember { mutableStateOf("SwiftPay Wallet") }

    // Parcel Booking State
    var selectedParcelCategory by remember { mutableStateOf(ParcelCategory.DOCUMENTS) }
    var parcelWeightKg by remember { mutableDoubleStateOf(1.5) }
    var recipientName by remember { mutableStateOf("Emma Watson") }
    var recipientPhone by remember { mutableStateOf("+1 (555) 831-2940") }
    var parcelNotes by remember { mutableStateOf("Please ring apartment bell #402") }

    // Active order check (most recent non-completed order)
    val activeOrder = orders.firstOrNull {
        it.status != OrderStatus.COMPLETED && it.status != OrderStatus.CANCELLED
    }

    // Dialogs
    var showWalletDialog by remember { mutableStateOf(false) }
    var showChatDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var ratingOrderTarget by remember { mutableStateOf<BookingOrder?>(null) }
    var ratingStars by remember { mutableIntStateOf(5) }
    var ratingReviewText by remember { mutableStateOf("") }

    val distanceKm = remember(pickupLocation, dropoffLocation) {
        repository.calculateDistanceKm(pickupLocation.coords, dropoffLocation.coords)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("customer_screen_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Wallet & Welcome Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, Jordan 👋",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Where are you heading today?",
                        fontSize = 13.sp,
                        color = Slate700
                    )
                }

                // Wallet Pill
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .clickable { showWalletDialog = true }
                        .testTag("wallet_balance_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = AmberPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format("$%.2f", walletBalance),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(AmberPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Funds",
                                tint = Slate900,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Active Order Banner (if in progress)
        if (activeOrder != null) {
            item {
                ActiveOrderCard(
                    order = activeOrder,
                    onAdvanceStep = { repository.driverAdvanceTripStep(activeOrder.id) },
                    onCancelClick = { showCancelDialog = true },
                    onChatClick = { showChatDialog = true }
                )
            }
        }

        // 3. Service Type Tabs (Bike vs Parcel)
        item {
            TabRow(
                selectedTabIndex = selectedServiceTab,
                containerColor = Slate100,
                contentColor = Slate900,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("service_tab_row")
            ) {
                Tab(
                    selected = selectedServiceTab == 0,
                    onClick = { selectedServiceTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.DirectionsBike,
                                contentDescription = null,
                                tint = if (selectedServiceTab == 0) AmberDark else Slate700,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Book Bike Ride",
                                fontWeight = if (selectedServiceTab == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedServiceTab == 1,
                    onClick = { selectedServiceTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = if (selectedServiceTab == 1) CyanDark else Slate700,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Send Parcel",
                                fontWeight = if (selectedServiceTab == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                )
            }
        }

        // 4. Interactive Map with Route Preview
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    CityMapCanvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        pickupCoords = pickupLocation.coords,
                        dropoffCoords = dropoffLocation.coords,
                        driverProgress = activeOrder?.driverProgress ?: 0f,
                        locations = locations,
                        selectedLocation = dropoffLocation,
                        mapLabel = if (selectedServiceTab == 0) "Bike Route Preview" else "Delivery Route Preview"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = EmeraldContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${distanceKm} km",
                                    color = OnEmeraldContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            val estTime = (distanceKm * 2.2).toInt() + 4
                            Text(
                                text = "~$estTime mins estimated arrival",
                                fontSize = 12.sp,
                                color = Slate700
                            )
                        }

                        if (tariffs.surgeMultiplier > 1.0) {
                            Surface(
                                color = Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "⚡ ${tariffs.surgeMultiplier}x Surge",
                                    color = Color(0xFF92400E),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Pickup & Dropoff Address Pickers
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Pickup Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(EmeraldContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.NearMe,
                                contentDescription = "Pickup",
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("PICKUP LOCATION", fontSize = 10.sp, color = Slate500, fontWeight = FontWeight.Bold)
                            Text(pickupLocation.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            Text(pickupLocation.address, fontSize = 11.sp, color = Slate700)
                        }
                    }

                    // Divider with Swap button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 42.dp),
                            color = Slate200
                        )
                        IconButton(
                            onClick = {
                                val temp = pickupLocation
                                pickupLocation = dropoffLocation
                                dropoffLocation = temp
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("swap_locations_button")
                        ) {
                            Icon(Icons.Default.SwapVert, contentDescription = "Swap", tint = AmberDark)
                        }
                    }

                    // Dropoff Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFFEF3C7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Dropoff",
                                tint = AmberDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("DROPOFF DESTINATION", fontSize = 10.sp, color = Slate500, fontWeight = FontWeight.Bold)
                            Text(dropoffLocation.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            Text(dropoffLocation.address, fontSize = 11.sp, color = Slate700)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Location Quick Chips
                    Text("Select Destination Shortcut:", fontSize = 11.sp, color = Slate500, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        locations.forEach { loc ->
                            val isSelected = dropoffLocation.name == loc.name
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AmberPrimary else Slate100,
                                modifier = Modifier
                                    .clickable { dropoffLocation = loc }
                                    .testTag("location_chip_${loc.category.lowercase()}")
                            ) {
                                Text(
                                    text = loc.name.take(18),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Slate900 else Slate700,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Mode Specific Content: BIKE RIDE
        if (selectedServiceTab == 0) {
            item {
                Text("Select Bike Experience", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
            }

            items(BikeTier.values()) { tier ->
                val isSelected = selectedBikeTier == tier
                val tierFare = repository.calculateFare(ServiceType.BIKE_RIDE, distanceKm, bikeTier = tier)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedBikeTier = tier }
                        .testTag("bike_tier_${tier.name.lowercase()}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, AmberDark) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(if (isSelected) AmberDark else Slate100, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBike,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Slate900,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tier.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                                if (tier == BikeTier.ECO) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = EmeraldContainer, shape = RoundedCornerShape(4.dp)) {
                                        Text("ECO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = OnEmeraldContainer, modifier = Modifier.padding(horizontal = 4.dp))
                                    }
                                }
                            }
                            Text(tier.description, fontSize = 12.sp, color = Slate700)
                            Text("Helmet & Sanitized Hairnet provided", fontSize = 10.sp, color = EmeraldSuccess)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format("$%.2f", tierFare),
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Slate900
                            )
                            Text("est. 3 min away", fontSize = 10.sp, color = Slate500)
                        }
                    }
                }
            }

            // Booking Action
            item {
                val totalFare = repository.calculateFare(ServiceType.BIKE_RIDE, distanceKm, bikeTier = selectedBikeTier)
                Button(
                    onClick = {
                        repository.createBikeBooking(
                            pickup = pickupLocation,
                            dropoff = dropoffLocation,
                            tier = selectedBikeTier,
                            paymentMethod = paymentMethod
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_bike_ride_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberDark)
                ) {
                    Icon(Icons.Default.DirectionsBike, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Book ${selectedBikeTier.title} • ${String.format("$%.2f", totalFare)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        } else {
            // 7. Mode Specific Content: PARCEL DELIVERY
            item {
                Text("Parcel Details & Handling", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
            }

            // Parcel Category Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ParcelCategory.values().forEach { cat ->
                        val isSelected = selectedParcelCategory == cat
                        val catIcon = when (cat) {
                            ParcelCategory.DOCUMENTS -> Icons.Default.Description
                            ParcelCategory.SMALL_BOX -> Icons.Default.Inventory
                            ParcelCategory.FOOD_FRAGILE -> Icons.Default.Fastfood
                            ParcelCategory.HEAVY_CARRIER -> Icons.Default.LocalShipping
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedParcelCategory = cat }
                                .testTag("parcel_category_${cat.name.lowercase()}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFE0F2FE) else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, CyanDark) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(if (isSelected) CyanDark else Slate100, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = catIcon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else Slate900,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cat.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                                    Text(cat.description, fontSize = 11.sp, color = Slate700)
                                }
                                Text("Max ${cat.maxWeightKg.toInt()} kg", fontSize = 11.sp, color = Slate500)
                            }
                        }
                    }
                }
            }

            // Weight & Recipient Form
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Estimated Weight", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            Text(
                                "${String.format("%.1f", parcelWeightKg)} kg",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanDark
                            )
                        }
                        Slider(
                            value = parcelWeightKg.toFloat(),
                            onValueChange = { parcelWeightKg = it.toDouble() },
                            valueRange = 0.5f..15.0f,
                            steps = 28,
                            modifier = Modifier.testTag("parcel_weight_slider")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = recipientName,
                            onValueChange = { recipientName = it },
                            label = { Text("Recipient Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("recipient_name_field"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = recipientPhone,
                            onValueChange = { recipientPhone = it },
                            label = { Text("Recipient Phone Number") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("recipient_phone_field"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = parcelNotes,
                            onValueChange = { parcelNotes = it },
                            label = { Text("Delivery Instructions / Notes") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("parcel_notes_field"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Security Handover notice
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = AmberDark, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "A 4-digit Security Verification PIN will be generated. The courier must confirm this PIN before handing over the parcel.",
                                    fontSize = 11.sp,
                                    color = Slate700
                                )
                            }
                        }
                    }
                }
            }

            // Parcel Submit Button
            item {
                val parcelFare = repository.calculateFare(
                    ServiceType.PARCEL_DELIVERY,
                    distanceKm,
                    parcelCategory = selectedParcelCategory,
                    parcelWeightKg = parcelWeightKg
                )

                Button(
                    onClick = {
                        repository.createParcelDelivery(
                            pickup = pickupLocation,
                            dropoff = dropoffLocation,
                            category = selectedParcelCategory,
                            weightKg = parcelWeightKg,
                            recipientName = recipientName,
                            recipientPhone = recipientPhone,
                            notes = parcelNotes,
                            paymentMethod = paymentMethod
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("send_parcel_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanDark)
                ) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Request Parcel Courier • ${String.format("$%.2f", parcelFare)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // 8. Past Trips & Deliveries History
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Activity & Receipts", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Icon(Icons.Default.History, contentDescription = null, tint = Slate500, modifier = Modifier.size(18.dp))
            }
        }

        val completedOrders = orders.filter { it.status == OrderStatus.COMPLETED || it.status == OrderStatus.CANCELLED }
        if (completedOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate100),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "No past orders yet. Book a ride or send a parcel to get started!",
                        fontSize = 12.sp,
                        color = Slate500,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(completedOrders) { pastOrder ->
                PastOrderCard(
                    order = pastOrder,
                    onRateClick = {
                        ratingOrderTarget = pastOrder
                        ratingStars = pastOrder.rating ?: 5
                        ratingReviewText = pastOrder.feedback ?: ""
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Wallet Top Up Dialog
    if (showWalletDialog) {
        AlertDialog(
            onDismissRequest = { showWalletDialog = false },
            title = { Text("SwiftPay Wallet", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Current Balance: ${String.format("$%.2f", walletBalance)}", fontSize = 16.sp, color = Slate700)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Select Top-Up Amount:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10.0, 25.0, 50.0).forEach { amt ->
                            OutlinedButton(
                                onClick = {
                                    repository.topUpWallet(amt)
                                    showWalletDialog = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+$${amt.toInt()}")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWalletDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Cancel Order Dialog
    if (showCancelDialog && activeOrder != null) {
        var cancelReason by remember { mutableStateOf("Changed my mind") }
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Order?", fontWeight = FontWeight.Bold, color = RoseError) },
            text = {
                Column {
                    Text("Are you sure you want to cancel order ${activeOrder.id}? Free cancellation applies before driver arrives.", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    listOf("Changed my mind", "Driver taking too long", "Wrong address entered").forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { cancelReason = r }
                                .padding(vertical = 4.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (cancelReason == r) AmberDark else Slate300,
                                modifier = Modifier.size(14.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(r, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.cancelOrder(activeOrder.id, UserRole.CUSTOMER, cancelReason)
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                ) {
                    Text("Confirm Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Order")
                }
            }
        )
    }

    // Quick Driver Chat Dialog
    if (showChatDialog && activeOrder != null) {
        var chatInput by remember { mutableStateOf("") }
        var messages by remember {
            mutableStateOf(
                listOf(
                    Pair("Driver", "Hello! I am on my way with a yellow helmet."),
                    Pair("System", "Order matched. Estimated arrival 3 minutes.")
                )
            )
        }

        AlertDialog(
            onDismissRequest = { showChatDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = AmberDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chat with ${activeOrder.assignedDriverName ?: "Driver"}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.height(260.dp)) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(messages) { msg ->
                            val isMe = msg.first == "Me"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    color = if (isMe) AmberPrimary else Slate100,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${msg.first}: ${msg.second}",
                                        fontSize = 12.sp,
                                        color = Slate900,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("Type message...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (chatInput.isNotBlank()) {
                                    messages = messages + Pair("Me", chatInput)
                                    chatInput = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.NearMe, contentDescription = "Send", tint = AmberDark)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChatDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Rate Order Dialog
    if (ratingOrderTarget != null) {
        val target = ratingOrderTarget!!
        AlertDialog(
            onDismissRequest = { ratingOrderTarget = null },
            title = { Text("Rate your Trip / Delivery", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("How was your experience with ${target.assignedDriverName ?: "your driver"}?", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in 1..5) {
                            IconButton(
                                onClick = { ratingStars = i },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "$i stars",
                                    tint = if (i <= ratingStars) AmberPrimary else Slate300,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = ratingReviewText,
                        onValueChange = { ratingReviewText = it },
                        label = { Text("Add feedback (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.rateOrder(target.id, ratingStars, ratingReviewText)
                        ratingOrderTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberDark)
                ) {
                    Text("Submit Rating")
                }
            },
            dismissButton = {
                TextButton(onClick = { ratingOrderTarget = null }) {
                    Text("Dismiss")
                }
            }
        )
    }
}

@Composable
fun ActiveOrderCard(
    order: BookingOrder,
    onAdvanceStep: () -> Unit,
    onCancelClick: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("active_order_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Live Pulse
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = AmberPrimary,
                        shape = CircleShape,
                        modifier = Modifier.size(8.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE ORDER • ${order.id}",
                        color = AmberPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step Progress Bar
            val progressFraction = when (order.status) {
                OrderStatus.SEARCHING_DRIVER -> 0.15f
                OrderStatus.ACCEPTED -> 0.40f
                OrderStatus.ARRIVED_AT_PICKUP -> 0.65f
                OrderStatus.IN_TRANSIT -> 0.85f
                OrderStatus.COMPLETED -> 1.0f
                else -> 0f
            }

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = AmberPrimary,
                trackColor = Slate700
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Pickup to Destination
            Text(
                text = "${order.pickupAddress} ➔ ${order.dropoffAddress}",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${order.serviceType.label} • Fare: ${String.format("$%.2f", order.fare)} (${order.paymentMethod})",
                color = Slate300,
                fontSize = 12.sp
            )

            // Parcel PIN reminder
            if (order.serviceType == ServiceType.PARCEL_DELIVERY) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Handover PIN for Courier:", color = Slate300, fontSize = 11.sp)
                        Surface(color = AmberPrimary, shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = order.verificationPin,
                                color = Slate900,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Driver Info if assigned
            if (order.assignedDriverName != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Slate700)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(AmberPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = order.assignedDriverName.take(1),
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(order.assignedDriverName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            RatingStarsRow(rating = order.assignedDriverRating ?: 4.9)
                        }
                        Text(order.assignedDriverBike ?: "Motorcycle", color = Slate300, fontSize = 11.sp)
                    }

                    // Quick Actions
                    IconButton(
                        onClick = onChatClick,
                        modifier = Modifier.testTag("driver_chat_button")
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "Chat", tint = AmberPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Simulation step button & cancel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCancelClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cancel_active_order_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError)
                ) {
                    Text("Cancel", fontSize = 12.sp)
                }

                Button(
                    onClick = onAdvanceStep,
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("advance_order_step_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                ) {
                    Text("Fast Forward Step ⏩", color = Slate900, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PastOrderCard(
    order: BookingOrder,
    onRateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("past_order_${order.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    Text(order.id, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                }
                StatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("${order.pickupAddress} ➔ ${order.dropoffAddress}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Slate900)
            Text("Driver: ${order.assignedDriverName ?: "None"} • ${order.distanceKm} km", fontSize = 11.sp, color = Slate700)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("$%.2f", order.fare),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Slate900
                )

                if (order.status == OrderStatus.COMPLETED) {
                    if (order.rating != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Rated: ", fontSize = 11.sp, color = Slate500)
                            RatingStarsRow(rating = order.rating.toDouble())
                        }
                    } else {
                        TextButton(
                            onClick = onRateClick,
                            modifier = Modifier.testTag("rate_trip_button_${order.id}")
                        ) {
                            Text("Rate Trip ★", fontSize = 12.sp, color = AmberDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
