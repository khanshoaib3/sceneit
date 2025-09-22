package com.github.khanshoaib3.sceneit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
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
