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


import com.tunjid.me.core.model.Result
import com.tunjid.me.core.model.minus
import com.tunjid.me.core.model.plus
import com.tunjid.me.core.ui.update
import com.tunjid.me.data.repository.AuthRepository
import com.tunjid.me.feature.FeatureWhileSubscribed
import com.tunjid.me.scaffold.lifecycle.Lifecycle
import com.tunjid.me.scaffold.lifecycle.monitorWhenActive
import com.tunjid.me.scaffold.nav.NavContext
import com.tunjid.me.scaffold.nav.NavMutation
import com.tunjid.me.scaffold.nav.canGoUp
import com.tunjid.mutator.ActionStateProducer
import com.tunjid.mutator.Mutation
import com.tunjid.mutator.coroutines.actionStateFlowProducer
import com.tunjid.mutator.coroutines.toMutationStream
import com.tunjid.mutator.mutation
import com.tunjid.treenav.MultiStackNav
import com.tunjid.treenav.pop
import com.tunjid.treenav.switch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

typealias SignInMutator = ActionStateProducer<Action, StateFlow<State>>

fun signInMutator(
    scope: CoroutineScope,
    @Suppress("UNUSED_PARAMETER")
    route: SignInRoute,
    initialState: State? = null,
    authRepository: AuthRepository,
    lifecycleStateFlow: StateFlow<Lifecycle>,
    navActions: (NavMutation) -> Unit,
): SignInMutator = scope.actionStateFlowProducer(
    initialState = initialState ?: State(),
    started = SharingStarted.WhileSubscribed(FeatureWhileSubscribed),
    mutationFlows = listOf<Flow<Mutation<State>>>(
        authRepository.isSignedIn.map { mutation { copy(isSignedIn = it) } },
    ).monitorWhenActive(lifecycleStateFlow),
    actionTransform = { actions ->
        actions.toMutationStream {
            when (val action = type()) {
                is Action.FieldChanged -> action.flow.formEditMutations()
                is Action.MessageConsumed -> action.flow.messageConsumptionMutations()
                is Action.Submit -> action.flow.submissionMutations(
                    authRepository = authRepository,
                    navActions = navActions
                )
            }
        }.monitorWhenActive(lifecycleStateFlow)
    }
)

private fun Flow<Action.FieldChanged>.formEditMutations(): Flow<Mutation<State>> =
    map { (updatedField) ->
        mutation {
            copy(fields = fields.update(updatedField))
        }
    }

/**
 * Mutations from consuming messages from the message queue
 */
private fun Flow<Action.MessageConsumed>.messageConsumptionMutations(): Flow<Mutation<State>> =
    map { (message) ->
        mutation { copy(messages = messages - message) }
    }

private fun Flow<Action.Submit>.submissionMutations(
    authRepository: AuthRepository,
    navActions: (NavMutation) -> Unit
): Flow<Mutation<State>> =
    debounce(200)
        .flatMapLatest { (request) ->
            flow {
                emit { copy(isSubmitting = true) }
                when (val result = authRepository.createSession(request = request)) {
                    is Result.Error -> emit {
                        copy(messages = messages + "Error signing in: ${result.message}")
                    }

                    else -> navActions(NavContext::resetNav)
                }
                emit { copy(isSubmitting = false) }
            }
        }

private fun NavContext.resetNav(): MultiStackNav {
    var newNav = mainNav
    for (i in 0.until(mainNav.stacks.size)) {
        newNav = newNav.switch(i)
        while (newNav.canGoUp) newNav = newNav.pop()
    }
    return newNav.switch(0)
}