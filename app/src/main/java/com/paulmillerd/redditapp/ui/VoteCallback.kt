package com.paulmillerd.redditapp.ui

import com.paulmillerd.redditapp.redditApi.responseModels.listing.Thing

interface VoteCallback {
    fun upvoteTapped(thing: Thing)
    fun downvoteTapped(thing: Thing)
}