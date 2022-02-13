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

package com.tunjid.me.common.globalui.slices

import com.tunjid.me.common.globalui.Ingress
import com.tunjid.me.common.globalui.InsetDescriptor
import com.tunjid.me.common.globalui.KeyboardAware
import com.tunjid.me.common.globalui.UiState
import com.tunjid.me.common.globalui.bottomNavVisible

data class SnackbarPositionalState(
    val bottomNavVisible: Boolean,
    override val ime: Ingress,
    override val navBarSize: Int,
    override val insetDescriptor: InsetDescriptor
) : KeyboardAware


val UiState.snackbarPositionalState
    get() = SnackbarPositionalState(
        bottomNavVisible = bottomNavVisible,
        ime = systemUI.dynamic.ime,
        navBarSize = systemUI.static.navBarSize,
        insetDescriptor = insetFlags
    )