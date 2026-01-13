package com.chac.convention.extensions

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.getByType

/** com.android.application 플러그인을 적용한 모듈의 gradle 속성에 접근하기 위한 Extensions (ex. :app 모듈) */
internal val Project.applicationExtension: CommonExtension<*, *, *, *, *, *>
    get() = extensions.getByType<ApplicationExtension>()

/** com.android.library 플러그인을 적용한 모듈의 gradle 속성에 접근하기 위한 Extensions (ex. :feature 하위 모듈) */
internal val Project.libraryExtension: CommonExtension<*, *, *, *, *, *>
    get() = extensions.getByType<LibraryExtension>()

/** 라이브러리 모듈과 애플리케이션 모듈의 gradle 속성에 접근하기 위한 Extensions */
internal val Project.androidExtension: CommonExtension<*, *, *, *, *, *>
    get() = runCatching { libraryExtension }
        .recoverCatching { applicationExtension }
        .onFailure { println("Could not find Library or Application extension from this project") }
        .getOrThrow()

/** 버전 카탈로그를 참조 하기 위한 Extension */
internal val ExtensionContainer.libs: VersionCatalog
    get() = getByType<VersionCatalogsExtension>().named("libs")
