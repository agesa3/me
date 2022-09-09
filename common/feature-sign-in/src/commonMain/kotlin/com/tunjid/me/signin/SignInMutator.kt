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

package com.tunjid.me.signin


import com.tunjid.me.core.ui.update
import com.tunjid.me.data.repository.AuthRepository
import com.tunjid.me.feature.FeatureWhileSubscribed
import com.tunjid.me.scaffold.lifecycle.Lifecycle
import com.tunjid.me.scaffold.lifecycle.monitorWhenActive
import com.tunjid.mutator.ActionStateProducer
import com.tunjid.mutator.Mutation
import com.tunjid.mutator.coroutines.actionStateFlowProducer
import com.tunjid.mutator.coroutines.toMutationStream
import com.tunjid.mutator.mutation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

typealias SignInMutator = ActionStateProducer<Action, StateFlow<State>>

fun signInMutator(
    scope: CoroutineScope,
    route: SignInRoute,
    initialState: State? = null,
    authRepository: AuthRepository,
    lifecycleStateFlow: StateFlow<Lifecycle>,
): SignInMutator = scope.actionStateFlowProducer(
    initialState = initialState ?: State(),
    started = SharingStarted.WhileSubscribed(FeatureWhileSubscribed),
    actionTransform = { actions ->
        merge(
            authRepository.isSignedIn.map { mutation { copy(isSignedIn = it) } },
            actions.toMutationStream {
                when (val action = type()) {
                    is Action.FieldChanged -> action.flow.formEditMutations()
                    is Action.Submit -> action.flow.submissionMutations(authRepository)
                }
            }
        ).monitorWhenActive(lifecycleStateFlow)
    }
)

private fun Flow<Action.FieldChanged>.formEditMutations(): Flow<Mutation<State>> =
    map { (updatedField) ->
        mutation {
            copy(fields = fields.update(updatedField))
        }
    }

private fun Flow<Action.Submit>.submissionMutations(
    authRepository: AuthRepository
): Flow<Mutation<State>> =
    debounce(200)
        .flatMapLatest { (request) ->
            flow {
                emit(mutation { copy(isSubmitting = true) })
                // TODO: Show snack bar if error
                authRepository.createSession(request = request)
                emit(mutation { copy(isSubmitting = false) })
            }
        }