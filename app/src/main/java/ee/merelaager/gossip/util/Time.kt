package ee.merelaager.gossip.util

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun formatCreatedAt(isoString: String): String {
    val zonedDateTime = ZonedDateTime.parse(isoString).withZoneSameInstant(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("dd.MM '@' HH:mm")
    return zonedDateTime.format(formatter)
}
