package com.cybersync.nodd.data

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrMatrix {

    /** One matrix cell per QR module, with a two-module quiet zone. */
    fun encode(text: String): BitMatrix {
        val hints = mutableMapOf<EncodeHintType, Any>(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 2,
        )
        // Declare UTF-8 only when the text needs it, so plain-ASCII codes stay readable by older scanners.
        if (text.any { it.code > 0x7F }) hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        return QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 0, 0, hints)
    }
}
