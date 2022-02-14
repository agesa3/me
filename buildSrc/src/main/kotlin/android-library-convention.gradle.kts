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

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.konan.properties.hasProperty

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

plugins {
    id("com.android.library")
}

android {
    compileSdk = 31

    defaultConfig {
        // Could have been 21, but I need sqlite 3.24.0 for upserts
        minSdk = 30
        targetSdk = 31
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.2.0-alpha02"
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
//    kotlinOptions {
//        jvmTarget = "11"
//        freeCompilerArgs = freeCompilerArgs + listOf(
//            "-Xuse-experimental=androidx.compose.animation.ExperimentalAnimationApi",
//            "-Xuse-experimental=androidx.compose.material.ExperimentalMaterialApi",
//            "-Xuse-experimental=kotlinx.serialization.ExperimentalSerializationApi",
//            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
//            "-Xuse-experimental=kotlinx.coroutines.FlowPreview"
//        )
//    }
    sourceSets {
        named("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res")
        }
    }
}