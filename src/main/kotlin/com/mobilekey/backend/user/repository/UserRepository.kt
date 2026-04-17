package com.mobilekey.backend.user.repository

import com.mobilekey.backend.common.config.JooqExecutor
import com.mobilekey.backend.generated.tables.records.UserRecord
import com.mobilekey.backend.generated.tables.references.USER
import com.mobilekey.backend.user.entity.User
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepository(private val jooq: JooqExecutor) {

    suspend fun save(user: User): User = jooq.execute {
        insertInto(USER)
            .set(USER.ID, user.id)
            .set(USER.LOGIN, user.login)
            .set(USER.EMAIL, user.email)
            .set(USER.PASSWORD, user.password)
            .execute()
        user
    }

    suspend fun findById(id: UUID): User? = jooq.execute {
        selectFrom(USER)
            .where(USER.ID.eq(id))
            .fetchOne { it.toUser() }
    }

    suspend fun findByLogin(login: String): User? = jooq.execute {
        selectFrom(USER)
            .where(USER.LOGIN.eq(login))
            .fetchOne { it.toUser() }
    }

    suspend fun findByEmail(email: String): User? = jooq.execute {
        selectFrom(USER)
            .where(USER.EMAIL.eq(email))
            .fetchOne { it.toUser() }
    }

    suspend fun existsByLogin(login: String): Boolean = jooq.execute {
        fetchExists(selectOne().from(USER).where(USER.LOGIN.eq(login)))
    }

    suspend fun existsByEmail(email: String): Boolean = jooq.execute {
        fetchExists(selectOne().from(USER).where(USER.EMAIL.eq(email)))
    }

    suspend fun update(user: User): User = jooq.execute {
        update(USER)
            .set(USER.LOGIN, user.login)
            .set(USER.PASSWORD, user.password)
            .where(USER.ID.eq(user.id))
            .execute()
        user
    }

    private fun UserRecord.toUser(): User =
        User(id = id, login = login, email = email, password = password)
}
