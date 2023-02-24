package cafe.rob.sanagen

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
class Word(word: String = "") {
    @PrimaryKey
    @ColumnInfo(name = "word")
    var word: String = word

}