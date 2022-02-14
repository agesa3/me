/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tunjid.me.data

import com.tunjid.me.common.data.AppDatabase
import com.tunjid.me.data.local.SessionCookieDao
import com.tunjid.me.data.local.SqlArchiveDao
import com.tunjid.me.data.local.SqlSessionCookieDao
import com.tunjid.me.common.data.network.NetworkMonitor
import com.tunjid.me.common.data.network.exponentialBackoff
import com.tunjid.me.core.model.ArchiveId
import com.tunjid.me.core.model.ArchiveKind
import com.tunjid.me.data.local.databaseDispatcher
import com.tunjid.me.data.network.ApiUrl
import com.tunjid.me.data.network.KtorNetworkService
import com.tunjid.me.data.network.NetworkService
import com.tunjid.me.data.repository.ArchiveRepository
import com.tunjid.me.data.repository.AuthRepository
import com.tunjid.me.data.repository.ReactiveArchiveRepository
import com.tunjid.me.data.repository.SessionCookieAuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

class DataModule(
    appScope: CoroutineScope,
    database: AppDatabase,
    networkMonitor: NetworkMonitor
) {
    private val archiveDao = SqlArchiveDao(
        database = database,
        dispatcher = databaseDispatcher(),
    )

    private val sessionCookieDao: SessionCookieDao = SqlSessionCookieDao(
        database = database,
        dispatcher = databaseDispatcher(),
    )

    private val networkService: NetworkService = KtorNetworkService(
        sessionCookieDao = sessionCookieDao
    )

    val archiveRepository: ArchiveRepository = ReactiveArchiveRepository(
        networkService = networkService,
        appScope = appScope,
        networkMonitor = networkMonitor,
        dao = archiveDao
    )

    val authRepository: AuthRepository = SessionCookieAuthRepository(
        networkService = networkService,
        dao = sessionCookieDao
    )

    init {
        appScope.launch {
            com.tunjid.me.common.data.network.modelEvents(
                url = "$ApiUrl/",
                dispatcher = databaseDispatcher()
            )
                .mapNotNull { event ->
                    val kind = when (event.collection) {
                        ArchiveKind.Articles.type -> ArchiveKind.Articles
                        ArchiveKind.Talks.type -> ArchiveKind.Talks
                        ArchiveKind.Projects.type -> ArchiveKind.Projects
                        else -> null
                    } ?: return@mapNotNull null

                    exponentialBackoff(
                        initialDelay = 1_000,
                        maxDelay = 20_000,
                        times = 5,
                        default = null
                    ) {
                        networkService.fetchArchive(
                            kind = kind, id = ArchiveId(
                                event.id
                            )
                        )
                    }
                }
                .collect {
                    archiveDao.saveArchive(it)
                }
        }
    }
}