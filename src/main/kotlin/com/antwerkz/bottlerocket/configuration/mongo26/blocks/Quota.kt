package com.antwerkz.bottlerocket.configuration.mongo26.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode

class Quota() : ConfigBlock {
    @Mode(ConfigMode.MONGOD) var enforced: Boolean? = null
    @Mode(ConfigMode.MONGOD) var maxFilesPerDB: Int? = null
}