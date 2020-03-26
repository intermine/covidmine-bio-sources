package org.intermine.bio.dataconversion;

/*
 * Copyright (C) 2002-2019 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.BufferedReader;
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
        System.out.println("dateAsString:" + dateAsString);
        try {
            reader = new CSVReader(new FileReader(dailyReportFile.getAbsolutePath()),',');
            reader.readNext();//header
            while ((drLine = reader.readNext()) != null) {
                createDailyReport(drLine, dateAsString);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Problem reading file " + dailyReportFileName);
        }
    }

    private void createDailyReport(String[] dailyReport, String dateAsString) {
        Item countryItem = createItem("Country");
        Item dailyReportItem = createItem("DailyReport");
        countryItem.setAttribute("name", dailyReport[3]);
        countryItem.setAttributeIfNotNull("province_state", dailyReport[2]);
        countryItem.setAttributeIfNotNull("latitude", dailyReport[5]);
        countryItem.setAttributeIfNotNull("longitude", dailyReport[6]);
        Date date = convertDate(dateAsString);

        
        dailyReportItem.setAttributeIfNotNull("date", Long.toString(date.getTime()));
        dailyReportItem.setAttributeIfNotNull("confirmed", dailyReport[7]);
        dailyReportItem.setAttributeIfNotNull("deaths", dailyReport[8]);
        dailyReportItem.setAttributeIfNotNull("recovered", dailyReport[9]);
        try {
            store(dailyReportItem);
            List<String> ids = Arrays.asList(dailyReportItem.getIdentifier());
            countryItem.setCollection("dailyReports", ids);
            store(countryItem);
        } catch (ObjectStoreException e) {
            e.printStackTrace();
        }
    }

    private Date convertDate(String dateAsString) {
        String pattern = "MM-dd-yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date date = null;
        try {
            date = simpleDateFormat.parse(dateAsString);
            System.out.println("in convertDate method: " + date.toString());
            return date;
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
