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

package com.tunjid.me.scaffold.globalui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tunjid.me.scaffold.globalui.slices.ToolbarItem

sealed class NavMode {
    object BottomNav : NavMode()
    object NavRail : NavMode()
}

sealed class NavVisibility {
    object Visible : NavVisibility()
    object Gone : NavVisibility()
    object GoneIfBottomNav : NavVisibility()
}

data class UiState(
    val toolbarItems: List<ToolbarItem> = listOf(),
    val toolbarShows: Boolean = false,
    val toolbarOverlaps: Boolean = false,
    val toolbarTitle: CharSequence = "",
    val fabIcon: ImageVector = Icons.Default.Done,
    val fabShows: Boolean = false,
    val fabExtended: Boolean = true,
    val fabEnabled: Boolean = true,
    val fabText: String = "",
    val backgroundColor: Int = Color.Transparent.toArgb(),
    val snackbarOffset: Dp = 0.dp,
    val snackbarMessages: com.tunjid.me.core.model.MessageQueue = com.tunjid.me.core.model.MessageQueue(),
    val navBarColor: Int = Color.Transparent.toArgb(),
    val lightStatusBar: Boolean = false,
    val navMode: NavMode = NavMode.BottomNav,
    val navVisibility: NavVisibility = NavVisibility.Visible,
    val statusBarColor: Int = Color.Transparent.toArgb(),
    val insetFlags: InsetDescriptor = InsetFlags.ALL,
    val isImmersive: Boolean = false,
    val systemUI: SystemUI = NoOpSystemUI,
    val fabClickListener: (Unit) -> Unit = emptyCallback(),
    val toolbarMenuClickListener: (ToolbarItem) -> Unit = emptyCallback(),
    val altToolbarMenuClickListener: (ToolbarItem) -> Unit = emptyCallback(),
    val snackbarMessageConsumer: (com.tunjid.me.core.model.Message) -> Unit = emptyCallback(),
)

private fun <T> emptyCallback(): (T) -> Unit = {}

val UiState.navBarSize get() = systemUI.static.navBarSize

val UiState.statusBarSize get() = systemUI.static.statusBarSize

val UiState.bottomNavVisible
    get() = navMode is NavMode.BottomNav && when (navVisibility) {
        NavVisibility.Visible -> true
        NavVisibility.Gone,
        NavVisibility.GoneIfBottomNav -> false
    }

val UiState.navRailVisible
    get() = navMode is NavMode.NavRail && when (navVisibility) {
        NavVisibility.Visible,
        NavVisibility.GoneIfBottomNav -> true

        NavVisibility.Gone -> false
    }

/**
 * Interface for [UiState] state slices that are aware of the keyboard. Useful for
 * keyboard visibility changes for bottom aligned views like Floating Action Buttons and Snack Bars
 */
interface KeyboardAware {
    val ime: Ingress
    val navBarSize: Int
    val insetDescriptor: InsetDescriptor
}

internal val KeyboardAware.keyboardSize get() = ime.bottom - navBarSize