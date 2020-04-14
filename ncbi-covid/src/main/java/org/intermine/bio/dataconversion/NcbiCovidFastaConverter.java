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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.biojava.nbio.core.sequence.template.Sequence;
import org.intermine.metadata.Model;
import org.intermine.model.FastPathObject;
import org.intermine.model.InterMineObject;
import org.intermine.model.bio.BioEntity;
import org.intermine.model.bio.Chromosome;
import org.intermine.model.bio.DataSet;
import org.intermine.model.bio.Location;
import org.intermine.model.bio.Organism;
import org.intermine.model.bio.SequenceFeature;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.DynamicUtil;
import org.intermine.bio.dataconversion.FastaLoaderTask;


/**
 * See https://intermine.readthedocs.io/en/latest/database/data-sources/library/fasta/
 * for details on the FASTA source.
 * @author
 */
public class NcbiCovidFastaConverter extends FastaLoaderTask
{
    protected static final Logger LOG = Logger.getLogger(NcbiCovidFastaConverter.class);
    //private static final String ORG_HEADER = " Homo sapiens ";
    private static final String REFSEQ = "refseq";

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
        String geoLocation = null;
        String isComplete= "N/A";
        String isRef = "N";

        // for the reference seq
        // >NC_045512 |China|refseq| complete
        // for the rest
        // >MT123291 |China|complete

        String[] headerSubStrings = header.split("\\|");
        int i =0;
        for (String token : headerSubStrings ) {
            if (i == 0) {
                seqIdentifier = token;
            }
            if (i == 1) {
                geoLocation = token;
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


            ObjectStore os = getIntegrationWriter().getObjectStore();
            Model model = os.getModel();
//        if (model.hasClassDescriptor(model.getPackageName() + ".Region)) {
//            Class<? extends FastPathObject> cdsCls =
//                    model.getClassDescriptorByName("UTR").getType();
//            if (!DynamicUtil.isInstance(bioEntity, cdsCls)) {
//                throw new RuntimeException("the InterMineObject passed to "
//                        + "FlyBaseUTRFastaDataLoaderTask.extraProcessing() is not a "
//                        + "UTR: " + bioEntity);
//            }
            
            if (null == seqIdentifier) {
                continue;
            }
//            InterMineObject strain = setStrain(seqIdentifier, geoLocation, isRef, isComplete,
//                    organism, model);

            InterMineObject region = setRegion(seqIdentifier, geoLocation, isRef, isComplete,
                    organism, model);

            //            if (strain != null) {
//                Set<? extends InterMineObject> mrnas = new HashSet(Collections.singleton(strain));
//                bioEntity.setFieldValue("transcripts", mrnas);
//            }
        }
    }



    /**
     * Create a Region with the given primaryIdentifier and organism or return null if Region is not in
     * the data model.
     * @param seqIdentifier primaryIdentifier of Region to create
     * @param geoLocation
     * @param isRef
     * @param isComplete
     * @param organism orgnism of Region to create
     * @param model the data model
     * @return an InterMineObject representing a Region or null if Region not in the data model
     * @throws ObjectStoreException if problem storing
     */
    private InterMineObject setStrain(String seqIdentifier, String geoLocation, String isRef,
                                      String isComplete, Organism organism, Model model)
            throws ObjectStoreException {
        InterMineObject strain = null;
        if (model.hasClassDescriptor(model.getPackageName() + ".Strain")) {
            @SuppressWarnings("unchecked") Class<? extends InterMineObject> strainCls =
                    (Class<? extends InterMineObject>) model.getClassDescriptorByName("Strain").getType();
            strain = getDirectDataLoader().createObject(strainCls);
            strain.setFieldValue("primaryIdentifier", seqIdentifier);
            strain.setFieldValue("referenceSequence", isRef);
            strain.setFieldValue("nucleotideCompleteness", isComplete);
            strain.setFieldValue("organism", organism);

            getDirectDataLoader().store(strain);
        }
        return strain;
    }

    /**
     * Create a Region with the given primaryIdentifier and organism or return null if Region is not in
     * the data model.
     * @param seqIdentifier primaryIdentifier of Region to create
     * @param geoLocation
     * @param isRef
     * @param isComplete
     * @param organism orgnism of Region to create
     * @param model the data model
     * @return an InterMineObject representing a Region or null if Region not in the data model
     * @throws ObjectStoreException if problem storing
     */
    private InterMineObject setRegion(String seqIdentifier, String geoLocation, String isRef,
                                      String isComplete, Organism organism, Model model)
            throws ObjectStoreException {
        InterMineObject region = null;
        if (model.hasClassDescriptor(model.getPackageName() + ".Strain")) {
            @SuppressWarnings("unchecked") Class<? extends InterMineObject> strainCls =
                    (Class<? extends InterMineObject>) model.getClassDescriptorByName("Strain").getType();
            region = getDirectDataLoader().createObject(strainCls);
            region.setFieldValue("primaryIdentifier", seqIdentifier);
            region.setFieldValue("referenceSequence", isRef);
            region.setFieldValue("nucleotideCompleteness", isComplete);
            region.setFieldValue("organism", organism);

            getDirectDataLoader().store(region);
        }
        return region;
    }



}

