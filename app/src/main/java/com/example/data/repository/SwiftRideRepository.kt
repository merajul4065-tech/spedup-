package com.example.data.repository

import com.example.data.models.BikeTier
import com.example.data.models.BookingOrder
import com.example.data.models.CityLocation
import com.example.data.models.Driver
import com.example.data.models.DriverStatus
import com.example.data.models.ManagerAuditLog
import com.example.data.models.OrderStatus
import com.example.data.models.ParcelCategory
import com.example.data.models.ServiceType
import com.example.data.models.TariffConfig
import com.example.data.models.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class SwiftRideRepository private constructor() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Predefined City Locations
    val predefinedLocations = listOf(
        CityLocation("Downtown Central Plaza", "100 Grand Ave, City Center", "Hub", Pair(0.50f, 0.48f)),
        CityLocation("Metro Express Station", "Terminal 3, Rail Transit Blvd", "Transit", Pair(0.35f, 0.38f)),
        CityLocation("Tech Horizon Campus", "500 Silicon Way, North Sector", "Business", Pair(0.72f, 0.28f)),
        CityLocation("Grand Plaza Shopping Mall", "420 Market Street, South District", "Mall", Pair(0.28f, 0.72f)),
        CityLocation("Westside Marina Bay", "88 Harbor View Promenade", "Leisure", Pair(0.18f, 0.42f)),
        CityLocation("International Airport T2", "Aviation Blvd, Cargo Gate 4", "Airport", Pair(0.86f, 0.82f)),
        CityLocation("City General Hospital", "300 Healthcare Drive", "Medical", Pair(0.44f, 0.24f)),
        CityLocation("Green Valley Farmers Market", "12 Organic Park Road", "Market", Pair(0.62f, 0.66f))
    )

    // Current Active Role
    private val _currentRole = MutableStateFlow(UserRole.CUSTOMER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Customer Wallet Balance
    private val _walletBalance = MutableStateFlow(68.50)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    // Drivers List
    private val _drivers = MutableStateFlow<List<Driver>>(
        listOf(
            Driver(
                id = "DRV-101",
                name = "Alex Vance",
                phone = "+1 (555) 392-1084",
                bikeModel = "Yamaha MT-07 Black",
                licensePlate = "TX-9021",
                rating = 4.9,
                totalTrips = 418,
                isOnline = true,
                currentStatus = DriverStatus.AVAILABLE,
                todayEarnings = 74.20,
                acceptanceRate = 98,
                coords = Pair(0.48f, 0.45f)
            ),
            Driver(
                id = "DRV-102",
                name = "Sarah Chen",
                phone = "+1 (555) 720-4491",
                bikeModel = "Honda CB300R Crimson",
                licensePlate = "KL-3419",
                rating = 4.8,
                totalTrips = 328,
                isOnline = true,
                currentStatus = DriverStatus.AVAILABLE,
                todayEarnings = 56.00,
                acceptanceRate = 95,
                coords = Pair(0.38f, 0.36f)
            ),
            Driver(
                id = "DRV-103",
                name = "Marcus Reed",
                phone = "+1 (555) 819-2045",
                bikeModel = "Kawasaki Ninja 400",
                licensePlate = "MN-7712",
                rating = 4.95,
                totalTrips = 592,
                isOnline = true,
                currentStatus = DriverStatus.AVAILABLE,
                todayEarnings = 92.50,
                acceptanceRate = 99,
                coords = Pair(0.68f, 0.32f)
            ),
            Driver(
                id = "DRV-104",
                name = "Priya Patel",
                phone = "+1 (555) 441-9823",
                bikeModel = "Zero FXS Electric Stealth",
                licensePlate = "EV-2204",
                rating = 5.0,
                totalTrips = 194,
                isOnline = false,
                currentStatus = DriverStatus.OFFLINE,
                todayEarnings = 38.00,
                acceptanceRate = 97,
                coords = Pair(0.25f, 0.68f)
            )
        )
    )
    val drivers: StateFlow<List<Driver>> = _drivers.asStateFlow()

    // Active Driver logged in Driver Mode
    private val _activeDriverId = MutableStateFlow("DRV-101")
    val activeDriverId: StateFlow<String> = _activeDriverId.asStateFlow()

    // Tariff Configuration
    private val _tariffs = MutableStateFlow(TariffConfig())
    val tariffs: StateFlow<TariffConfig> = _tariffs.asStateFlow()

    // Orders List
    private val _orders = MutableStateFlow<List<BookingOrder>>(
        listOf(
            // Seed sample past completed order
            BookingOrder(
                id = "SR-4091",
                serviceType = ServiceType.BIKE_RIDE,
                customerName = "Jordan Miller",
                customerPhone = "+1 (555) 120-8833",
                pickupAddress = "Metro Express Station",
                pickupCoords = Pair(0.35f, 0.38f),
                dropoffAddress = "Downtown Central Plaza",
                dropoffCoords = Pair(0.50f, 0.48f),
                distanceKm = 4.2,
                estimatedMinutes = 9,
                fare = 7.54,
                status = OrderStatus.COMPLETED,
                createdAt = System.currentTimeMillis() - 7200000,
                assignedDriverId = "DRV-101",
                assignedDriverName = "Alex Vance",
                assignedDriverPhone = "+1 (555) 392-1084",
                assignedDriverBike = "Yamaha MT-07 Black (TX-9021)",
                assignedDriverRating = 4.9,
                rating = 5,
                feedback = "Very fast, safe driving and clean helmet provided!"
            ),
            BookingOrder(
                id = "SR-3920",
                serviceType = ServiceType.PARCEL_DELIVERY,
                customerName = "Jordan Miller",
                customerPhone = "+1 (555) 120-8833",
                pickupAddress = "Tech Horizon Campus",
                pickupCoords = Pair(0.72f, 0.28f),
                dropoffAddress = "Grand Plaza Shopping Mall",
                dropoffCoords = Pair(0.28f, 0.72f),
                distanceKm = 8.6,
                estimatedMinutes = 16,
                fare = 15.54,
                status = OrderStatus.COMPLETED,
                createdAt = System.currentTimeMillis() - 18000000,
                assignedDriverId = "DRV-102",
                assignedDriverName = "Sarah Chen",
                assignedDriverPhone = "+1 (555) 720-4491",
                assignedDriverBike = "Honda CB300R (KL-3419)",
                assignedDriverRating = 4.8,
                parcelCategory = ParcelCategory.DOCUMENTS,
                recipientName = "Legal Counsel Dept",
                recipientPhone = "+1 (555) 662-9900",
                deliveryNotes = "Fragile legal documents. Handover only with PIN.",
                verificationPin = "5812",
                rating = 5,
                feedback = "Instant delivery and secure PIN handover."
            )
        )
    )
    val orders: StateFlow<List<BookingOrder>> = _orders.asStateFlow()

    // Manager Audit Logs
    private val _auditLogs = MutableStateFlow<List<ManagerAuditLog>>(
        listOf(
            ManagerAuditLog(
                id = "LOG-01",
                timestamp = System.currentTimeMillis() - 3600000,
                action = "System Initialization",
                category = "System",
                details = "SwiftRide Dispatcher Engine v2.4 initialized with 4 active registered drivers."
            ),
            ManagerAuditLog(
                id = "LOG-02",
                timestamp = System.currentTimeMillis() - 1800000,
                action = "Driver Status Checked",
                category = "Fleet",
                details = "Driver DRV-101 (Alex Vance) inspection documents and safety checklist verified."
            )
        )
    )
    val auditLogs: StateFlow<List<ManagerAuditLog>> = _auditLogs.asStateFlow()

    fun setRole(role: UserRole) {
        _currentRole.value = role
    }

    fun setActiveDriver(driverId: String) {
        _activeDriverId.value = driverId
    }

    fun calculateFare(
        serviceType: ServiceType,
        distanceKm: Double,
        bikeTier: BikeTier = BikeTier.STANDARD,
        parcelCategory: ParcelCategory? = null,
        parcelWeightKg: Double = 1.0
    ): Double {
        val t = _tariffs.value
        val rawFare = when (serviceType) {
            ServiceType.BIKE_RIDE -> {
                val base = t.bikeBaseFare * bikeTier.baseRateMultiplier
                val dist = distanceKm * t.bikePerKm * bikeTier.baseRateMultiplier
                (base + dist) * t.surgeMultiplier
            }
            ServiceType.PARCEL_DELIVERY -> {
                val categoryAddon = parcelCategory?.feeAddon ?: 0.0
                val weightAddon = if (parcelWeightKg > 2.0) (parcelWeightKg - 2.0) * 0.80 else 0.0
                val base = t.parcelBaseFare + categoryAddon + weightAddon
                val dist = distanceKm * t.parcelPerKm
                (base + dist) * t.surgeMultiplier
            }
        }
        return Math.round(rawFare * 100.0) / 100.0
    }

    fun calculateDistanceKm(from: Pair<Float, Float>, to: Pair<Float, Float>): Double {
        val dx = (from.first - to.first) * 12.0
        val dy = (from.second - to.second) * 12.0
        val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1.2)
        return Math.round(dist * 10.0) / 10.0
    }

    fun createBikeBooking(
        pickup: CityLocation,
        dropoff: CityLocation,
        tier: BikeTier,
        paymentMethod: String = "SwiftPay Wallet"
    ): BookingOrder {
        val distance = calculateDistanceKm(pickup.coords, dropoff.coords)
        val eta = (distance * 2.2).toInt() + 3 + tier.etaMinutesAdd
        val fare = calculateFare(ServiceType.BIKE_RIDE, distance, bikeTier = tier)

        val newOrder = BookingOrder(
            id = "SR-" + (1000..9999).random(),
            serviceType = ServiceType.BIKE_RIDE,
            customerName = "Jordan Miller",
            customerPhone = "+1 (555) 120-8833",
            pickupAddress = pickup.name,
            pickupCoords = pickup.coords,
            dropoffAddress = dropoff.name,
            dropoffCoords = dropoff.coords,
            distanceKm = distance,
            estimatedMinutes = eta,
            fare = fare,
            status = OrderStatus.SEARCHING_DRIVER,
            bikeTier = tier,
            paymentMethod = paymentMethod
        )

        _orders.update { listOf(newOrder) + it }
        logAudit("Order Created", "Booking", "New Bike Ride ${newOrder.id} booked from ${pickup.name} to ${dropoff.name} for $$fare")

        // Trigger automatic dispatch matching after brief delay
        simulateDriverMatch(newOrder.id)
        return newOrder
    }

    fun createParcelDelivery(
        pickup: CityLocation,
        dropoff: CityLocation,
        category: ParcelCategory,
        weightKg: Double,
        recipientName: String,
        recipientPhone: String,
        notes: String,
        paymentMethod: String = "SwiftPay Wallet"
    ): BookingOrder {
        val distance = calculateDistanceKm(pickup.coords, dropoff.coords)
        val eta = (distance * 2.5).toInt() + 5
        val fare = calculateFare(ServiceType.PARCEL_DELIVERY, distance, parcelCategory = category, parcelWeightKg = weightKg)
        val pin = (1000..9999).random().toString()

        val newOrder = BookingOrder(
            id = "SR-" + (1000..9999).random(),
            serviceType = ServiceType.PARCEL_DELIVERY,
            customerName = "Jordan Miller",
            customerPhone = "+1 (555) 120-8833",
            pickupAddress = pickup.name,
            pickupCoords = pickup.coords,
            dropoffAddress = dropoff.name,
            dropoffCoords = dropoff.coords,
            distanceKm = distance,
            estimatedMinutes = eta,
            fare = fare,
            status = OrderStatus.SEARCHING_DRIVER,
            parcelCategory = category,
            parcelWeightKg = weightKg,
            recipientName = recipientName.ifBlank { "Recipient" },
            recipientPhone = recipientPhone.ifBlank { "+1 (555) 000-0000" },
            deliveryNotes = notes,
            verificationPin = pin,
            paymentMethod = paymentMethod
        )

        _orders.update { listOf(newOrder) + it }
        logAudit("Order Created", "Parcel", "New Parcel ${newOrder.id} (${category.title}, ${weightKg}kg) booked for $$fare. PIN: $pin")

        simulateDriverMatch(newOrder.id)
        return newOrder
    }

    private fun simulateDriverMatch(orderId: String) {
        scope.launch {
            delay(1200)
            // Pick an online available driver (prioritize currently active driver if available so user can test in Driver view!)
            val activeId = _activeDriverId.value
            val onlineDrivers = _drivers.value.filter { it.isOnline && it.isApproved && it.currentStatus == DriverStatus.AVAILABLE }
            val chosen = onlineDrivers.find { it.id == activeId } ?: onlineDrivers.firstOrNull()

            if (chosen != null) {
                // If the user is in customer mode, we assign or alert the driver
                assignDriverToOrder(orderId, chosen)
            }
        }
    }

    fun assignDriverToOrder(orderId: String, driver: Driver) {
        _orders.update { list ->
            list.map { order ->
                if (order.id == orderId) {
                    order.copy(
                        status = OrderStatus.ACCEPTED,
                        assignedDriverId = driver.id,
                        assignedDriverName = driver.name,
                        assignedDriverPhone = driver.phone,
                        assignedDriverBike = "${driver.bikeModel} (${driver.licensePlate})",
                        assignedDriverRating = driver.rating,
                        driverProgress = 0.05f
                    )
                } else order
            }
        }

        _drivers.update { list ->
            list.map { d ->
                if (d.id == driver.id) {
                    d.copy(currentStatus = DriverStatus.EN_ROUTE_PICKUP, currentOrderId = orderId)
                } else d
            }
        }

        logAudit("Driver Assigned", "Dispatch", "Driver ${driver.name} assigned to order $orderId")
    }

    fun driverAdvanceTripStep(orderId: String): OrderStatus {
        var nextStatus = OrderStatus.ACCEPTED
        var isFinishing = false
        var fareEarned = 0.0
        var driverId: String? = null

        _orders.update { list ->
            list.map { order ->
                if (order.id == orderId) {
                    driverId = order.assignedDriverId
                    fareEarned = order.fare
                    nextStatus = when (order.status) {
                        OrderStatus.SEARCHING_DRIVER -> OrderStatus.ACCEPTED
                        OrderStatus.ACCEPTED -> OrderStatus.ARRIVED_AT_PICKUP
                        OrderStatus.ARRIVED_AT_PICKUP -> OrderStatus.IN_TRANSIT
                        OrderStatus.IN_TRANSIT -> {
                            isFinishing = true
                            OrderStatus.COMPLETED
                        }
                        OrderStatus.COMPLETED -> OrderStatus.COMPLETED
                        OrderStatus.CANCELLED -> OrderStatus.CANCELLED
                    }
                    val newProgress = when (nextStatus) {
                        OrderStatus.ACCEPTED -> 0.15f
                        OrderStatus.ARRIVED_AT_PICKUP -> 0.35f
                        OrderStatus.IN_TRANSIT -> 0.70f
                        OrderStatus.COMPLETED -> 1.0f
                        else -> order.driverProgress
                    }
                    order.copy(status = nextStatus, driverProgress = newProgress)
                } else order
            }
        }

        if (isFinishing && driverId != null) {
            _drivers.update { list ->
                list.map { d ->
                    if (d.id == driverId) {
                        d.copy(
                            currentStatus = DriverStatus.AVAILABLE,
                            currentOrderId = null,
                            todayEarnings = Math.round((d.todayEarnings + fareEarned * 0.82) * 100.0) / 100.0,
                            totalTrips = d.totalTrips + 1
                        )
                    } else d
                }
            }
            // Deduct customer wallet if paid with wallet
            _walletBalance.update { (it - fareEarned).coerceAtLeast(0.0) }
            logAudit("Trip Completed", "Fleet", "Order $orderId completed by driver $driverId. Fare: $$fareEarned")
        } else if (driverId != null) {
            val dStatus = if (nextStatus == OrderStatus.IN_TRANSIT) DriverStatus.ON_TRIP else DriverStatus.EN_ROUTE_PICKUP
            _drivers.update { list ->
                list.map { d ->
                    if (d.id == driverId) d.copy(currentStatus = dStatus) else d
                }
            }
        }

        return nextStatus
    }

    fun driverToggleOnline(driverId: String) {
        _drivers.update { list ->
            list.map { d ->
                if (d.id == driverId) {
                    val newOnline = !d.isOnline
                    d.copy(
                        isOnline = newOnline,
                        currentStatus = if (newOnline) DriverStatus.AVAILABLE else DriverStatus.OFFLINE
                    )
                } else d
            }
        }
    }

    fun cancelOrder(orderId: String, role: UserRole, reason: String) {
        var assignedDriver: String? = null
        _orders.update { list ->
            list.map { o ->
                if (o.id == orderId) {
                    assignedDriver = o.assignedDriverId
                    o.copy(status = OrderStatus.CANCELLED, feedback = "Cancelled: $reason")
                } else o
            }
        }

        if (assignedDriver != null) {
            _drivers.update { list ->
                list.map { d ->
                    if (d.id == assignedDriver) {
                        d.copy(currentStatus = DriverStatus.AVAILABLE, currentOrderId = null)
                    } else d
                }
            }
        }

        logAudit("Order Cancelled", "Intervention", "Order $orderId cancelled by ${role.label}. Reason: $reason")
    }

    fun managerReassignDriver(orderId: String, newDriverId: String) {
        val newDriver = _drivers.value.find { it.id == newDriverId } ?: return
        var previousDriverId: String? = null

        _orders.update { list ->
            list.map { o ->
                if (o.id == orderId) {
                    previousDriverId = o.assignedDriverId
                    o.copy(
                        assignedDriverId = newDriver.id,
                        assignedDriverName = newDriver.name,
                        assignedDriverPhone = newDriver.phone,
                        assignedDriverBike = "${newDriver.bikeModel} (${newDriver.licensePlate})",
                        assignedDriverRating = newDriver.rating,
                        status = OrderStatus.ACCEPTED
                    )
                } else o
            }
        }

        _drivers.update { list ->
            list.map { d ->
                when (d.id) {
                    previousDriverId -> d.copy(currentStatus = DriverStatus.AVAILABLE, currentOrderId = null)
                    newDriver.id -> d.copy(currentStatus = DriverStatus.EN_ROUTE_PICKUP, currentOrderId = orderId)
                    else -> d
                }
            }
        }

        logAudit("Manual Dispatch Override", "Manager Intervention", "Reassigned order $orderId from driver $previousDriverId to ${newDriver.name}")
    }

    fun managerToggleDriverApproval(driverId: String) {
        _drivers.update { list ->
            list.map { d ->
                if (d.id == driverId) {
                    val newApproval = !d.isApproved
                    val newOnline = if (!newApproval) false else d.isOnline
                    val newStatus = if (!newApproval) DriverStatus.OFFLINE else d.currentStatus
                    d.copy(isApproved = newApproval, isOnline = newOnline, currentStatus = newStatus)
                } else d
            }
        }
        val driver = _drivers.value.find { it.id == driverId }
        val statusText = if (driver?.isApproved == true) "Approved" else "Suspended"
        logAudit("Driver Status Changed", "Fleet Compliance", "Driver ${driver?.name} ($driverId) set to $statusText")
    }

    fun managerUpdateTariffs(newTariff: TariffConfig) {
        _tariffs.value = newTariff
        logAudit(
            "Tariff Updated",
            "Pricing",
            "Base Bike: $${newTariff.bikeBaseFare}, Parcel: $${newTariff.parcelBaseFare}, Surge: ${newTariff.surgeMultiplier}x (${newTariff.surgeReason})"
        )
    }

    fun managerRegisterDriver(name: String, phone: String, bike: String, plate: String): Driver {
        val newDriver = Driver(
            id = "DRV-" + (105..999).random(),
            name = name,
            phone = phone,
            bikeModel = bike,
            licensePlate = plate,
            rating = 5.0,
            totalTrips = 0,
            isOnline = true,
            currentStatus = DriverStatus.AVAILABLE,
            coords = Pair(0.5f, 0.5f)
        )
        _drivers.update { it + newDriver }
        logAudit("Driver Registered", "Fleet", "New driver ${newDriver.name} (${newDriver.bikeModel}) onboarded.")
        return newDriver
    }

    fun rateOrder(orderId: String, stars: Int, review: String) {
        _orders.update { list ->
            list.map { o ->
                if (o.id == orderId) {
                    o.copy(rating = stars, feedback = review)
                } else o
            }
        }
    }

    fun topUpWallet(amount: Double) {
        _walletBalance.update { it + amount }
    }

    fun logAudit(action: String, category: String, details: String) {
        val entry = ManagerAuditLog(
            id = "LOG-" + (100..999).random(),
            timestamp = System.currentTimeMillis(),
            action = action,
            category = category,
            details = details
        )
        _auditLogs.update { listOf(entry) + it }
    }

    companion object {
        @Volatile
        private var instance: SwiftRideRepository? = null

        fun getInstance(): SwiftRideRepository {
            return instance ?: synchronized(this) {
                instance ?: SwiftRideRepository().also { instance = it }
            }
        }
    }
}
