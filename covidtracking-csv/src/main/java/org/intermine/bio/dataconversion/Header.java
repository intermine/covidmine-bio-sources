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
    DATE(0),
    STATE_CODE(1),
    CONFIRMED(2),
    DEATHS(14),
    NEW_DEATHS(20),
    NEW_CONFIRMED(23);

    private int pos;

    private Header(int pos) {
        this.pos = pos;
    }

    public int getPos() {
        return pos;
    }
}
