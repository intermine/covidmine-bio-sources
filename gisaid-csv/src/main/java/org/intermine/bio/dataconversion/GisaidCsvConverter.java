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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
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
    public GisaidCsvConverter(ItemWriter writer, Model model) {
        super(writer, model);
    }

    @Override
    public void process(File dataDir) throws Exception {
        if (dataDir.isDirectory()) {
            for (File dailyReport : dataDir.listFiles()) {
                processFile(dailyReport);
            }
        }
    }

    private void processFile(File dailyReportFile) {
        System.out.println(dailyReportFile.getName());
        String dailyReportFileName = dailyReportFile.getName();
        if (!dailyReportFileName.equals("03-24-2020.csv")) {
            return;//justone file for now
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
                createDistribution(drLine, dateAsString);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Problem reading file " + dailyReportFileName);
        }
    }

    private void createDistribution(String[] dailyReport, String dateAsString) {
        Item geoLocation = createItem("GeoLocation");
        geoLocation.setAttributeIfNotNull("latitude", dailyReport[5]);
        geoLocation.setAttributeIfNotNull("longitude", dailyReport[6]);
        geoLocation.setAttributeIfNotNull("province", dailyReport[1]);
        geoLocation.setAttributeIfNotNull("state", dailyReport[2]);
        geoLocation.setAttributeIfNotNull("country", dailyReport[3]);

        Item distribution = createItem("Distribution");
        Date date = convertDate(dateAsString);
        distribution.setAttributeIfNotNull("date", Long.toString(date.getTime()));
        distribution.setAttributeIfNotNull("confirmed", dailyReport[7]);
        distribution.setAttributeIfNotNull("deaths", dailyReport[8]);
        distribution.setAttributeIfNotNull("recovered", dailyReport[9]);
        try {
            distribution.setReference("geoLocation", geoLocation);
            store(distribution);
            List<String> distributionIds = Arrays.asList(distribution.getIdentifier());
            geoLocation.setCollection("distributions", distributionIds);
            store(geoLocation);
        } catch (ObjectStoreException e) {
            e.printStackTrace();
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
}
