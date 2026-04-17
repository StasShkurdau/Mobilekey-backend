package com.mobilekey.backend.user.repository

import com.mobilekey.backend.common.config.JooqExecutor
import com.mobilekey.backend.user.entity.User
import org.jooq.Record
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.table
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepository(private val jooq: JooqExecutor) {

    companion object {
        private val USERS = table("users")
        private val ID = field("id", UUID::class.java)
        private val LOGIN = field("login", String::class.java)
        private val EMAIL = field("email", String::class.java)
        private val PASSWORD = field("password", String::class.java)
    }

    suspend fun save(user: User): User = jooq.execute {
        insertInto(USERS)
            .set(ID, user.id)
            .set(LOGIN, user.login)
            .set(EMAIL, user.email)
            .set(PASSWORD, user.password)
            .execute()
        user
    }

    suspend fun findById(id: UUID): User? = jooq.execute {
        select(ID, LOGIN, EMAIL, PASSWORD)
            .from(USERS)
            .where(ID.eq(id))
            .fetchOne { toUser(it) }
    }

    suspend fun findByLogin(login: String): User? = jooq.execute {
        select(ID, LOGIN, EMAIL, PASSWORD)
            .from(USERS)
            .where(LOGIN.eq(login))
            .fetchOne { toUser(it) }
    }

    suspend fun findByEmail(email: String): User? = jooq.execute {
        select(ID, LOGIN, EMAIL, PASSWORD)
            .from(USERS)
            .where(EMAIL.eq(email))
            .fetchOne { toUser(it) }
    }

    suspend fun existsByLogin(login: String): Boolean = jooq.execute {
        fetchExists(
            selectOne().from(USERS).where(LOGIN.eq(login))
        )
    }

    suspend fun existsByEmail(email: String): Boolean = jooq.execute {
        fetchExists(
            selectOne().from(USERS).where(EMAIL.eq(email))
        )
    }

    suspend fun update(user: User): User = jooq.execute {
        update(USERS)
            .set(LOGIN, user.login)
            .set(PASSWORD, user.password)
            .where(ID.eq(user.id))
            .execute()
        user
    }

    private fun toUser(record: Record): User {
        return User(
            id = record.get(ID)!!,
            login = record.get(LOGIN)!!,
            email = record.get(EMAIL)!!,
            password = record.get(PASSWORD)!!,
        )
    }
}
