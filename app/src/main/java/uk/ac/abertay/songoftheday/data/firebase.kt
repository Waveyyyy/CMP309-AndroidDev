package uk.ac.abertay.songoftheday.data

data class FirebaseData (
    var tracks: MutableList<TrackData> = mutableListOf()
)

class TrackData (
    var track: String = "",
    var addedBy: String = "",
    var dateAdded: String = "",
    var href: String = ""
)