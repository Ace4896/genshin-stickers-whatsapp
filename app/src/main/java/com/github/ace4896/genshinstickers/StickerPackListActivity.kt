/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.github.ace4896.genshinstickers

import android.os.AsyncTask
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.ace4896.genshinstickers.StickerPackListAdapter.OnAddButtonClickedListener
import com.github.ace4896.genshinstickers.WhitelistCheck.isWhitelisted
import java.lang.ref.WeakReference
import java.util.Arrays
import kotlin.math.max
import kotlin.math.min

class StickerPackListActivity : AddStickerPackActivity() {
    private var packLayoutManager: LinearLayoutManager? = null
    private var packRecyclerView: RecyclerView? = null
    private var allStickerPacksListAdapter: StickerPackListAdapter? = null
    private var whiteListCheckAsyncTask: WhiteListCheckAsyncTask? = null
    private var stickerPackList: ArrayList<StickerPack>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sticker_pack_list)
        packRecyclerView = findViewById(R.id.sticker_pack_list)
        stickerPackList = intent.getParcelableArrayListExtra(EXTRA_STICKER_PACK_LIST_DATA)
        showStickerPackList(stickerPackList)
        if (supportActionBar != null) {
            supportActionBar!!.title = getResources().getQuantityString(
                R.plurals.title_activity_sticker_packs_list,
                stickerPackList!!.size
            )
        }
    }

    override fun onResume() {
        super.onResume()
        whiteListCheckAsyncTask = WhiteListCheckAsyncTask(this)
        whiteListCheckAsyncTask!!.execute(*stickerPackList!!.toTypedArray<StickerPack>())
    }

    override fun onPause() {
        super.onPause()
        if (whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask!!.isCancelled) {
            whiteListCheckAsyncTask!!.cancel(true)
        }
    }

    private fun showStickerPackList(stickerPackList: List<StickerPack>?) {
        allStickerPacksListAdapter =
            StickerPackListAdapter(stickerPackList!!, object : OnAddButtonClickedListener {
                override fun onAddButtonClicked(stickerPack: StickerPack?) {
                    addStickerPackToWhatsApp(stickerPack!!.identifier!!, stickerPack!!.name!!)
                }
            })
        packRecyclerView!!.setAdapter(allStickerPacksListAdapter)
        packLayoutManager = LinearLayoutManager(this)
        packLayoutManager!!.setOrientation(RecyclerView.VERTICAL)
        val dividerItemDecoration = DividerItemDecoration(
            packRecyclerView!!.context,
            packLayoutManager!!.orientation
        )
        packRecyclerView!!.addItemDecoration(dividerItemDecoration)
        packRecyclerView!!.setLayoutManager(packLayoutManager)
        packRecyclerView!!.getViewTreeObserver()
            .addOnGlobalLayoutListener { recalculateColumnCount() }
    }

    private fun recalculateColumnCount() {
        val previewSize =
            getResources().getDimensionPixelSize(R.dimen.sticker_pack_list_item_preview_image_size)
        val firstVisibleItemPosition = packLayoutManager!!.findFirstVisibleItemPosition()
        val viewHolder =
            packRecyclerView!!.findViewHolderForAdapterPosition(firstVisibleItemPosition) as StickerPackListItemViewHolder?
        if (viewHolder != null) {
            val widthOfImageRow = viewHolder.imageRowView.measuredWidth
            val max = max((widthOfImageRow / previewSize).toDouble(), 1.0).toInt()
            val maxNumberOfImagesInARow = min(
                STICKER_PREVIEW_DISPLAY_LIMIT.toDouble(),
                max.toDouble()
            ).toInt()
            val minMarginBetweenImages =
                (widthOfImageRow - maxNumberOfImagesInARow * previewSize) / (maxNumberOfImagesInARow - 1)
            allStickerPacksListAdapter!!.setImageRowSpec(
                maxNumberOfImagesInARow,
                minMarginBetweenImages
            )
        }
    }

    internal class WhiteListCheckAsyncTask(stickerPackListActivity: StickerPackListActivity) :
        AsyncTask<StickerPack?, Void?, List<StickerPack?>>() {
        private val stickerPackListActivityWeakReference: WeakReference<StickerPackListActivity>

        init {
            stickerPackListActivityWeakReference = WeakReference(stickerPackListActivity)
        }

        protected override fun doInBackground(vararg stickerPackArray: StickerPack?): List<StickerPack?>? {
            val stickerPackListActivity = stickerPackListActivityWeakReference.get()
                ?: return listOf(*stickerPackArray)
            for (stickerPack in stickerPackArray) {
                stickerPack?.let { pack ->
                    pack.isWhitelisted = isWhitelisted(stickerPackListActivity, pack.identifier!!)
                }
            }
            return listOf(*stickerPackArray)
        }

        override fun onPostExecute(stickerPackList: List<StickerPack?>) {
            val stickerPackListActivity = stickerPackListActivityWeakReference.get()
            if (stickerPackListActivity != null) {
                stickerPackListActivity.allStickerPacksListAdapter!!.setStickerPackList(
                    stickerPackList
                )
                stickerPackListActivity.allStickerPacksListAdapter!!.notifyDataSetChanged()
            }
        }
    }

    companion object {
        const val EXTRA_STICKER_PACK_LIST_DATA = "sticker_pack_list"
        private const val STICKER_PREVIEW_DISPLAY_LIMIT = 5
    }
}
