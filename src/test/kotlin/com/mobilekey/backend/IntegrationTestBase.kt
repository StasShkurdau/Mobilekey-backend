package com.mobilekey.backend

import org.jooq.DSLContext
import org.jooq.impl.DSL.table
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(IntegrationTestBase.TestConfig::class)
abstract class IntegrationTestBase {

    @TestConfiguration
    class TestConfig {
        @Bean
        fun stringRedisTemplate(connectionFactory: RedisConnectionFactory): StringRedisTemplate {
            return StringRedisTemplate(connectionFactory)
        }
    }

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var dsl: DSLContext

    @Autowired
    lateinit var redisTemplate: StringRedisTemplate

    @MockitoBean
    lateinit var mailSender: JavaMailSender

    val authClient: AuthTestClient by lazy { AuthTestClient(webTestClient) }

    @BeforeEach
    fun cleanUp() {
        dsl.deleteFrom(table("users")).execute()
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()
    }
}
