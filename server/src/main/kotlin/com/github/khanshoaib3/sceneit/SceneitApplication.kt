package com.github.khanshoaib3.sceneit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@EnableJpaAuditing
@RestController
class SceneitApplication {
    @GetMapping("/")
    fun root(): String {
        return String.format("Server for scene-it.")
    }
}

fun main(args: Array<String>) {
    runApplication<SceneitApplication>(*args)
}
