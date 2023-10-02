package com.vitorpamplona.amethyst.service

import android.content.Context
import androidx.compose.runtime.Immutable
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.service.lnurl.LightningAddressResolver
import com.vitorpamplona.amethyst.ui.components.GenericLoadable
import com.vitorpamplona.quartz.events.Event
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Base64

@Immutable
data class CashuToken(
    val token: String,
    val mint: String,
    val totalAmount: Long,
    val fees: Int,
    val redeemInvoiceAmount: Long,
    val proofs: JsonNode
)

class CashuProcessor {
    fun parse(cashuToken: String): GenericLoadable<CashuToken> {
        checkNotInMainThread()

        try {
            val base64token = cashuToken.replace("cashuA", "")
            val cashu = jacksonObjectMapper().readTree(String(Base64.getDecoder().decode(base64token)))
            val token = cashu.get("token").get(0)
            val proofs = token.get("proofs")
            val mint = token.get("mint").asText()

            var totalAmount = 0L
            for (proof in proofs) {
                totalAmount += proof.get("amount").asLong()
            }
            val fees = Math.max(((totalAmount * 0.02).toInt()), 2)
            val redeemInvoiceAmount = totalAmount - fees

            return GenericLoadable.Loaded(CashuToken(cashuToken, mint, totalAmount, fees, redeemInvoiceAmount, proofs))
        } catch (e: Exception) {
            return GenericLoadable.Error<CashuToken>("Could not parse this cashu token")
        }
    }

    suspend fun melt(token: CashuToken, lud16: String, onSuccess: (String, String) -> Unit, onError: (String, String) -> Unit, context: Context) {
        checkNotInMainThread()

        runCatching {
            LightningAddressResolver().lnAddressInvoice(
                lnaddress = lud16,
                milliSats = token.redeemInvoiceAmount * 1000, // Make invoice and leave room for fees
                message = "Redeem Cashu",
                onSuccess = { invoice ->
                    meltInvoice(token, invoice, onSuccess, onError, context)
                },
                onProgress = {
                },
                onError = onError,
                context = context
            )
        }
    }

    private fun meltInvoice(token: CashuToken, invoice: String, onSuccess: (String, String) -> Unit, onError: (String, String) -> Unit, context: Context) {
        try {
            val client = HttpClient.getHttpClient()
            val url = token.mint + "/melt" // Melt cashu tokens at Mint

            val factory = Event.mapper.nodeFactory

            val jsonObject = factory.objectNode()
            jsonObject.put("proofs", token.proofs)
            jsonObject.put("pr", invoice)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonObject.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use {
                val body = it.body.string()
                val tree = jacksonObjectMapper().readTree(body)

                val successful = tree?.get("paid")?.asText() == "true"

                if (successful) {
                    onSuccess(
                        context.getString(R.string.cashu_sucessful_redemption),
                        context.getString(R.string.cashu_sucessful_redemption_explainer, token.totalAmount.toString(), token.fees.toString())
                    )
                } else {
                    val msg = tree?.get("detail")?.asText()?.split('.')?.getOrNull(0)?.ifBlank { null }
                    onError(
                        context.getString(R.string.cashu_failed_redemption),
                        if (msg != null) {
                            context.getString(R.string.cashu_failed_redemption_explainer_error_msg, msg)
                        } else {
                            context.getString(R.string.cashu_failed_redemption_explainer_error_msg)
                        }
                    )
                }
            }
        } catch (e: Exception) {
            onError(context.getString(R.string.cashu_sucessful_redemption), context.getString(R.string.cashu_failed_redemption_explainer_error_msg, e.message))
        }
    }
}
