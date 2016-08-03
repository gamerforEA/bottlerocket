package com.antwerkz.bottlerocket.executable

import com.antwerkz.bottlerocket.MongoExecutable
import com.antwerkz.bottlerocket.MongoManager
import com.jayway.awaitility.Awaitility
import com.jayway.awaitility.Duration
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.process.Processes
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class Mongod(manager: MongoManager, name: String,
                    port: Int, baseDir: File) : MongoExecutable(manager, name, port, baseDir) {
    companion object {
        private val LOG = LoggerFactory.getLogger(Mongod::class.java)
    }

    override val logger = LoggerFactory.getLogger("Mongod.${port}")

    private val stdOut: FileOutputStream by lazy {
        FileOutputStream(File(baseDir, "mongod.out"))
    }

    private val stdErr: FileOutputStream by lazy {
        FileOutputStream(File(baseDir, "mongod.err"))
    }

    private val configFile = File(baseDir, "mongod.conf")

    fun start(replicaSetName: String? = null) {
        if (process == null || !process?.isAlive!!) {
            LOG.info("Starting mongod on port ${port}")
            baseDir.mkdirs()
            val configFile = configFile
            manager.writeConfig(configFile, config)

            val args = arrayListOf(manager.mongod,
                  "--config", configFile.absolutePath)
            if(replicaSetName != null) {
                args.addAll(arrayOf("--replSet", replicaSetName))
            }
            val processResult = ProcessExecutor()
                  .command(args)
                  .redirectOutput(stdOut)
                  .redirectError(stdErr)
                  .destroyOnExit()
                  .start()

            process = Processes.newPidProcess(processResult?.process)

            waitForStartUp()
        } else {
            LOG.warn("start() was called on a running server: ${port}")
        }
    }

    fun shutdown2() {
        if (process != null ) {
            LOG.info("Stopping mongod on port ${port}")
            baseDir.mkdirs()

            val args = arrayListOf(manager.mongod,
                  "--config", configFile.absolutePath, "--shutdown")
            val processResult = ProcessExecutor()
                    .command(args)
                    .redirectOutput(stdOut)
                    .redirectError(stdErr)
                    .destroyOnExit()
                    .start()

            val shutdown = Processes.newPidProcess(processResult?.process)

            Awaitility
                    .await()
                    .atMost(10, TimeUnit.SECONDS)
                    .pollInterval(Duration.ONE_SECOND)
                    .until<Boolean>({
                        println("Waiting for server to die")
                        !shutdown.isAlive
                    })

            stdOut.close()
            stdErr.close()
        }
    }
}