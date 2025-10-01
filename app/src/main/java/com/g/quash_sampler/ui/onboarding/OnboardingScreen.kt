package com.g.quash_sampler.ui.onboarding

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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.g.quash_sampler.domain.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    user: User,
    onOnboardingComplete: () -> Unit,
    onSkipOnboarding: () -> Unit = {},
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Initialize with user data
    LaunchedEffect(user) {
        viewModel.initializeOnboarding(user)
    }

    // Handle completion
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onOnboardingComplete()
        }
    }

    // Show completion screen if ready
    if (uiState.showCompletionScreen) {
        WelcomeCompletionScreen(
            userName = uiState.user?.name ?: "User",
            userRole = uiState.selectedRole,
            onContinueToHome = {
                viewModel.finishOnboarding()
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Setup Profile",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Step ${uiState.currentStep} of ${uiState.totalSteps}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    if (uiState.currentStep > 1) {
                        IconButton(onClick = { viewModel.previousStep() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // Skip entire onboarding and go to home
                            onSkipOnboarding()
                        }
                    ) {
                        Text("Skip All")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            // Progress Indicator
            ProgressIndicator(
                currentStep = uiState.currentStep,
                totalSteps = uiState.totalSteps,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step Content
            Box(
                modifier = Modifier.weight(1f)
            ) {
                when (uiState.currentStep) {
                    1 -> WelcomeStep(user = user)
                    2 -> PersonalInfoStep(
                        name = uiState.name,
                        onNameChange = viewModel::updateName,
                        nameError = uiState.nameError
                    )
                    3 -> ContactDetailsStep(
                        address = uiState.address,
                        dateOfBirth = uiState.dateOfBirth,
                        onAddressChange = viewModel::updateAddress,
                        onDateOfBirthChange = viewModel::updateDateOfBirth,
                        dateOfBirthError = uiState.dateOfBirthError
                    )
                    4 -> RoleSelectionStep(
                        selectedRole = uiState.selectedRole,
                        onRoleSelected = viewModel::updateRole
                    )
                }
            }

            // Navigation Buttons
            OnboardingNavigation(
                currentStep = uiState.currentStep,
                totalSteps = uiState.totalSteps,
                canProceed = viewModel.canProceedToNextStep(),
                isLoading = uiState.isCompleting,
                onNext = {
                    if (uiState.currentStep == 4) { // Complete after role selection
                        viewModel.completeOnboarding(user.id)
                    } else {
                        viewModel.nextStep()
                    }
                },
                onSkip = { viewModel.skipCurrentStep() }
            )
        }
    }
}

@Composable
fun ProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Progress bar
        LinearProgressIndicator(
            progress = { currentStep.toFloat() / totalSteps.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Step indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(totalSteps) { index ->
                val stepNumber = index + 1
                val isCompleted = stepNumber < currentStep
                val isCurrent = stepNumber == currentStep

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> MaterialTheme.colorScheme.primary
                                isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = stepNumber.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isCurrent)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeStep(user: User) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.name.take(2).uppercase(),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome, ${user.name}!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Let's complete your profile to get the best experience from Quash Sampler",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "We'll help you:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                BulletPoint("✨ Set up your personal information")
                BulletPoint("📍 Add your contact details")
                BulletPoint("👤 Choose your role in the team")
                BulletPoint("🚀 Get started with bug reporting")
            }
        }
    }
}

@Composable
fun BulletPoint(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        modifier = Modifier.padding(vertical = 2.dp),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
    )
}

@Composable
fun PersonalInfoStep(
    name: String,
    onNameChange: (String) -> Unit,
    nameError: String?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Personal Information",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tell us a bit about yourself",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            OnboardingTextField(
                value = name,
                onValueChange = onNameChange,
                label = "Full Name",
                placeholder = "Enter your full name",
                leadingIcon = Icons.Filled.Person,
                isError = nameError != null,
                errorMessage = nameError,
                isRequired = true
            )
        }
    }
}

@Composable
fun ContactDetailsStep(
    address: String,
    dateOfBirth: String,
    onAddressChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    dateOfBirthError: String?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Contact Details",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "This helps us provide better support",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            OnboardingTextField(
                value = address,
                onValueChange = onAddressChange,
                label = "Address",
                placeholder = "Enter your address",
                leadingIcon = Icons.Filled.Person,
                isOptional = true
            )
        }

        item {
            OnboardingTextField(
                value = dateOfBirth,
                onValueChange = onDateOfBirthChange,
                label = "Date of Birth",
                placeholder = "YYYY-MM-DD",
                leadingIcon = Icons.Filled.DateRange,
                keyboardType = KeyboardType.Number,
                isError = dateOfBirthError != null,
                errorMessage = dateOfBirthError,
                isOptional = true
            )
        }
    }
}

@Composable
fun OnboardingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorMessage: String? = null,
    isRequired: Boolean = false,
    isOptional: Boolean = false
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            if (isOptional) {
                Text(
                    text = "Optional",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    leadingIcon,
                    contentDescription = null,
                    tint = if (isError) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = isError,
            singleLine = true
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun OnboardingNavigation(
    currentStep: Int,
    totalSteps: Int,
    canProceed: Boolean,
    isLoading: Boolean,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (currentStep > 1 && currentStep < totalSteps) {
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier.weight(1f)
            ) {
                Text("Skip Step")
            }
        }

        Button(
            onClick = onNext,
            enabled = canProceed && !isLoading,
            modifier = Modifier.weight(if (currentStep > 1 && currentStep < totalSteps) 1f else 2f)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        when (currentStep) {
                            1 -> "Get Started"
                            4 -> "Complete Profile"
                            else -> "Next"
                        }
                    )
                    if (currentStep < 4) {
                        Icon(
                            Icons.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun OnboardingScreenPreview() {
    OnboardingScreen(
        user = User(
            id = "preview-id",
            name = "John Doe",
            email = "john@example.com"
        ),
        onOnboardingComplete = {},
        onSkipOnboarding = {}
    )
}