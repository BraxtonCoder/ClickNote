package com.example.clicknote.ui.subscription

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

@Composable
fun StripePaymentSheet(
    clientSecret: String,
    publishableKey: String,
    onPaymentResult: (PaymentSheetResult) -> Unit
) {
    val context = LocalContext.current
    val paymentSheet = remember { PaymentSheet(context as Activity) }
    
    LaunchedEffect(clientSecret) {
        PaymentConfiguration.init(context, publishableKey)
        
        val paymentSheetConfig = PaymentSheet.Configuration(
            merchantDisplayName = "ClickNote",
            googlePay = PaymentSheet.GooglePayConfiguration(
                environment = PaymentSheet.GooglePayConfiguration.Environment.Test,
                countryCode = "GB",
                currencyCode = "GBP"
            )
        )
        
        paymentSheet.presentWithPaymentIntent(
            clientSecret,
            paymentSheetConfig
        )
    }
    
    DisposableEffect(Unit) {
        val activity = context as Activity
        val callback = { result: PaymentSheetResult ->
            onPaymentResult(result)
        }
        
        paymentSheet.setOnPaymentResult(activity, callback)
        
        onDispose {
            // Cleanup if needed
        }
    }
} 