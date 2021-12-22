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

package com.tunjid.me.ui.archive

import com.tunjid.me.data.archive.Archive
import com.tunjid.me.data.archive.ArchiveContentFilter
import com.tunjid.me.data.archive.ArchiveQuery
import com.tunjid.me.data.archive.ArchiveRepository
import com.tunjid.mutator.Mutation
import com.tunjid.mutator.Mutator
import com.tunjid.mutator.coroutines.stateFlowMutator
import com.tunjid.mutator.coroutines.toMutationStream
import com.tunjid.tiler.Tile
import com.tunjid.tiler.Tile.Input
import com.tunjid.tiler.flattenWith
import com.tunjid.tiler.tiledList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

typealias ArchiveMutator = Mutator<Action, StateFlow<State>>

private val publishedDateFormatter = DateTimeFormatter
    .ofPattern("MMM dd yyyy")
    .withZone(ZoneId.systemDefault())

enum class FilterType {
    Tag, Category
}

data class State(
    val route: ArchiveRoute,
    val filterState: FilterState = FilterState(),
    val listStateSummary: ListState = ListState(),
    val items: List<ArchiveItem> = listOf(ArchiveItem.Loading)
)

data class FilterState(
    val expanded: Boolean = false,
    val filter: ArchiveContentFilter = ArchiveContentFilter(),
    val categoryText: String = "",
    val tagText: String = "",
)

sealed class ArchiveItem {
    data class Result(
        val archive: Archive,
        val query: ArchiveQuery,
    ) : ArchiveItem()

    object Loading : ArchiveItem()
}

val ArchiveItem.key: String
    get() = when (this) {
        ArchiveItem.Loading -> "Loading"
        is ArchiveItem.Result -> archive.key
    }

val ArchiveItem.Result.prettyDate: String get() = publishedDateFormatter.format(archive.created.toJavaInstant())

sealed class Action {
    data class Fetch(
        val query: ArchiveQuery,
        val reset: Boolean = false
    ) : Action()

    data class UpdateListState(val listState: ListState) : Action()
    data class FilterChanged(
        val type: FilterType,
        val text: String
    ) : Action()

    object ToggleFilter : Action()
}

data class ListState(
    val firstVisibleItemIndex: Int = 0,
    val firstVisibleItemScrollOffset: Int = 0
)

fun archiveMutator(
    scope: CoroutineScope,
    route: ArchiveRoute,
    repo: ArchiveRepository
): Mutator<Action, StateFlow<State>> = stateFlowMutator(
    scope = scope,
    initialState = State(
        route = route,
        filterState = FilterState(
            filter = ArchiveContentFilter(
                tags = route.query.contentFilter?.tags ?: listOf(),
                categories = route.query.contentFilter?.categories ?: listOf()
            ),
        )
    ),
    started = SharingStarted.WhileSubscribed(2000),
    transform = { actions ->
        actions.toMutationStream {
            when (val action = type()) {
                is Action.Fetch -> action.flow
                    .toArchiveItems(repo = repo)
                    // Debounce loads where archives are empty
                    .debounce { archives -> if (archives.isEmpty()) 5000 else 0 }
                    .map { archives ->
                        Mutation { copy(items = archives) }
                    }
                is Action.UpdateListState -> action.flow.map { (listState) ->
                    Mutation { copy(listStateSummary = listState) }
                }
                is Action.FilterChanged -> action.flow.map { (type, text) ->
                    Mutation {
                        copy(
                            filterState = filterState.copy(
                                categoryText = when {
                                    type === FilterType.Category -> text
                                    else -> filterState.categoryText
                                },
                                tagText = when {
                                    type === FilterType.Tag -> text
                                    else -> filterState.tagText
                                },
                            )
                        )
                    }
                }
                Action.ToggleFilter -> action.flow.map {
                    Mutation {
                        copy(filterState = filterState.copy(expanded = !filterState.expanded))
                    }
                }
            }
        }
    }
)

private fun ArchiveRepository.archiveTiler(): (Flow<Input<ArchiveQuery, List<ArchiveItem>>>) -> Flow<List<List<ArchiveItem>>> =
    tiledList(
        flattener = Tile.Flattener.PivotSorted(
            comparator = compareBy(ArchiveQuery::offset),
        ),
        fetcher = { query ->
            monitorArchives(query).map { archives ->
                archives.map { archive ->
                    ArchiveItem.Result(
                        archive = archive,
                        query = query
                    )
                }
            }
        }
    )

private fun Flow<Action.Fetch>.toArchiveItems(repo: ArchiveRepository): Flow<List<ArchiveItem>> =
    queryChanges()
        .flatMapLatest { (oldPages, newPages) ->
            oldPages
                .filterNot { newPages.contains(it) }
                .map { Tile.Request.Off<ArchiveQuery, List<ArchiveItem>>(it) }
                .plus(newPages.map { Tile.Request.On(it) })
                .asFlow()
        }
        .flattenWith(repo.archiveTiler())
        .map { it.flatten() }

private fun Flow<Action.Fetch>.queryChanges(): Flow<Pair<List<ArchiveQuery>, List<ArchiveQuery>>> =
    map { (query) ->
        listOf(
            query.copy(offset = query.offset - query.limit),
            query.copy(offset = query.offset + query.limit),
            query
        )
            .filter { it.offset >= 0 }
    }
        .scan(Pair(listOf(), listOf())) { pair, new ->
            pair.copy(
                first = pair.second,
                second = new
            )
        }
