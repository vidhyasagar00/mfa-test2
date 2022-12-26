package com.net.routee.setUp

import java.io.InputStreamReader

/**
 * Mock response file reader
 *
 * @constructor
 *
 * @param path
 */
class MockResponseFileReader(path: String) {
    val content: String

    init {
        val reader = InputStreamReader(this.javaClass.classLoader.getResourceAsStream(path))
        content = reader.readText()
        reader.close()
    }
}