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

package com.tunjid.me.scaffold.nav

data class RouteParams(
    val path: String,
    val params: Map<String, String>
)

interface RouteParser<T : AppRoute> {
    val pattern: String
    fun route(matchResult: MatchResult): T
//    fun mutator(routeScope: CoroutineScope, route: R): T

    fun String.parseRoute(): RouteParams {
        val pathIndex = indexOf('?')
        return try {
            when {
                pathIndex < 0 || pathIndex == lastIndex -> RouteParams(
                    path = this,
                    params = mapOf()
                )
                else -> RouteParams(
                    path = substring(0, pathIndex),
                    params = substring(pathIndex + 1, lastIndex)
                        .split("&")
                        .associate {
                            val keyValue = it.split("=")
                            keyValue[0] to keyValue[1]
                        }
                )
            }
        } catch (e: Exception) {
            RouteParams(
                path = this,
                params = mapOf()
            )
        }
    }
}

fun <T : AppRoute> routeParser(
    pattern: String,
    routeMapper: (MatchResult) -> T
) = object : RouteParser<T> {
    override val pattern: String
        get() = pattern

    override fun route(matchResult: MatchResult): T = routeMapper(matchResult)
}