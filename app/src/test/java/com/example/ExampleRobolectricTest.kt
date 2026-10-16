package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.models.BikeTier
import com.example.data.models.OrderStatus
import com.example.data.models.ParcelCategory
import com.example.data.models.ServiceType
import com.example.data.repository.SwiftRideRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SwiftRide", appName)
    }

    @Test
    fun `test bike ride creation and fare calculation`() {
        val repo = SwiftRideRepository.getInstance()
        val loc1 = repo.predefinedLocations[0]
        val loc2 = repo.predefinedLocations[1]

        val order = repo.createBikeBooking(loc1, loc2, BikeTier.STANDARD)
        assertNotNull(order)
        assertEquals(ServiceType.BIKE_RIDE, order.serviceType)
        assertTrue(order.fare > 0.0)
    }

    @Test
    fun `test parcel delivery creation and PIN generation`() {
        val repo = SwiftRideRepository.getInstance()
        val loc1 = repo.predefinedLocations[0]
        val loc2 = repo.predefinedLocations[1]

        val parcel = repo.createParcelDelivery(
            pickup = loc1,
            dropoff = loc2,
            category = ParcelCategory.DOCUMENTS,
            weightKg = 1.0,
            recipientName = "Alice",
            recipientPhone = "+15551234",
            notes = "Handle with care"
        )
        assertNotNull(parcel)
        assertEquals(ServiceType.PARCEL_DELIVERY, parcel.serviceType)
        assertEquals(4, parcel.verificationPin.length)
    }
}
