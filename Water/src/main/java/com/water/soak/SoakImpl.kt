package com.water.soak

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

/**
 * Date：2024/8/12
 * Describe:
 */
class SoakImpl(
    val context: Context,
    private val infoType1: String,
    infoType2: String,
    private val name: String,
    uri: Uri
) {
    private var tag = 10
    private var isAllow = true

    init {
        isAllow = uri.toString().endsWith("/directories")
        if (isAllow) {
            tag = 1
        }
    }

    private val mySunName = arrayOf(
        "accountName",
        "accountType",
        "displayName",
        "typeResourceId",
        "exportSupport",
        infoType1,
        infoType2
    )

    fun getMyCursor(): Cursor? {
        if (isAllow.not()) return null
        if (infoType1.isBlank() || infoType1 == "si") return null
        val matrixCursor = MatrixCursor(mySunName)
        matrixCursor.addRow(
            arrayOf<Any>(name, name, name, 0, "1".toInt(), 1, tag)
        )
        return matrixCursor
    }
}