package com.mobilekey.backend

import org.jooq.DSLContext
import org.jooq.impl.DSL.table
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegrationTestBase {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var dsl: DSLContext

    @Autowired
    lateinit var redisTemplate: StringRedisTemplate

    @MockBean
    lateinit var mailSender: JavaMailSender

    val authClient: AuthTestClient by lazy { AuthTestClient(restTemplate) }

    @BeforeEach
    fun cleanUp() {
        dsl.deleteFrom(table("users")).execute()
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()
    }
}
