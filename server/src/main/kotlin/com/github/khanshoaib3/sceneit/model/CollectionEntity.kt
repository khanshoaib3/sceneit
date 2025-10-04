package com.github.khanshoaib3.sceneit.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.NotBlank
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.io.Serializable

@Entity
@Table(
    name = "collections",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["slug"]),
    ]
)
class CollectionEntity : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    lateinit var user: UserEntity

    @NotBlank
    lateinit var name: String

    @NotBlank
    lateinit var slug: String

    @ManyToMany(mappedBy = "collections", cascade = [CascadeType.ALL])
    var entries: MutableSet<MediaEntity> = mutableSetOf()

    constructor(
        user: UserEntity,
        name: String,
        slug: String,
        entries: MutableSet<MediaEntity> = mutableSetOf()
    ) {
        this.user = user
        this.name = name
        this.slug = slug
        this.entries = entries
    }
}