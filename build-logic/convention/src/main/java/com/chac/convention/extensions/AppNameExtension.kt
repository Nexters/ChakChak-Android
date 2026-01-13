package com.chac.convention.extensions

import org.gradle.api.Project

/** 기본 패키지 */
val basePackage: String get() = "com.chac"

/**
 * 모듈의 namespace를 셋팅
 * @param name [basePackage]를 제외한 나머지 패키지
 */
fun Project.setNamespace(name: String = "") {
    androidExtension.apply {
        namespace = StringBuilder().apply {
            append(basePackage)
            if (name.isNotBlank()) {
                append(".$name")
            }
        }.toString()
    }
}
