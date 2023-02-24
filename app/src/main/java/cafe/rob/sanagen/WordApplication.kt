package cafe.rob.sanagen

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class WordApplication: Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val database by lazy { WordDatabase.getDatabase(this, applicationScope) }
    val wordRepository by lazy { WordRepository(database.wordDao()) }
}