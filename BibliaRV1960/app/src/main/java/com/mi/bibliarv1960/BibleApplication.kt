package com.mi.bibliarv1960

import android.app.Application
import com.mi.bibliarv1960.data.local.BibleContentDatabase
import com.mi.bibliarv1960.data.local.UserDataDatabase
import com.mi.bibliarv1960.data.repository.BibleRepository
import com.mi.bibliarv1960.data.preferences.DataStoreManager

class BibleApplication : Application() {
    val contentDatabase by lazy { BibleContentDatabase.getDatabase(this) }
    val userDataDatabase by lazy { UserDataDatabase.getDatabase(this) }
    val repository by lazy { 
        BibleRepository(
            contentDatabase.bibleContentDao(),
            userDataDatabase.userDataDao(),
        ) 
    }
    val dataStoreManager by lazy { DataStoreManager(this) }
}
