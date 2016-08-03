package com.antwerkz.bottlerocket.configuration

import org.apache.http.client.fluent.Request
import org.jsoup.Jsoup
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import java.net.URI
import com.antwerkz.bottlerocket.configuration.mongo26.Configuration as Config26
import com.antwerkz.bottlerocket.configuration.mongo30.Configuration as Config30

class ConfigurationDocsTest {
    private var elements: MutableList<String> = arrayListOf()

    fun loadLinks(url: String, version: String) {
        val uri = URI(url)
        val file = File("build", "${version}-configuration-options.html")

        if (!file.exists()) {
            Request.Get(uri)
                    .execute()
                    .saveContent(file)
        }
        val doc = Jsoup.parse(file, "UTF-8")
        elements = when (version) {
            "3.2.0", "3.0.0", "2.6.0" -> {
                doc.select("a[class=headerlink]")
                        .filter({ it.attr("href").contains('.') })
                        .map({ it.attr("href") })
                        .toMutableList()

            }
            else -> throw IllegalArgumentException("Unknown version: ${version}");
        }
    }

    @Test(dataProvider = "urls")
    fun checkDocs(version: String, url: String, configuration: ConfigBlock) {
        loadLinks(url, version)
        check(configuration.toProperties(mode = ConfigMode.ALL, includeAll = true))
        Assert.assertTrue(elements.isEmpty(), "elements should be empty now but has ${elements.size} items left: \n${elements}")
    }

    private fun check(map: Map<String, String>) {
        map.keys.forEach {
            Assert.assertTrue(elements.remove("#${it}"), "Found ${it} in the configuration file but not in the docs.");
        }
    }

    @DataProvider(name = "urls")
    fun urls(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("3.0.0", "http://docs.mongodb.org/v3.0/reference/configuration-options/", Config30()),
                arrayOf("2.6.0", "http://docs.mongodb.org/v2.6/reference/configuration-options/", Config26())
        )
    }
}