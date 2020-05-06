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
 * Store the position of the data we want to extract from the csv file
 * @author Daniela Butano
 */
public class HeaderMap {

    private HashMap<Header, Integer> headerMap;

    public HeaderMap(String[] headerLine) {
        headerMap = new HashMap<>();
        String header = null;
        for (int pos = 0; pos< headerLine.length; pos++) {
            header = headerLine[pos];
            if (header.equalsIgnoreCase("date")) {
                headerMap.put(Header.DATE, pos);
            } else if (header.equalsIgnoreCase("state")) {
                headerMap.put(Header.STATE_CODE, pos);
            } else if (header.equalsIgnoreCase("positive")) {
                headerMap.put(Header.CONFIRMED, pos);
            } else if (header.equalsIgnoreCase("death")) {
                headerMap.put(Header.DEATHS, pos);
            } else if (header.equalsIgnoreCase("positiveIncrease")) {
                headerMap.put(Header.NEW_CONFIRMED, pos);
            } else if (header.equalsIgnoreCase("deathIncrease")) {
                headerMap.put(Header.NEW_DEATHS, pos);
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