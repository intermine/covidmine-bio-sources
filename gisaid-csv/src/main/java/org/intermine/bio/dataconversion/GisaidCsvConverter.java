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

/**
 * 
 * @author Daniela Butano
 */
public class GisaidCsvConverter extends BioDirectoryConverter {
    private static final String FILE_NAME_PATTERN = "MM-dd-yyyy";
    private static final String FILE_EXTENSION = ".csv";
    private static final char FILE_SEPARATOR = ',';
    private GsaidHeaderMap header;
    private Map<String, Item> locations = new HashMap<>();
    private Map<String, List<String>> locationDistributionIds = new HashMap<>();

    public GisaidCsvConverter(ItemWriter writer, Model model) {
        super(writer, model, "GISAID ", "Data set by Johns Hopkins CSSE");
    }

    @Override
    public void process(File dataDir) throws Exception {
        if (dataDir.isDirectory()) {
            for (File dailyReport : dataDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return s.toLowerCase().endsWith(FILE_EXTENSION);
                }
            })) {
                storeDistributions(dailyReport);
            }
            storeGeoLocations();
        }
    }

    private void storeDistributions(File dailyReportFile) {
        String dailyReportFileName = dailyReportFile.getName();
        System.out.println("Processing " + dailyReportFileName);

        String[] drLine = null;
        CSVReader reader = null;
        String dateAsString = dailyReportFileName.substring(0,
                dailyReportFileName.indexOf(".csv"));
        try {
            reader = new CSVReader(new FileReader(dailyReportFile.getAbsolutePath()),
                    FILE_SEPARATOR);
            header = new GsaidHeaderMap(reader.readNext());
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
        String confirmed = getFieldValue(Header.CONFIRMED, dailyReport);
        distribution.setAttributeIfNotNull("totalConfirmed", confirmed);
        String deaths = getFieldValue(Header.DEATHS, dailyReport);
        distribution.setAttributeIfNotNull("totalDeaths", deaths);
        String recovered = getFieldValue(Header.RECOVERED, dailyReport);
        distribution.setAttributeIfNotNull("totalRecovered", recovered);
        String active = getFieldValue(Header.ACTIVE, dailyReport);
        if (active.equals(StringUtils.EMPTY) || "0".equals(active)) {
            active = calculateActive(confirmed, recovered, deaths);
        }
        distribution.setAttribute("totalActive", active);
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
                geoLocation.setCollection("distributions",
                        locationDistributionIds.get(locationKey));
                store(geoLocation);
            }
        } catch (ObjectStoreException e) {
            e.printStackTrace();
        }
    }

    private String getFieldValue(Header label, String[] fields) {
        int pos;
        pos = header.getPosition(label);
        if (pos != -1) {
            return fields[pos];
        }
        return StringUtils.EMPTY;
    }

    private String calculateActive(String confirmed, String recovered, String deaths) {
        int active = 0;
        if (!confirmed.isEmpty()) {
            active = Integer.parseInt(confirmed);
            if (active == 0) {
                return Integer.toString(active);
            }
        }
        if (!recovered.isEmpty()) {
            active = active - Integer.parseInt(recovered);
        }
        if (!deaths.isEmpty()) {
            active = active - Integer.parseInt(deaths);
        }
        if (active < 0) {
            active = 0;
        }
        return Integer.toString(active);
    }

    private class GeoLocation {
        String latitude;
        String longitude;
        String province;
        String state;
        String country;
        String locationKey;

        public GeoLocation(String[] fields) {
            latitude = getFieldValue(Header.LATITUDE, fields);
            longitude = getFieldValue(Header.LONGITUDE, fields);
            province = getFieldValue(Header.PROVINCE, fields);
            state = getFieldValue(Header.STATE, fields);
            country = getFieldValue(Header.COUNTRY, fields);
            locationKey = latitude + longitude + province + state + country;
            locationKey = StringUtils.deleteWhitespace(locationKey);
        }
    }





















}
