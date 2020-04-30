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
 *
 * @author Daniela Butano
 */

public enum Header {
    COUNTRY(1),
    DATE(2),
    CONFIRMED(3),
    NEW_CONFIRMED(4),
    DEATHS(5),
    NEW_DEATHS(6);

    private int pos;

    private Header(int pos) {
        this.pos = pos;
    }

    public int getPos() {
        return pos;
    }
}
