package com.github.khanshoaib3.sceneit.controller

import com.github.khanshoaib3.sceneit.exception.BusinessExceptions
import com.github.khanshoaib3.sceneit.model.MediaEntity
import com.github.khanshoaib3.sceneit.model.MediaSourceType
import com.github.khanshoaib3.sceneit.model.MediaType
import com.github.khanshoaib3.sceneit.payload.response.MessageResponse
import com.github.khanshoaib3.sceneit.repository.MediaRepository
import com.github.khanshoaib3.sceneit.repository.UserRepository
import com.github.khanshoaib3.sceneit.security.services.UserDetailsImpl
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.rmi.UnexpectedException
import java.time.Instant

data class MediaAddRequest(
    @field:Size(min = 5)
    val title: String,
    @Enumerated(EnumType.STRING)
    val type: MediaType,
    val completionTimestamp: String,
    val imageUrl: String? = null,
    @Enumerated(EnumType.STRING)
    val sourceType: MediaSourceType? = null,
    val sourceId: String? = null,
)

data class MediaUpdateRequest(
    val id: Long,
    @field:Size(min = 5)
    val title: String,
    @Enumerated(EnumType.STRING)
    val type: MediaType,
    val completionTimestamps: List<String>,
    val imageUrl: String? = null,
    @Enumerated(EnumType.STRING)
    val sourceType: MediaSourceType? = null,
    val sourceId: String? = null,
)

data class MediaAddRewatchRequest(
    val id: Long,
    val completionTimestamp: String,
)

data class MediaDeleteRequest(
    val id: Long
)

data class MediaResponse(
    val id: Long,
    val title: String,
    @Enumerated(EnumType.STRING)
    val type: MediaType,
    val completionTimestamps: List<String>,
    val imageUrl: String?,
    @Enumerated(EnumType.STRING)
    val sourceType: MediaSourceType?,
    val sourceId: String?,
)

fun MediaEntity.toMediaResponse() =
    MediaResponse(
        id = this.id
            ?: throw UnexpectedException("Id is null for media entity titled: ${this.title}, by ${this.user.username}"),
        title = this.title,
        type = this.type,
        completionTimestamps = this.completionTimestamps.map { it.toString() },
        imageUrl = this.imageUrl,
        sourceType = this.sourceType,
        sourceId = this.sourceId
    )

data class MediaAllResponse(
    val medias: List<MediaResponse>
)

@RestController
@RequestMapping("/media")
class MediaController(
    private val mediaRepository: MediaRepository,
    private val userRepository: UserRepository,
) {
    @GetMapping("/")
    fun all(): MediaAllResponse {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as UserDetailsImpl
        val userEntity =
            userRepository.findByUsername(userDetails.username) ?: throw BusinessExceptions.UsernameNotFoundException()
        return MediaAllResponse(mediaRepository.findByUser(userEntity)?.map { it.toMediaResponse() } ?: listOf())
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<MediaResponse> {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as UserDetailsImpl
        val userEntity =
            userRepository.findByUsername(userDetails.username) ?: throw BusinessExceptions.UsernameNotFoundException()
        return ResponseEntity.ok().body(
            mediaRepository.findByUserAndId(user = userEntity, id = id)?.toMediaResponse()
                ?: throw BusinessExceptions.InvalidMediaId()
        )
    }

    @PostMapping("/add")
    fun add(@Valid @RequestBody request: MediaAddRequest): ResponseEntity<MessageResponse> {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as UserDetailsImpl
        val userEntity =
            userRepository.findByUsername(userDetails.username) ?: throw BusinessExceptions.UsernameNotFoundException()
        val timestamp: Instant =
            runCatching { Instant.parse(request.completionTimestamp) }.getOrElse { throw BusinessExceptions.InvalidTimestamp() }
        val media = MediaEntity(
            user = userEntity,
            title = request.title,
            type = request.type,
            completionTimestamps = mutableSetOf(timestamp),
            imageUrl = request.imageUrl,
            sourceType = request.sourceType,
            sourceId = request.sourceId,
        )
        mediaRepository.save(media)

        return ResponseEntity.ok().body(MessageResponse("Media titled, `${request.title}`, created successfully."))
    }

    @PutMapping("/add-rewatch")
    @Transactional
    fun addRewatch(@Valid @RequestBody request: MediaAddRewatchRequest): ResponseEntity<MessageResponse> {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as UserDetailsImpl
        val userEntity =
            userRepository.findByUsername(userDetails.username) ?: throw BusinessExceptions.UsernameNotFoundException()
        val timestamp: Instant =
            runCatching { Instant.parse(request.completionTimestamp) }.getOrElse { throw BusinessExceptions.InvalidTimestamp() }
        mediaRepository.findByUserAndId(user = userEntity, id = request.id)?.let {
            it.completionTimestamps.add(timestamp)
            mediaRepository.save(it)
        } ?: throw BusinessExceptions.InvalidMediaId()

        return ResponseEntity.ok().body(MessageResponse("Completion timestamp added!"))
    }

    @PutMapping("/update")
    @Transactional
    fun update(@Valid @RequestBody request: MediaUpdateRequest): ResponseEntity<MessageResponse> {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as UserDetailsImpl
        val userEntity =
            userRepository.findByUsername(userDetails.username) ?: throw BusinessExceptions.UsernameNotFoundException()
        val timestamps =
            runCatching { request.completionTimestamps.map { Instant.parse(it) } }.getOrElse { throw BusinessExceptions.InvalidTimestamp() }

        mediaRepository.findByUserAndId(user = userEntity, id = request.id)?.let {
            it.title = request.title
            it.type = request.type
            it.completionTimestamps = timestamps.toMutableSet()
            it.imageUrl = request.imageUrl
            it.sourceType = request.sourceType
            it.sourceId = request.sourceId

            mediaRepository.save(it)
        } ?: throw BusinessExceptions.InvalidMediaId()

        return ResponseEntity.ok().body(MessageResponse("Media updated successfully!"))
    }

    @DeleteMapping("/delete")
    @Transactional
    fun delete(@Valid @RequestBody request: MediaDeleteRequest): ResponseEntity<MessageResponse> {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as UserDetailsImpl
        val userEntity =
            userRepository.findByUsername(userDetails.username) ?: throw BusinessExceptions.UsernameNotFoundException()
        mediaRepository.findByUserAndId(user = userEntity, id = request.id)?.let {
            mediaRepository.delete(it)
        } ?: throw BusinessExceptions.InvalidMediaId()

        return ResponseEntity.ok().body(MessageResponse("Media deleted successfully!"))
    }
}