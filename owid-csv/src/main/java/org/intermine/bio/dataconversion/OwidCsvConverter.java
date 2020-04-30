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

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
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
 * 
 * @author Daniela Butano
 */
public class OwidCsvConverter extends BioDirectoryConverter {
    private static final Logger LOG = Logger.getLogger(OwidCsvConverter.class);
    private static final String FILE_NAME_PATTERN = "yyyy-MM-dd";
    private static final String FILE_EXTENSION = ".csv";
    private static final char FILE_SEPARATOR = ',';
    private Map<String, Item> locations = new HashMap<>();
    private Map<String, List<String>> locationDistributionIds = new HashMap<>();

    public OwidCsvConverter(ItemWriter writer, Model model) {
        super(writer, model, "OWID ", "Our World in Data COVID-19 dataset");
    }

    @Override
    public void process(File dataDir) throws Exception {
        LOG.warn("OwidCsvConverter process files started..");
        if (dataDir.isDirectory()) {
            for (File covidData : dataDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return s.toLowerCase().endsWith(FILE_EXTENSION);
                }
            })) {
                storeDistributions(covidData);
            }
            storeGeoLocations();
        }
        LOG.warn("OwidCsvConverter process files completed.");
    }

    private void storeDistributions(File covidDataFile) {
        String covidDataFileName = covidDataFile.getName();

        String[] drLine = null;
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(covidDataFile.getAbsolutePath()),
                    FILE_SEPARATOR);
            //skip header
            reader.readNext();
            while ((drLine = reader.readNext()) != null) {
                storeDistribution(drLine);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Problem reading file " + covidDataFileName);
        }
    }

    private void storeDistribution(String[] countryDailyReport) {
        GeoLocation location = new GeoLocation(countryDailyReport);
        Item geoLocation = createGeoLocation(location);
        Item distribution = createItem("Distribution");
        Date date = convertDate(getFieldValue(Header.DATE, countryDailyReport));
        distribution.setAttributeIfNotNull("date", Long.toString(date.getTime()));
        String confirmed = getFieldValue(Header.CONFIRMED, countryDailyReport);
        distribution.setAttributeIfNotNull("totalCases", confirmed);
        String newConfirmed = getFieldValue(Header.NEW_CONFIRMED, countryDailyReport);
        distribution.setAttributeIfNotNull("newCases", newConfirmed);
        String deaths = getFieldValue(Header.DEATHS, countryDailyReport);
        distribution.setAttributeIfNotNull("totalDeaths", deaths);
        String newDeaths = getFieldValue(Header.NEW_DEATHS, countryDailyReport);
        distribution.setAttributeIfNotNull("newDeaths", newDeaths);

        try {
            distribution.setReference("geoLocation", geoLocation);
            store(distribution);
            cacheDistributionIds(location.locationKey, distribution.getIdentifier());
        } catch (ObjectStoreException e) {
            e.printStackTrace();
        }
    }

    private Item createGeoLocation(GeoLocation location) {
        String locationKey = location.locationKey;
        if (locations.containsKey(locationKey)) {
            return locations.get(locationKey);
        } else {
            Item geoLocationItem = createItem("GeoLocation");
            geoLocationItem.setAttributeIfNotNull("country", location.country);
            locations.put(locationKey, geoLocationItem);
            return geoLocationItem;
        }
    }

    private Date convertDate(String dateAsString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FILE_NAME_PATTERN);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = simpleDateFormat.parse(dateAsString);
        } catch (ParseException ex) {
            ex.printStackTrace();
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
            e.printStackTrace();
        }
    }

    private String getFieldValue(Header label, String[] fields) {
        return fields[label.getPos()].trim();
    }

    private class GeoLocation {
        String country;
        String locationKey;

        public GeoLocation(String[] countryDailyReport) {
            country = getFieldValue(Header.COUNTRY, countryDailyReport);
            locationKey = StringUtils.deleteWhitespace(country);
        }
    }





















}
