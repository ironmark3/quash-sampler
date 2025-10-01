package com.g.quash_sampler.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.g.quash_sampler.domain.model.ProfileUpdateRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load profile when the screen is first created
    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    // Show success message when profile is updated
    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            viewModel.clearUpdateSuccess()
        }
    }
    var isEditing by remember { mutableStateOf(false) }

    // Form state - initialized from uiState.user
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Reporter") }
    var showRoleDropdown by remember { mutableStateOf(false) }

    // Update form state when user data changes
    LaunchedEffect(uiState.user) {
        uiState.user?.let { user ->
            name = user.name
            email = user.email ?: ""
            phone = user.phone ?: ""
            address = user.address ?: ""
            dateOfBirth = user.dateOfBirth ?: ""
            role = user.role
        }
    }

    val roles = listOf("Reporter", "Developer", "QA")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (isEditing) {
                                // Save changes
                                viewModel.updateProfile(
                                    userId,
                                    ProfileUpdateRequest(
                                        name = name,
                                        address = address.takeIf { it.isNotBlank() },
                                        dateOfBirth = dateOfBirth.takeIf { it.isNotBlank() },
                                        role = role
                                    )
                                )
                            }
                            isEditing = !isEditing
                        }
                    ) {
                        Text(if (isEditing) "Save" else "Edit")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Show error if there's one
        uiState.error?.let { errorMessage ->
            LaunchedEffect(errorMessage) {
                viewModel.clearError()
            }
        }

        // Show loading state
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header
            item {
                ProfileHeader(
                    name = name,
                    email = email.takeIf { it.isNotBlank() },
                    isComplete = uiState.user?.isProfileComplete ?: false,
                    completionPercentage = uiState.profileCompletion?.completionPercentage ?: uiState.user?.profileCompletionPercentage ?: 0
                )
            }

            // Profile Fields
            item {
                Text(
                    "Personal Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                ProfileField(
                    label = "Full Name",
                    value = name,
                    onValueChange = { name = it },
                    isEditing = isEditing,
                    icon = Icons.Filled.Person
                )
            }

            item {
                ProfileField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    isEditing = false, // Email should not be editable
                    icon = Icons.Filled.Email,
                    keyboardType = KeyboardType.Email
                )
            }

            item {
                ProfileField(
                    label = "Phone",
                    value = phone,
                    onValueChange = { phone = it },
                    isEditing = false, // Phone should not be editable
                    icon = Icons.Filled.Phone,
                    keyboardType = KeyboardType.Phone
                )
            }

            item {
                ProfileField(
                    label = "Address",
                    value = address,
                    onValueChange = { address = it },
                    isEditing = isEditing,
                    icon = Icons.Filled.Person
                )
            }

            item {
                ProfileField(
                    label = "Date of Birth",
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    isEditing = isEditing,
                    icon = Icons.Filled.DateRange,
                    placeholder = "YYYY-MM-DD"
                )
            }

            item {
                RoleField(
                    label = "Role",
                    value = role,
                    options = roles,
                    isEditing = isEditing,
                    showDropdown = showRoleDropdown,
                    onDropdownToggle = { showRoleDropdown = it },
                    onValueChange = {
                        role = it
                        showRoleDropdown = false
                    }
                )
            }
        }
        }
    }
}

@Composable
fun ProfileHeader(
    name: String,
    email: String?,
    isComplete: Boolean,
    completionPercentage: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Avatar
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(2).uppercase(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name
        Text(
            text = name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Email
        if (email != null) {
            Text(
                text = email,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Completion Status
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { completionPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Profile ${completionPercentage}% complete",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditing: Boolean,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String = ""
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                )
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            if (isEditing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(placeholder) },
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    singleLine = true
                )
            } else {
                Text(
                    text = value.ifBlank { "Not set" },
                    fontSize = 16.sp,
                    color = if (value.isBlank())
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun RoleField(
    label: String,
    value: String,
    options: List<String>,
    isEditing: Boolean,
    showDropdown: Boolean,
    onDropdownToggle: (Boolean) -> Unit,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(enabled = isEditing) { onDropdownToggle(true) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBox,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )

                if (isEditing) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
            }

            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { onDropdownToggle(false) }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { onValueChange(option) }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        userId = "preview-user-id"
    )
}