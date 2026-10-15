package com.zenith26.fest.data.api

import com.zenith26.fest.data.model.EventCreateIn
import com.zenith26.fest.data.model.EventOut
import com.zenith26.fest.data.model.RegistrationDetail
import com.zenith26.fest.data.model.RegistrationIn
import com.zenith26.fest.data.model.RegistrationOut
import com.zenith26.fest.data.model.StatsOut
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service interface defining the exact Zenith '26 API contract.
 */
interface ZenithApiService {

    /**
     * GET /api/events
     * Fetches all events with live available seats.
     * Supports optional category and search query parameters.
     */
    @GET("api/events")
    suspend fun getEvents(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): List<EventOut>

    /**
     * GET /api/events/{event_id}
     * Fetches detailed information for a single event.
     */
    @GET("api/events/{event_id}")
    suspend fun getEventDetail(
        @Path("event_id") eventId: Int
    ): EventOut

    /**
     * POST /api/events/{event_id}/register
     * Registers a student for an event with strict server-side validation.
     */
    @POST("api/events/{event_id}/register")
    suspend fun registerForEvent(
        @Path("event_id") eventId: Int,
        @Body body: RegistrationIn
    ): RegistrationOut

    /**
     * GET /api/registrations
     * Returns raw list of registrations, optionally filtered by event_id.
     */
    @GET("api/registrations")
    suspend fun getRegistrations(
        @Query("event_id") eventId: Int? = null
    ): List<RegistrationDetail>

    /**
     * GET /api/stats
     * Live computed festival statistics (total events, registrations, utilization %, etc.).
     */
    @GET("api/stats")
    suspend fun getStats(): StatsOut

    /**
     * POST /api/events
     * Admin stretch goal: Create a new festival event.
     */
    @POST("api/events")
    suspend fun createEvent(
        @Body body: EventCreateIn
    ): EventOut

    /**
     * DELETE /api/registrations/{registration_id}
     * Admin stretch goal: Cancel a registration and restore seat availability.
     */
    @DELETE("api/registrations/{registration_id}")
    suspend fun deleteRegistration(
        @Path("registration_id") registrationId: Int
    ): Map<String, Any>
}
