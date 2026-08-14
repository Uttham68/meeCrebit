package com.example.engine

import com.example.data.model.ExpenseCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.engine.ml.CategoryPredictionResult
import com.example.engine.ml.LocalCategorizationModel
import java.util.Locale
import java.util.regex.Pattern

data class ParsedSmsResult(
    val isValidTransaction: Boolean,
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.DEBIT,
    val merchant: String = "Unknown",
    val category: ExpenseCategory = ExpenseCategory.OTHERS,
    val accountNumber: String = "XX0000",
    val bankName: String = "Bank",
    val balanceAfter: Double? = null,
    val rawBody: String = "",
    val sender: String = "",
    val reasonIfNotValid: String = "",
    val mlConfidence: Float = 0.95f,
    val isMlPredicted: Boolean = true,
    val isNovelMerchant: Boolean = false,
    val matchedFeatures: List<String> = emptyList(),
    val classProbabilities: Map<ExpenseCategory, Float> = emptyMap()
)

object SmsParserEngine {

    // Regex patterns for amounts
    private val AMOUNT_PATTERNS = listOf(
        Pattern.compile("""(?:rs\.?|inr|₹|\$|usd|eur|gbp)\s*[:\.]?\s*([0-9,]+(?:\.[0-9]{1,2})?)""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:rs\.?|inr|₹|\$|usd)""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:amount|sum of|spent|debited by|credited with|txn of)\s*[:\.]?\s*(?:rs\.?|inr|₹|\$)?\s*([0-9,]+(?:\.[0-9]{1,2})?)""", Pattern.CASE_INSENSITIVE)
    )

    // Patterns for balance
    private val BALANCE_PATTERNS = listOf(
        Pattern.compile("""(?:bal|balance|avail(?:able)?\s*bal(?:ance)?|avl\s*bal|avbl\s*lmt)\s*(?:is|:)?\s*(?:rs\.?|inr|₹|\$)?\s*([0-9,]+(?:\.[0-9]{1,2})?)""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""total\s*bal(?:ance)?\s*[:\-]?\s*(?:rs\.?|inr|₹|\$)?\s*([0-9,]+(?:\.[0-9]{1,2})?)""", Pattern.CASE_INSENSITIVE)
    )

    // Patterns for account number
    private val ACCOUNT_PATTERNS = listOf(
        Pattern.compile("""(?:a\/c|acct|account|card|ending\s*in|ending)\s*(?:no\.?)?\s*[\*\#xX]*([0-9]{3,4})""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""[\*\#xX]{2,}([0-9]{3,4})"""),
        Pattern.compile("""(?:XX|xx)([0-9]{3,4})""")
    )

    // Debit keywords
    private val DEBIT_KEYWORDS = listOf(
        "debited", "spent", "paid", "withdrawn", "sent", "purchase", "charged",
        "transferred to", "vpa", "pos txn", "atm wdl", "payment to", "used at", "deducted"
    )

    // Credit keywords
    private val CREDIT_KEYWORDS = listOf(
        "credited", "deposited", "received", "refund", "cashback", "salary credited",
        "added to your", "reversed", "inward"
    )

    // OTP / Non-transactional phrases (only reject if no transaction debit/credit context)
    private val PURE_OTP_PATTERNS = listOf(
        "is your otp", "is your verification code", "one time password",
        "do not share this otp", "login otp", "security code is", "secret code"
    )

    // Bank list
    private val KNOWN_BANKS = listOf(
        "HDFC", "SBI", "ICICI", "AXIS", "KOTAK", "CHASE", "WELLS FARGO", "CITI",
        "BOA", "BANK OF AMERICA", "BARCLAYS", "HSBC", "PNB", "BOB", "INDUSIND",
        "YES BANK", "FEDERAL", "CANARA", "PAYTM", "PAYPAL", "APPLE CARD", "CAPITAL ONE"
    )

    fun parse(
        smsBody: String,
        sender: String = "",
        customRules: List<com.example.data.model.MerchantRuleEntity>? = null
    ): ParsedSmsResult {
        val lowerBody = smsBody.lowercase(Locale.getDefault())
        val cleanBody = smsBody.replace("\n", " ").trim()

        // 1. Check if it's purely an OTP message without financial movement
        val hasDebit = DEBIT_KEYWORDS.any { lowerBody.contains(it) }
        val hasCredit = CREDIT_KEYWORDS.any { lowerBody.contains(it) }

        if (!hasDebit && !hasCredit) {
            return ParsedSmsResult(
                isValidTransaction = false,
                rawBody = smsBody,
                sender = sender,
                reasonIfNotValid = "No debit/credit transaction indicator found in SMS."
            )
        }

        val isPureOtp = PURE_OTP_PATTERNS.any { lowerBody.contains(it) } && !lowerBody.contains("debited") && !lowerBody.contains("credited")
        if (isPureOtp) {
            return ParsedSmsResult(
                isValidTransaction = false,
                rawBody = smsBody,
                sender = sender,
                reasonIfNotValid = "Identified as OTP / verification message."
            )
        }

        // 2. Extract Amount
        var amount: Double? = null
        for (pattern in AMOUNT_PATTERNS) {
            val matcher = pattern.matcher(cleanBody)
            if (matcher.find()) {
                val amtStr = matcher.group(1)?.replace(",", "")
                val parsed = amtStr?.toDoubleOrNull()
                if (parsed != null && parsed > 0.0) {
                    amount = parsed
                    break
                }
            }
        }

        if (amount == null) {
            return ParsedSmsResult(
                isValidTransaction = false,
                rawBody = smsBody,
                sender = sender,
                reasonIfNotValid = "Could not extract numerical amount."
            )
        }

        // 3. Determine Transaction Type
        val type = if (hasDebit) {
            TransactionType.DEBIT
        } else {
            TransactionType.CREDIT
        }

        // 4. Extract Account Number
        var account = "XX0000"
        for (pattern in ACCOUNT_PATTERNS) {
            val matcher = pattern.matcher(cleanBody)
            if (matcher.find()) {
                val lastDigits = matcher.group(1)
                if (lastDigits != null) {
                    account = "XX$lastDigits"
                    break
                }
            }
        }

        // 5. Extract Available Balance
        var balance: Double? = null
        for (pattern in BALANCE_PATTERNS) {
            val matcher = pattern.matcher(cleanBody)
            if (matcher.find()) {
                val balStr = matcher.group(1)?.replace(",", "")
                balance = balStr?.toDoubleOrNull()
                if (balance != null) break
            }
        }

        // 6. Extract Bank Name
        var bankName = "Bank"
        val upperSender = sender.uppercase(Locale.getDefault())
        for (b in KNOWN_BANKS) {
            if (upperSender.contains(b) || cleanBody.uppercase(Locale.getDefault()).contains(b)) {
                bankName = b
                break
            }
        }

        // 7. Extract Merchant & ML Classification
        val initialMerchant = extractMerchant(cleanBody, type)
        val mlModel = LocalCategorizationModel.getInstance()
        val mlPrediction = mlModel.predict(cleanBody, initialMerchant)

        var finalMerchant = if (initialMerchant == "Direct Payment" && mlPrediction.extractedMerchant != "Direct Payment") {
            mlPrediction.extractedMerchant
        } else {
            initialMerchant
        }

        var category = if (type == TransactionType.CREDIT) {
            categorize(finalMerchant, cleanBody, type)
        } else {
            // Blend ML prediction with rule-based heuristics
            if (mlPrediction.confidence >= 0.35f) {
                mlPrediction.topCategory
            } else {
                categorize(finalMerchant, cleanBody, type)
            }
        }

        // Apply Custom Merchant Category Rules if provided
        customRules?.let { rules ->
            for (rule in rules) {
                if (rule.matches(cleanBody, finalMerchant)) {
                    category = rule.targetCategory
                    if (!rule.overrideMerchantName.isNullOrBlank()) {
                        finalMerchant = rule.overrideMerchantName
                    }
                    break
                }
            }
        }

        return ParsedSmsResult(
            isValidTransaction = true,
            amount = amount,
            type = type,
            merchant = finalMerchant,
            category = category,
            accountNumber = account,
            bankName = bankName,
            balanceAfter = balance,
            rawBody = smsBody,
            sender = sender,
            mlConfidence = mlPrediction.confidence,
            isMlPredicted = true,
            isNovelMerchant = mlPrediction.isNovelMerchant,
            matchedFeatures = mlPrediction.matchedFeatures,
            classProbabilities = mlPrediction.classProbabilities
        )
    }

    private fun extractMerchant(text: String, type: TransactionType): String {
        val lower = text.lowercase(Locale.getDefault())

        // Quick known merchant keywords check
        val known = findKnownMerchant(lower)
        if (known != null) return known

        // Regex heuristic: "to <merchant>", "at <merchant>", "info: <merchant>", "vpa <merchant>"
        val patterns = listOf(
            Pattern.compile("""(?:to|at|info\s*:?|vpa|paid\s+to|spent\s+at)\s+([A-Za-z0-9\s\.\&\*\-_]{2,24})(?:\s+on|\s+ref|\s+upi|\s+bal|\s+via|\s+dated|\.|\,|$)""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""(?:towards|for)\s+([A-Za-z0-9\s\.\&\*\-_]{2,24})(?:\s+on|\s+ref|\s+upi|\s+bal|\.|\,|$)""", Pattern.CASE_INSENSITIVE)
        )

        for (pattern in patterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val candidate = matcher.group(1)?.trim()
                if (!candidate.isNullOrBlank() && candidate.length > 1 && !candidate.contains("a/c", true)) {
                    // Clean up common suffix or prefixes
                    val cleaned = candidate.replace(Regex("""^(vpa|the|mr|m/s)\s+""", RegexOption.IGNORE_CASE), "").trim()
                    if (cleaned.length in 2..28) {
                        return capitalizeWords(cleaned)
                    }
                }
            }
        }

        return if (type == TransactionType.CREDIT) "Bank Deposit / Transfer" else "Direct Payment"
    }

    private fun findKnownMerchant(text: String): String? {
        val dict = mapOf(
            "swiggy" to "Swiggy",
            "zomato" to "Zomato",
            "blinkit" to "Blinkit",
            "zepto" to "Zepto",
            "instamart" to "Instamart",
            "bigbasket" to "BigBasket",
            "amazon" to "Amazon",
            "flipkart" to "Flipkart",
            "myntra" to "Myntra",
            "uber" to "Uber",
            "ola" to "Ola Cabs",
            "rapido" to "Rapido",
            "netflix" to "Netflix",
            "spotify" to "Spotify",
            "starbucks" to "Starbucks",
            "mcdonald" to "McDonald's",
            "kfc" to "KFC",
            "burger king" to "Burger King",
            "domino" to "Domino's Pizza",
            "subway" to "Subway",
            "walmart" to "Walmart",
            "target" to "Target",
            "costco" to "Costco",
            "trader joe" to "Trader Joe's",
            "whole foods" to "Whole Foods",
            "apple" to "Apple Store",
            "google" to "Google Play",
            "steam" to "Steam Games",
            "playstation" to "PlayStation",
            "airtel" to "Airtel",
            "jio" to "Jio Infocomm",
            "vodafone" to "Vodafone",
            "apollo" to "Apollo Pharmacy",
            "cult" to "Cult.fit",
            "zerodha" to "Zerodha",
            "groww" to "Groww",
            "shell" to "Shell Fuel",
            "hpcl" to "HPCL Fuel",
            "iocl" to "Indian Oil",
            "bpcl" to "BPCL Fuel",
            "irctc" to "IRCTC Railways",
            "makemytrip" to "MakeMyTrip",
            "indigo" to "IndiGo Airlines",
            "salary" to "Monthly Salary",
            "dividend" to "Stock Dividend",
            "cashback" to "Cashback Reward"
        )

        for ((key, value) in dict) {
            if (text.contains(key)) return value
        }
        return null
    }

    private fun categorize(merchant: String, body: String, type: TransactionType): ExpenseCategory {
        val lowerMerchant = merchant.lowercase(Locale.getDefault())
        val lowerBody = body.lowercase(Locale.getDefault())

        if (type == TransactionType.CREDIT) {
            return if (lowerBody.contains("salary") || lowerMerchant.contains("salary")) {
                ExpenseCategory.SALARY_INCOME
            } else if (lowerBody.contains("dividend") || lowerBody.contains("mutual fund") || lowerBody.contains("interest")) {
                ExpenseCategory.INVESTMENTS
            } else {
                ExpenseCategory.SALARY_INCOME
            }
        }

        // Check Food & Dining
        if (listOf("swiggy", "zomato", "starbucks", "mcdonald", "kfc", "burger king", "domino", "subway", "cafe", "restaurant", "dining", "pizza", "coffee", "bistro").any { lowerMerchant.contains(it) || lowerBody.contains(it) }) {
            return ExpenseCategory.FOOD_DINING
        }

        // Check Groceries
        if (listOf("blinkit", "zepto", "instamart", "bigbasket", "walmart", "target", "supermarket", "grocery", "dmart", "spencer", "provision", "costco", "trader joe").any { lowerMerchant.contains(it) || lowerBody.contains(it) }) {
            return ExpenseCategory.GROCERIES
        }

        // Check Transport
        if (listOf("uber", "ola", "rapido", "lyft", "fuel", "petrol", "diesel", "shell", "hpcl", "iocl", "bpcl", "metro", "irctc", "flight", "airlines", "indigo", "parking", "toll", "fastag").any { lowerMerchant.contains(it) || lowerBody.contains(it) }) {
            return ExpenseCategory.TRANSPORT
        }

        // Check Shopping
        if (listOf("amazon", "flipkart", "myntra", "zara", "h&m", "nike", "adidas", "apple", "retail", "clothing", "fashion", "mall", "electronics", "best buy").any { lowerMerchant.contains(it) || lowerBody.contains(it) }) {
            return ExpenseCategory.SHOPPING
        }

        // Check Bills & Utilities
        if (listOf("airtel", "jio", "vodafone", "electricity", "water", "gas", "broadband", "wifi", "bescom", "bill", "recharge", "rent", "maintenance").any { lowerMerchant.contains(it) || lowerBody.contains(it) }) {
            return ExpenseCategory.BILLS_UTILITIES
        }

        // Check Entertainment
        if (listOf("netflix", "spotify", "hotstar", "disney", "prime video", "bookmyshow", "pvr", "inox", "cinema", "movie", "steam", "playstation", "xbox", "youtube", "game").any { lowerMerchant.contains(it) || lowerBody.contains(it) }) {
            return ExpenseCategory.ENTERTAINMENT
        }

        // Check Health & Wellness
        if (listOf("pharmacy", "apollo", "medplus", "1mg", "cvs", "walgreens", "hospital", "clinic", "doctor", "dental", "cult", "gym", "fitness", "lab").any { lowerMerchant.contains(it) || lowerBody.contains(it) }) {
            return ExpenseCategory.HEALTH_FITNESS
        }

        // Check Investments
        if (listOf("zerodha", "groww", "sip", "mutual fund", "stocks", "vanguard", "fidelity", "crypto", "binance", "deposit", "investment").any { lowerMerchant.contains(it) || lowerBody.contains(it) }) {
            return ExpenseCategory.INVESTMENTS
        }

        return ExpenseCategory.OTHERS
    }

    private fun capitalizeWords(str: String): String {
        return str.split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.lowercase(Locale.getDefault())
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
    }

    fun toEntity(parsed: ParsedSmsResult, customTimestamp: Long = System.currentTimeMillis()): TransactionEntity {
        return TransactionEntity(
            amount = parsed.amount,
            type = parsed.type,
            merchant = parsed.merchant,
            category = parsed.category,
            accountNumber = parsed.accountNumber,
            bankName = parsed.bankName,
            balanceAfter = parsed.balanceAfter,
            rawSmsBody = parsed.rawBody,
            sender = parsed.sender,
            timestamp = customTimestamp,
            isManual = false
        )
    }
}
