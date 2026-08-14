package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

enum class RuleMatchType(val displayName: String) {
    CONTAINS("Contains Text"),
    STARTS_WITH("Starts With"),
    EXACT("Exact Match"),
    REGEX("Regular Expression")
}

@Entity(tableName = "merchant_rules")
data class MerchantRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pattern: String,
    val matchType: RuleMatchType = RuleMatchType.CONTAINS,
    val targetCategory: ExpenseCategory,
    val overrideMerchantName: String? = null,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun matches(rawText: String, merchantCandidate: String): Boolean {
        if (!isEnabled) return false
        val lowerPattern = pattern.trim().lowercase(Locale.getDefault())
        val lowerRaw = rawText.lowercase(Locale.getDefault())
        val lowerMerchant = merchantCandidate.lowercase(Locale.getDefault())

        return when (matchType) {
            RuleMatchType.CONTAINS -> lowerRaw.contains(lowerPattern) || lowerMerchant.contains(lowerPattern)
            RuleMatchType.STARTS_WITH -> lowerMerchant.startsWith(lowerPattern) || lowerRaw.startsWith(lowerPattern)
            RuleMatchType.EXACT -> lowerMerchant == lowerPattern
            RuleMatchType.REGEX -> runCatching {
                val regex = Regex(pattern, RegexOption.IGNORE_CASE)
                regex.containsMatchIn(rawText) || regex.containsMatchIn(merchantCandidate)
            }.getOrDefault(false)
        }
    }
}
