package com.zenith26.fest.data.repository

import com.zenith26.fest.data.api.ApiClient
import com.zenith26.fest.data.model.EventCreateIn
import com.zenith26.fest.data.model.EventOut
import com.zenith26.fest.data.model.RegistrationDetail
import com.zenith26.fest.data.model.RegistrationIn
import com.zenith26.fest.data.model.RegistrationOut
import com.zenith26.fest.data.model.StatsOut
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository handling all backend operations for Zenith '26.
 */
class FestRepository {

    private val apiService get() = ApiClient.getService()

    suspend fun getEvents(category: String?, search: String?): Result<List<EventOut>> =
        withContext(Dispatchers.IO) {
            try {
                val catParam = if (category.isNullOrBlank() || category.equals("All", ignoreCase = true)) null else category
                val searchParam = if (search.isNullOrBlank()) null else search
                val events = apiService.getEvents(catParam, searchParam)
                Result.success(events)
            } catch (t: Throwable) {
                Result.failure(Exception(ApiClient.parseErrorMessage(t), t))
            }
        }

    suspend fun getEventDetail(eventId: Int): Result<EventOut> =
        withContext(Dispatchers.IO) {
            try {
                val event = apiService.getEventDetail(eventId)
                Result.success(event)
            } catch (t: Throwable) {
                Result.failure(Exception(ApiClient.parseErrorMessage(t), t))
            }
        }

    suspend fun registerForEvent(eventId: Int, reg: RegistrationIn): Result<RegistrationOut> =
        withContext(Dispatchers.IO) {
            try {
                val out = apiService.registerForEvent(eventId, reg)
                Result.success(out)
            } catch (t: Throwable) {
                Result.failure(Exception(ApiClient.parseErrorMessage(t), t))
            }
        }

    suspend fun getStats(): Result<StatsOut> =
        withContext(Dispatchers.IO) {
            try {
                val stats = apiService.getStats()
                Result.success(stats)
            } catch (t: Throwable) {
                Result.failure(Exception(ApiClient.parseErrorMessage(t), t))
            }
        }

    suspend fun getRegistrations(eventId: Int? = null): Result<List<RegistrationDetail>> =
        withContext(Dispatchers.IO) {
            try {
                val list = apiService.getRegistrations(eventId)
                Result.success(list)
            } catch (t: Throwable) {
                Result.failure(Exception(ApiClient.parseErrorMessage(t), t))
            }
        }

    suspend fun createEvent(eventCreate: EventCreateIn): Result<EventOut> =
        withContext(Dispatchers.IO) {
            try {
                val created = apiService.createEvent(eventCreate)
                Result.success(created)
            } catch (t: Throwable) {
                Result.failure(Exception(ApiClient.parseErrorMessage(t), t))
            }
        }

    suspend fun deleteRegistration(regId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                apiService.deleteRegistration(regId)
                Result.success(Unit)
            } catch (t: Throwable) {
                Result.failure(Exception(ApiClient.parseErrorMessage(t), t))
            }
        }
}
