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
import java.util.Locale

class IcalFilter(
    private val sourceUrl: String,
    filter: String,
) {
    private val filterElements: List<String> = filter.split(',')

    private fun String.isIncludedInFilter(): Boolean {
        return filterElements.any { toLowerCase(Locale.US).contains(it.toLowerCase(Locale.US)) }
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

        // Remve events we're not interested in
        val iterator = ical.events.iterator()
        while (iterator.hasNext()) {
            val event = iterator.next()
            val eventSummary = event.summary.value
            if (!eventSummary.isIncludedInFilter()) iterator.remove()
        }

        // Serialize it
        return Biweekly.write(ical).go()
    }
}
