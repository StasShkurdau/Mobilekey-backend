package com.mobilekey.backend.repository

import com.mobilekey.backend.entity.User
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.table
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepository(private val dsl: DSLContext) {

    companion object {
        private val USERS = table("users")
        private val ID = field("id", UUID::class.java)
        private val LOGIN = field("login", String::class.java)
        private val EMAIL = field("email", String::class.java)
        private val PASSWORD = field("password", String::class.java)
    }

    fun save(user: User): User {
        dsl.insertInto(USERS)
            .set(ID, user.id)
            .set(LOGIN, user.login)
            .set(EMAIL, user.email)
            .set(PASSWORD, user.password)
            .execute()
        return user
    }

    fun findById(id: UUID): User? {
        return dsl.select(ID, LOGIN, EMAIL, PASSWORD)
            .from(USERS)
            .where(ID.eq(id))
            .fetchOne { toUser(it) }
    }

    fun findByLogin(login: String): User? {
        return dsl.select(ID, LOGIN, EMAIL, PASSWORD)
            .from(USERS)
            .where(LOGIN.eq(login))
            .fetchOne { toUser(it) }
    }

    fun findByEmail(email: String): User? {
        return dsl.select(ID, LOGIN, EMAIL, PASSWORD)
            .from(USERS)
            .where(EMAIL.eq(email))
            .fetchOne { toUser(it) }
    }

    fun existsByLogin(login: String): Boolean {
        return dsl.fetchExists(
            dsl.selectOne().from(USERS).where(LOGIN.eq(login))
        )
    }

    fun existsByEmail(email: String): Boolean {
        return dsl.fetchExists(
            dsl.selectOne().from(USERS).where(EMAIL.eq(email))
        )
    }

    fun update(user: User): User {
        dsl.update(USERS)
            .set(LOGIN, user.login)
            .set(EMAIL, user.email)
            .set(PASSWORD, user.password)
            .where(ID.eq(user.id))
            .execute()
        return user
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
