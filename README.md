# iCal filter

This little webapp can filter an iCal document.

## How to use

Craft an URL that contains the URL of the original iCal, and a comma separated list of words that will be used to filter the events.

Only the events whose `SUMMARY` field matches one of the words in the filter will be kept in the result.

An instance is running on Heroku.

### Usage

`https://ical-filter.herokuapp.com/` `<source url>` `/` `<comma separated filters>`

### Example

https://ical-filter.herokuapp.com/https%3A%2F%2Fwww.calendarlabs.com%2Fical-calendar%2Fics%2F45%2FFrance_Holidays.ics/easter,saints

## Licence

Copyright (C) 2021-present Benoit 'BoD' Lubek (BoD@JRAF.org)

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
