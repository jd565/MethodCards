package com.jd565.methods.generate

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.xml.xml
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.asSource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.io.asInputStream
import kotlinx.io.buffered
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

class CccbrService {
    val ktor = HttpClient(CIO) {
        val xmlInstance = XML {
            defaultPolicy {
                this.unknownChildHandler = XmlConfig.Companion.IGNORING_UNKNOWN_CHILD_HANDLER
            }
        }
        install(ContentNegotiation) {
            xml(xmlInstance)
        }
        install(ContentEncoding)
        install(HttpTimeout) {
            connectTimeoutMillis = 30_000L
            requestTimeoutMillis = 30_000L
        }
    }

    suspend fun getAllMethods() : XmlCollection {
        val channel = ktor.get("https://methods.cccbr.org.uk/xml/CCCBR_methods.xml.zip")
            .body<ByteReadChannel>()
        val zis = ZipInputStream(channel.asSource().buffered().asInputStream())
        val entry = zis.nextEntry
        check(entry != null)
        val out = ByteArrayOutputStream()
        zis.copyTo(out)
        out.close()
        zis.closeEntry()
        check(zis.nextEntry == null)
        zis.close()

        val string = out.toString()
        .trim().replaceFirst("^([\\W]+)<","<");
        val xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + string
            .dropWhile { it != '\n' }

        val xmlInstance = XML {
            defaultPolicy {
                this.unknownChildHandler = XmlConfig.Companion.IGNORING_UNKNOWN_CHILD_HANDLER
            }
        }
        return xmlInstance.decodeFromString<XmlCollection>(xml)
    }

    suspend fun getBellboardPerformances(pages: Int = 1): List<XmlPerformance> {
        return coroutineScope {
            (1..pages)
                .chunked(5)
                .flatMap { pages ->
                    pages.map { page ->
                        async {
                            ktor.get("https://bb.ringingworld.co.uk/export.php?length=q-or-p") {
                                parameter("length", "q-or-p")
                                parameter("pagesize", 500)
                                parameter("page", page)
                            }.body<XmlPerformances>()
                        }
                    }.awaitAll().flatMap { it.performances }
                }
        }
    }
}
