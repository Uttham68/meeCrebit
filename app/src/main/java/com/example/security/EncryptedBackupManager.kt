package com.example.security

import android.content.Context
import android.util.Base64
import com.example.data.model.BudgetEntity
import com.example.data.model.MerchantRuleEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.ZenProfileEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class BackupMetadata(
    val exportTimestamp: Long,
    val transactionCount: Int,
    val budgetCount: Int,
    val ruleCount: Int,
    val fileSizeFormatted: String
)

data class RestoredLedgerData(
    val transactions: List<TransactionEntity>,
    val budgets: List<BudgetEntity>,
    val rules: List<MerchantRuleEntity>,
    val zenProfile: ZenProfileEntity?
)

object EncryptedBackupManager {

    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
    private const val PBKDF2_ITERATIONS = 10000
    private const val KEY_LENGTH = 256

    /**
     * Creates an AES-256 GCM encrypted backup file from offline ledger data.
     */
    fun createEncryptedBackup(
        context: Context,
        password: String,
        transactions: List<TransactionEntity>,
        budgets: List<BudgetEntity>,
        rules: List<MerchantRuleEntity>,
        zenProfile: ZenProfileEntity?
    ): File {
        val rootJson = JSONObject().apply {
            put("version", 1)
            put("app", "meeCrebit")
            put("exportTimestamp", System.currentTimeMillis())
            put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

            // Serialize transactions
            val txArray = JSONArray()
            for (tx in transactions) {
                txArray.put(JSONObject().apply {
                    put("id", tx.id)
                    put("amount", tx.amount)
                    put("type", tx.type.name)
                    put("merchant", tx.merchant)
                    put("category", tx.category.name)
                    put("accountNumber", tx.accountNumber)
                    put("bankName", tx.bankName)
                    if (tx.balanceAfter != null) put("balanceAfter", tx.balanceAfter)
                    if (tx.rawSmsBody != null) put("rawSmsBody", tx.rawSmsBody)
                    if (tx.sender != null) put("sender", tx.sender)
                    put("timestamp", tx.timestamp)
                    put("isManual", tx.isManual)
                })
            }
            put("transactions", txArray)

            // Serialize budgets
            val budgetArray = JSONArray()
            for (b in budgets) {
                budgetArray.put(JSONObject().apply {
                    put("id", b.id)
                    put("category", b.category.name)
                    put("monthlyLimit", b.monthlyLimit)
                    put("monthYear", b.monthYear)
                })
            }
            put("budgets", budgetArray)

            // Serialize rules
            val ruleArray = JSONArray()
            for (r in rules) {
                ruleArray.put(JSONObject().apply {
                    put("id", r.id)
                    put("pattern", r.pattern)
                    put("matchType", r.matchType.name)
                    put("targetCategory", r.targetCategory.name)
                    if (r.overrideMerchantName != null) put("overrideMerchantName", r.overrideMerchantName)
                    put("isEnabled", r.isEnabled)
                })
            }
            put("rules", ruleArray)

            // Zen profile
            if (zenProfile != null) {
                put("zenProfile", JSONObject().apply {
                    put("totalPoints", zenProfile.totalPoints)
                    put("privacyScore", zenProfile.privacyScore)
                })
            }
        }

        val plainBytes = rootJson.toString().toByteArray(StandardCharsets.UTF_8)
        val encryptedPayload = encryptWithPassword(plainBytes, password)

        val timestampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val backupFile = File(context.cacheDir, "meeCrebit_Encrypted_Backup_$timestampStr.meecrebit")
        FileOutputStream(backupFile).use { it.write(encryptedPayload.toByteArray(StandardCharsets.UTF_8)) }

        return backupFile
    }

    /**
     * Decrypts an encrypted backup file using user-supplied password.
     */
    fun restoreFromEncryptedBackup(
        backupFileContent: String,
        password: String
    ): RestoredLedgerData {
        val decryptedJsonString = decryptWithPassword(backupFileContent, password)
        val root = JSONObject(decryptedJsonString)

        val appTag = root.optString("app", "")
        if (appTag != "meeCrebit") {
            throw IllegalArgumentException("Invalid backup file format for meeCrebit.")
        }

        // Restore transactions
        val txList = mutableListOf<TransactionEntity>()
        val txArray = root.optJSONArray("transactions") ?: JSONArray()
        for (i in 0 until txArray.length()) {
            val obj = txArray.getJSONObject(i)
            txList.add(
                TransactionEntity(
                    id = 0, // Auto-generate new IDs on restore
                    amount = obj.getDouble("amount"),
                    type = com.example.data.model.TransactionType.valueOf(obj.getString("type")),
                    merchant = obj.getString("merchant"),
                    category = com.example.data.model.ExpenseCategory.valueOf(obj.getString("category")),
                    accountNumber = obj.getString("accountNumber"),
                    bankName = obj.getString("bankName"),
                    balanceAfter = if (obj.has("balanceAfter")) obj.getDouble("balanceAfter") else null,
                    rawSmsBody = if (obj.has("rawSmsBody")) obj.getString("rawSmsBody") else null,
                    sender = if (obj.has("sender")) obj.getString("sender") else null,
                    timestamp = obj.getLong("timestamp"),
                    isManual = obj.optBoolean("isManual", false)
                )
            )
        }

        // Restore budgets
        val budgetList = mutableListOf<BudgetEntity>()
        val budgetArray = root.optJSONArray("budgets") ?: JSONArray()
        for (i in 0 until budgetArray.length()) {
            val obj = budgetArray.getJSONObject(i)
            budgetList.add(
                BudgetEntity(
                    id = 0,
                    category = com.example.data.model.ExpenseCategory.valueOf(obj.getString("category")),
                    monthlyLimit = obj.getDouble("monthlyLimit"),
                    monthYear = obj.getString("monthYear")
                )
            )
        }

        // Restore rules
        val ruleList = mutableListOf<MerchantRuleEntity>()
        val ruleArray = root.optJSONArray("rules") ?: JSONArray()
        for (i in 0 until ruleArray.length()) {
            val obj = ruleArray.getJSONObject(i)
            ruleList.add(
                MerchantRuleEntity(
                    id = 0,
                    pattern = obj.getString("pattern"),
                    matchType = com.example.data.model.RuleMatchType.valueOf(obj.getString("matchType")),
                    targetCategory = com.example.data.model.ExpenseCategory.valueOf(obj.getString("targetCategory")),
                    overrideMerchantName = if (obj.has("overrideMerchantName")) obj.getString("overrideMerchantName") else null,
                    isEnabled = obj.optBoolean("isEnabled", true)
                )
            )
        }

        var zenProfile: ZenProfileEntity? = null
        if (root.has("zenProfile")) {
            val pObj = root.getJSONObject("zenProfile")
            zenProfile = ZenProfileEntity(
                id = 1,
                totalPoints = pObj.optInt("totalPoints", 120),
                privacyScore = pObj.optInt("privacyScore", 100)
            )
        }

        return RestoredLedgerData(
            transactions = txList,
            budgets = budgetList,
            rules = ruleList,
            zenProfile = zenProfile
        )
    }

    private fun encryptWithPassword(plainBytes: ByteArray, password: CharSequence): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)

        val iv = ByteArray(GCM_IV_LENGTH)
        random.nextBytes(iv)

        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(password.toString().toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH)
        val secretKey = SecretKeySpec(keyFactory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val cipherText = cipher.doFinal(plainBytes)

        val envelope = JSONObject().apply {
            put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
            put("ciphertext", Base64.encodeToString(cipherText, Base64.NO_WRAP))
            put("iterations", PBKDF2_ITERATIONS)
        }

        return envelope.toString()
    }

    private fun decryptWithPassword(envelopeJsonStr: String, password: CharSequence): String {
        val envelope = JSONObject(envelopeJsonStr)
        val salt = Base64.decode(envelope.getString("salt"), Base64.NO_WRAP)
        val iv = Base64.decode(envelope.getString("iv"), Base64.NO_WRAP)
        val cipherText = Base64.decode(envelope.getString("ciphertext"), Base64.NO_WRAP)
        val iterations = envelope.optInt("iterations", PBKDF2_ITERATIONS)

        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(password.toString().toCharArray(), salt, iterations, KEY_LENGTH)
        val secretKey = SecretKeySpec(keyFactory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

        val plainBytes = cipher.doFinal(cipherText)
        return String(plainBytes, StandardCharsets.UTF_8)
    }
}
