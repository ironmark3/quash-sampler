package com.g.quash_sampler.data.repository

import com.g.quash_sampler.data.remote.ApiService
import com.g.quash_sampler.domain.model.BugFormData
import com.g.quash_sampler.domain.model.BugListResponse
import com.g.quash_sampler.domain.model.BugReportResponse
import com.g.quash_sampler.domain.model.BugStatsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed class BugResult<out T> {
    data class Success<T>(val data: T) : BugResult<T>()
    data class Error(val message: String) : BugResult<Nothing>()
    data object Loading : BugResult<Nothing>()
}

@Singleton
class BugRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun createBugReport(
        formData: BugFormData,
        reporterId: String
    ): Flow<BugResult<BugReportResponse>> = flow {
        try {
            emit(BugResult.Loading)

            // Prepare multipart request parts
            val titlePart = formData.title.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionPart = formData.description.toRequestBody("text/plain".toMediaTypeOrNull())
            val priorityPart = formData.priority.toRequestBody("text/plain".toMediaTypeOrNull())
            val categoryPart = formData.category.toRequestBody("text/plain".toMediaTypeOrNull())
            val reporterPart = reporterId.toRequestBody("text/plain".toMediaTypeOrNull())
            val reproducibilityPart = formData.reproducibility.toRequestBody("text/plain".toMediaTypeOrNull())
            val severityPart = formData.severity.toRequestBody("text/plain".toMediaTypeOrNull())

            // Optional fields
            val stepsToReproducePart = if (formData.stepsToReproduce.isNotBlank()) {
                formData.stepsToReproduce.toRequestBody("text/plain".toMediaTypeOrNull())
            } else null

            val expectedBehaviorPart = if (formData.expectedBehavior.isNotBlank()) {
                formData.expectedBehavior.toRequestBody("text/plain".toMediaTypeOrNull())
            } else null

            val actualBehaviorPart = if (formData.actualBehavior.isNotBlank()) {
                formData.actualBehavior.toRequestBody("text/plain".toMediaTypeOrNull())
            } else null

            val environmentPart = if (formData.environment.isNotBlank()) {
                formData.environment.toRequestBody("text/plain".toMediaTypeOrNull())
            } else null

            // Prepare file attachments
            val attachmentParts = formData.attachments.mapIndexed { index, file ->
                val fileRequestBody = file.asRequestBody("*/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData(
                    "attachments",
                    file.name,
                    fileRequestBody
                )
            }

            val response = apiService.createBugReport(
                title = titlePart,
                description = descriptionPart,
                priority = priorityPart,
                category = categoryPart,
                reporter = reporterPart,
                stepsToReproduce = stepsToReproducePart,
                expectedBehavior = expectedBehaviorPart,
                actualBehavior = actualBehaviorPart,
                environment = environmentPart,
                reproducibility = reproducibilityPart,
                severity = severityPart,
                attachments = attachmentParts.ifEmpty { null }
            )

            if (response.isSuccessful) {
                response.body()?.let { data ->
                    emit(BugResult.Success(data))
                } ?: emit(BugResult.Error("Empty response body"))
            } else {
                emit(BugResult.Error("Failed to create bug report: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(BugResult.Error("Network error: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getBugReports(
        status: String? = null,
        priority: String? = null,
        category: String? = null,
        reporter: String? = null,
        assignedTo: String? = null,
        search: String? = null,
        page: Int = 1,
        limit: Int = 10,
        sortBy: String = "createdAt",
        sortOrder: String = "desc"
    ): Flow<BugResult<BugListResponse>> = flow {
        try {
            emit(BugResult.Loading)

            val response = apiService.getBugReports(
                status = status,
                priority = priority,
                category = category,
                reporter = reporter,
                assignedTo = assignedTo,
                search = search,
                page = page,
                limit = limit,
                sortBy = sortBy,
                sortOrder = sortOrder
            )

            if (response.isSuccessful) {
                response.body()?.let { data ->
                    emit(BugResult.Success(data))
                } ?: emit(BugResult.Error("Empty response body"))
            } else {
                emit(BugResult.Error("Failed to fetch bug reports: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(BugResult.Error("Network error: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getBugStats(
        reporter: String? = null,
        assignedTo: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ): Flow<BugResult<BugStatsResponse>> = flow {
        try {
            emit(BugResult.Loading)

            val response = apiService.getBugStats(
                reporter = reporter,
                assignedTo = assignedTo,
                dateFrom = dateFrom,
                dateTo = dateTo
            )

            if (response.isSuccessful) {
                response.body()?.let { data ->
                    emit(BugResult.Success(data))
                } ?: emit(BugResult.Error("Empty response body"))
            } else {
                emit(BugResult.Error("Failed to fetch bug statistics: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(BugResult.Error("Network error: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getBugReport(bugId: String): Flow<BugResult<BugReportResponse>> = flow {
        try {
            emit(BugResult.Loading)

            val response = apiService.getBugReport(bugId)

            if (response.isSuccessful) {
                response.body()?.let { data ->
                    emit(BugResult.Success(data))
                } ?: emit(BugResult.Error("Empty response body"))
            } else {
                emit(BugResult.Error("Failed to fetch bug report: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(BugResult.Error("Network error: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)
}