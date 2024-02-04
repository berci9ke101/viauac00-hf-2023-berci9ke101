package hu.kszi2.android.schpincer.api


import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json

/**
 * Common json parser with ignore setting
 */
private val json = Json { ignoreUnknownKeys = true }

private val API_URL = "https://schpincer.sch.bme.hu/api"
private val ITEMS_NOW = "/items/now"
private val ITEMS_TOMORROW = "/items/tomorrow"
private val ITEMS_DEVELOPER = "/items"

/**
 * Serializable class for Openings
 *
 * @property circleName the name of the opening circle
 * @property nextOpeningDate the date of the next opening in epoch millis
 * @property outOfStock indicates whether you can order from the circle or not
 */

@Serializable
data class Opening(
    @SerialName("circleName") var circleName: String,
    @SerialName("nextOpeningDate") var nextOpeningDate: Long,
    @SerialName("outOfStock") var outOfStock: Boolean
) {
    /**
     * Two Opening objects are equivalent if their opening date and their circle name are the same
     *
     * @param other other object to compare with
     * @return whether the two opening objects are the same
     */
    override operator fun equals(other: Any?): Boolean {
        if (other is Opening) {
            return this.circleName == other.circleName && this.nextOpeningDate == other.nextOpeningDate
        }
        return false
    }

    /**
     * HashCode function
     * @return the hash code of the object
     */
    override fun hashCode(): Int {
        var result = circleName.hashCode()
        result = 31 * result + nextOpeningDate.hashCode()
        result = 31 * result + outOfStock.hashCode()
        return result
    }
}


/**
 * Returns the body of a HTTP webpage, terminates if the get time exceeds the given timeout
 * @param url URL of the webpage
 * @param timeMillis timeout time in millis
 * @return the body of the page as a string
 */
private suspend fun getBody(url: Url, timeMillis: Long = 20000L): String {
    val client = HttpClient(CIO) {
        install(UserAgent) {
            //shh, magic ;)
            agent =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36 OPR/102.0.0.0"
        }
    }
    return run { withTimeout(timeMillis) { client.get(url).body() } }
}

/**
 * Parses the body of the SchPincerApi and returns the current openings
 *
 * @return a list of the current openings
 */
private suspend fun parseBody(): List<Opening> {
    //we need this magic to use the public api, so we request the items that are available now
    //and we match them to a circle, kinda jerky
    val now: String = getBody(Url(API_URL + ITEMS_NOW))
    val tomorrow: String = getBody(Url(API_URL + ITEMS_TOMORROW))

    //parser
    return try {
        val ret = mutableSetOf<Opening>()
        json.decodeFromString<List<Opening>>(now).forEach {
            if (!it.outOfStock) {
                ret.add(it)
            }
        }
        json.decodeFromString<List<Opening>>(tomorrow).forEach {
            if (!it.outOfStock) {
                ret.add(it)
            }
        }
        ret.toList()
    } catch (e: Throwable) { //handle errors...
        listOf()
    }
}

/**
 * Returns the current openings
 *
 * @return a list of the current openings
 */
suspend fun getOpenings() = parseBody()

/**
 * Returns the current openings (dummy developer openings)
 *
 * @return a list of the current openings
 */
suspend fun getDeveloperOpenings(): List<Opening> {
    val develop: String = getBody(Url(API_URL + ITEMS_DEVELOPER))
    //parser
    return try {
        val ret = mutableSetOf<Opening>()
        json.decodeFromString<List<Opening>>(develop).forEach {
            if (!it.outOfStock) {
                ret.add(it)
            }
        }
        ret.toList()
    } catch (e: Throwable) { //handle errors...
        listOf()
    }
}