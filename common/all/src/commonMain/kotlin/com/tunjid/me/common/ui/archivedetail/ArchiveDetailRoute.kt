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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material.MaterialRichText
import com.tunjid.me.common.di.LocalAppDependencies
import com.tunjid.me.core.model.ArchiveId
import com.tunjid.me.core.model.ArchiveKind
import com.tunjid.me.common.globalui.InsetFlags
import com.tunjid.me.common.globalui.NavVisibility
import com.tunjid.me.common.globalui.ScreenUiState
import com.tunjid.me.common.globalui.UiState
import com.tunjid.me.common.globalui.currentUiState
import com.tunjid.me.common.nav.AppRoute
import com.tunjid.me.common.ui.archiveedit.ArchiveEditRoute
import com.tunjid.me.common.ui.archivelist.ArchiveListRoute
import com.tunjid.mutator.accept
import com.tunjid.treenav.MultiStackNav
import com.tunjid.treenav.push
import kotlinx.serialization.Serializable

@Serializable
data class ArchiveDetailRoute(
    val kind: com.tunjid.me.core.model.ArchiveKind,
    val archiveId: com.tunjid.me.core.model.ArchiveId
) : AppRoute<ArchiveDetailMutator> {
    override val id: String
        get() = "archive-detail-$kind-$archiveId"

    @Composable
    override fun Render() {
        ArchiveDetailScreen(
            mutator = LocalAppDependencies.current.routeDependencies(this)
        )
    }

    override fun navRailRoute(nav: MultiStackNav): AppRoute<*>? {
        val activeStack = nav.stacks.getOrNull(nav.currentIndex) ?: return null
        val previous = activeStack.routes.getOrNull(activeStack.routes.lastIndex - 1)
        return if (previous is ArchiveListRoute) previous else null
    }
}

@Composable
private fun ArchiveDetailScreen(mutator: ArchiveDetailMutator) {
    val state by mutator.state.collectAsState()
    val scrollState = rememberScrollState()
    val navBarSizeDp = with(LocalDensity.current) { state.navBarSize.toDp() }
    val canEdit = state.canEdit
    val navMutator = LocalAppDependencies.current.appMutator.navMutator
    ScreenUiState(
        UiState(
            toolbarShows = true,
            toolbarTitle = state.archive?.title ?: "Detail",
            navVisibility = NavVisibility.GoneIfBottomNav,
            // Prevents UI from jittering as load starts
            fabShows = if (state.hasFetchedAuthStatus) canEdit else currentUiState.fabShows,
            fabExtended = true,
            fabText = "Edit",
            fabIcon = Icons.Default.Edit,
            fabClickListener = {
                navMutator.accept {
                    push(
                        ArchiveEditRoute(
                            kind = state.kind,
                            archiveId = state.archive?.id
                        )
                    )
                }
            },
            insetFlags = InsetFlags.NO_BOTTOM,
            statusBarColor = MaterialTheme.colors.primary.toArgb(),
        )
    )

    val archive = state.archive

    Column(
        modifier = Modifier
            .verticalScroll(state = scrollState),
    ) {
        Spacer(modifier = Modifier.padding(8.dp))
        MaterialRichText(
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            if (archive != null) Markdown(
                content = archive.body
            )
        }
        Spacer(modifier = Modifier.padding(8.dp + navBarSizeDp))
    }
}