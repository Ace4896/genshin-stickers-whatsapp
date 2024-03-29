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
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.parcelableCreator

@Parcelize
data class StickerPack(
    val identifier: String,
    val name: String,
    val publisher: String,
    val trayImageFile: String,
    val publisherEmail: String,
    val publisherWebsite: String,
    val privacyPolicyWebsite: String,
    val licenseAgreementWebsite: String,
    var iosAppStoreLink: String,
    private var _stickers: List<Sticker>,
    var totalSize: Long,
    var androidPlayStoreLink: String,
    var isWhitelisted: Boolean,
    val imageDataVersion: String,
    val avoidCache: Boolean,
    val animatedStickerPack: Boolean,
) : Parcelable {
    var stickers: List<Sticker> = _stickers
        set(value) {
            field = value

            this.totalSize = 0
            for (sticker in value) {
                this.totalSize += sticker.size
            }
        }

    constructor(
        identifier: String?,
        name: String?,
        publisher: String?,
        trayImageFile: String?,
        publisherEmail: String?,
        publisherWebsite: String?,
        privacyPolicyWebsite: String?,
        licenseAgreementWebsite: String?,
        imageDataVersion: String?,
        avoidCache: Boolean,
        animatedStickerPack: Boolean
    ) : this(
        identifier = identifier ?: "",
        name = name ?: "",
        publisher = publisher ?: "",
        trayImageFile = trayImageFile ?: "",
        publisherEmail = publisherEmail ?: "",
        publisherWebsite = publisherWebsite ?: "",
        privacyPolicyWebsite = privacyPolicyWebsite ?: "",
        licenseAgreementWebsite = licenseAgreementWebsite ?: "",
        iosAppStoreLink = "",
        _stickers = listOf(),
        totalSize = 0,
        androidPlayStoreLink = "",
        isWhitelisted = false,
        imageDataVersion = imageDataVersion ?: "",
        avoidCache = avoidCache,
        animatedStickerPack = animatedStickerPack,
    )

    private companion object : Parceler<StickerPack> {
        override fun StickerPack.write(parcel: Parcel, flags: Int) {
            parcel.writeString(identifier)
            parcel.writeString(name)
            parcel.writeString(publisher)
            parcel.writeString(trayImageFile)
            parcel.writeString(publisherEmail)
            parcel.writeString(publisherWebsite)
            parcel.writeString(privacyPolicyWebsite)
            parcel.writeString(licenseAgreementWebsite)
            parcel.writeString(iosAppStoreLink)
            parcel.writeTypedList(stickers)
            parcel.writeLong(totalSize)
            parcel.writeString(androidPlayStoreLink)
            parcel.writeByte((if (isWhitelisted) 1 else 0).toByte())
            parcel.writeString(imageDataVersion)
            parcel.writeByte((if (avoidCache) 1 else 0).toByte())
            parcel.writeByte((if (animatedStickerPack) 1 else 0).toByte())
        }

        override fun create(parcel: Parcel): StickerPack {
            return StickerPack(
                identifier = parcel.readString() ?: "",
                name = parcel.readString() ?: "",
                publisher = parcel.readString() ?: "",
                trayImageFile = parcel.readString() ?: "",
                publisherEmail = parcel.readString() ?: "",
                publisherWebsite = parcel.readString() ?: "",
                privacyPolicyWebsite = parcel.readString() ?: "",
                licenseAgreementWebsite = parcel.readString() ?: "",
                iosAppStoreLink = parcel.readString() ?: "",
                _stickers = parcel.createTypedArrayList(parcelableCreator<Sticker>()) ?: listOf(),
                totalSize = parcel.readLong(),
                androidPlayStoreLink = parcel.readString() ?: "",
                isWhitelisted = parcel.readByte().toInt() != 0,
                imageDataVersion = parcel.readString() ?: "",
                avoidCache = parcel.readByte().toInt() != 0,
                animatedStickerPack = parcel.readByte().toInt() != 0,
            )
        }
    }
}
