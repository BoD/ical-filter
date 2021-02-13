/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2021-present Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.jraf.icalfilter.main

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.jraf.icalfilter.core.IcalFilter
import org.jraf.icalfilter.exceptions.IcalDownloadException
import org.slf4j.LoggerFactory

private const val DEFAULT_PORT = 8042

private const val ENV_PORT = "PORT"

private const val PATH_SOURCE_URL = "sourceUrl"
private const val PATH_FILTER = "filter"

private const val APP_URL = "https://ical-filter.herokuapp.com"

private val LOGGER = LoggerFactory.getLogger("org.jraf.icalfilter.main")

suspend fun main() {
    val listenPort = System.getenv(ENV_PORT)?.toInt() ?: DEFAULT_PORT
    embeddedServer(Netty, listenPort) {
        install(DefaultHeaders)

        install(StatusPages) {
            status(HttpStatusCode.NotFound) {
                call.respondText(
                    text = """
                        Get a filtered version of an iCal document. Only the events that match the filter will be kept.

                        Usage: $APP_URL/<source url>/<comma separated filters>[/<comma separated filters>]
                        Example: $APP_URL/https%3A%2F%2Fwww.calendarlabs.com%2Fical-calendar%2Fics%2F45%2FFrance_Holidays.ics/easter,saints
                        
                        See https://github.com/BoD/ical-filter for more info.
                    """.trimIndent(),
                    status = it
                )
            }

            exception<IcalDownloadException> { cause ->
                call.respond(
                    HttpStatusCode.NotFound,
                    "Could not download source: ${cause.message}"
                )
            }
        }

        routing {
            get("{$PATH_SOURCE_URL}/{$PATH_FILTER...}") {
                val sourceUrl = call.parameters[PATH_SOURCE_URL]!!
                val filterSet = call.parameters.getAll(PATH_FILTER)!!
                val icalFilter = IcalFilter(sourceUrl, filterSet)
                call.respondText(
                    icalFilter.filtered(),
                    ContentType("text", "calendar").withCharset(Charsets.UTF_8)
                )
            }
        }
    }.start(wait = true)
}
