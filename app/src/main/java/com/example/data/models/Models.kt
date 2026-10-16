package com.example.data.models

enum class UserRole(val label: String, val subtitle: String) {
    CUSTOMER("Customer", "Book rides & send parcels"),
    DRIVER("Driver Partner", "Accept requests & earn"),
    MANAGER("Admin Manager", "Fleet & dispatch control")
}

enum class ServiceType(val label: String) {
    BIKE_RIDE("Bike Taxi"),
    PARCEL_DELIVERY("Parcel Courier")
}

enum class BikeTier(
    val title: String,
    val description: String,
    val baseRateMultiplier: Double,
    val etaMinutesAdd: Int
) {
    STANDARD("Standard Moto", "Quick & agile 125cc commuter", 1.0, 0),
    EXPRESS("Cruiser Pro", "250cc with padded seat & visor helmet", 1.35, -2),
    ECO("Eco E-Bike", "Zero-emission electric city glide", 0.9, 1)
}

enum class ParcelCategory(
    val title: String,
    val description: String,
    val maxWeightKg: Double,
    val feeAddon: Double
) {
    DOCUMENTS("Documents & Letters", "Envelopes, contracts, passports (waterproof)", 1.5, 0.0),
    SMALL_BOX("Electronics & Gifts", "Gadgets, parcels, clothing (padded)", 5.0, 1.5),
    FOOD_FRAGILE("Food & Fragile", "Cakes, flowers, meals (upright thermal bag)", 4.0, 2.0),
    HEAVY_CARRIER("Heavy & Cargo", "Hardware, boxes, supplies (rear rack)", 15.0, 4.0)
}

enum class OrderStatus(val label: String, val stepIndex: Int) {
    SEARCHING_DRIVER("Finding Nearby Driver", 0),
    ACCEPTED("Driver Assigned", 1),
    ARRIVED_AT_PICKUP("Driver at Pickup", 2),
    IN_TRANSIT("In Transit", 3),
    COMPLETED("Completed & Paid", 4),
    CANCELLED("Cancelled", -1)
}

enum class DriverStatus(val label: String) {
    OFFLINE("Offline"),
    AVAILABLE("Available"),
    EN_ROUTE_PICKUP("Heading to Pickup"),
    ON_TRIP("On Active Job")
}

data class Driver(
    val id: String,
    val name: String,
    val phone: String,
    val bikeModel: String,
    val licensePlate: String,
    val rating: Double,
    val totalTrips: Int,
    val isOnline: Boolean = true,
    val currentStatus: DriverStatus = DriverStatus.AVAILABLE,
    val todayEarnings: Double = 0.0,
    val acceptanceRate: Int = 96,
    val coords: Pair<Float, Float> = Pair(0.5f, 0.5f),
    val currentOrderId: String? = null,
    val isApproved: Boolean = true
)

data class BookingOrder(
    val id: String,
    val serviceType: ServiceType,
    val customerName: String,
    val customerPhone: String,
    val pickupAddress: String,
    val pickupCoords: Pair<Float, Float>,
    val dropoffAddress: String,
    val dropoffCoords: Pair<Float, Float>,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val fare: Double,
    val status: OrderStatus = OrderStatus.SEARCHING_DRIVER,
    val createdAt: Long = System.currentTimeMillis(),
    val assignedDriverId: String? = null,
    val assignedDriverName: String? = null,
    val assignedDriverPhone: String? = null,
    val assignedDriverBike: String? = null,
    val assignedDriverRating: Double? = null,
    val bikeTier: BikeTier = BikeTier.STANDARD,
    val parcelCategory: ParcelCategory? = null,
    val parcelWeightKg: Double = 1.0,
    val recipientName: String? = null,
    val recipientPhone: String? = null,
    val deliveryNotes: String? = null,
    val verificationPin: String = "4829",
    val driverProgress: Float = 0.0f, // 0.0 to 1.0 along route
    val rating: Int? = null,
    val feedback: String? = null,
    val paymentMethod: String = "SwiftPay Wallet"
)

data class TariffConfig(
    val bikeBaseFare: Double = 2.50,
    val bikePerKm: Double = 1.20,
    val parcelBaseFare: Double = 3.50,
    val parcelPerKm: Double = 1.40,
    val surgeMultiplier: Double = 1.0, // 1.0, 1.25, 1.5, 2.0
    val surgeReason: String = "Normal Traffic"
)

data class CityLocation(
    val name: String,
    val address: String,
    val category: String, // Hub, Transit, Mall, Airport, Business
    val coords: Pair<Float, Float>
)

data class ManagerAuditLog(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String,
    val category: String,
    val details: String
)
