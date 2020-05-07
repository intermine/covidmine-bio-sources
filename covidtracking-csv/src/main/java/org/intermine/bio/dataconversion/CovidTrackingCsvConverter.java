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

import java.io.Reader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;
import org.apache.log4j.Logger;

/**
 * Converter to read the COVID Tracking Project DataSet
 * https://covidtracking.com/api/v1/states/daily.csv
 * @author Daniela Butano
 */
public class CovidTrackingCsvConverter extends BioFileConverter {
    private static final Logger LOG = Logger.getLogger(CovidTrackingCsvConverter.class);
    private static final char FILE_SEPARATOR = ',';
    private static final String DATE_PATTERN = "yyyyMMdd";
    private static final String TIME_ZONE = "UTC";
    private static final String US_COUNTRY = "United States";
    private HeaderMap header;
    private Map<String, Item> locations = new HashMap<>();
    private Map<String, List<String>> locationDistributionIds = new HashMap<>();
    private Properties statesCodes = new Properties();

    public CovidTrackingCsvConverter(ItemWriter writer, Model model) {
        super(writer, model, "COVIDTrackingProject", "Covid-19 data for US states");
    }

    @Override
    public void process(Reader inputReader) throws Exception {
        LOG.warn("CovidTrackingCsvConverter process files started..");
        initStatesCodes();
        String[] drLine = null;
        CSVReader reader = null;
        try {
            reader = new CSVReader(inputReader, FILE_SEPARATOR);
            header = new HeaderMap(reader.readNext());
            while ((drLine = reader.readNext()) != null) {
                storeDistribution(drLine);
            }
        } catch (IOException ex) {
            new RuntimeException("Problem reading the file", ex);
        }
        storeGeoLocations();
        LOG.warn("CovidTrackingCsvConverter process files completed.");
    }

    private void initStatesCodes() {
        try {
            InputStream is = null;
            try {
                is = CovidTrackingCsvConverter.class.getClassLoader()
                        .getResourceAsStream("US-states.properties");
                statesCodes.load(is);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        } catch (IOException ex) {
            LOG.error("Failed to load US-codes.properties file");
        }

    }

    private void storeDistribution(String[] countryDailyReport) {
        GeoLocation location = new GeoLocation(countryDailyReport);
        if (StringUtils.isEmpty(location.state)) {
            return;//we do no have a state (we only load the 50 states)
        }
        Item geoLocation = createGeoLocation(location);
        Item distribution = createItem("Distribution");
        Date date = convertDate(getFieldValue(Header.DATE, countryDailyReport));
        distribution.setAttributeIfNotNull("date", Long.toString(date.getTime()));
        String confirmed = getFieldValue(Header.CONFIRMED, countryDailyReport);
        distribution.setAttributeIfNotNull("totalCases", confirmed);
        String deaths = getFieldValue(Header.DEATHS, countryDailyReport);
        distribution.setAttributeIfNotNull("totalDeaths", deaths);
        String newDeaths = getFieldValue(Header.NEW_DEATHS, countryDailyReport);
        distribution.setAttributeIfNotNull("newDeaths", newDeaths);
        String newConfirmed = getFieldValue(Header.NEW_CONFIRMED, countryDailyReport);
        distribution.setAttributeIfNotNull("newCases", newConfirmed);

        try {
            distribution.setReference("geoLocation", geoLocation);
            store(distribution);
            cacheDistributionIds(location.locationKey, distribution.getIdentifier());
        } catch (ObjectStoreException e) {
            throw new RuntimeException("Error storing distribution ", e);
        }
    }

    private Item createGeoLocation(GeoLocation location) {
        String locationKey = location.locationKey;
        if (locations.containsKey(locationKey)) {
            return locations.get(locationKey);
        } else {
            Item geoLocationItem = createItem("GeoLocation");
            geoLocationItem.setAttributeIfNotNull("country", location.country);
            geoLocationItem.setAttributeIfNotNull("state", location.state);
            locations.put(locationKey, geoLocationItem);
            return geoLocationItem;
        }
    }

    private Date convertDate(String dateAsString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
        Date date = null;
        try {
            date = simpleDateFormat.parse(dateAsString);
        } catch (ParseException ex) {
            throw new RuntimeException("Error parsing the date " + dateAsString, ex);
        } finally {
            return date;
        }
    }

    private void cacheDistributionIds(String geoLocationKey, String distributionId) {
        if (locationDistributionIds.containsKey(geoLocationKey)) {
            List<String> ids = locationDistributionIds.get(geoLocationKey);
            List<String> updatedIds = new ArrayList<>(ids);
            updatedIds.add(distributionId);
            locationDistributionIds.put(geoLocationKey, updatedIds);
        } else {
            List<String> ids = Arrays.asList(distributionId);
            locationDistributionIds.put(geoLocationKey, ids);
        }
    }

    private void storeGeoLocations() {
        try {
            for (String locationKey : locations.keySet()) {
                Item geoLocation = locations.get(locationKey);
                geoLocation.setCollection("distributions",
                        locationDistributionIds.get(locationKey));
                store(geoLocation);
            }
        } catch (ObjectStoreException e) {
            throw new RuntimeException("Error storing geoLocation ", e);
        }
    }

    private String getFieldValue(Header label, String[] fields) {
        int pos = header.getPosition(label);
        if (pos != -1) {
            return fields[pos].trim();
        }
        return StringUtils.EMPTY;
    }

    private class GeoLocation {
        String country;
        String state;
        String locationKey;

        public GeoLocation(String[] stateDailyReport) {
            country = US_COUNTRY;
            String stateCode = getFieldValue(Header.STATE_CODE, stateDailyReport);
            state = statesCodes.getProperty(stateCode);
            locationKey = stateCode;
        }
    }





















}
