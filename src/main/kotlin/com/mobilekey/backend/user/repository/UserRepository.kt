package com.mobilekey.backend.user.repository

import com.mobilekey.backend.common.config.JooqExecutor
import com.mobilekey.backend.generated.tables.records.UserProfileRecord
import com.mobilekey.backend.generated.tables.references.USER_PROFILE
import com.mobilekey.backend.user.entity.User
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepository(private val jooq: JooqExecutor) {

    suspend fun save(user: User): User = jooq.execute {
        insertInto(USER_PROFILE)
            .set(USER_PROFILE.ID, user.id)
            .set(USER_PROFILE.LOGIN, user.login)
            .set(USER_PROFILE.EMAIL, user.email)
            .set(USER_PROFILE.PASSWORD, user.password)
            .execute()
        user
    }

    suspend fun findById(id: UUID): User? = jooq.execute {
        selectFrom(USER_PROFILE)
            .where(USER_PROFILE.ID.eq(id))
            .fetchOne { it.toUser() }
    }

    suspend fun findByLogin(login: String): User? = jooq.execute {
        selectFrom(USER_PROFILE)
            .where(USER_PROFILE.LOGIN.eq(login))
            .fetchOne { it.toUser() }
    }

    suspend fun findByEmail(email: String): User? = jooq.execute {
        selectFrom(USER_PROFILE)
            .where(USER_PROFILE.EMAIL.eq(email))
            .fetchOne { it.toUser() }
    }

    suspend fun existsByLogin(login: String): Boolean = jooq.execute {
        fetchExists(selectOne().from(USER_PROFILE).where(USER_PROFILE.LOGIN.eq(login)))
    }

    suspend fun existsByEmail(email: String): Boolean = jooq.execute {
        fetchExists(selectOne().from(USER_PROFILE).where(USER_PROFILE.EMAIL.eq(email)))
    }

    suspend fun update(user: User): User = jooq.execute {
        updateWithContext(ctx = this, user = user)
    }

    fun updateWithContext(ctx: DSLContext, user: User): User {
        ctx.update(USER_PROFILE)
            .set(USER_PROFILE.LOGIN, user.login)
            .set(USER_PROFILE.PASSWORD, user.password)
            .set(USER_PROFILE.AVATAR_ID, user.avatarId)
            .where(USER_PROFILE.ID.eq(user.id))
            .execute()
        return user
    }

    private fun UserProfileRecord.toUser(): User =
        User(id = id, login = login, email = email, password = password, avatarId = avatarId)
}
