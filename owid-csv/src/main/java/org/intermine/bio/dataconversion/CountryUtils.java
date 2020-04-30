package org.intermine.bio.dataconversion;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
 * Copyright (C) 2002-2020 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */
public final class CountryUtils {
    private static Properties prop;
    private static Logger LOG = Logger.getLogger(CountryUtils.class);

    private CountryUtils() {
    }

    public static Properties getProperties() {
        if (prop == null) {
            try {
                prop = new Properties();
                InputStream is = null;
                try {
                    is = CountryUtils.class.getClassLoader()
                            .getResourceAsStream("countries.properties");
                    prop.load(is);
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            } catch (IOException ex) {
                LOG.error("Failed to load countries.properties file");
            }
        }
        return prop;
    }

    public static String getCountry(String country) {
        String newCountry = getProperties().getProperty(country);
        if (newCountry == null) {
            return country;
        }
        return  newCountry;
    }
}
