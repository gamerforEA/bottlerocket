package com.antwerkz.bottlerocket.configuration.mongo26.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode
import com.antwerkz.bottlerocket.configuration.types.ClusterRole

class Sharding() : ConfigBlock {
    @Mode(ConfigMode.MONGOD) var clusterRole: ClusterRole? = null
    var archiveMovedChunks: Boolean? = null
    var autoSplit: Boolean? = null
    var configDB: String? = null
    var chunkSize: Int? = null
}