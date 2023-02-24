@file:OptIn(ExperimentalFoundationApi::class)

package cafe.rob.sanagen

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Paint.Align
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.rob.sanagen.ui.theme.SanagenTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import dev.burnoo.compose.rememberpreference.rememberBooleanPreference
import kotlin.math.roundToInt
import kotlin.random.Random




class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




        setContent {
            SanagenTheme {

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val owner = LocalViewModelStoreOwner.current

                    owner?.let {
                        val viewModel: WordViewModel = viewModel(
                            it,
                            "WordViewModel",
                            WordViewModelFactory(
                                (LocalContext.current.applicationContext as WordApplication).wordRepository
                            )
                        )

                        Generator(12, viewModel)
                    }

                }
            }
        }

        MobileAds.initialize(this) {}
    }


}

var clickedTimes = 0

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}


@Composable
fun SavedWords(viewModel: WordViewModel) {
    val allWords by viewModel.allWords.observeAsState(listOf())
    val mContext = LocalContext.current
    val view = LocalView.current
    var clickedTimesM by remember { mutableStateOf(0) }
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        stickyHeader {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.background(MaterialTheme.colorScheme.background, RectangleShape)
                    .padding(0.dp, 0.dp, 0.dp, 4.dp).fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Favorite, "heart",
                    modifier = Modifier.padding(horizontal = 4.dp)
                        .clickable {
                            clickedTimes += 1
                        })
                Text(
                    text = "Favs",
                    fontWeight = FontWeight.Bold, modifier = Modifier.combinedClickable(
                        onClick = {
                            mToast(mContext, "Long tap to delete all favourites!")
                        },
                        onLongClick = {
                            viewModel.deleteAll()
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        }
                    ).padding(2.dp)
                )
            }
        }
        allWords.forEach {
            item(
                key = it.word
            ) {
                Text(
                    text = it.word,
                    modifier = Modifier.combinedClickable(
                        onClick = {
                            mToast(mContext, "Long tap to delete!")
                        },
                        onLongClick = {
                            viewModel.delete(it.word)
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        }
                    ).padding(4.dp).animateItemPlacement(),
                )
            }
        }
    }
}

private fun mToast(context: Context, text: String){
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

@Composable
fun Generator(wordCount: Int = 18, viewModel: WordViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    Column {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var sliderValue by remember { mutableStateOf(1000f) }
            var lengthValue by remember { mutableStateOf(5f) }
            var randomValue by rememberBooleanPreference(
                "randval", false, false
            )
            var randomValueThresh by rememberBooleanPreference(
                "randthre", false, false
            )

            var showAds by rememberBooleanPreference(
                "adsenabled", true, true
            )

            val wordList = remember { mutableStateListOf<String>() }



            fun regenWordList(threshold: Int, wordSize: Int, listSize: Int) {
                wordList.clear()
                generateSequence {
                    val wLength = if (randomValue)
                        Random.nextInt(3, 10)
                    else
                        wordSize

                    val wThresh = if (randomValueThresh)
                        Random.nextInt(600, 2200)
                    else
                        threshold
                    WordGenerator.coerceWordGen(desiredLength = wLength, minThresh = wThresh)
                }.take(listSize).toCollection(wordList)
            }

            regenWordList(sliderValue.roundToInt(), lengthValue.roundToInt(), wordCount)


            if (showAds)
                AdvertView()

            LazyVerticalGrid(
                columns = GridCells.Fixed(
                    if (lengthValue <= 11)
                        3
                    else
                        2
                ),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                itemsIndexed(wordList) { idx, word  ->
                    Text(
                        fontWeight = FontWeight.Bold,
                        text = word,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(4.dp)
                            .clickable {
                                viewModel.insert(word)
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            }
                        )

                }

            }

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                "normalness: ${sliderValue.roundToInt()}"
            )

            Slider(
                value = sliderValue,
                onValueChange = {
                    sliderValue = it
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                },
                steps = 7,
                valueRange = 400f..2000f,
                enabled = !randomValueThresh
            )

            Text(
                "length: ${lengthValue.roundToInt()}"
            )

            Slider(
                value = lengthValue,
                onValueChange = {
                    lengthValue = it
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                },
                steps = 9,
                valueRange = 3f..13f,
                enabled = !randomValue
            )

            Divider()

            Row {
                Column(
                    modifier = Modifier.fillMaxHeight().weight(1.25f).padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = randomValue,
                            onCheckedChange = {
                                randomValue = it
                            }
                        )

                        Text(
                            text = "random word length",
                            textAlign = TextAlign.Start,
                            modifier = Modifier.width(164.dp).clickable {
                                randomValue = !randomValue
                            },
                            fontSize = 16.sp
                        )
                    }
                    Spacer(
                        Modifier.padding(8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = randomValueThresh,
                            onCheckedChange = {
                                randomValueThresh = it
                            }
                        )

                        Text(
                            text = "random normalness",
                            textAlign = TextAlign.Start,
                            modifier = Modifier.width(164.dp).clickable {
                                randomValueThresh = !randomValueThresh
                            },
                            fontSize = 16.sp
                        )
                    }

                    Spacer(
                        Modifier.padding(16.dp)
                    )

                    Button(
                        onClick = {
                            if (clickedTimes == 4) {
                                showAds = !showAds
                                if (showAds)
                                    mToast(context, "Ad enabled")
                                else
                                    mToast(context, "Ad disabled")
                            }
                            clickedTimes = 0
                            regenWordList(wordSize = lengthValue.roundToInt(), threshold = sliderValue.roundToInt(), listSize =  wordCount)
                        },
                        content = {
                            Text("regenerate")
                        }
                    )
                }

                Column(
                    modifier =  Modifier.fillMaxHeight().weight(1f)
                ) {
                    SavedWords(viewModel)
                }
            }

        }



    }
}

@Composable
fun AdvertView(modifier: Modifier = Modifier) {
    val isInEditMode = LocalInspectionMode.current
    if (isInEditMode) {
        Text(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.Red)
                .padding(horizontal = 2.dp, vertical = 6.dp),
            textAlign = TextAlign.Center,
            color = Color.White,
            text = "Advert Here",
        )
    } else {
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = context.getString(R.string.ad_id_banner)
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AdvertPreview() {
    AdvertView()
}
