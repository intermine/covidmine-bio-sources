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

import org.apache.log4j.Logger;
import org.biojava.nbio.core.sequence.template.Sequence;
import org.intermine.metadata.Model;
import org.intermine.model.bio.BioEntity;
import org.intermine.model.bio.DataSet;
import org.intermine.model.bio.GeoLocation;
import org.intermine.model.bio.Organism;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;

import java.util.HashMap;
import java.util.Map;


/**
 * See https://intermine.readthedocs.io/en/latest/database/data-sources/library/fasta/
 * for details on the FASTA source.
 * @author
 */
public class NcbiCovidFastaConverter extends FastaLoaderTask
{
    protected static final Logger LOG = Logger.getLogger(NcbiCovidFastaConverter.class);
    private static final String REFSEQ = "refseq";
    private Map<String, GeoLocation> geoMap = new HashMap<String, GeoLocation>();

    /**
     * Return a Chromosome object for the given item.
     * @param country the id
     * @param organism the Organism to reference from the Chromosome
     * @return the Chromosome
     * @throws ObjectStoreException if problem fetching Chromosome
     */
    protected GeoLocation getGeoLocation(String country, Organism organism)
            throws ObjectStoreException {
        if (geoMap.containsKey(country)) {
            return geoMap.get(country);
        }
        GeoLocation gLoc = getDirectDataLoader().createObject(GeoLocation.class);
        gLoc.setFieldValue("country",country);
//        gLoc.setOrganism(organism);
//        gLoc.addDataSets(getDataSet());
        getDirectDataLoader().store(gLoc);
        geoMap.put(country, gLoc);
        return gLoc;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void extraProcessing(Sequence bioJavaSequence,
            org.intermine.model.bio.Sequence flymineSequence,
            BioEntity bioEntity, Organism organism,
            DataSet dataSet)
        throws ObjectStoreException {
        String header = bioJavaSequence.getAccession().getID();

        String seqIdentifier = null;
        String country = null;
        String isComplete= "N/A";
        String isRef = "N";

        Map<String, String[]> recordMap = new HashMap<String, String[]>();

        // for the reference seq
        // >NC_045512 |China|refseq| complete
        // for the rest
        // >MT123291 |China|complete

        String[] headerSubStrings = header.split("\\|");
        int i =0;
        for (String token : headerSubStrings ) {
//            LOG.info("XXXX [" + i + "] ->" + token + "<-");
            if (i == 0) {
                seqIdentifier = token.trim();
            }
            if (i == 1) {
                country = token;
            }
            if (i == 2) {
                if (token.contains(REFSEQ)) {
                    isRef = "Y";
                    isComplete = "Y";
                    continue;
                } else {
                    isComplete = "Y";
                }
            }
            i++;
        }

        ObjectStore os = getIntegrationWriter().getObjectStore();
            Model model = os.getModel();
//            if (null == seqIdentifier) {
//                continue;
//            }

//            InterMineObject strain = setStrain(seqIdentifier, geoLocation, isRef, isComplete,
//                    organism, model);

        LOG.info("YYY " + seqIdentifier + "|" + isRef + "|" + isComplete);


        bioEntity.setFieldValue("referenceSequence", isRef);
        bioEntity.setFieldValue("nucleotideCompleteness", isComplete);

        GeoLocation  geoLocation = getGeoLocation(country, organism);
        bioEntity.setFieldValue("geoLocation", geoLocation);

            //            if (strain != null) {
//                Set<? extends InterMineObject> mrnas = new HashSet(Collections.singleton(strain));
//                bioEntity.setFieldValue("transcripts", mrnas);
//            }

    }

}

