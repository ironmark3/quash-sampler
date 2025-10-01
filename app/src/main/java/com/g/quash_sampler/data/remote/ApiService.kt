package com.g.quash_sampler.data.remote

import com.g.quash_sampler.domain.model.LoginRequest
import com.g.quash_sampler.domain.model.LoginResponse
import com.g.quash_sampler.domain.model.OtpRequest
import com.g.quash_sampler.domain.model.OtpResponse
import com.g.quash_sampler.domain.model.ProfileUpdateRequest
import com.g.quash_sampler.domain.model.ProfileResponse
import com.g.quash_sampler.domain.model.ProfileCompletionResponse
import com.g.quash_sampler.domain.model.BugReportRequest
import com.g.quash_sampler.domain.model.BugReportResponse
import com.g.quash_sampler.domain.model.BugListResponse
import com.g.quash_sampler.domain.model.BugStatsResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpRequest): Response<OtpResponse>

    @GET("profile/{userId}")
    suspend fun getProfile(@Path("userId") userId: String): Response<ProfileResponse>

    @PUT("profile/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: String,
        @Body request: ProfileUpdateRequest
    ): Response<ProfileResponse>

    @GET("profile/{userId}/completion")
    suspend fun getProfileCompletion(@Path("userId") userId: String): Response<ProfileCompletionResponse>

    // Bug Reporting APIs
    @Multipart
    @POST("bugs")
    suspend fun createBugReport(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("priority") priority: RequestBody,
        @Part("category") category: RequestBody,
        @Part("reporter") reporter: RequestBody,
        @Part("stepsToReproduce") stepsToReproduce: RequestBody?,
        @Part("expectedBehavior") expectedBehavior: RequestBody?,
        @Part("actualBehavior") actualBehavior: RequestBody?,
        @Part("environment") environment: RequestBody?,
        @Part("reproducibility") reproducibility: RequestBody,
        @Part("severity") severity: RequestBody,
        @Part attachments: List<MultipartBody.Part>?
    ): Response<BugReportResponse>

    @GET("bugs")
    suspend fun getBugReports(
        @Query("status") status: String? = null,
        @Query("priority") priority: String? = null,
        @Query("category") category: String? = null,
        @Query("reporter") reporter: String? = null,
        @Query("assignedTo") assignedTo: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("sortOrder") sortOrder: String = "desc"
    ): Response<BugListResponse>

    @GET("bugs/stats")
    suspend fun getBugStats(
        @Query("reporter") reporter: String? = null,
        @Query("assignedTo") assignedTo: String? = null,
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null
    ): Response<BugStatsResponse>

    @GET("bugs/{bugId}")
    suspend fun getBugReport(@Path("bugId") bugId: String): Response<BugReportResponse>
}