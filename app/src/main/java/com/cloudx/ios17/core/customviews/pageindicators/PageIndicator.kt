package com.cloudx.ios17.core.customviews.pageindicators

/** Base class for a page indicator. */
interface PageIndicator {
    fun setScroll(currentScroll: Int, totalScroll: Int)
    fun setActiveMarker(activePage: Int)
    fun setMarkersCount(numMarkers: Int)
}
