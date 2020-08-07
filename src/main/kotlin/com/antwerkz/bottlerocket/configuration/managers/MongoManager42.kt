package com.antwerkz.bottlerocket.configuration.managers

import com.antwerkz.bottlerocket.MongoManager
import com.github.zafarkhaja.semver.Version

class MongoManager42(version: Version) : MongoManager(
    version,
    linuxBaseUrl = "https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-${linux()}-",
    macBaseUrl = "https://fastdl.mongodb.org/osx/mongodb-macos-x86_64-",
    windowsBaseUrl = "https://fastdl.mongodb.org/win32/mongodb-win32-x86_64-2012plus-"
)