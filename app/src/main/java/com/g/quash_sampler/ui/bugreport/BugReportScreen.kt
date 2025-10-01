package com.g.quash_sampler.ui.bugreport

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.g.quash_sampler.domain.model.BugFormOptions
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugReportScreen(
    userId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    viewModel: BugReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Handle success state
    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            onNavigateToHome()
        }
    }

    // Error handling
    uiState.error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Optionally show snackbar here
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report a Bug") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Field
            item {
                OutlinedTextFieldWithError(
                    value = uiState.formData.title,
                    onValueChange = viewModel::updateTitle,
                    label = "Bug Title",
                    placeholder = "Brief description of the issue",
                    error = uiState.validation.titleError,
                    isRequired = true
                )
            }

            // Description Field
            item {
                OutlinedTextFieldWithError(
                    value = uiState.formData.description,
                    onValueChange = viewModel::updateDescription,
                    label = "Description",
                    placeholder = "Detailed description of the bug",
                    error = uiState.validation.descriptionError,
                    isRequired = true,
                    minLines = 3,
                    maxLines = 6
                )
            }

            // Priority and Category Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DropdownFieldWithError(
                        value = uiState.formData.priority,
                        onValueChange = viewModel::updatePriority,
                        label = "Priority",
                        options = BugFormOptions.PRIORITIES,
                        error = uiState.validation.priorityError,
                        modifier = Modifier.weight(1f)
                    )
                    DropdownFieldWithError(
                        value = uiState.formData.category,
                        onValueChange = viewModel::updateCategory,
                        label = "Category",
                        options = BugFormOptions.CATEGORIES,
                        error = uiState.validation.categoryError,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Steps to Reproduce
            item {
                OutlinedTextFieldWithError(
                    value = uiState.formData.stepsToReproduce,
                    onValueChange = viewModel::updateStepsToReproduce,
                    label = "Steps to Reproduce",
                    placeholder = "1. Go to...\n2. Click on...\n3. See error...",
                    minLines = 3,
                    maxLines = 5
                )
            }

            // Expected vs Actual Behavior
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextFieldWithError(
                        value = uiState.formData.expectedBehavior,
                        onValueChange = viewModel::updateExpectedBehavior,
                        label = "Expected Behavior",
                        placeholder = "What should happen",
                        modifier = Modifier.weight(1f),
                        minLines = 2,
                        maxLines = 3
                    )
                    OutlinedTextFieldWithError(
                        value = uiState.formData.actualBehavior,
                        onValueChange = viewModel::updateActualBehavior,
                        label = "Actual Behavior",
                        placeholder = "What actually happens",
                        modifier = Modifier.weight(1f),
                        minLines = 2,
                        maxLines = 3
                    )
                }
            }

            // Environment and Additional Info
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextFieldWithError(
                        value = uiState.formData.environment,
                        onValueChange = viewModel::updateEnvironment,
                        label = "Environment",
                        placeholder = "Device, OS version, app version",
                        error = uiState.validation.environmentError,
                        isRequired = true,
                        modifier = Modifier.weight(1f)
                    )
                    DropdownFieldWithError(
                        value = uiState.formData.reproducibility,
                        onValueChange = viewModel::updateReproducibility,
                        label = "Reproducibility",
                        options = BugFormOptions.REPRODUCIBILITY,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Attachments Section
            item {
                AttachmentsSection(
                    attachments = uiState.formData.attachments,
                    onAddAttachment = viewModel::addAttachment,
                    onRemoveAttachment = viewModel::removeAttachment,
                    context = context
                )
            }

            // Error Display
            uiState.error?.let { error ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Submit Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.submitBugReport(userId) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.validation.isValid && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Submit Bug Report")
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun OutlinedTextFieldWithError(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    error: String? = null,
    isRequired: Boolean = false,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(if (isRequired) "$label *" else label)
            },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            isError = error != null,
            minLines = minLines,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            )
        )
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun DropdownFieldWithError(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>,
    error: String? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Box {
            OutlinedTextField(
                value = value,
                onValueChange = { },
                readOnly = true,
                label = { Text(label) },
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Open dropdown")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                isError = error != null
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun AttachmentsSection(
    attachments: List<File>,
    onAddAttachment: (File) -> Unit,
    onRemoveAttachment: (File) -> Unit,
    context: Context
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Attachments",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            IconButton(
                onClick = { showBottomSheet = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add attachment")
            }
        }

        if (attachments.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(attachments) { file ->
                    AttachmentItem(
                        file = file,
                        onRemove = { onRemoveAttachment(file) }
                    )
                }
            }
        } else {
            Text(
                text = "No attachments added",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }

    if (showBottomSheet) {
        AttachmentBottomSheet(
            onDismiss = { showBottomSheet = false },
            onFileSelected = { file ->
                onAddAttachment(file)
                showBottomSheet = false
            },
            context = context
        )
    }
}

@Composable
fun AttachmentItem(
    file: File,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = file.name,
                    fontSize = 10.sp,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun AttachmentBottomSheet(
    onDismiss: () -> Unit,
    onFileSelected: (File) -> Unit,
    context: Context
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Convert URI to File (simplified implementation)
            // In production, you'd want proper file handling
            val file = File(context.cacheDir, "attachment_${System.currentTimeMillis()}")
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                onFileSelected(file)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Attachment",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AttachmentOption(
                icon = Icons.Default.Add,
                title = "Take Photo",
                subtitle = "Capture a screenshot",
                onClick = {
                    // TODO: Implement camera capture
                    onDismiss()
                }
            )

            AttachmentOption(
                icon = Icons.Default.Settings,
                title = "Choose from Gallery",
                subtitle = "Select an existing image",
                onClick = {
                    photoPickerLauncher.launch("image/*")
                }
            )

            AttachmentOption(
                icon = Icons.Default.Info,
                title = "Choose File",
                subtitle = "Select any file type",
                onClick = {
                    photoPickerLauncher.launch("*/*")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AttachmentOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Preview
@Composable
fun BugReportScreenPreview() {
    BugReportScreen(userId = "preview-user-id")
}