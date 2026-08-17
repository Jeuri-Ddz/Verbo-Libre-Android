package com.mi.bibliarv1960.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Reader : Screen("reader/{bookId}/{chapter}?verse={verse}") {
        fun createRoute(bookId: Int, chapter: Int, verse: Int? = null): String {
            return if (verse != null) "reader/$bookId/$chapter?verse=$verse"
            else "reader/$bookId/$chapter"
        }
    }
    object Search : Screen("search")
    object Purpose : Screen("purpose")
    object DailyVerse : Screen("daily_verse")
    object Devotional : Screen("devotional")
    object Challenge : Screen("challenge")
}
