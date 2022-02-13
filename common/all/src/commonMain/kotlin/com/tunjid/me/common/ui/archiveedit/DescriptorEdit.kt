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

package com.tunjid.me.common.ui.archiveedit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tunjid.me.core.model.ArchiveUpsert
import com.tunjid.me.core.model.Descriptor
import com.tunjid.me.common.ui.common.ChipAction
import com.tunjid.me.common.ui.common.ChipEditInfo
import com.tunjid.me.common.ui.common.Chips

@Composable
fun EditChips(
    modifier: Modifier,
    upsert: com.tunjid.me.core.model.ArchiveUpsert,
    state: ChipsState,
    onChanged: (Action) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Chips(
            modifier = Modifier.fillMaxWidth(),
            name = "Categories:",
            chips = upsert.categories.map(com.tunjid.me.core.model.Descriptor.Category::value),
            color = MaterialTheme.colors.primaryVariant,
            editInfo = ChipEditInfo(
                currentText = state.categoryText.value,
                onChipChanged = onChipFilterChanged(
                    state = state,
                    reader = ChipsState::categoryText,
                    writer = com.tunjid.me.core.model.Descriptor::Category,
                    onChanged = onChanged
                )
            )
        )
        Chips(
            modifier = Modifier.fillMaxWidth(),
            name = "Tags:",
            chips = upsert.tags.map(com.tunjid.me.core.model.Descriptor.Tag::value),
            color = MaterialTheme.colors.secondary,
            editInfo = ChipEditInfo(
                currentText = state.tagText.value,
                onChipChanged = onChipFilterChanged(
                    state = state,
                    reader = ChipsState::tagText,
                    writer = com.tunjid.me.core.model.Descriptor::Tag,
                    onChanged = onChanged
                )
            )
        )
    }
}

private fun onChipFilterChanged(
    state: ChipsState,
    reader: (ChipsState) -> com.tunjid.me.core.model.Descriptor,
    writer: (String) -> com.tunjid.me.core.model.Descriptor,
    onChanged: (Action) -> Unit
): (ChipAction) -> Unit = {
    when (it) {
        ChipAction.Added -> onChanged(
            Action.ChipEdit(chipAction = it, descriptor = reader(state))
        )
        is ChipAction.Changed -> onChanged(
                Action.ChipEdit(chipAction = it, descriptor = writer(it.text))
            )
        is ChipAction.Removed -> onChanged(
                Action.ChipEdit(chipAction = it, descriptor = writer(it.text))
            )
    }
}