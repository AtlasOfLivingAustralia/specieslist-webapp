package org.ala.util;

import au.com.bytecode.opencsv.CSVReader;
import org.ala.dao.TaxonConceptDao;
import org.ala.dto.ExtendedTaxonConceptDTO;
import org.ala.model.SimpleProperty;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.FileReader;
import java.util.*;

@Component("deduplicationUtil")
public class DeduplicationUtil {

	@Inject
	protected TaxonConceptDao taxonConceptDao;

    public static void main(String[] args) throws Exception {

        String infosourceUid = "dr395";
        String fileOfIDs = "/data/plantNET.csv";
        CSVReader reader = new CSVReader(new FileReader(fileOfIDs));
		DeduplicationUtil ddu = SpringUtils.getContext().getBean(DeduplicationUtil.class);
        String[] row = reader.readNext();
        while(row != null){
            String guid = row[0];
            System.out.println(guid);
            ExtendedTaxonConceptDTO tdto = ddu.taxonConceptDao.getExtendedTaxonConceptByGuid(guid);
            if(tdto.getTaxonConcept() != null){
                List<SimpleProperty> simplePropertyList = tdto.getSimpleProperties();
                List<SimpleProperty> forResource = new ArrayList<SimpleProperty>();
                for(SimpleProperty sp: simplePropertyList){
                    if(infosourceUid.equals(sp.getInfoSourceUid())) forResource.add(sp);
                }

                if(!forResource.isEmpty()){
                    simplePropertyList.removeAll(forResource);
                    simplePropertyList.add(forResource.get(0));
                    ddu.taxonConceptDao.setTextPropertiesFor(guid,simplePropertyList);
                }
            }
            row = reader.readNext();
        }
        reader.close();
	}

	/**
	 * @param taxonConceptDao the taxonConceptDao to set
	 */
	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}
}


