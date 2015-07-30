package com.antwerkz.bottlerocket.configuration

import com.antwerkz.bottlerocket.configuration.State.ENABLED
import com.antwerkz.bottlerocket.configuration.blocks.*
import org.testng.Assert
import org.testng.annotations.Test
import kotlin.platform.platformStatic

public class ConfigurationTest {
    companion object {
        public platformStatic val COMPLEX_CONFIG: String =
              "net:\n" +
                    "  bindIp: 127.0.0.1\n" +
                    "  port: 27017\n" +
                    "processManagement:\n" +
                    "  fork: true\n" +
                    "replication:\n" +
                    "  oplogSizeMB: 10\n" +
                    "storage:\n" +
                    "  dbPath: /var/lib/mongodb\n" +
                    "  mmapv1:\n" +
                    "    preallocDataFiles: false\n" +
                    "    smallFiles: true\n" +
                    "  repairPath: /var/lib/mongodb_tmp\n" +
                    "systemLog:\n" +
                    "  component:\n" +
                    "    accessControl:\n" +
                    "      verbosity: 2\n" +
                    "  destination: file\n" +
                    "  logAppend: true\n" +
                    "  logRotate: rename\n" +
                    "  path: /var/log/mongodb/mongod.log"
        //              "setParameter:\n" +
        //              "   enableLocalhostAuthBypass: false\n" +

    }

    @Test
    public fun testYaml() {
        val configuration = Configuration(
              systemLog = SystemLog(
                    destination = Destination.SYSLOG,
                    component = Component(
                          accessControl = LogComponent.AccessControl(verbosity = Verbosity.FIVE)
                    )
              )
        )
        val target =
              "net:\n" +
                    "  bindIp: 127.0.0.1\n" +
                    "  port: 27017\n" +
                    "replication:\n" +
                    "  oplogSizeMB: 10\n" +
                    "storage:\n" +
                    "  mmapv1:\n" +
                    "    preallocDataFiles: false\n" +
                    "    smallFiles: true\n" +
                    "systemLog:\n" +
                    "  component:\n" +
                    "    accessControl:\n" +
                    "      verbosity: 5\n" +
                    "  destination: syslog"
        Assert.assertEquals(configuration.toYaml(), target)
    }

    @Test
    public fun complexExample() {
        val configuration = Configuration(
              storage = Storage(dbPath = "/var/lib/mongodb"),
              systemLog = SystemLog(
                    destination = Destination.FILE,
                    path = "/var/log/mongodb/mongod.log",
                    logAppend = true,
                    component = Component(
                          accessControl = LogComponent.AccessControl(verbosity = Verbosity.TWO)
                    )
              ),
              processManagement = ProcessManagement(
                    fork = true
              )
        )
        val target =
              "net:\n" +
                    "  bindIp: 127.0.0.1\n" +
                    "  port: 27017\n" +
                    "processManagement:\n" +
                    "  fork: true\n" +
                    "replication:\n" +
                    "  oplogSizeMB: 10\n" +
                    "storage:\n" +
                    "  dbPath: /var/lib/mongodb\n" +
                    "  mmapv1:\n" +
                    "    preallocDataFiles: false\n" +
                    "    smallFiles: true\n" +
                    "systemLog:\n" +
                    "  component:\n" +
                    "    accessControl:\n" +
                    "      verbosity: 2\n" +
                    "  destination: file\n" +
                    "  logAppend: true\n" +
                    "  path: /var/log/mongodb/mongod.log";
        Assert.assertEquals(configuration.toYaml(), target);
    }

    @Test
    public fun testBuilder() {
        val config = configuration {
            systemLog {
                destination = Destination.SYSLOG
                component {
                    accessControl {
                        verbosity = Verbosity.FIVE
                    }
                }
            }
        }
        Assert.assertEquals(config.toYaml(),
              "net:\n" +
                    "  bindIp: 127.0.0.1\n" +
                    "  port: 27017\n" +
                    "replication:\n" +
                    "  oplogSizeMB: 10\n" +
                    "storage:\n" +
                    "  mmapv1:\n" +
                    "    preallocDataFiles: false\n" +
                    "    smallFiles: true\n" +
                    "systemLog:\n" +
                    "  component:\n" +
                    "    accessControl:\n" +
                    "      verbosity: 5\n" +
                    "  destination: syslog")
    }

    @Test
    public fun testComplexBuilder() {
        val configuration = configuration {
            storage {
                dbPath = "/var/lib/mongodb"
                repairPath = "/var/lib/mongodb_tmp"
            }
            systemLog {
                destination = Destination.FILE
                path = "/var/log/mongodb/mongod.log"
                logAppend = true
                logRotate = RotateBehavior.RENAME
                component {
                    accessControl {
                        verbosity = Verbosity.TWO
                    }
                }
            }
            processManagement {
                fork = true
            }
        }


        Assert.assertEquals(configuration.toYaml(), COMPLEX_CONFIG);
    }

    @Test
    public fun mongosConfig() {
        val path = "/var/lib/mongo/data"
        val configuration = configuration {
            storage {
                dbPath = path
            }
        }

        Assert.assertFalse(configuration.toYaml(mode = ConfigMode.MONGOS).contains(path))
    }

    @Test
    fun mergeConfig() {
        val config = configuration {
            storage {
                dbPath = "/var/lib/mongo/noodle"
            }
            net {
                port = 12345;
            }
        }
        val update = configuration {
            processManagement {
                operationProfiling {
                    slowOpThresholdMs = 50;
                }
            }
            security {
                authorization = ENABLED
            }
        }

        Assert.assertNull(config.security.authorization)
        config.merge(update);
        Assert.assertEquals(config.security.authorization, ENABLED)
        Assert.assertEquals(config.operationProfiling.slowOpThresholdMs, 50)
        Assert.assertNotEquals(update.storage.dbPath, "/var/lib/mongo/noodle")
    }

    @Test
    fun properties() {
        val configuration = Configuration(
              storage = Storage(dbPath = "/var/lib/mongodb"),
              systemLog = SystemLog(
                    destination = Destination.FILE,
                    path = "/var/log/mongodb/mongod.log",
                    logAppend = true,
                    logRotate = RotateBehavior.RENAME,
                    component = Component(
                          accessControl = LogComponent.AccessControl(verbosity = Verbosity.TWO)
                    )
              ),
              processManagement = ProcessManagement(
                    fork = true
              )
        )

        val s = configuration.toProperties(mode = ConfigMode.ALL)
        println("s = \n${s}")
        Assert.assertNotNull(s)
    }
}