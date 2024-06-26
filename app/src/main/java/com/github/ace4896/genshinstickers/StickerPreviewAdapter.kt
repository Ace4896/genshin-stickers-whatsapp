/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.github.ace4896.genshinstickers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.SimpleDraweeView
import com.github.ace4896.genshinstickers.StickerPackLoader.getStickerAssetUri
import kotlin.math.max
import kotlin.math.min

class StickerPreviewAdapter internal constructor(
    private val layoutInflater: LayoutInflater,
    private val errorResource: Int,
    private val cellSize: Int,
    private val cellPadding: Int,
    private val stickerPack: StickerPack,
    private val expandedStickerPreview: SimpleDraweeView?
) : RecyclerView.Adapter<StickerPreviewViewHolder>() {
    private val cellLimit = 0
    private var recyclerView: RecyclerView? = null
    private var clickedStickerPreview: View? = null
    private var expandedViewLeftX = 0f
    private var expandedViewTopY = 0f
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): StickerPreviewViewHolder {
        val itemView = layoutInflater.inflate(R.layout.sticker_image_item, viewGroup, false)
        val vh = StickerPreviewViewHolder(itemView)
        val layoutParams = vh.stickerPreviewView.layoutParams
        layoutParams.height = cellSize
        layoutParams.width = cellSize
        vh.stickerPreviewView.setLayoutParams(layoutParams)
        vh.stickerPreviewView.setPadding(cellPadding, cellPadding, cellPadding, cellPadding)
        return vh
    }

    override fun onBindViewHolder(stickerPreviewViewHolder: StickerPreviewViewHolder, i: Int) {
        stickerPreviewViewHolder.stickerPreviewView.setImageResource(errorResource)
        stickerPreviewViewHolder.stickerPreviewView.setImageURI(
            getStickerAssetUri(
                stickerPack.identifier, stickerPack.stickers!![i]!!.imageFileName
            )
        )
        stickerPreviewViewHolder.stickerPreviewView.setOnClickListener {
            expandPreview(
                i,
                stickerPreviewViewHolder.stickerPreviewView
            )
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        recyclerView.addOnScrollListener(hideExpandedViewScrollListener)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        recyclerView.removeOnScrollListener(hideExpandedViewScrollListener)
        this.recyclerView = null
    }

    private val hideExpandedViewScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dx != 0 || dy != 0) {
                    hideExpandedStickerPreview()
                }
            }
        }

    private fun positionExpandedStickerPreview(selectedPosition: Int) {
        if (expandedStickerPreview != null) {
            // Calculate the view's center (x, y), then use expandedStickerPreview's height and
            // width to
            // figure out what where to position it.
            val recyclerViewLayoutParams = recyclerView!!.layoutParams as MarginLayoutParams
            val recyclerViewLeftMargin = recyclerViewLayoutParams.leftMargin
            val recyclerViewRightMargin = recyclerViewLayoutParams.rightMargin
            val recyclerViewWidth = recyclerView!!.width
            val recyclerViewHeight = recyclerView!!.height
            val clickedViewHolder =
                recyclerView!!.findViewHolderForAdapterPosition(selectedPosition) as StickerPreviewViewHolder?
            if (clickedViewHolder == null) {
                hideExpandedStickerPreview()
                return
            }
            clickedStickerPreview = clickedViewHolder.itemView
            val clickedViewCenterX = (clickedStickerPreview!!.x
                    + recyclerViewLeftMargin
                    + (clickedStickerPreview!!.width / 2f))
            val clickedViewCenterY = clickedStickerPreview!!.y + clickedStickerPreview!!.height / 2f
            expandedViewLeftX = clickedViewCenterX - expandedStickerPreview.width / 2f
            expandedViewTopY = clickedViewCenterY - expandedStickerPreview.height / 2f

            // If the new x or y positions are negative, anchor them to 0 to avoid clipping
            // the left side of the device and the top of the recycler view.
            expandedViewLeftX = max(expandedViewLeftX.toDouble(), 0.0).toFloat()
            expandedViewTopY = max(expandedViewTopY.toDouble(), 0.0).toFloat()

            // If the bottom or right sides are clipped, we need to move the top left positions
            // so that those sides are no longer clipped.
            val adjustmentX = max(
                (((expandedViewLeftX
                        + expandedStickerPreview.width
                        )) - recyclerViewWidth
                        - recyclerViewRightMargin).toDouble(),
                0.0
            ).toFloat()
            val adjustmentY = max(
                (expandedViewTopY + expandedStickerPreview.height - recyclerViewHeight).toDouble(),
                0.0
            ).toFloat()
            expandedViewLeftX -= adjustmentX
            expandedViewTopY -= adjustmentY
            expandedStickerPreview.x = expandedViewLeftX
            expandedStickerPreview.y = expandedViewTopY
        }
    }

    private fun expandPreview(position: Int, clickedStickerPreview: View) {
        if (isStickerPreviewExpanded) {
            hideExpandedStickerPreview()
            return
        }
        this.clickedStickerPreview = clickedStickerPreview
        if (expandedStickerPreview != null) {
            positionExpandedStickerPreview(position)
            val stickerAssetUri = getStickerAssetUri(
                stickerPack.identifier,
                stickerPack.stickers!![position]!!.imageFileName
            )
            val controller: DraweeController = Fresco.newDraweeControllerBuilder()
                .setUri(stickerAssetUri)
                .setAutoPlayAnimations(true)
                .build()
            expandedStickerPreview.setImageResource(errorResource)
            expandedStickerPreview.setController(controller)
            expandedStickerPreview.setVisibility(View.VISIBLE)
            recyclerView!!.setAlpha(EXPANDED_STICKER_PREVIEW_BACKGROUND_ALPHA)
            expandedStickerPreview.setOnClickListener(View.OnClickListener { hideExpandedStickerPreview() })
        }
    }

    fun hideExpandedStickerPreview() {
        if (isStickerPreviewExpanded && expandedStickerPreview != null) {
            clickedStickerPreview!!.visibility = View.VISIBLE
            expandedStickerPreview.setVisibility(View.INVISIBLE)
            recyclerView!!.setAlpha(COLLAPSED_STICKER_PREVIEW_BACKGROUND_ALPHA)
        }
    }

    private val isStickerPreviewExpanded: Boolean
        get() = expandedStickerPreview != null && expandedStickerPreview.visibility == View.VISIBLE

    override fun getItemCount(): Int {
        val numberOfPreviewImagesInPack: Int = stickerPack.stickers!!.size
        return if (cellLimit > 0) {
            min(numberOfPreviewImagesInPack.toDouble(), cellLimit.toDouble()).toInt()
        } else numberOfPreviewImagesInPack
    }

    companion object {
        private const val COLLAPSED_STICKER_PREVIEW_BACKGROUND_ALPHA = 1f
        private const val EXPANDED_STICKER_PREVIEW_BACKGROUND_ALPHA = 0.2f
    }
}
