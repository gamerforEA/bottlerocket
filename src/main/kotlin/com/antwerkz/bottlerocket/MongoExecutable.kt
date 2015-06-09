package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.ConfigMode.MONGOS
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.configuration.Destination
import com.antwerkz.bottlerocket.configuration.configuration
import com.antwerkz.bottlerocket.executable.ConfigServer
import com.antwerkz.bottlerocket.executable.Mongod
import com.mongodb.ServerAddress
import org.bson.codecs.BsonDocumentCodec
import org.bson.codecs.DecoderContext
import org.bson.json.JsonReader
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import org.zeroturnaround.process.JavaProcess
import org.zeroturnaround.process.Processes
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

public open class MongoExecutable(val manager: MongoManager, val name: String, val port: Int, val baseDir: File) {
    public var process: JavaProcess? = null
        protected set
    val configuration: Configuration
    public var authEnabled: Boolean = false

    companion object {
        private val LOG = LoggerFactory.getLogger(javaClass<MongoExecutable>())
    }

    init {
        configuration = configuration {
            net {
                this.port = this@MongoExecutable.port
                bindIp = "localhost"
            }
            processManagement {
                pidFilePath = File(baseDir, "${name}.pid").toString()
            }
            storage {
                dbPath = baseDir.getAbsolutePath()

            }
            systemLog {
                destination = Destination.FILE
                path = "${baseDir}/mongod.log"
            }
        }
    }

    fun isAlive(): Boolean {
        return process?.isAlive() ?: false;
    }

    fun clean() {
        shutdown()
        baseDir.deleteTree()
    }

    fun shutdown() {
        if (isAlive()) {
            LOG.info("Shutting down mongod on port ${port}")
            ProcessExecutor().command(manager.mongo,
                  "admin", "--port", "${port}", "--quiet")
                  .redirectOutput(Slf4jStream.of(LoggerFactory.getLogger(javaClass<MongoExecutable>())).asError())
                  .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass<MongoExecutable>())).asError())
                  .redirectInput(ByteArrayInputStream("db.shutdownServer()".toByteArray()))
                  .execute()
            process?.destroy(true)
        }
    }

    fun waitForStartUp() {
        val start = System.currentTimeMillis();
        var connected = tryConnect();
        while (!connected && System.currentTimeMillis() - start < 30000) {
            Thread.sleep(5000)
            connected = tryConnect()
        }
    }

    fun tryConnect(): Boolean {
        val stream = ByteArrayOutputStream()
        ProcessExecutor()
              .command(listOf(manager.mongo,
                    "admin", "--port", "${port}", "--quiet"))
              .redirectOutput(stream)
              .redirectError(Slf4jStream.of(LoggerFactory.getLogger(this.javaClass)).asInfo())
              .redirectInput(ByteArrayInputStream("db.stats()".toByteArray()))
              .execute()

        val json = String(stream.toByteArray()).trim()
        try {
            BsonDocumentCodec().decode(JsonReader(json), DecoderContext.builder().build())
            return true
        } catch(e: Exception) {
            return false
        }
    }
}


