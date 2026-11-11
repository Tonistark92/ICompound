package com.icompound.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.dial(phone: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
    runCatching { startActivity(intent) } // devices with no dialer (tablets) would crash otherwise
}
