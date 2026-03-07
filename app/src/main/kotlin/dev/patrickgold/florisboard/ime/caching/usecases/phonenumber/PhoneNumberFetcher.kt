package dev.patrickgold.florisboard.ime.caching.usecases.phonenumber

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.SubscriptionManager

@SuppressLint("HardwareIds", "MissingPermission")
fun getPhoneNumbers(context: Context): List<String> {
    val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
    val subscriptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        subscriptionManager.activeSubscriptionInfoList ?: emptyList()
    } else {
        emptyList()
    }
    return subscriptions
        .mapNotNull { info ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                info.number?.takeIf { it.isNotBlank() }
            } else null
        }
        .distinct()
}
