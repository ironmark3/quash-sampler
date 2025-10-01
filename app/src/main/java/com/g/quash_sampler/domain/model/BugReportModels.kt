package com.g.quash_sampler.domain.model

import com.squareup.moshi.JsonClass
import java.io.File

@JsonClass(generateAdapter = true)
data class BugReport(
    val id: String,
    val bugId: String,
    val title: String,
    val description: String,
    val status: String = "Open",
    val priority: String = "Medium",
    val category: String = "Other",
    val reporter: User,
    val assignedTo: User? = null,
    val stepsToReproduce: String? = null,
    val expectedBehavior: String? = null,
    val actualBehavior: String? = null,
    val environment: String? = null,
    val attachments: List<Attachment> = emptyList(),
    val tags: List<String> = emptyList(),
    val reproducibility: String = "Always",
    val severity: String = "Medium",
    val createdAt: String,
    val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class Attachment(
    val filename: String,
    val originalName: String,
    val path: String,
    val size: Long,
    val mimetype: String
)

@JsonClass(generateAdapter = true)
data class BugReportRequest(
    val title: String,
    val description: String,
    val priority: String = "Medium",
    val category: String = "Other",
    val reporter: String, // User ID
    val stepsToReproduce: String? = null,
    val expectedBehavior: String? = null,
    val actualBehavior: String? = null,
    val environment: String? = null,
    val reproducibility: String = "Always",
    val severity: String = "Medium"
)

@JsonClass(generateAdapter = true)
data class BugReportResponse(
    val success: Boolean,
    val message: String,
    val bug: BugReport? = null
)

@JsonClass(generateAdapter = true)
data class BugListResponse(
    val success: Boolean,
    val bugs: List<BugReport> = emptyList(),
    val pagination: PaginationInfo
)

@JsonClass(generateAdapter = true)
data class PaginationInfo(
    val page: Int,
    val limit: Int,
    val total: Int,
    val pages: Int
)

@JsonClass(generateAdapter = true)
data class BugStatsResponse(
    val success: Boolean,
    val stats: BugStats
)

@JsonClass(generateAdapter = true)
data class BugStats(
    val totalBugs: Int,
    val recentBugs: Int,
    val statusDistribution: Map<String, Int>,
    val priorityDistribution: Map<String, Int>,
    val categoryDistribution: Map<String, Int>
)

// UI-specific models
data class BugFormData(
    val title: String = "",
    val description: String = "",
    val priority: String = "Medium",
    val category: String = "Other",
    val stepsToReproduce: String = "",
    val expectedBehavior: String = "",
    val actualBehavior: String = "",
    val environment: String = "",
    val reproducibility: String = "Always",
    val severity: String = "Medium",
    val attachments: List<File> = emptyList()
)

// Validation result
data class BugFormValidation(
    val isValid: Boolean,
    val titleError: String? = null,
    val descriptionError: String? = null,
    val categoryError: String? = null,
    val priorityError: String? = null,
    val environmentError: String? = null
)

// Form field options
object BugFormOptions {
    val PRIORITIES = listOf("Low", "Medium", "High", "Critical")
    val CATEGORIES = listOf("UI/UX", "Functionality", "Performance", "Security", "Compatibility", "Data", "Other")
    val REPRODUCIBILITY = listOf("Always", "Sometimes", "Rarely", "Unable to Reproduce")
    val SEVERITY = listOf("Low", "Medium", "High", "Critical")
}