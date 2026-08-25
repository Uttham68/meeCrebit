package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CustomReportFilter
import com.example.data.model.DateRangePreset
import com.example.data.model.ExpenseCategory
import com.example.data.model.ReportEngine
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.TransactionTypeFilter
import com.example.engine.SmsParserEngine
import com.example.engine.ml.LocalCategorizationModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertNotNull(appName)
    }

    @Test
    fun `test on-device machine learning categorization model`() {
        val model = LocalCategorizationModel.getInstance()
        val metrics = model.getModelMetrics()

        assertTrue(metrics.totalSamplesTrained > 0)
        assertTrue(metrics.accuracy > 0.85)

        // Test food SMS prediction
        val foodSms = "Rs 450 debited at Swiggy Bangalore for food delivery"
        val foodPred = model.predict(foodSms)
        assertEquals(ExpenseCategory.FOOD_DINING, foodPred.topCategory)
        assertTrue(foodPred.confidence > 0.4f)

        // Test novel merchant extraction
        val novelSms = "Charged $45.00 at Artisan Blue Roasters on 14-Aug"
        val novelPred = model.predict(novelSms)
        assertTrue(novelPred.isNovelMerchant)
        assertTrue(novelPred.extractedMerchant.contains("Artisan Blue", ignoreCase = true))

        // Test online reinforcement feedback
        model.reinforceFeedback("Purchase at Acme Store", ExpenseCategory.SHOPPING, "Acme Store")
        val updatedPred = model.predict("Purchase at Acme Store")
        assertEquals(ExpenseCategory.SHOPPING, updatedPred.topCategory)
    }

    @Test
    fun `test custom report engine insights generation`() {
        val now = System.currentTimeMillis()
        val testTxList = listOf(
            TransactionEntity(
                id = 1L,
                amount = 120.0,
                type = TransactionType.DEBIT,
                merchant = "Whole Foods Market",
                category = ExpenseCategory.GROCERIES,
                accountNumber = "XX1234",
                bankName = "Chase",
                timestamp = now - (2L * 24 * 60 * 60 * 1000)
            ),
            TransactionEntity(
                id = 2L,
                amount = 45.0,
                type = TransactionType.DEBIT,
                merchant = "Starbucks",
                category = ExpenseCategory.FOOD_DINING,
                accountNumber = "XX1234",
                bankName = "Chase",
                timestamp = now - (1L * 24 * 60 * 60 * 1000)
            ),
            TransactionEntity(
                id = 3L,
                amount = 3200.0,
                type = TransactionType.CREDIT,
                merchant = "TechCorp Global",
                category = ExpenseCategory.SALARY_INCOME,
                accountNumber = "XX1234",
                bankName = "Chase",
                timestamp = now - (5L * 24 * 60 * 60 * 1000)
            )
        )

        // Slicing by debit only & last 30 days
        val filter = CustomReportFilter(
            datePreset = DateRangePreset.LAST_30_DAYS,
            typeFilter = TransactionTypeFilter.DEBIT_ONLY
        )
        val insights = ReportEngine.generateInsights(testTxList, filter)

        assertEquals(2, insights.transactionCount)
        assertEquals(165.0, insights.totalDebits, 0.01)
        assertEquals(82.5, insights.averageSpendPerTx, 0.01)
        assertEquals(2, insights.categoryBreakdown.size)
        assertEquals(2, insights.topMerchants.size)
    }

    @Test
    fun `test sms parser engine with integrated ML heuristics`() {
        val sms = "Rs 420.00 spent on HDFC Bank Card ending 8492 at SWIGGY BANGALORE on 14-AUG-26. Avl bal: Rs 42,500.00."
        val result = SmsParserEngine.parse(sms, "HDFCBK")

        assertTrue(result.isValidTransaction)
        assertEquals(420.0, result.amount, 0.01)
        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals("Swiggy", result.merchant)
        assertEquals(ExpenseCategory.FOOD_DINING, result.category)
        assertEquals("HDFC", result.bankName)
        assertEquals(42500.0, result.balanceAfter ?: 0.0, 0.01)
        assertTrue(result.isMlPredicted)
    }

    @Test
    fun `test sms parser rejects non-transactions and isolates transaction amount from balance`() {
        // Test OTP rejection
        val otpSms = "123456 is your secret OTP for transaction of Rs 500 at Swiggy. Do not share."
        val otpResult = SmsParserEngine.parse(otpSms, "HDFCBK")
        org.junit.Assert.assertFalse(otpResult.isValidTransaction)

        // Test bill statement reminder rejection
        val billSms = "Your credit card statement is generated. Total amount due is Rs 40,000.00 by 10-Sep."
        val billResult = SmsParserEngine.parse(billSms, "KOTAK")
        org.junit.Assert.assertFalse(billResult.isValidTransaction)

        // Test balance vs transaction amount isolation
        val upiSms = "Dear UPI user A/C XX1234 debited by 72.00 on 24-08-26 to RAM REDDY CHICKEN DH. Avl Bal: Rs 0.00 - Kotak Bank"
        val upiResult = SmsParserEngine.parse(upiSms, "KOTAK")
        assertTrue(upiResult.isValidTransaction)
        assertEquals(72.0, upiResult.amount, 0.01)
        assertEquals(TransactionType.DEBIT, upiResult.type)
        assertEquals(0.0, upiResult.balanceAfter ?: -1.0, 0.01)

        // Test date parsing for August 2026
        val timestamp = SmsParserEngine.extractTransactionDate(upiSms)
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        assertEquals(2026, cal.get(java.util.Calendar.YEAR))
        assertEquals(java.util.Calendar.AUGUST, cal.get(java.util.Calendar.MONTH))
        assertEquals(24, cal.get(java.util.Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `test biometric auth manager lock state and preferences`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = com.example.security.BiometricAuthManager.getInstance(context)

        manager.isBiometricEnabled = true
        assertTrue(manager.isBiometricEnabled)

        manager.recordAppPaused()
        manager.autoLockTimeoutMillis = 0L
        assertTrue(manager.shouldLockOnResume())

        manager.isBiometricEnabled = false
        org.junit.Assert.assertFalse(manager.shouldLockOnResume())
    }
}
