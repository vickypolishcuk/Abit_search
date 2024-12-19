package com.example.course_work.functions

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.security.cert.X509Certificate
import javax.net.ssl.*

import com.example.course_work.models.Majority
import com.example.course_work.models.MajorityInfo
import com.example.course_work.models.RateData
import com.example.course_work.models.Regions
import com.example.course_work.models.Search
import com.example.course_work.models.University
import com.example.course_work.models.UniversityData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.nodes.Element

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.File
import android.content.Context
import android.content.res.Resources
import com.example.course_work.R
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.map
import java.util.concurrent.Semaphore

//@Serializable
//data class SearchNew(
//    val okr: String, // окр (магістр / бакалавр)
//    val status: String, // статус
//    val numberOfPlace: String, // номер у списку
//    val priority: String, // пріоритет заяви
//    val point: String, // конкурсний бал
//    val sbo: String, // середній бал документа про освіту
//    val consistOfSubjects: List<String>,// складові конкурсного балу
//    val consistOfPoint: List<String>,// складові конкурсного балу
//    val university: String, // навчальний заклад
//    val majority: String, // спеціальність
//    val kvota: String, // квота
//    val doc: String, // документи
//)
//
//@Serializable
//data class DataWrapper(
//    val name: String,
//    @Contextual val data: SearchNew
//)
//
//fun parser(context: Context) {
////    val fileName = "data2024.json" // Назва JSON файлу раніше ти надав мені код
//
//    val inputStream = context.resources.openRawResource(R.raw.data2024)
//    val jsonString = inputStream.bufferedReader().use { it.readText() }
//
//    // Декодуємо JSON у список DataWrapper
//    val dataList = Json.decodeFromString<List<DataWrapper>>(jsonString)
//
//    // Фільтруємо об'єкти та отримуємо список типу List<Search>
//    val filteredResults: List<SearchNew> = dataList
//        .filter { it.name == "Name2" } // Фільтрація за ім'ям
//        .map { it.data } // Отримуємо лише Search
//
//    // Вивід результату
//    println("Знайдені об'єкти:")
//    filteredResults.forEach { println(it) }
//}

fun trustAllCertificates() {
    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

        override fun checkClientTrusted(certificates: Array<X509Certificate>, authType: String) {}

        override fun checkServerTrusted(certificates: Array<X509Certificate>, authType: String) {}
    })

    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, trustAllCerts, java.security.SecureRandom())
    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)

    // Ігнорування перевірки хостнейма
    HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
}

// Функція для парсингу інформації про спеціальність
suspend fun parseMajorityInfo(rateListHref: String): MajorityInfo? = withContext(Dispatchers.IO) {
    trustAllCertificates()

    val url = "https://abit-poisk.org.ua${rateListHref}"
    var data: MajorityInfo? = null

    try {
        val document: Document = Jsoup.connect(url).get()

        val header = document.selectFirst("div.card-header")!!

        // Заголовок
        val title = header.selectFirst(".headline")!!.ownText().trim()

        // Підзаголовок
        val majorityDiv = document.selectFirst("div.card-after-header > div.horizontal-scroll-xs")
        val subTitle = majorityDiv?.ownText()?.split("/")?.lastOrNull()?.trim()?: ""

        // Деталі інституту
        val instituteDetails = header.selectFirst("div.subhead-2.horizontal-scroll-xs")?.text()?.replace("•", "•") ?: ""

        // Інформація про місця
        val seatsInfo = header.select("div.body-2").getOrNull(0)?.text()?.replace("•", "•") ?: ""

        // Інформація про заявки та конкурс
        val applicationInfo = header.select("div.body-2").getOrNull(1)?.text()?.replace("•", "•") ?: ""

        // Прохідний бал (може не бути)
        val passingScore = header.select("div.body-2 div span.with-material-icon a").text().trim() ?: ""

        data = MajorityInfo(
            title = title,
            subTitle = subTitle,
            majorityDetails = instituteDetails,
            seatsInfo = seatsInfo,
            applicationInfo = applicationInfo,
            passingScore = passingScore
        )
    } catch (e: Exception) {
        println("Помилка: ${e.message}")
    }
    data
}

// для сторінки спеціальностей, повертаємо інформацію про всі спеціальності
suspend fun parseMajority(parameter: String): List<Majority> = withContext(Dispatchers.IO) {

    trustAllCertificates()

    // Формування URL для запиту
    val url = "https://abit-poisk.org.ua${parameter}"

    // Масив для збереження даних
    val data = mutableListOf<Majority>()

    try {
        // Надсилання GET-запиту
        val document: Document = Jsoup.connect(url).get()

        // Обробка отриманої HTML-сторінки
        val table = document.select("table")

        // Ітерація по рядках таблиці та перетворення в масиви
        val rows = table.select("tr")

        // Пропускаємо перший рядок, оскільки це заголовок таблиці
        for (row in rows.drop(1)) {
            // Отримуємо комірки рядка
            val cells = row.select("td")

            if (cells.size >= 5) {
                // Отримуємо значення з кожної комірки
                val okr = cells[0].text().trim() // ОКР
                val titleElement = cells[1].select(".title").first() // Знаходимо елемент з класом "title"
                val institution = titleElement?.nextElementSibling()?.text()?.trim() ?: "" // Інститут
                val descriptionText = cells[1].select(".secondary-text").text().trim()
                // Очищаємо опис від зайвої інформації (до першого появлення "Заяв" чи інших даних)
                val description = descriptionText.split("Заяв")[0].trim() // Певний опис
                val name = titleElement?.text()?.trim() ?: "" // Спеціальність
                val freePlaceText = cells[2].text().trim() // Бюджетні місця
                val freePlace = freePlaceText.split("max")[0].trim()
                val totalPlace = cells[3].text().trim() // Всього місць
                val numberOfClaims = cells[5].text().trim() // Конкурс
                val href = cells[5].select("a[href]").attr("href")

                // Додаємо об'єкт до списку
                data.add(Majority(name, institution, description, okr, freePlace, totalPlace, numberOfClaims, href))
            }
        }
    } catch (e: Exception) {
        println("Помилка: ${e.message}")
    }
    data
}

// Отримання списку регіонів
suspend fun getRegions(): List<Regions> = withContext(Dispatchers.IO) {
    trustAllCertificates()
    try {
        // Завантаження HTML сторінки
        val url = "https://abit-poisk.org.ua/rate2024"
        val document: Document = Jsoup.connect(url).get()

        // Вибір всіх елементів <a> всередині таблиці з університетами
        val rows = document.select("tr")

        val regions = rows.mapNotNull { row ->
            val regionLink = row.select("a[href]").firstOrNull()
            // Перевірка чи є тег <a> та чи є місця в університеті, і якщо так, то витягнення назви та посилання
            if (regionLink != null) {
                val regionsName = regionLink.text().trim()
                val regionsUrl = regionLink.attr("href")
                Regions(regionsName, regionsUrl)
            } else {
                null
            }
        }
        regions

    } catch (e: Exception) {
        println("Error fetching data: ${e.message}")
        emptyList()
    }
}

// Для сторінки спеціальностей, ширша інформація про конкретний заклад
suspend fun getUniversitiesInfo(regionHref: String, universityHref: String): University? = withContext(Dispatchers.IO) {
    trustAllCertificates()

    // Завантаження HTML сторінки
    val url = "https://abit-poisk.org.ua${regionHref}" // змінити регіон для інший університетів з інших регіонів

    try {
        val document: Document = Jsoup.connect(url).get()
        // val universityHref = "/rate2024/univer/174"

        val row: Element? = document.select("tr")
            .firstOrNull { row ->
                row.select("a[href]").any { it.attr("href").contains(universityHref) }
            }

        // Якщо рядок знайдено, витягуємо числа
        if (row != null) {
            val universityName = row.select("a").text()
            val universityUrl = row.attr("href")
            val (first, second, third, fourth) = row.select("td.text-right").map { it.text().trim()}
            University(universityName, first, second, third, fourth, universityUrl, regionHref)
        } else {
            println("No data found, returning empty list")
            null
        }

    } catch (e: Exception) {
        // Логування помилки
        println("Error fetching data: ${e.message}")
         // Повертаємо пустий список у випадку помилки
        null
    }
}

// Отримання даних рейтингового списку
suspend fun parseRateList(parameter: String): List<RateData> = withContext(Dispatchers.IO) {
    trustAllCertificates()

    val link = "https://abit-poisk.org.ua${parameter}"
    var currentPage = 1

    // Масив для збереження даних
    val data = mutableListOf<RateData>()

    try {
        while (true) {
            val currentLink = if (currentPage == 1) link else "$link/?page=$currentPage"
            println(currentLink)

            // Надсилання GET-запиту
            val document: Document = Jsoup.connect(currentLink).get()

            // Обробка отриманої HTML-сторінки
            val table = document.select("table")

            // Ітерація по рядках таблиці та перетворення в масиви
            val rows = table.select("tr")

            // Пропускаємо перший рядок, оскільки це заголовок таблиці
            for (row in rows.drop(1)) {
                // Отримуємо комірки рядка
                val cells = row.select("td")

                if (cells.size >= 7) {
                    // Отримуємо значення з кожної комірки
                    val number = cells[0].text().trim() // номер у списку
                    val name = cells[1].select("a").text().trim() // Ім'я
                    val priority = cells[2].text().trim() // пріоритет
                    val point = cells[3].text().trim() // конкурсний бал
                    val status = cells[4].text().trim() // Статус
                    val kvota = cells[6].text().trim() // Квота
                    var doc = cells[7].text().trim() // Виконано вимоги

                    val subjectElements = cells[5].select("li")
                    val consistOfSubjects = mutableListOf<String>()
                    val consistOfPoint = mutableListOf<String>()

                    subjectElements.takeWhile {
                        (!it.text().contains("ГК")) and (!it.text().contains("СК")) and (!it.text().contains("РК"))
                    }.forEach { element ->
                        val parts = element.text().split(":").toMutableList()
                        if (parts.size == 2) {
                            if (parts[0] == "Середній бал документа про освіту") {
                                parts[0] = "СБО"
                            }
                            else if (parts[0].contains("ЄФВВ")) {
                                parts[0] = "ЄФВВ"
                            }
                            else if (parts[0].contains("(")) {
                                parts[0] = "Іноземна мова"
                            }
                            consistOfSubjects.add(parts[0].trim()) // Назва предмета
                            consistOfPoint.add(parts[1].trim()) // Бал
                        }
                    }
                    if (doc == "done_all") {
                        doc = "+"
                    }

                    // Додаємо об'єкт до списку
                    data.add(
                        RateData(
                            name,
                            number,
                            priority,
                            status,
                            point,
                            kvota,
                            doc,
                            consistOfSubjects,
                            consistOfPoint
                        )
                    )
                }
            }
            // Перевірка, чи містить сторінка посилання на наступну сторінку
            val nextPageLink = "${parameter}/?page=${currentPage + 1}"
            val hasNextPage = document.select("a[href]").any { it.attr("href") == nextPageLink }

            if (!hasNextPage) {
                break // Якщо посилання немає, виходимо з циклу
            }

            currentPage++
        }
    } catch (e: Exception) {
        println("Помилка: ${e.message}")
    }
    data
}

suspend fun getUniversitiesByRegion(regionUrl: String, year: String): List<UniversityData> = withContext(Dispatchers.IO) {
    return@withContext try {
        // Формуємо URL для одного регіону
        val url = "https://abit-poisk.org.ua$regionUrl".replace("2024", year)
        try {
            val document: Document = Jsoup.connect(url).get()
            val rows = document.select("tr")

            // Вибір університетів
            rows.mapNotNull { row ->
                val universityLink = row.select("a[href]").firstOrNull()
                val numericData = row.select("td.text-right").map { it.text().trim() }

                // Перевірка чи є бюджетні місця
                if (universityLink != null && numericData.size > 2 && numericData[2] != "0") {
                    val universityName = universityLink.text().trim()
                    val universityUrl = universityLink.attr("href")
                    UniversityData(universityName, universityUrl, regionUrl)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            println("Error fetching universities for region $regionUrl: ${e.message}")
            emptyList()
        }
    } catch (e: Exception) {
        println("Error fetching data: ${e.message}")
        emptyList()
    }
}


suspend fun parseSearchAsync(
    name: String,
    year: String,
    region: String
): List<Search> = coroutineScope {
    val baseUrl = "https://abit-poisk.org.ua"
    val universitiesList = getUniversitiesByRegion(region, year)
//    val data = mutableListOf<Search>()
//    val semaphore = Semaphore(5) // Обмеження на 5 паралельних запитів
//
//    val jobs = universitiesList.map { university ->
//        async(Dispatchers.IO) {
//            semaphore.acquire() // Займаємо "слот"
//            try {
//                parseUniversity(university, name, baseUrl, year, data)
//            } catch (e: Exception) {
//                println("Помилка при парсингу університету ${university.name}: ${e.message}")
//            } finally {
//                semaphore.release() // Звільняємо "слот"
//            }
//        }
//    }
//    jobs.awaitAll() // Чекаємо завершення всіх запущених задач

    val data = mutableListOf<Search>()
    val channel = Channel<UniversityData>(capacity = 5) // Канал для обмеження паралельних завдань
    println("universitiesList.size=${universitiesList.size}")

    val workerJobs = List(5) { // 5 паралельних "працівників"
        launch(Dispatchers.IO) {
            for (university in channel) {
                try {
                    parseUniversity(university, name, baseUrl, year, data)
                } catch (e: Exception) {
                    println("Помилка при парсингу університету ${university.name}: ${e.message}")
                }
            }
        }
    }

    // Надсилаємо університети в канал
    universitiesList.forEach { channel.send(it) }

    channel.close() // Закриваємо канал, коли всі університети були відправлені
    workerJobs.joinAll()

    data

}

private suspend fun parseUniversity(
    university: UniversityData,
    name: String,
    baseUrl: String,
    year: String,
    data: MutableList<Search>
) {
    val universityUrl = "$baseUrl${university.href}".replace("2024", year)
    println("universityUrl=$universityUrl")

    if (universityUrl != "https://abit-poisk.org.ua/rate${year}/univer/1178") {
        try {
            val universityDocument = Jsoup.connect(universityUrl).get()
            val table = universityDocument.selectFirst("table") ?: return
            val rows = table.select("tr")

            for (row in rows.drop(1)) {
                val cells = row.select("td")
                if (cells.size >= 5) {
                    // Знаходимо елемент span, який містить кількість заяв
                    val applicationsText = row.select("span")
                        .filter { it.text().contains("Заяв") }
                        .first()?.text()

                    // Перевірка, чи є заяви для спеціальності
                    if (applicationsText != null) {
                        val applicationsCount =
                            applicationsText.split(" ").getOrNull(1)?.toIntOrNull() ?: 0
                        if (applicationsCount == 0) {
                            // Якщо кількість заяв = 0, пропускаємо спеціальність
                            println("Немає заяв для спеціальності.")
                            continue
                        }
                    } else {
                        // Якщо не знайшли інформацію про кількість заяв, пропускаємо
                        println("Не вдалося знайти кількість заяв для спеціальності.")
                        continue
                    }
                    val specialtyHref = row.select("td")[5].select("a[href]").attr("href")
                    if (specialtyHref.isNotEmpty()) {
                        parseSpecialty(baseUrl, specialtyHref, name, university.name, data)
                    }
                }
            }
        } catch (e: Exception) {
            println("Помилка при парсингу університетів: $e")
        }
    }
}

private suspend fun parseSpecialty(
    baseUrl: String,
    specialtyHref: String,
    name: String,
    universityName: String,
    data: MutableList<Search>
) {
    var currentPage = 1

    while (true) {
        val currentLink =
            if (currentPage == 1) "$baseUrl$specialtyHref" else "$baseUrl$specialtyHref/?page=$currentPage"
        try {
            val specialtyDocument = Jsoup.connect(currentLink).get()
            delay(100) // Встановити затримку між запитами для уникнення блокування - 0.1 секунди
            println("currentLink=${currentLink}")

            if (specialtyDocument.text().contains(name)) {
                println("specialtyTable contains $name")

                val specialtyTable = specialtyDocument.selectFirst("table")!!
                val specialtyRows = specialtyTable.select("tr")

                for (specialtyRow in specialtyRows.drop(1)) {
                    val nameCell =
                        specialtyRow.selectFirst("td.application-cell-ab-name a")
                            ?.text()
                            ?.trim()
                    val cells = specialtyRow.select("td")

                    if (nameCell == name) {
                        // Отримуємо значення з кожної комірки
                        val numberOfPlace = cells[0].text().trim()
                        val priority = cells[2].text().trim()
                        val point = cells[3].text().trim()
                        val status = cells[4].text().trim()
                        val kvota = cells[6].text().trim()
                        var doc = cells[7].text().trim()
                        var sbo = "0.0"

                        val consistOfSubjects = mutableListOf<String>()
                        val consistOfPoint = mutableListOf<String>()
                        if (cells[5].text() != "&mdash;") {
                            val subjectElements = cells[5].select("li")

                            subjectElements.takeWhile {
                                (!it.text().contains("ГК")) and (!it.text()
                                    .contains("СК")) and (!it.text().contains("РК"))
                            }.forEach { element ->
                                val parts =
                                    element.text().split(":").toMutableList()
                                if (parts.size == 2) {
                                    if (parts[0].trim() == "Середній бал документа про освіту") {
                                        sbo = parts[1].trim()
                                    } else if (parts[0].contains("ЄФВВ")) {
                                        parts[0] = "ЄФВВ"
                                    } else if (parts[0].contains("(")) {
                                        parts[0] = "Іноземна мова"
                                    }
                                    else {
                                        consistOfSubjects.add(parts[0].trim())
                                        consistOfPoint.add(parts[1].trim())
                                    }
                                }
                            }
                        }

                        if (doc == "done_all") {
                            doc = "+"
                        }

                        val header =
                            specialtyDocument.selectFirst("div.card-header")!!
                        val majority =
                            header.selectFirst(".headline")!!.ownText().trim()
                        val instituteDetails =
                            header.selectFirst("div.subhead-2.horizontal-scroll-xs")
                                ?.text()?.replace("•", "•") ?: ""
                        var okr: String
                        if (instituteDetails.contains("Бакалавр (на основі: ПЗСО)")) {
                            okr = "Б"
                        } else if (instituteDetails.contains("Бакалавр (на основі: Молодший спеціаліст)")) {
                            okr = "Бм"
                        } else if (instituteDetails.contains("Магістр (на основі: Бакалавр)")) {
                            okr = "М"
                        } else if (instituteDetails.contains("Бакалавр (на основі: Фаховий молодший бакалавр)")) {
                            okr = "Бф"
                        } else if (instituteDetails.contains("Фаховий молодший бакалавр")) {
                            okr = "Ф"
                        } else {
                            okr = "Б"
                        }

                        // Додавання до списку
                        val newSearch = Search(
                            okr,
                            status,
                            numberOfPlace,
                            priority,
                            point,
                            sbo,
                            consistOfSubjects,
                            consistOfPoint,
                            universityName,
                            majority,
                            kvota,
                            doc,
                            specialtyHref
                        )
                        synchronized(data) {
                            data.add(newSearch)
                        }
                    }
                }
            }

            val nextPageLink = "$specialtyHref/?page=${currentPage + 1}"
            val hasNextPage =
                specialtyDocument.select("a[href]").any { it.attr("href") == nextPageLink }

            if (!hasNextPage) break
            currentPage++
        } catch (e: Exception) {
            println("Помилка при парсингу спеціальності: $e")
            break
        }
    }
}

