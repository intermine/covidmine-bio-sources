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

/**
 * Data we want to extract from the csv file
 * @author Daniela Butano
 */

public enum Header {
    DATE,
    STATE_CODE,
    CONFIRMED,
    DEATHS,
    NEW_DEATHS,
    NEW_CONFIRMED;
}
