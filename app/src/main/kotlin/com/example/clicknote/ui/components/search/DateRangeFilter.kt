package com.example.clicknote.ui.components.search

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.clicknote.R
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
fun DateRangeFilter(
    onDateRangeSelected: (LocalDateTime?, LocalDateTime?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomDatePicker by remember { mutableStateOf(false) }
    val calendarState = rememberSheetState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.filter_by_date),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Predefined date ranges
            DateRangeOption(
                text = stringResource(R.string.today),
                onClick = {
                    val now = LocalDateTime.now()
                    val startOfDay = now.with(LocalTime.MIN)
                    onDateRangeSelected(startOfDay, now)
                }
            )

            DateRangeOption(
                text = stringResource(R.string.last_7_days),
                onClick = {
                    val now = LocalDateTime.now()
                    val sevenDaysAgo = now.minusDays(7)
                    onDateRangeSelected(sevenDaysAgo, now)
                }
            )

            DateRangeOption(
                text = stringResource(R.string.last_30_days),
                onClick = {
                    val now = LocalDateTime.now()
                    val thirtyDaysAgo = now.minusDays(30)
                    onDateRangeSelected(thirtyDaysAgo, now)
                }
            )

            DateRangeOption(
                text = stringResource(R.string.last_3_months),
                onClick = {
                    val now = LocalDateTime.now()
                    val threeMonthsAgo = now.minusMonths(3)
                    onDateRangeSelected(threeMonthsAgo, now)
                }
            )

            DateRangeOption(
                text = stringResource(R.string.last_6_months),
                onClick = {
                    val now = LocalDateTime.now()
                    val sixMonthsAgo = now.minusMonths(6)
                    onDateRangeSelected(sixMonthsAgo, now)
                }
            )

            DateRangeOption(
                text = stringResource(R.string.last_year),
                onClick = {
                    val now = LocalDateTime.now()
                    val oneYearAgo = now.minusYears(1)
                    onDateRangeSelected(oneYearAgo, now)
                }
            )

            DateRangeOption(
                text = stringResource(R.string.custom_range),
                onClick = { showCustomDatePicker = true }
            )

            DateRangeOption(
                text = stringResource(R.string.all_time),
                onClick = { onDateRangeSelected(null, null) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.close))
            }
        }
    }

    if (showCustomDatePicker) {
        var startDate by remember { mutableStateOf<LocalDateTime?>(null) }

        CalendarDialog(
            state = calendarState,
            config = CalendarConfig(
                monthSelection = true,
                yearSelection = true
            ),
            selection = CalendarSelection.Period { startLocalDate, endLocalDate ->
                startDate = startLocalDate.atStartOfDay()
                val endDate = endLocalDate.atTime(LocalTime.MAX)
                onDateRangeSelected(startDate, endDate)
                showCustomDatePicker = false
            }
        )
    }
}

@Composable
private fun DateRangeOption(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text)
    }
} 