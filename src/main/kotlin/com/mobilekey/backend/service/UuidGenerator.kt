package com.mobilekey.backend.service

import com.fasterxml.uuid.Generators
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UuidGenerator {
    private val generator = Generators.timeBasedEpochGenerator()

    fun generate(): UUID = generator.generate()
}
