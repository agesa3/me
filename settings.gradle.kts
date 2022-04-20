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

//dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
//    repositories {
//        google()
//        mavenCentral()
//    }
//}
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        mavenCentral()
        google()
    }

    plugins {
        val kotlinVersion = "1.6.10"
        val agpVersion = "7.0.4"
        val composeVersion = "1.0.1"
        val sqlDelightVersion = "1.5.3"

        kotlin("jvm").version(kotlinVersion)
        kotlin("multiplatform").version(kotlinVersion)
        kotlin("android").version(kotlinVersion)
        kotlin("plugin.serialization").version(kotlinVersion)
        kotlin("plugin.parcelize").version(kotlinVersion)

        id("com.android.application").version(agpVersion)
        id("com.android.library").version(agpVersion)
        id("org.jetbrains.compose").version(composeVersion)

        id("com.squareup.sqldelight").version(sqlDelightVersion)
    }
}

rootProject.name = "Me"
include(
    ":common:core",
    ":common:core-ui",
    ":common:data",
    ":common:scaffold",
    ":common:app",
    ":common:feature-template",
    ":common:feature-archive-list",
    ":common:feature-archive-detail",
    ":common:feature-archive-edit",
    ":common:feature-profile",
    ":common:feature-settings",
    ":common:feature-sign-in",
    ":android",
    ":desktop",
    ":serverEvents"
)
