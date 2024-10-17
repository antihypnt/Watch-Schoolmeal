package com.example.watch_schoolmeal.presentation

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Bundle
import android.text.style.TtsSpan.TextBuilder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.RotaryScrollEvent
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {

    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val day = dateFormat.format(Date())

    val calendar = Calendar.getInstance()
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    calendar.time = Date()
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    val nextDay = dateFormat.format(calendar.time)

    var today by remember { mutableStateOf(arrayOf("")) }
    var tmr by remember { mutableStateOf(arrayOf("")) }
    var todayB by remember { mutableStateOf("") }
    var todayL by remember { mutableStateOf("") }
    var todayD by remember { mutableStateOf("") }
    var tmrB by remember { mutableStateOf("") }
    var tmrL by remember { mutableStateOf("") }
    var tmrD by remember { mutableStateOf("") }

    var mode by remember { mutableIntStateOf(when (currentHour) {
        1, 2, 3, 4, 5, 6, 7, 8 -> 1
        9, 10, 11, 12, 13 -> 2
        14, 15, 16, 17 -> 3
        else -> 4
    }) }

    fun modeToTitle(mode: Int): String {
        if (mode == 1) return "오늘의 아침"
        else if (mode == 2) return "오늘의 점심"
        else if (mode == 3) return "오늘의 저녁"
        else if (mode == 4) return "내일의 아침"
        else if (mode == 5) return "내일의 점심"
        else if (mode == 6) return "내일의 저녁"
        else return "일시적 오류가 발생했어요."
    }

    fun modeToContent(mode: Int): String {
        if (mode == 1) return todayB
        else if (mode == 2) return todayL
        else if (mode == 3) return todayD
        else if (mode == 4) return tmrB
        else if (mode == 5) return tmrL
        else if (mode == 6) return tmrD
        else return "개발자에게 문의하세요."
    }

    LaunchedEffect(day) {
        today = GetSchoolMeal(day)
        tmr = GetSchoolMeal(nextDay)
        todayB = today[0]
        todayL = today[1]
        todayD = today[2]
        tmrB = tmr[0]
        tmrL = tmr[1]
        tmrD = tmr[2]
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .onRotaryScrollEvent { event ->
                val delta = event.verticalScrollPixels
                if (delta > 0) {
                    if (mode < 3) mode += 3
                } else {
                    if (mode > 3) mode -= 3
                }
                true
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (mode != 1) {Text(modifier = Modifier.clickable { mode -= 1 }, text = " ←", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 30.sp)}
            else {Text(text = " ←", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 30.sp)}

            Spacer(modifier = Modifier.weight(1f))

            Column (
                modifier = Modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    text = modeToTitle(mode)
                )

                Text(
                    modifier = Modifier,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFFF79F1C),
                    fontSize = 10.sp,
                    text = if (mode <= 3) {day.substring(0, 4) + "." + day.substring(4, 6) + "." + day.substring(6, 8)} else {nextDay.substring(0, 4) + "." + nextDay.substring(4, 6) + "." + nextDay.substring(6, 8)}
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    modifier = Modifier,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = 12.sp,
                    text = modeToContent(mode)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (mode != 6) {Text(modifier = Modifier.clickable { mode += 1 }, text = "→ ", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 30.sp)}
            else {Text(text = "→ ", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 30.sp)}
        }
    }
}

suspend fun GetSchoolMeal(day: String): Array<String> {
    return withContext(Dispatchers.IO) {
        try {
            var breakfast = ""
            var lunch = ""
            var dinner = ""
            val docs = Jsoup.connect("https://open.neis.go.kr/hub/mealServiceDietInfo?KEY=67c2bd83a7e14117a89cd682f1bb8673&Type=json&ATPT_OFCDC_SC_CODE=J10&SD_SCHUL_CODE=7530851&MLSV_YMD=${day}").ignoreContentType(true).get().select("body").toString()
            val schoolMealJSON = JSONObject(docs.substring(docs.indexOf("{"), docs.lastIndexOf("}") + 1))
            val data1 = schoolMealJSON.getJSONArray("mealServiceDietInfo")
            val data2 = JSONObject(data1[1].toString())
            val data3 = data2.getJSONArray("row")
            try {breakfast = JSONObject(data3[0].toString())["DDISH_NM"].toString().replace("\n", "").replace("<br>", "\n").replace("&amp;", "&").replace(Regex("\\(.*?\\)"), "").trim()} catch (e: Exception) {breakfast = "아침 정보가 없습니다."}
            try {lunch = JSONObject(data3[1].toString())["DDISH_NM"].toString().replace("\n", "").replace("<br>", "\n").replace("&amp;", "&").replace(Regex("\\(.*?\\)"), "").trim()} catch (e: Exception) {lunch = "점심 정보가 없습니다."}
            try {dinner = JSONObject(data3[2].toString())["DDISH_NM"].toString().replace("\n", "").replace("<br>", "\n").replace("&amp;", "&").replace(Regex("\\(.*?\\)"), "").trim()} catch (e: Exception) {dinner = "저녁 정보가 없습니다."}
            arrayOf(breakfast.toString(), lunch.toString(), dinner.toString())
        } catch (e: Exception) {
            arrayOf("", "", "")
        }
    }
}
