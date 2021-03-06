package com.antwerkz.bottlerocket

import com.github.zafarkhaja.semver.Version
import com.mongodb.ReadPreference
import com.mongodb.client.MongoClient
import org.bson.Document
import org.slf4j.LoggerFactory
import java.io.File

object BottleRocket {
    private val LOG = LoggerFactory.getLogger(BottleRocket::class.java)

    @JvmField
    val TEMP_DIR = System.getProperty("java.io.tmpdir")

    @JvmField
    var DEFAULT_NAME = "rocket"

    @JvmField
    var DEFAULT_PORT = 30000

    @JvmField
    var DEFAULT_VERSION = Version.forIntegers(4, 4, 1)

    @JvmField
    var DEFAULT_BASE_DIR = File(if (File("build").exists()) "build" else "target", DEFAULT_NAME)
}

internal fun MongoClient.runCommand(command: Document, readPreference: ReadPreference = ReadPreference.primary()): Document {
    try {
        return getDatabase("admin")
            .runCommand(command, readPreference)
    } catch (e: Exception) {
        throw RuntimeException("command failed: $command with preference $readPreference", e)
    }
}