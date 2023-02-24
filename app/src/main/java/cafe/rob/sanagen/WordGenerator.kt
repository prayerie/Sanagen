package cafe.rob.sanagen

import android.widget.Toast
import androidx.compose.ui.text.capitalize
import java.util.Locale
import kotlin.random.Random


object WordGenerator {
    private fun Char.toAlphaIndex(): Int = this.lowercaseChar().code - 97
    private fun Int.fromAlphaIndex(): Char = (this + 97).toChar()

    private val blacklist = arrayListOf(
        "cunt", "shit", "nigg", "faggot", "tranny", "dyke", "cock", "whore"
    )

    private val probMatrix: Array<IntArray> = arrayOf(intArrayOf(97, 3175, 6104, 2051, 4998, 1347, 2313, 4289, 4178, 633, 750, 6859, 5892, 4711, 1268, 3477, 15, 9771, 3107, 5975, 1364, 1688, 2121, 166, 572, 723),
        intArrayOf(3087, 750, 16, 218, 926, 27, 67, 130, 1253, 0, 129, 284, 1448, 289, 1274, 74, 3, 978, 215, 237, 1178, 1, 117, 5, 217, 13),
        intArrayOf(4579, 62, 657, 138, 3707, 18, 15, 51, 6637, 2, 35, 327, 137, 3909, 2128, 58, 1, 1346, 2854, 807, 1407, 4, 27, 204, 354, 4),
        intArrayOf(3356, 65, 26, 719, 10313, 15, 38, 53, 2635, 1, 34, 1028, 31, 5049, 1844, 44, 0, 2120, 102, 59, 1072, 11, 125, 0, 209, 4),
        intArrayOf(479, 3017, 4361, 7433, 2854, 1639, 3766, 5548, 5556, 435, 2871, 10012, 5388, 9360, 642, 4968, 1, 12825, 7059, 12294, 1369, 5089, 1557, 384, 590, 1682),
        intArrayOf(754, 32, 11, 108, 1316, 1280, 53, 83, 1450, 2, 68, 293, 90, 889, 696, 50, 0, 425, 156, 232, 410, 4, 52, 14, 57, 0),
        intArrayOf(2494, 8, 15, 476, 1403, 13, 874, 20, 2513, 1, 21, 235, 7, 12022, 2117, 20, 0, 1124, 103, 85, 935, 5, 19, 3, 102, 2),
        intArrayOf(545, 38, 5494, 179, 467, 18, 1424, 45, 60, 0, 192, 99, 33, 298, 258, 2006, 0, 314, 4163, 3892, 40, 6, 641, 68, 54, 43),
        intArrayOf(2691, 2371, 2854, 5497, 1147, 2437, 2184, 3704, 65, 213, 1913, 9659, 4478, 5335, 1148, 2783, 13, 9668, 5208, 14182, 1351, 2358, 1456, 490, 434, 690),
        intArrayOf(104, 84, 0, 110, 132, 3, 7, 10, 61, 6, 11, 9, 3, 156, 58, 9, 0, 54, 23, 10, 45, 1, 4, 0, 12, 0),
        intArrayOf(1114, 6, 3243, 16, 296, 6, 15, 29, 425, 2, 65, 394, 8, 1030, 782, 35, 0, 910, 761, 25, 166, 0, 67, 0, 23, 12),
        intArrayOf(9475, 3103, 1708, 918, 5793, 1416, 1395, 334, 4895, 0, 462, 5679, 117, 331, 4560, 2106, 1, 910, 1365, 1164, 3312, 27, 251, 17, 391, 120),
        intArrayOf(3388, 67, 26, 230, 3120, 14, 193, 322, 2712, 0, 74, 397, 1176, 246, 3590, 79, 2, 1632, 1622, 319, 2293, 0, 49, 6, 475, 18),
        intArrayOf(12390, 45, 25, 317, 11250, 21, 755, 339, 21428, 0, 346, 287, 238, 1499, 14065, 62, 0, 1509, 1049, 283, 4929, 7, 502, 4, 395, 15),
        intArrayOf(109, 2680, 7090, 2164, 890, 1654, 1421, 3987, 7181, 547, 318, 4854, 3265, 2981, 2928, 3259, 2, 6938, 2455, 4829, 274, 767, 1247, 119, 316, 259),
        intArrayOf(2765, 24, 20, 107, 2028, 9, 35, 57, 1675, 3, 58, 312, 2381, 239, 2459, 1534, 0, 774, 2462, 129, 1269, 4, 58, 373, 462, 12),
        intArrayOf(107, 1, 78, 10, 329, 0, 2, 7, 198, 0, 0, 10, 8, 153, 78, 0, 1, 66, 318, 1, 12, 0, 0, 4, 2, 6),
        intArrayOf(10028, 1986, 2559, 1331, 21613, 1169, 2327, 774, 2507, 5, 171, 77, 47, 243, 8127, 3551, 2, 1747, 98, 5018, 4507, 38, 317, 4, 301, 6),
        intArrayOf(5009, 664, 473, 1473, 18195, 187, 885, 373, 10367, 4, 973, 1490, 830, 5348, 3089, 1082, 1, 5316, 6775, 3340, 4674, 41, 400, 7, 860, 9),
        intArrayOf(12627, 127, 2769, 84, 4739, 508, 105, 969, 6708, 0, 70, 1102, 33, 8066, 2973, 987, 3, 3016, 11405, 2414, 2909, 9, 48, 361, 229, 15),
        intArrayOf(1732, 1770, 1933, 1134, 661, 1243, 1259, 1109, 459, 575, 171, 1902, 1282, 923, 5081, 1250, 1913, 2155, 2406, 2273, 18, 134, 44, 89, 116, 54),
        intArrayOf(1261, 30, 7, 164, 1324, 0, 0, 6, 2374, 1, 8, 327, 17, 507, 1590, 5, 0, 525, 37, 21, 115, 31, 1, 18, 16, 10),
        intArrayOf(831, 31, 2, 219, 968, 11, 69, 177, 49, 1, 111, 85, 33, 179, 1975, 47, 2, 246, 624, 414, 14, 0, 21, 10, 139, 11),
        intArrayOf(302, 3, 0, 0, 1569, 0, 0, 0, 248, 0, 0, 12, 0, 41, 407, 1, 0, 14, 0, 1, 108, 0, 1, 33, 26, 0),
        intArrayOf(1416, 273, 655, 442, 963, 217, 440, 823, 33, 0, 280, 4313, 309, 461, 453, 275, 0, 1756, 666, 2073, 62, 63, 53, 53, 6, 70),
        intArrayOf(463, 1, 26, 21, 238, 0, 16, 5, 2152, 0, 0, 15, 1, 126, 178, 3, 0, 40, 10, 221, 167, 0, 9, 2, 9, 307))

    fun wordGen(startWith: Char = ' ', desiredLength: Int = 6, minThresh: Int = 500): String {
        var c = if (startWith == ' ')
            Random.nextInt(0, 25)
        else
            startWith.toAlphaIndex()
        val word = mutableListOf(c)
        val sumf = probMatrix[c].sum()

        for (i in 0 until desiredLength - 1) {
            val perc = mutableListOf<Int>()
            for (j in 0..25)
                perc.add(probMatrix[j][c] / sumf) // todo to float?
            val randVal = Random.nextFloat() * perc.max()
            val x = (0..25).shuffled()
            for (k in x) {
                if (perc[k] >= randVal) {
                    if (probMatrix[k][c] < minThresh)
                        continue
                    word.add(k)
                    c = k
                    break
                }
            }

        }

        return word.joinToString(separator = "", transform = {
                it.fromAlphaIndex().toString()
            })
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }

    fun coerceWordGen(startWith: Char = ' ', desiredLength: Int = 6, minThresh: Int = 500): String {
        var word = ""
        var tries = 0
        while (word.length < 3 || blacklist.any {  b ->
                word.contains(b, ignoreCase = true)
            }) {
            word = wordGen(startWith, desiredLength, minThresh)
            tries += 1
            if (tries > 1000)
                break
        }
        return word
    }
}