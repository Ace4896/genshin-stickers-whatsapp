/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.github.ace4896.genshinstickers

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Sticker(
    val imageFileName: String,
    val emojis: List<String>,
    var size: Long,
) : Parcelable {
    constructor(imageFileName: String?, emojis: List<String>?) : this(
        imageFileName = imageFileName ?: "",
        emojis = emojis ?: listOf(),
        size = 0,
    )
}
