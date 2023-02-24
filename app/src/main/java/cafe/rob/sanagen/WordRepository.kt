package cafe.rob.sanagen

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class WordRepository(private val wordDao: WordDao) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    val words: LiveData<List<Word>> = wordDao.getAll().asLiveData()

    @WorkerThread
    suspend fun insert(w: String) {
        wordDao.insert(Word(w))
    }

    @WorkerThread
    suspend fun delete(w: String) {
        wordDao.deleteBy(w)
    }

    @WorkerThread
    suspend fun deleteAll() {
        wordDao.deleteAll()
    }
}