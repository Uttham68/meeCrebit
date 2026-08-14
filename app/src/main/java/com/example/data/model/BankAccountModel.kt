package com.example.data.model

import java.util.Locale

enum class AccountType(val displayName: String) {
    SAVINGS("Savings Account"),
    CURRENT("Current Account"),
    CREDIT_CARD("Credit Card"),
    CASH_WALLET("Cash on Hand"),
    WALLET("Digital Wallet")
}

data class BankAccountBalance(
    val accountKey: String, // e.g. "HDFC_XX9123"
    val bankName: String,
    val accountNumber: String,
    val accountType: AccountType,
    val currentBalance: Double,
    val lastUpdatedTimestamp: Long,
    val transactionCount: Int,
    val totalDebits: Double,
    val totalCredits: Double
)

data class MultiBankSummary(
    val accounts: List<BankAccountBalance>,
    val totalLiquidBalance: Double, // Sum of savings, current, cash
    val totalCreditCardSpend: Double,
    val cashOnHand: Double,
    val lastSyncTimestamp: Long
)

object MultiBankBalanceCalculator {

    fun calculateBalances(transactions: List<TransactionEntity>): MultiBankSummary {
        val accountMap = mutableMapOf<String, MutableList<TransactionEntity>>()
        var cashOnHand = 0.0

        for (tx in transactions) {
            val bank = tx.bankName.ifBlank { "Offline Bank" }
            val acc = tx.accountNumber.ifBlank { "XX0000" }
            val key = "${bank.uppercase(Locale.getDefault())}_${acc.uppercase(Locale.getDefault())}"

            accountMap.getOrPut(key) { mutableListOf() }.add(tx)

            // Cash on hand tracking: Cash withdrawals from ATM add to Cash wallet
            if (tx.category == ExpenseCategory.CASH || tx.merchant.contains("ATM", true)) {
                if (tx.type == TransactionType.DEBIT) {
                    cashOnHand += tx.amount
                }
            }
        }

        val accountList = mutableListOf<BankAccountBalance>()
        var totalLiquid = 0.0
        var totalCreditCardSpend = 0.0
        var latestSync = 0L

        for ((key, txList) in accountMap) {
            val sorted = txList.sortedBy { it.timestamp }
            val latest = sorted.last()
            if (latest.timestamp > latestSync) {
                latestSync = latest.timestamp
            }

            // Determine latest balance: take explicitly reported balanceAfter if present, else calculate
            val latestReportedBal = sorted.lastOrNull { it.balanceAfter != null }?.balanceAfter
            val computedBal = if (latestReportedBal != null) {
                latestReportedBal
            } else {
                val debits = sorted.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
                val credits = sorted.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount }
                (credits - debits).coerceAtLeast(0.0)
            }

            val totalDeb = sorted.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
            val totalCred = sorted.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount }

            val isCreditCard = latest.rawSmsBody?.contains("card", true) == true ||
                    latest.rawSmsBody?.contains("limit", true) == true ||
                    latest.accountNumber.startsWith("CC")

            val accType = when {
                isCreditCard -> AccountType.CREDIT_CARD
                key.contains("PAYTM", true) -> AccountType.WALLET
                else -> AccountType.SAVINGS
            }

            if (accType == AccountType.CREDIT_CARD) {
                totalCreditCardSpend += totalDeb
            } else {
                totalLiquid += computedBal
            }

            accountList.add(
                BankAccountBalance(
                    accountKey = key,
                    bankName = latest.bankName,
                    accountNumber = latest.accountNumber,
                    accountType = accType,
                    currentBalance = computedBal,
                    lastUpdatedTimestamp = latest.timestamp,
                    transactionCount = sorted.size,
                    totalDebits = totalDeb,
                    totalCredits = totalCred
                )
            )
        }

        // Add Cash on Hand wallet if cash was withdrawn or recorded
        if (cashOnHand > 0.0) {
            accountList.add(
                BankAccountBalance(
                    accountKey = "WALLET_CASH",
                    bankName = "Cash Wallet",
                    accountNumber = "On Hand",
                    accountType = AccountType.CASH_WALLET,
                    currentBalance = cashOnHand,
                    lastUpdatedTimestamp = System.currentTimeMillis(),
                    transactionCount = 1,
                    totalDebits = 0.0,
                    totalCredits = cashOnHand
                )
            )
            totalLiquid += cashOnHand
        }

        return MultiBankSummary(
            accounts = accountList.sortedByDescending { it.currentBalance },
            totalLiquidBalance = totalLiquid,
            totalCreditCardSpend = totalCreditCardSpend,
            cashOnHand = cashOnHand,
            lastSyncTimestamp = if (latestSync > 0) latestSync else System.currentTimeMillis()
        )
    }
}
