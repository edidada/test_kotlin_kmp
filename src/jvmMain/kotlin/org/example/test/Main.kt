package org.example.test

import kotlinx.coroutines.runBlocking
import org.example.test.kmp.PlatformMarker
import org.example.test.kmp.demoCoroutines
import org.example.test.kmp.runAllDemos

// JVM 入口：main 函数编译为 org.example.test.MainKt
@PlatformMarker // expect 注解在 JVM 侧的 actual 实现直接可用
fun annotatedNote() = println("@PlatformMarker 注解函数（JVM actual 注解实现）")

fun main() {
    println("Hello World!")
    println(runAllDemos())
    println(runBlocking { demoCoroutines() }) // runBlocking 仅 JVM/Native 可用
    annotatedNote()
}
