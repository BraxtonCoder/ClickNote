package com.example.clicknote.ui.onboarding

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.R
import com.example.clicknote.domain.model.Permission
import com.example.clicknote.domain.model.PermissionType
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    @DrawableRes val imageRes: Int,
    @RawRes val videoRes: Int? = null,
    val backgroundColor: Color = Color(0xFFFBFBFB)
)

val onboardingPages = listOf(
    OnboardingPage(
        title = stringResource(R.string.onboarding_welcome_title),
        description = stringResource(R.string.onboarding_welcome_description),
        imageRes = R.drawable.onboarding_welcome,
        backgroundColor = Color(0xFFFBFBFB)
    ),
    OnboardingPage(
        title = stringResource(R.string.onboarding_quick_access_title),
        description = stringResource(R.string.onboarding_quick_access_description),
        imageRes = R.drawable.onboarding_quick_access,
        videoRes = R.raw.demo_volume_buttons
    ),
    OnboardingPage(
        title = stringResource(R.string.onboarding_folders_title),
        description = stringResource(R.string.onboarding_folders_description),
        imageRes = R.drawable.onboarding_organize,
        videoRes = R.raw.demo_folders
    ),
    OnboardingPage(
        title = stringResource(R.string.onboarding_transcription_title),
        description = stringResource(R.string.onboarding_transcription_description),
        imageRes = R.drawable.onboarding_transcription,
        videoRes = R.raw.demo_transcription
    ),
    OnboardingPage(
        title = stringResource(R.string.onboarding_ai_title),
        description = stringResource(R.string.onboarding_ai_description),
        imageRes = R.drawable.onboarding_ai,
        videoRes = R.raw.demo_ai_summary
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Skip button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            TextButton(
                onClick = onOnboardingComplete,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(stringResource(R.string.skip))
            }
        }
        
        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPage(onboardingPages[page])
        }
        
        // Bottom section with indicators and buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Page indicators
            Row(
                Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    }
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .size(if (pagerState.currentPage == iteration) 10.dp else 8.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = color
                        ) {
                            Box(Modifier.fillMaxSize())
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back button
                AnimatedVisibility(
                    visible = pagerState.currentPage > 0,
                    enter = fadeIn() + slideInHorizontally(),
                    exit = fadeOut() + slideOutHorizontally()
                ) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.back))
                    }
                }
                
                // Next/Get Started button
                Button(
                    onClick = {
                        if (pagerState.currentPage < pagerState.pageCount - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onOnboardingComplete()
                        }
                    },
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        if (pagerState.currentPage < pagerState.pageCount - 1) {
                            stringResource(R.string.next)
                        } else {
                            stringResource(R.string.get_started)
                        }
                    )
                    if (pagerState.currentPage < pagerState.pageCount - 1) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 32.dp)
        )

        if (page.videoRes != null) {
            VideoPlayer(
                videoRes = page.videoRes,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f/9f)
            )
        } else {
            Image(
                painter = painterResource(page.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f/9f)
            )
        }

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun PermissionContent(
    permission: Permission,
    isGranted: Boolean,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ArrowForward, // Replace with actual permission icon
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(permission.titleRes),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(permission.descriptionRes),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        if (!isGranted) {
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = if (permission.isRequired) {
                        stringResource(R.string.grant_permission_required)
                    } else {
                        stringResource(R.string.grant_permission_optional)
                    }
                )
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.permission_granted),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
} 