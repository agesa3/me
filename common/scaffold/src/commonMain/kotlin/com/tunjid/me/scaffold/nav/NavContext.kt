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

import com.tunjid.treenav.MultiStackNav
import com.tunjid.treenav.strings.RouteParser

interface NavContext {
    val mainNav: MultiStackNav
    val String.toRoute: AppRoute
}

class ImmutableNavContext(
    private val state: MultiStackNav,
    private val routeParser: RouteParser<AppRoute>
) : NavContext {
    override val mainNav: MultiStackNav get() = state

    override val String.toRoute: AppRoute
        get() = routeParser.parse(this) ?: Route404
}
