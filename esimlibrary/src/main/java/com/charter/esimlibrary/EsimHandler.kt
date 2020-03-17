package com.charter.esimlibrary

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.telephony.euicc.DownloadableSubscription
import android.telephony.euicc.EuiccManager
import android.util.Log

class EsimHandler(val onSuccess: (result: String) -> Unit, val onFailure: () -> Unit = {}) {

    companion object {
        const val ACTION_DOWNLOAD_SUBSCRIPTION = "download_subscription"
        const val TAG_ESIM = "TAG_ESIM"
    }

    private lateinit var context: Context

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val resultCode = resultCode
            val detailedCode = intent?.getIntExtra(
                EuiccManager.EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE, 0)
            Log.d(TAG_ESIM, "onReceive: detailedCode: $detailedCode")
            Log.d(TAG_ESIM, "onReceive: resultCode: $resultCode")

            if (resultCode == EuiccManager.EMBEDDED_SUBSCRIPTION_RESULT_OK) { /*Download profile was successful*/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // eSim is active
                    context?.getString(R.string.on_success_response_active_esim)?.let {
                        onSuccess(it)
                    }
                } else {
                    // eSim is inactive due to the SDK does not support this API level
                    context?.getString(R.string.on_success_response_inactive_esim)?.let {
                        onSuccess(it)
                    }
                }
            } else { /*Download profile was not successful*/
                onFailure()
                Log.d(TAG_ESIM, "onReceive: detailedCode: $detailedCode")
            }
        }
    }

    fun init(context: Context) {
        this.context = context
        this.context.registerReceiver(receiver, IntentFilter(ACTION_DOWNLOAD_SUBSCRIPTION),
            null, null)
    }

    fun downloadEsim(code: String) {
        val mgr = context.getSystemService(Context.EUICC_SERVICE) as EuiccManager

        if (!mgr.isEnabled) {
            Log.d(TAG_ESIM, context.getString(R.string.euiccmanager_null_check))
            return
        }

        // Download subscription asynchronously
        val sub = DownloadableSubscription.forActivationCode(code)
        val intent = Intent(ACTION_DOWNLOAD_SUBSCRIPTION)
        val callbackIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        mgr.downloadSubscription(sub, true, callbackIntent)
    }

    fun onDestroy() {
        context.unregisterReceiver(receiver)
    }
}