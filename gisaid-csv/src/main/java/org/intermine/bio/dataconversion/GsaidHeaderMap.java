package org.intermine.bio.dataconversion;

/*
 * Copyright (C) 2002-2020 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.HashMap;

/**
 *
 * @author Daniela Butano
 */
public class GsaidHeaderMap {

    private HashMap<Header, Integer> headerMap;

    public GsaidHeaderMap(String[] headerLine) {
        headerMap = new HashMap<>();
        String header = null;
        for (int pos = 0; pos< headerLine.length; pos++) {
            header = headerLine[pos];
            if (header.contains("Admin")) {
                headerMap.put(Header.PROVINCE, pos);
            } else if (header.contains("State")) {
                headerMap.put(Header.STATE, pos);
            } else if (header.contains("Country")) {
                headerMap.put(Header.COUNTRY, pos);
            } else if (header.contains("Lat")) {
                headerMap.put(Header.LATITUDE, pos);
            } else if (header.contains("Long")) {
                headerMap.put(Header.LONGITUDE, pos);
            } else if (header.contains("Confirmed")) {
                headerMap.put(Header.CONFIRMED, pos);
            } else if (header.contains("Deaths")) {
                headerMap.put(Header.DEATHS, pos);
            } else if (header.contains("Recovered")) {
                headerMap.put(Header.RECOVERED, pos);
            } else if (header.contains("Active")) {
                headerMap.put(Header.ACTIVE, pos);
            }
        }
    }

    public int getPosition(Header header) {
        if (!headerMap.containsKey(header)) {
            return -1;
        }
        return headerMap.get(header);
    }
}