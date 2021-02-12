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

package org.jraf.icalfilter.core

import biweekly.Biweekly
import biweekly.ICalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jraf.icalfilter.exceptions.IcalDownloadException
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit

class IcalFilter(
    private val sourceUrl: String,
    filterSet: List<String>,
) {
    private val filterSet: List<List<String>> = filterSet.map { it.split(',') }

    private fun String.matchesFilterSet(): Boolean =
        filterSet.all { filter ->
            filter.any { filterElement ->
                matchesFilterElement(filterElement)
            }
        }

    private fun String.matchesFilterElement(filterElement: String): Boolean =
        if (filterElement.startsWith('!')) {
            val elem = filterElement.drop(1)
            !toLowerCase(Locale.US).contains(elem.toLowerCase(Locale.US))
        } else {
            toLowerCase(Locale.US).contains(filterElement.toLowerCase(Locale.US))
        }

    private suspend fun downloadSource(): String = withContext(Dispatchers.IO) {
        try {
            @Suppress("BlockingMethodInNonBlockingContext")
            URL(sourceUrl).readText()
        } catch (t: Throwable) {
            throw IcalDownloadException(t)
        }
    }

    suspend fun filtered(): String {
        // Download the source ical
        val icalText = downloadSource()

        // Parse it
        @Suppress("BlockingMethodInNonBlockingContext")
        val ical: ICalendar = Biweekly.parse(icalText).first()

        // Remove events we're not interested in
        val iterator = ical.events.iterator()
        while (iterator.hasNext()) {
            val event = iterator.next()
            val eventSummary = event.summary.value
            val filteredOut = !eventSummary.matchesFilterSet()
            val tooOld =
                System.currentTimeMillis() - event.dateStart.value.time > TimeUnit.DAYS.toMillis(7)
            if (filteredOut || tooOld) iterator.remove()
        }

        // Serialize it
        return Biweekly.write(ical).go()
    }
}
