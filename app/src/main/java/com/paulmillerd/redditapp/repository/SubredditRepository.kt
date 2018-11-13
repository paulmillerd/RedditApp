package com.paulmillerd.redditapp.repository

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PageKeyedDataSource
import android.arch.paging.PagedList
import android.text.TextUtils
import com.paulmillerd.redditapp.SortOrder
import com.paulmillerd.redditapp.api.RedditService
import com.paulmillerd.redditapp.api.responseModels.listing.Listing
import com.paulmillerd.redditapp.api.responseModels.listing.Thing
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubredditRepository @Inject constructor(private val redditService: RedditService) {

    fun getListing(subreddit: String?, sortOrder: SortOrder): LiveData<PagedList<Thing>> {
        return LivePagedListBuilder<String, Thing>(object : DataSource.Factory<String, Thing>() {
            override fun create(): DataSource<String, Thing> {
                return object : PageKeyedDataSource<String, Thing>() {
                    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, Thing>) {
                        fetchPage(subreddit, sortOrder, null, callback, null)
                    }

                    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, Thing>) {
                        fetchPage(subreddit, sortOrder, params.key, null, callback)
                    }

                    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, Thing>) {
                        fetchPage(subreddit, sortOrder, params.key, null, callback)
                    }
                }
            }
        }, 10).build()
    }

    private fun fetchPage(subreddit: String?, sortOrder: SortOrder, afterKey: String? = null,
                          initCallback: PageKeyedDataSource.LoadInitialCallback<String, Thing>?,
                          callback: PageKeyedDataSource.LoadCallback<String, Thing>?) {
        if (TextUtils.isEmpty(subreddit)) {
            redditService.getFrontPage(sortOrder.pathParam, afterKey)
        } else {
            redditService.getSubreddit(subreddit!!, sortOrder.pathParam, afterKey)
        }.enqueue(object : Callback<Listing> {
            override fun onResponse(call: Call<Listing>, response: Response<Listing>) {
                if (response.isSuccessful && response.body() != null) {
                    val list = mutableListOf<Thing?>()
                    response.body()?.data?.children?.let { children ->
                        list.addAll(children.asIterable())
                    }
                    if (initCallback != null) {
                        initCallback.onResult(list, null, response.body()?.data?.after)
                    } else callback?.onResult(list, response.body()?.data?.after)
                }
            }

            override fun onFailure(call: Call<Listing>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

}