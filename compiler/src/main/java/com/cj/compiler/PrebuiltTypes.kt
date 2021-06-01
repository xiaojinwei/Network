/*
 * Copyright (C) 2018 Bennyhuo.
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package com.cj.compiler

import com.squareup.javapoet.ClassName


val CONTEXT = ClassName.get("android.content","Context")
val NETWORK = ClassName.get("com.cj.network","Network")
val INITIALIZER = ClassName.get("androidx.startup","Initializer")
val LIST = ClassName.get("java.util","List")
val ARRAYLIST = ClassName.get("java.util","ArrayList")
val CONFIGMODULE = ClassName.get("com.cj.runtime.integration","ConfigModule")
