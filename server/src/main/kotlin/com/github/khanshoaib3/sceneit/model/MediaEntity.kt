package com.github.khanshoaib3.sceneit.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.io.Serializable
import java.time.Instant


enum class MediaType {
    MOVIE, TV_SERIES, ANIME, MANGA, BOOK, GAME
}

enum class MediaSourceType {
    TMDB, IGDB, MY_ANIME_LIST
}

data class WatchTimestamps(
    val startTimestamp: Instant?,
    val completedTimestamp: Instant?,
)

@Converter(autoApply = true)
class WatchTimestampsConverter : AttributeConverter<WatchTimestamps?, String?> {
    override fun convertToDatabaseColumn(watchTimestamps: WatchTimestamps?): String? {
        if (watchTimestamps == null) return null
        if (watchTimestamps.startTimestamp == null && watchTimestamps.completedTimestamp == null) return null
        return "${watchTimestamps.startTimestamp?.toString() ?: "null"}||${watchTimestamps.completedTimestamp?.toString() ?: "null"}"
    }

    override fun convertToEntityAttribute(s: String?): WatchTimestamps? {
        if (s.isNullOrBlank()) return null
        val timestampsInString = s.split("||")
        if (timestampsInString.size != 2) return null
        return WatchTimestamps(
            startTimestamp = if (timestampsInString[0] == "null") null else Instant.parse(timestampsInString[0]),
            completedTimestamp = if (timestampsInString[1] == "null") null else Instant.parse(timestampsInString[1])
        )
    }
}

@Entity
@Table(name = "medias")
class MediaEntity : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    lateinit var user: UserEntity

    @NotBlank
    lateinit var title: String

    @NotNull
    @Enumerated(EnumType.STRING)
    lateinit var type: MediaType

    var imageUrl: String? = null

    var sourceType: MediaSourceType? = null

    var sourceId: String? = null

    @NotNull
    @ElementCollection
    @Convert(converter = WatchTimestampsConverter::class)
    lateinit var watchTimes: MutableSet<WatchTimestamps>

    @ManyToMany
    @JoinTable(
        name = "media_collections",
        joinColumns = [JoinColumn(name = "media_id")],
        inverseJoinColumns = [JoinColumn(name = "collection_id")]
    )
    lateinit var collections: MutableSet<CollectionEntity>

    constructor(
        user: UserEntity,
        title: String,
        type: MediaType,
        imageUrl: String? = null,
        sourceType: MediaSourceType? = null,
        sourceId: String? = null,
        watchTimes: MutableSet<WatchTimestamps> = mutableSetOf(),
        collections: MutableSet<CollectionEntity> = mutableSetOf(),
    ) {
        this.user = user
        this.title = title
        this.type = type
        this.imageUrl = imageUrl
        this.sourceType = sourceType
        this.sourceId = sourceId
        this.watchTimes = watchTimes
        this.collections = collections
    }
}