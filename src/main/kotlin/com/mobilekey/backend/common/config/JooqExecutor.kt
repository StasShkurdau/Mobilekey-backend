package com.mobilekey.backend.common.config

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class JooqExecutor(val dsl: DSLContext) {

    private val dispatcher = Dispatchers.IO.limitedParallelism(128)

    suspend fun <T> execute(block: DSLContext.() -> T): T {
        return withContext(dispatcher) {
            dsl.block()
        }
    }

    suspend fun <T> transaction(block: (DSLContext) -> T): T {
        return withContext(dispatcher) {
            dsl.transactionResult { config ->
                block(config.dsl())
            }
        }
    }
}
