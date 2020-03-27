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
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;
import org.intermine.dataconversion.DirectoryConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;

/**
 * 
 * @author Daniela BUtano
 */
public class GisaidCsvConverter extends DirectoryConverter
{
    private static final String FILE_NAME_PATTERN = "MM-dd-yyyy";
    private static final char FILE_SEPARATOR = ',';
    private Map<String, Item> locations = new HashMap<>();
    private Map<String, List<String>> locationDistributionIds = new HashMap<>();

    public GisaidCsvConverter(ItemWriter writer, Model model) {
        super(writer, model);
    }

    @Override
    public void process(File dataDir) throws Exception {
        if (dataDir.isDirectory()) {
            for (File dailyReport : dataDir.listFiles()) {
                storeDistributions(dailyReport);
            }
            storeGeoLocations();
        }
    }

    private void storeDistributions(File dailyReportFile) {
        System.out.println(dailyReportFile.getName());
        String dailyReportFileName = dailyReportFile.getName();
        if (!dailyReportFileName.equals("03-24-2020.csv")
                && !dailyReportFileName.equals("03-23-2020.csv")
                && !dailyReportFileName.equals("03-22-2020.csv")) {
            return;//just two files for now
        }
        String[] drLine = null;
        CSVReader reader = null;
        String dateAsString = dailyReportFileName.substring(0,
                dailyReportFileName.indexOf(".csv"));
        try {
            reader = new CSVReader(new FileReader(dailyReportFile.getAbsolutePath()),
                    FILE_SEPARATOR);
            reader.readNext();//header
            while ((drLine = reader.readNext()) != null) {
                storeDistribution(drLine, dateAsString);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Problem reading file " + dailyReportFileName);
        }
    }

    private void storeDistribution(String[] dailyReport, String dateAsString) {
        GeoLocation location = new GeoLocation(dailyReport);
        Item geoLocation = createGeoLocation(location);
        Item distribution = createItem("Distribution");
        Date date = convertDate(dateAsString);
        distribution.setAttributeIfNotNull("date", Long.toString(date.getTime()));
        distribution.setAttributeIfNotNull("confirmed", dailyReport[7]);
        distribution.setAttributeIfNotNull("deaths", dailyReport[8]);
        distribution.setAttributeIfNotNull("recovered", dailyReport[9]);
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
            geoLocationItem.setAttributeIfNotNull("latitude", location.latitude);
            geoLocationItem.setAttributeIfNotNull("longitude", location.longitude);
            geoLocationItem.setAttributeIfNotNull("province", location.province);
            geoLocationItem.setAttributeIfNotNull("state", location.state);
            geoLocationItem.setAttributeIfNotNull("country", location.country);
            locations.put(locationKey, geoLocationItem);
            return geoLocationItem;
        }
    }

    private Date convertDate(String dateAsString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FILE_NAME_PATTERN);
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
                geoLocation.setCollection("distributions", locationDistributionIds.get(locationKey));
                store(geoLocation);
            }
        } catch (ObjectStoreException e) {
            e.printStackTrace();
        }
    }

    private class GeoLocation {
        String latitude;
        String longitude;
        String province;
        String state;
        String country;
        String locationKey;

        public GeoLocation(String[] fields) {
            latitude = (fields[5] != null) ? fields[5] : StringUtils.EMPTY;
            longitude = (fields[6] != null) ? fields[6] : StringUtils.EMPTY;
            province = (fields[1] != null) ? fields[1] : StringUtils.EMPTY;
            state = (fields[2] != null) ? fields[2] : StringUtils.EMPTY;
            country = (fields[3] != null) ? fields[3] : StringUtils.EMPTY;
            locationKey = latitude + longitude + province + state + country;
            locationKey = StringUtils.deleteWhitespace(locationKey);
        }
    }





















}
