/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.github.ace4896.genshinstickers

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

internal class Sticker : Parcelable {
    @JvmField
    val imageFileName: String?

    @JvmField
    val emojis: List<String>?

    @JvmField
    var size: Long = 0

    constructor(imageFileName: String?, emojis: List<String>?) {
        this.imageFileName = imageFileName
        this.emojis = emojis
    }

    private constructor(input: Parcel) {
        imageFileName = input.readString()
        emojis = input.createStringArrayList()
        size = input.readLong()
    }

    fun setSize(size: Long) {
        this.size = size
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(imageFileName)
        dest.writeStringList(emojis)
        dest.writeLong(size)
    }

    companion object {
        @JvmField
        val CREATOR: Creator<Sticker?> = object : Creator<Sticker?> {
            override fun createFromParcel(`in`: Parcel): Sticker {
                return Sticker(`in`)
            }

            override fun newArray(size: Int): Array<Sticker?> {
                return arrayOfNulls(size)
            }
        }
    }
}
