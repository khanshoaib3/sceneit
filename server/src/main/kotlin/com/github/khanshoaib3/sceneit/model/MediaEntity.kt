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

    @Enumerated(EnumType.STRING)
    var sourceType: MediaSourceType? = null

    var sourceId: String? = null

    @NotNull
    @ElementCollection
    lateinit var completionTimestamps: MutableSet<Instant>

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
        completionTimestamps: MutableSet<Instant> = mutableSetOf(),
        imageUrl: String? = null,
        collections: MutableSet<CollectionEntity> = mutableSetOf(),
        sourceType: MediaSourceType? = null,
        sourceId: String? = null,
    ) {
        this.user = user
        this.title = title
        this.type = type
        this.completionTimestamps = completionTimestamps
        this.imageUrl = imageUrl
        this.collections = collections
        this.sourceType = sourceType
        this.sourceId = sourceId
    }
}