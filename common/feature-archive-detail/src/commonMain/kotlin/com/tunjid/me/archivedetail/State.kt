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

package com.tunjid.me.archivedetail

import com.tunjid.me.core.model.Archive
import com.tunjid.me.core.model.ArchiveKind
import com.tunjid.me.core.utilities.ByteSerializable
import com.tunjid.me.scaffold.nav.NavMutation
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

sealed class Action {
    data class Navigate(val navMutation: NavMutation) : Action()
}

@Serializable
data class State(
    val hasFetchedAuthStatus: Boolean = false,
    val signedInUserId: com.tunjid.me.core.model.UserId? = null,
    val navBarSize: Int,
    val wasDeleted: Boolean = false,
    val kind: ArchiveKind,
    // Read this from the DB
    @Transient
    val archive: Archive? = null,
) : ByteSerializable

val State.canEdit: Boolean get() = signedInUserId != null && signedInUserId == archive?.author?.id