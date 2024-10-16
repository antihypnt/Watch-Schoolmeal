package com.example.watch_schoolmeal.presentation

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup

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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Greeting(dateFormat.format(Date()))
    }
}

@Composable
fun Greeting(day: String) {
    var schoolMeal by remember { mutableStateOf("로딩중...") }

    LaunchedEffect(day) {
        schoolMeal = GetSchoolMeal(day)
    }

    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = Color.White,
        text = schoolMeal
    )
}

suspend fun GetSchoolMeal(day: String): String {
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
            lunch.toString()
        } catch (e: Exception) {
            "오류가 발생했습니다.\n개발자 (IG : @antihypnt)에게 문의해 주세요."
        }
    }
}
