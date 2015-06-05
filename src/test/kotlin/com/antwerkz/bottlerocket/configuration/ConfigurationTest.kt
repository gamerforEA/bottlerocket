package com.antwerkz.bottlerocket.configuration

import com.antwerkz.bottlerocket.configuration.blocks.Component
import com.antwerkz.bottlerocket.configuration.blocks.ProcessManagement
import com.antwerkz.bottlerocket.configuration.blocks.Storage
import com.antwerkz.bottlerocket.configuration.blocks.SystemLog
import org.testng.Assert
import org.testng.annotations.Test

public class ConfigurationTest {
    Test public fun testYaml() {
        val configuration = Configuration(
              systemLog = SystemLog(
                    destination = Destination.SYSLOG,
                    component = Component(
                          accessControl = LogComponent.AccessControl(verbosity = Verbosity.FIVE)
                    )
              )
        )
        val target =
              "systemLog:\n" +
                    "  component:\n" +
                    "    accessControl:\n" +
                    "      verbosity: 5\n" +
                    "  destination: syslog"
        Assert.assertEquals(configuration.toYaml(), target)
    }

    Test public fun complexExample() {
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
        val target =
              "processManagement:\n" +
                    "  fork: true\n" +
                    "storage:\n" +
                    "  dbPath: /var/lib/mongodb\n" +
                    "  repairPath: /var/lib/mongodb_tmp\n" +
                    "systemLog:\n" +
                    "  component:\n" +
                    "    accessControl:\n" +
                    "      verbosity: 2\n" +
                    "  destination: file\n" +
                    "  logAppend: true\n" +
                    "  path: /var/log/mongodb/mongod.log" +
                    ""
        //              "setParameter:\n" +
        //              "   enableLocalhostAuthBypass: false\n" +
        Assert.assertEquals(configuration.toYaml(), target);
    }

    Test public fun testBuilder() {
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
        val target = "systemLog:\n" +
              "  component:\n" +
              "    accessControl:\n" +
              "      verbosity: 5\n" +
              "  destination: syslog"
        println("${config.toYaml()}")
        Assert.assertEquals(config.toYaml(), target)
    }

    Test public fun testComplexBuilder() {
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

        val target = "processManagement:\n" +
              "  fork: true\n" +
              "storage:\n" +
              "  dbPath: /var/lib/mongodb\n" +
              "  repairPath: /var/lib/mongodb_tmp\n" +
              "systemLog:\n" +
              "  component:\n" +
              "    accessControl:\n" +
              "      verbosity: 2\n" +
              "  destination: file\n" +
              "  logAppend: true\n" +
              "  path: /var/log/mongodb/mongod.log"
        //              "setParameter:\n" +
        //              "   enableLocalhostAuthBypass: false\n" +

        Assert.assertEquals(configuration.toYaml(), target);
    }

    Test public fun mongosConfig() {
        val path = "/var/lib/mongo/data"
        val configuration = configuration {
            storage {
                dbPath = path
            }
        }

        Assert.assertFalse(configuration.toYaml(mode = ConfigMode.MONGOS).contains(path))
    }

    public fun printAll() {
        ConfigBlock.OMIT_DEFAULTED = false
        println("Configuration() = ${Configuration().toYaml()}")
    }
}