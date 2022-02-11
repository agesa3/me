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

package com.tunjid.me.common.ui.archivedetail


import com.tunjid.me.common.app.AppMutator
import com.tunjid.me.common.app.monitorWhenActive
import com.tunjid.me.common.data.model.ArchiveKind
import com.tunjid.me.common.data.repository.ArchiveRepository
import com.tunjid.me.common.data.repository.AuthRepository
import com.tunjid.me.common.globalui.navBarSize
import com.tunjid.mutator.Mutation
import com.tunjid.mutator.Mutator
import com.tunjid.mutator.coroutines.stateFlowMutator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

typealias ArchiveDetailMutator = Mutator<Unit, StateFlow<State>>

fun archiveDetailMutator(
    scope: CoroutineScope,
    route: ArchiveDetailRoute,
    initialState: State? = null,
    archiveRepository: ArchiveRepository,
    authRepository: AuthRepository,
    appMutator: AppMutator,
): ArchiveDetailMutator = stateFlowMutator(
    scope = scope,
    initialState = initialState ?: State(
        kind = route.kind,
        navBarSize = appMutator.globalUiMutator.state.value.navBarSize,
    ),
    started = SharingStarted.WhileSubscribed(2000),
    actionTransform = {
        merge(
            appMutator.navbarSizeMutations(),
            authRepository.signedInUserStream.map { Mutation { copy(signedInUserId = it?.id) } },
            archiveRepository.archiveLoadMutations(
                kind = route.kind,
                id = route.archiveId
            )
        ).monitorWhenActive(appMutator)
    }
)

private fun AppMutator.navbarSizeMutations(): Flow<Mutation<State>> =
    globalUiMutator.state
        .map { it.navBarSize }
        .map {
            Mutation { copy(navBarSize = it) }
        }

private fun ArchiveRepository.archiveLoadMutations(
    id: String,
    kind: ArchiveKind
): Flow<Mutation<State>> = monitorArchive(
    kind = kind,
    id = id
)
    .map { fetchedArchive ->
        Mutation { copy(archive = fetchedArchive) }
    }