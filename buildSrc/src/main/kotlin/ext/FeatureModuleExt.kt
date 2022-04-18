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

import gradle.kotlin.dsl.accessors._84dd20d1a49d379320fe96b7964013dd.kotlin
import gradle.kotlin.dsl.accessors._84dd20d1a49d379320fe96b7964013dd.sourceSets
import org.gradle.api.artifacts.VersionCatalogsExtension

fun org.gradle.api.Project.configureFeatureModule() {
    kotlin {
        sourceSets {
            val catalogs = extensions.getByType(VersionCatalogsExtension::class.java)
            val libs = catalogs.named("libs")

            named("commonMain") {
                dependencies {
                    implementation(project(":common:data"))
                    implementation(project(":common:scaffold"))
                    implementation(project(":common:feature-template"))

                    implementation(libs.findDependency("jetbrains-compose-runtime").get())
                    implementation(libs.findDependency("jetbrains-compose-animation").get())
                    implementation(libs.findDependency("jetbrains-compose-material").get())
                    implementation(libs.findDependency("jetbrains-compose-foundation-layout").get())

                    implementation(libs.findDependency("kotlinx-coroutines-core").get())
                    api(libs.findDependency("tunjid-treenav-core-common").get())
                    api(libs.findDependency("tunjid-treenav-strings-common").get())
                }
            }
            named("androidMain") {
                dependencies {
                    implementation(libs.findDependency("androidx-compose-foundation-layout").get())
                }
            }
            named("desktopMain") {
                dependencies {
                }
            }
            named("commonTest") {
                dependencies {
                    implementation(kotlin("test"))
                }
            }
        }
    }
}