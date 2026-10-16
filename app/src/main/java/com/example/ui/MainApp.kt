package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.OrderStatus
import com.example.data.models.UserRole
import com.example.data.repository.SwiftRideRepository
import com.example.ui.components.RoleSwitcherBar
import com.example.ui.customer.CustomerScreen
import com.example.ui.driver.DriverScreen
import com.example.ui.manager.ManagerScreen
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    repository: SwiftRideRepository = SwiftRideRepository.getInstance()
) {
    val currentRole by repository.currentRole.collectAsState()
    val orders by repository.orders.collectAsState()
    val drivers by repository.drivers.collectAsState()
    val activeDriverId by repository.activeDriverId.collectAsState()

    // BackHandler: if in Driver or Manager mode, return to Customer
    BackHandler(enabled = currentRole != UserRole.CUSTOMER) {
        repository.setRole(UserRole.CUSTOMER)
    }

    // Dynamic badges for switcher
    val currentDriver = drivers.find { it.id == activeDriverId } ?: drivers.first()
    val driverAlertCount = orders.count {
        it.status == OrderStatus.SEARCHING_DRIVER ||
                (it.assignedDriverId == currentDriver.id && (it.status == OrderStatus.ACCEPTED || it.status == OrderStatus.ARRIVED_AT_PICKUP || it.status == OrderStatus.IN_TRANSIT))
    }
    val customerActiveCount = orders.count {
        it.status != OrderStatus.COMPLETED && it.status != OrderStatus.CANCELLED
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("main_scaffold"),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Brand Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Slate900, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBike,
                                    contentDescription = "SwiftRide Logo",
                                    tint = AmberPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "SwiftRide",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Slate900,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Moto & Parcel Courier",
                                    fontSize = 10.sp,
                                    color = AmberDark,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Role Tag
                        Surface(
                            color = Slate900,
                            shape = CircleShape
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = EmeraldSuccess,
                                    shape = CircleShape,
                                    modifier = Modifier.size(6.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentRole.label.uppercase(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Role Switcher Navigation Bar
                RoleSwitcherBar(
                    currentRole = currentRole,
                    onRoleSelected = { repository.setRole(it) },
                    driverAlertCount = driverAlertCount,
                    activeOrderCount = customerActiveCount
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentRole) {
                UserRole.CUSTOMER -> CustomerScreen(repository = repository)
                UserRole.DRIVER -> DriverScreen(repository = repository)
                UserRole.MANAGER -> ManagerScreen(repository = repository)
            }
        }
    }
}
