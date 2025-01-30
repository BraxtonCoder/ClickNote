package com.example.clicknote.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.clicknote.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(
    navController: NavController,
    viewModel: PhoneAuthViewModel = hiltViewModel()
) {
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var selectedCountryCode by remember { mutableStateOf("+1") }
    var showCountryPicker by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val uiState by viewModel.uiState.collectAsState()
    val countdownSeconds by viewModel.countdownSeconds.collectAsState()
    
    val exampleNumber = viewModel.getExampleNumber(selectedCountryCode)
    val formattedNumber = viewModel.formatPhoneNumberForDisplay(phoneNumber, selectedCountryCode)
    val countryName = viewModel.getRegionDisplayName(selectedCountryCode)

    LaunchedEffect(uiState) {
        when (uiState) {
            is PhoneAuthUiState.Success -> {
                navController.navigate(Screen.Notes.route) {
                    popUpTo(Screen.PhoneAuth.route) { inclusive = true }
                }
            }
            else -> {} // Handle other states if needed
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Phone Authentication",
            style = MaterialTheme.typography.headlineMedium
        )

        // Country code selector
        OutlinedButton(
            onClick = { showCountryPicker = true }
        ) {
            Text("$selectedCountryCode $countryName")
            Icon(Icons.Default.ArrowDropDown, "Select country")
        }

        // Phone number input
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            placeholder = { Text(exampleNumber) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        when (uiState) {
            is PhoneAuthUiState.CodeSent -> {
                // Verification code input
                OutlinedTextField(
                    value = verificationCode,
                    onValueChange = { if (it.length <= 6) verificationCode = it },
                    label = { Text("Verification Code") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { viewModel.verifyPhoneNumberWithCode(verificationCode) },
                    enabled = verificationCode.length == 6,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Verify Code")
                }

                // Resend code button with countdown
                TextButton(
                    onClick = { viewModel.resendVerificationCode(phoneNumber, selectedCountryCode) },
                    enabled = countdownSeconds == null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (countdownSeconds != null) {
                            "Resend code in ${countdownSeconds}s"
                        } else {
                            "Resend Code"
                        }
                    )
                }
            }
            is PhoneAuthUiState.Initial -> {
                Button(
                    onClick = { viewModel.startPhoneNumberVerification(phoneNumber, selectedCountryCode) },
                    enabled = phoneNumber.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Send Verification Code")
                }
            }
            is PhoneAuthUiState.Loading -> {
                CircularProgressIndicator()
            }
            is PhoneAuthUiState.Error -> {
                Text(
                    text = (uiState as PhoneAuthUiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {}
        }
    }

    // Updated country picker dialog
    if (showCountryPicker) {
        AlertDialog(
            onDismissRequest = { 
                showCountryPicker = false
                searchQuery = ""
            },
            title = { Text("Select Country") },
            text = {
                Column {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search countries") },
                        leadingIcon = { Icon(Icons.Default.Search, "Search") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        singleLine = true
                    )

                    // Country list
                    LazyColumn(
                        modifier = Modifier.height(400.dp)
                    ) {
                        CountryCodes.regions.forEach { region ->
                            val countriesInRegion = CountryCodes.getCountriesByRegion(region, searchQuery)
                            if (countriesInRegion.isNotEmpty()) {
                                item {
                                    RegionHeader(region)
                                }
                                items(countriesInRegion) { country ->
                                    CountryCodeItem(
                                        code = country.code,
                                        country = country.name,
                                        onSelect = {
                                            selectedCountryCode = it
                                            showCountryPicker = false
                                            searchQuery = ""
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showCountryPicker = false
                        searchQuery = ""
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun CountryCodeItem(
    code: String,
    country: String,
    onSelect: (String) -> Unit
) {
    TextButton(
        onClick = { onSelect(code) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = CountryCodes.getFlag(country),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = country,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Text(
                text = code,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RegionHeader(
    region: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = region,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

private object CountryCodes {
    data class Country(
        val code: String,
        val name: String,
        val region: String,
        val flag: String
    )

    private val countries = listOf(
        // North America
        Country("+1", "United States", "North America", "🇺🇸"),
        Country("+1", "Canada", "North America", "🇨🇦"),
        
        // Europe
        Country("+44", "United Kingdom", "Europe", "🇬🇧"),
        Country("+49", "Germany", "Europe", "🇩🇪"),
        Country("+33", "France", "Europe", "🇫🇷"),
        Country("+39", "Italy", "Europe", "🇮🇹"),
        Country("+34", "Spain", "Europe", "🇪🇸"),
        Country("+351", "Portugal", "Europe", "🇵🇹"),
        Country("+31", "Netherlands", "Europe", "🇳🇱"),
        Country("+32", "Belgium", "Europe", "🇧🇪"),
        Country("+41", "Switzerland", "Europe", "🇨🇭"),
        Country("+43", "Austria", "Europe", "🇦🇹"),
        Country("+46", "Sweden", "Europe", "🇸🇪"),
        Country("+47", "Norway", "Europe", "🇳🇴"),
        Country("+45", "Denmark", "Europe", "🇩🇰"),
        Country("+358", "Finland", "Europe", "🇫🇮"),
        Country("+48", "Poland", "Europe", "🇵🇱"),
        Country("+420", "Czech Republic", "Europe", "🇨🇿"),
        Country("+36", "Hungary", "Europe", "🇭🇺"),
        Country("+30", "Greece", "Europe", "🇬🇷"),
        
        // Asia
        Country("+91", "India", "Asia", "🇮🇳"),
        Country("+86", "China", "Asia", "🇨🇳"),
        Country("+81", "Japan", "Asia", "🇯🇵"),
        Country("+82", "South Korea", "Asia", "🇰🇷"),
        Country("+65", "Singapore", "Asia", "🇸🇬"),
        Country("+852", "Hong Kong", "Asia", "🇭🇰"),
        Country("+886", "Taiwan", "Asia", "🇹🇼"),
        Country("+84", "Vietnam", "Asia", "🇻🇳"),
        Country("+66", "Thailand", "Asia", "🇹🇭"),
        Country("+62", "Indonesia", "Asia", "🇮🇩"),
        Country("+60", "Malaysia", "Asia", "🇲🇾"),
        Country("+63", "Philippines", "Asia", "🇵🇭"),
        
        // Middle East
        Country("+972", "Israel", "Middle East", "🇮🇱"),
        Country("+971", "United Arab Emirates", "Middle East", "🇦🇪"),
        Country("+966", "Saudi Arabia", "Middle East", "🇸🇦"),
        Country("+974", "Qatar", "Middle East", "🇶🇦"),
        Country("+973", "Bahrain", "Middle East", "🇧🇭"),
        Country("+965", "Kuwait", "Middle East", "🇰🇼"),
        Country("+968", "Oman", "Middle East", "🇴🇲"),
        
        // Oceania
        Country("+61", "Australia", "Oceania", "🇦🇺"),
        Country("+64", "New Zealand", "Oceania", "🇳🇿"),
        
        // South America
        Country("+55", "Brazil", "South America", "🇧🇷"),
        Country("+54", "Argentina", "South America", "🇦🇷"),
        Country("+56", "Chile", "South America", "🇨🇱"),
        Country("+57", "Colombia", "South America", "🇨🇴"),
        Country("+51", "Peru", "South America", "🇵🇪"),
        Country("+58", "Venezuela", "South America", "🇻🇪"),
        
        // Africa
        Country("+27", "South Africa", "Africa", "🇿🇦"),
        Country("+234", "Nigeria", "Africa", "🇳🇬"),
        Country("+20", "Egypt", "Africa", "🇪🇬"),
        Country("+254", "Kenya", "Africa", "🇰🇪"),
        
        // South Asia
        Country("+92", "Pakistan", "South Asia", "🇵🇰"),
        Country("+880", "Bangladesh", "South Asia", "🇧🇩"),
        Country("+94", "Sri Lanka", "South Asia", "🇱🇰"),
        Country("+977", "Nepal", "South Asia", "🇳🇵")
    )

    val regions = countries.map { it.region }.distinct().sorted()

    fun getCountriesByRegion(region: String, searchQuery: String = ""): List<Country> {
        return countries.filter { 
            it.region == region && 
            (it.name.contains(searchQuery, ignoreCase = true) || 
             it.code.contains(searchQuery, ignoreCase = true))
        }.sortedBy { it.name }
    }

    fun getFlag(countryName: String): String {
        return countries.find { it.name == countryName }?.flag ?: "🏳️"
    }

    val list = countries.map { it.code to it.name }
} 