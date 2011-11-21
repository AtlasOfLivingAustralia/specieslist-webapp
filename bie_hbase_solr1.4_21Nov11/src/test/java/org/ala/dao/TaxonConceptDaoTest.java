package org.ala.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.ala.dto.ExtendedTaxonConceptDTO;
import org.ala.dto.SearchTaxonConceptDTO;
import org.ala.model.CommonName;
import org.ala.model.ConservationStatus;
import org.ala.model.ExtantStatus;
import org.ala.model.Habitat;
import org.ala.model.Image;
import org.ala.model.PestStatus;
import org.ala.model.OccurrencesInGeoregion;
import org.ala.model.SimpleProperty;
import org.ala.model.TaxonConcept;
import org.ala.model.TaxonName;
import org.ala.util.SpringUtils;

public class TaxonConceptDaoTest extends TestCase {
	
	private final String TEST_TCDAO_GUID = "urn:lsid:afd:taxon:123";

	public void testGetPage() throws Exception {
		TaxonConceptDao tcDao = initTaxonConceptDao();
		//page through DB
		List<ExtendedTaxonConceptDTO> tcDTOs = tcDao.getPage("urn:lsid:biodiversity.org.au:afd.taxon:aa745ff0-c776-4d0e-851d-369ba0e6f537", 100);
		for(ExtendedTaxonConceptDTO e: tcDTOs){
			if(e.getTaxonConcept()!=null){
				System.out.println("GUID: "+e.getTaxonConcept().getGuid()+", ScientificName: "+e.getTaxonConcept().getNameString());
			} else {
				System.out.println("NULL Taxon concept");
			}
		}
	}
	
	public void testIsIconic() throws Exception {
		
		TaxonConceptDao tcDao = initTaxonConceptDao();
		boolean success = tcDao.setIsIconic("urn:lsid:biodiversity.org.au:afd.taxon:aa745ff0-c776-4d0e-851d-369ba0e6f537");
		System.out.println("Success: "+success);
		boolean isIconic = tcDao.isIconic("urn:lsid:biodiversity.org.au:afd.taxon:aa745ff0-c776-4d0e-851d-369ba0e6f537");
		System.out.println("Is iconic: "+isIconic);
		isIconic = tcDao.isIconic("urn:lsid:catalogueoflife.org:taxon:24e7d624-60a7-102d-be47-00304854f810:ac2010");
		System.out.println("Is iconic: "+isIconic);
		
	}
	
	public void testAddSynonym() throws Exception {

		TaxonConceptDao tcDao = initTaxonConceptDao();

		TaxonConcept synonym1 = new TaxonConcept();
		synonym1.setGuid("urn:lsid:afd:taxon:124");
		synonym1.setNameString("Sarcophilus satanius");
		tcDao.addSynonym(TEST_TCDAO_GUID, synonym1);

		List<TaxonConcept> synonyms = tcDao.getSynonymsFor(TEST_TCDAO_GUID);

		assertEquals(synonyms.size(), 1);
		assertEquals(synonyms.get(0).getGuid(), "urn:lsid:afd:taxon:124");
		assertEquals(synonyms.get(0).getNameString(), "Sarcophilus satanius");

		TaxonConcept synonym2 = new TaxonConcept();
		synonym2.setGuid("urn:lsid:afd:taxon:125");
		synonym2.setNameString("Sarcophilus laniarius");
		tcDao.addSynonym(TEST_TCDAO_GUID, synonym2);		

		//refresh local
		synonyms = tcDao.getSynonymsFor(TEST_TCDAO_GUID);

		//sort order should place Sarcophilus satanius below
		assertEquals(synonyms.size(), 2);
		assertEquals(synonyms.get(1).getGuid(), "urn:lsid:afd:taxon:124");
		assertEquals(synonyms.get(1).getNameString(), "Sarcophilus satanius");

		//clear up after test
		tcDao.delete(TEST_TCDAO_GUID);
	}

	public void testAddImages() throws Exception {
		TaxonConceptDao tcDao = initTaxonConceptDao();
		
		List<Image> expectedImgList = new ArrayList<Image>();

		Image image1 = new Image();
		//		Image image2 = new Image();

		image1.setGuid("urn:lsid:afd:taxon:125");
		//		image2.setGuid("urn:lsid:afd:taxon:126");

		image1.setTitle("Test1");
		//		image2.setTitle("Test2");

		image1.setIdentifier("T ID1");
		//		image2.setIdentifier("T ID2");

		expectedImgList.add(image1);
		//		expectedImgList.add(image2);

		tcDao.addImage(TEST_TCDAO_GUID, image1);
		//		tcDao.addImage(TEST_TCDAO_GUID, image2);

		List<Image> imageList = tcDao.getImages(TEST_TCDAO_GUID);

		for (Image image : imageList) {
			assertEquals(image.getGuid(), "urn:lsid:afd:taxon:125");
			assertEquals(image.getTitle(), "Test1");
			assertEquals(image.getIdentifier(), "T ID1");
		}
		
		tcDao.delete(TEST_TCDAO_GUID);
	}

	public void testAddTaxonName() throws Exception {
		TaxonConceptDao tcDao = initTaxonConceptDao();
		
		TaxonName taxonName = new TaxonName();
		
		taxonName.setGuid("urn:lsid:afd:taxon:125");
		taxonName.setNameComplete("Test Taxon Name");
		
		tcDao.addTaxonName(TEST_TCDAO_GUID, taxonName);
		
		TaxonName tName = tcDao.getTaxonNameFor(TEST_TCDAO_GUID);
		
		assertEquals(tName.getGuid(), "urn:lsid:afd:taxon:125");
		assertEquals(tName.getNameComplete(), "Test Taxon Name");
		
		tcDao.delete(TEST_TCDAO_GUID);
	}
	
	public void testAddCommonName() throws Exception {
		TaxonConceptDao tcDao = initTaxonConceptDao();
		
		CommonName commonName = new CommonName();
		
		commonName.setGuid("urn:lsid:afd:taxon:125");
		commonName.setNameString("Test Common Name");
		
		tcDao.addCommonName(TEST_TCDAO_GUID, commonName);
		
		List<CommonName> cNameList = tcDao.getCommonNamesFor(TEST_TCDAO_GUID);
		
		for (CommonName cName : cNameList) {
			assertEquals(cName.getGuid(), "urn:lsid:afd:taxon:125");
			assertEquals(cName.getNameString(), "Test Common Name");
		}
		
		tcDao.delete(TEST_TCDAO_GUID);
	}
	
	public void testAddConservationStatus() throws Exception {
		TaxonConceptDao tcDao = initTaxonConceptDao();
		
		ConservationStatus conservationStatus = new ConservationStatus();
		
		conservationStatus.setStatus("Test status");
		conservationStatus.setSystem("Test system");
		conservationStatus.setRegion("Test region");
		
		tcDao.addConservationStatus(TEST_TCDAO_GUID, conservationStatus);
		
		List<ConservationStatus> conStatList = tcDao.getConservationStatuses(TEST_TCDAO_GUID);
		
		for (ConservationStatus conStat : conStatList) {
			assertEquals(conStat.getStatus(), "Test status");
			assertEquals(conStat.getSystem(), "Test system");
			assertEquals(conStat.getRegion(), "Test region");
		}
		
		tcDao.delete(TEST_TCDAO_GUID);
	}
	
	public void testAddPestStatus() throws Exception {
		TaxonConceptDao tcDao = initTaxonConceptDao();
		
		PestStatus pestStatus = new PestStatus();
		
		pestStatus.setStatus("Test status");
		pestStatus.setRegion("Test region");
		
		tcDao.addPestStatus(TEST_TCDAO_GUID, pestStatus);
		
		List<PestStatus> pestStatList = tcDao.getPestStatuses(TEST_TCDAO_GUID);
		
		for (PestStatus pestStat : pestStatList) {
			assertEquals(pestStat.getStatus(), "Test status");
			assertEquals(pestStat.getRegion(), "Test region");
		}
		
		tcDao.delete(TEST_TCDAO_GUID);
	}
	
	public void testAddExtantStatus() throws Exception {
		TaxonConceptDao tcDao = initTaxonConceptDao();
		
		List<ExtantStatus> extantStatusList = new ArrayList<ExtantStatus>();
		
		ExtantStatus extantStatus = new ExtantStatus();
		
		extantStatus.setStatus("Test status");
		extantStatusList.add(extantStatus);
		
		tcDao.addExtantStatus(TEST_TCDAO_GUID, extantStatusList);
		
		List<ExtantStatus> extStatList = tcDao.getExtantStatuses(TEST_TCDAO_GUID);
		
		for (ExtantStatus extStat : extStatList) {
			assertEquals(extStat.getStatus(), "Test status");
		}
		
		tcDao.delete(TEST_TCDAO_GUID);
	}
		
	public void testAddHabitat() throws Exception {
		TaxonConceptDao tcDao = initTaxonConceptDao();
		
		List<Habitat> habitatList = new ArrayList<Habitat>();
		
		Habitat habitat = new Habitat();
		
		habitat.setStatus("Test status");
		habitatList.add(habitat);
		
		tcDao.addHabitat(TEST_TCDAO_GUID, habitatList);
		
		List<Habitat> habList = tcDao.getHabitats(TEST_TCDAO_GUID);
		
		for (Habitat hab : habList) {
			assertEquals(hab.getStatus(), "Test status");
		}
		
		tcDao.delete(TEST_TCDAO_GUID);
	}

	public void testAddRegions() throws Exception {
		TaxonConceptDao tcDao = initTaxonConceptDao();
		
		List<OccurrencesInGeoregion> regionList = new ArrayList<OccurrencesInGeoregion>();
		
		OccurrencesInGeoregion region = new OccurrencesInGeoregion();
		
		region.setOccurrences(1);
		region.setRegionId("R ID");
		region.setRegionName("R Name");
		region.setRegionType("R Type");
		region.setTaxonId(TEST_TCDAO_GUID);
		
		regionList.add(region);
		
		tcDao.addRegions(TEST_TCDAO_GUID, regionList);
		
		List<OccurrencesInGeoregion> rList = tcDao.getRegions(TEST_TCDAO_GUID);
		
		for (OccurrencesInGeoregion r : rList) {
			assertEquals(r.getOccurrences(), 1);
			assertEquals(r.getRegionId(), "R ID");
			assertEquals(r.getRegionName(), "R Name");
			assertEquals(r.getRegionType(), "R Type");
			assertEquals(r.getTaxonId(), TEST_TCDAO_GUID);			
		}
		
		tcDao.delete(TEST_TCDAO_GUID);
	}
	
	public void testAddChildTaxon() throws Exception {
		TaxonConceptDao tcDao = initTaxonConceptDao();
		
		TaxonConcept childTaxon = new TaxonConcept();
		childTaxon.setGuid("urn:lsid:afd:taxon:133");
		childTaxon.setNameString("Sarcophilus Child");
		
		tcDao.addChildTaxon(TEST_TCDAO_GUID, childTaxon);
		
		List<TaxonConcept> cTaxonList = tcDao.getChildConceptsFor(TEST_TCDAO_GUID);
		
		for (TaxonConcept tc : cTaxonList) {
			assertEquals(tc.getGuid(), "urn:lsid:afd:taxon:133");
			assertEquals(tc.getNameString(), "Sarcophilus Child");
		}
		
		tcDao.delete(TEST_TCDAO_GUID);
	}
	
	public void testAddParentTaxon() throws Exception {
		TaxonConceptDao tcDao = initTaxonConceptDao();
		
		TaxonConcept parentTaxon = new TaxonConcept();
		parentTaxon.setGuid("urn:lsid:afd:taxon:133");
		parentTaxon.setNameString("Sarcophilus Parent");
		
		tcDao.addParentTaxon(TEST_TCDAO_GUID, parentTaxon);
		
		List<TaxonConcept> pTaxonList = tcDao.getParentConceptsFor(TEST_TCDAO_GUID);
		
		for (TaxonConcept tc : pTaxonList) {
			assertEquals(tc.getGuid(), "urn:lsid:afd:taxon:133");
			assertEquals(tc.getNameString(), "Sarcophilus Parent");
		}
		
		tcDao.delete(TEST_TCDAO_GUID);
	}
		
	public void testAddTextProperty() throws Exception {
		TaxonConceptDao tcDao = initTaxonConceptDao();
		
		SimpleProperty simpleProperty = new SimpleProperty();
		simpleProperty.setName("Simple property");
		simpleProperty.setValue("Simple value");
		simpleProperty.setTitle("Simple title");
		simpleProperty.setIdentifier("Simple id");
		
		tcDao.addTextProperty(TEST_TCDAO_GUID, simpleProperty);
		
		List<SimpleProperty> sPropertyList = tcDao.getTextPropertiesFor(TEST_TCDAO_GUID);
		
		for (SimpleProperty sp : sPropertyList) {
			assertEquals(sp.getName(), "Simple property");
			assertEquals(sp.getValue(), "Simple value");
			assertEquals(sp.getTitle(), "Simple title");
			assertEquals(sp.getIdentifier(), "Simple id");
		}
		
		tcDao.delete(TEST_TCDAO_GUID);
	}

	public void testGetByGuid() throws Exception {
		TaxonConceptDao tcDao = initTaxonConceptDao();
		
		TaxonConcept tc = tcDao.getByGuid(TEST_TCDAO_GUID);
		
		assertEquals(tc.getGuid(), TEST_TCDAO_GUID);
		assertEquals(tc.getNameString(), "Sarcophilus harrisii");
		
		tcDao.delete(TEST_TCDAO_GUID);
	}
		
	public void testGetExtendedTaxonConceptByGuid() throws Exception {
		TaxonConceptDao tcDao = initTaxonConceptDao();
		
		ExtendedTaxonConceptDTO etc = new ExtendedTaxonConceptDTO();
		
		TaxonConcept childTaxon = new TaxonConcept();
		childTaxon.setGuid("urn:lsid:afd:taxon:133");
		childTaxon.setNameString("Sarcophilus Child");
		
		tcDao.addChildTaxon(TEST_TCDAO_GUID, childTaxon);
		
		SimpleProperty simpleProperty = new SimpleProperty();
		simpleProperty.setName("Simple property");
		simpleProperty.setValue("Simple value");
		simpleProperty.setTitle("Simple title");
		simpleProperty.setIdentifier("Simple id");
		
		tcDao.addTextProperty(TEST_TCDAO_GUID, simpleProperty);
		
		TaxonConcept parentTaxon = new TaxonConcept();
		parentTaxon.setGuid("urn:lsid:afd:taxon:134");
		parentTaxon.setNameString("Sarcophilus Parent");
		
		tcDao.addParentTaxon(TEST_TCDAO_GUID, parentTaxon);
		
		TaxonConcept synonym1 = new TaxonConcept();
		synonym1.setGuid("urn:lsid:afd:taxon:124");
		synonym1.setNameString("Sarcophilus satanius");
		
		tcDao.addSynonym(TEST_TCDAO_GUID, synonym1);
		
		Image image1 = new Image();
		image1.setGuid("urn:lsid:afd:taxon:125");
		image1.setTitle("Test1");
		image1.setIdentifier("T ID1");

		tcDao.addImage(TEST_TCDAO_GUID, image1);
		
		TaxonName taxonName = new TaxonName();
		taxonName.setGuid("urn:lsid:afd:taxon:125");
		taxonName.setNameComplete("Test Taxon Name");
		
		tcDao.addTaxonName(TEST_TCDAO_GUID, taxonName);
		
		CommonName commonName = new CommonName();
		commonName.setGuid("urn:lsid:afd:taxon:125");
		commonName.setNameString("Test Common Name");
		
		tcDao.addCommonName(TEST_TCDAO_GUID, commonName);
		
		ConservationStatus conservationStatus = new ConservationStatus();
		conservationStatus.setStatus("Test status");
		conservationStatus.setSystem("Test system");
		conservationStatus.setRegion("Test region");
		
		tcDao.addConservationStatus(TEST_TCDAO_GUID, conservationStatus);
		
		PestStatus pestStatus = new PestStatus();
		pestStatus.setStatus("Test status");
		pestStatus.setRegion("Test region");
		
		tcDao.addPestStatus(TEST_TCDAO_GUID, pestStatus);

		List<ExtantStatus> extantStatusList = new ArrayList<ExtantStatus>();		
		ExtantStatus extantStatus = new ExtantStatus();
		extantStatus.setStatus("Test status");
		extantStatusList.add(extantStatus);
		
		tcDao.addExtantStatus(TEST_TCDAO_GUID, extantStatusList);
		
		List<Habitat> habitatList = new ArrayList<Habitat>();
		Habitat habitat = new Habitat();
		habitat.setStatus("Test status");
		habitatList.add(habitat);
		
		tcDao.addHabitat(TEST_TCDAO_GUID, habitatList);
		
		List<OccurrencesInGeoregion> regionList = new ArrayList<OccurrencesInGeoregion>();
		OccurrencesInGeoregion region = new OccurrencesInGeoregion();
		region.setOccurrences(1);
		region.setRegionId("R ID");
		region.setRegionName("R Name");
		region.setRegionType("R Type");
		region.setTaxonId(TEST_TCDAO_GUID);
		regionList.add(region);
		
		tcDao.addRegions(TEST_TCDAO_GUID, regionList);
		
		etc = tcDao.getExtendedTaxonConceptByGuid(TEST_TCDAO_GUID);
		
		assertEquals(etc.getChildConcepts().get(0).getGuid(), "urn:lsid:afd:taxon:133");
		assertEquals(etc.getChildConcepts().get(0).getNameString(), "Sarcophilus Child");
		
		assertEquals(etc.getSimpleProperties().get(0).getName(), "Simple property");
		assertEquals(etc.getSimpleProperties().get(0).getValue(), "Simple value");
		assertEquals(etc.getSimpleProperties().get(0).getTitle(), "Simple title");
		assertEquals(etc.getSimpleProperties().get(0).getIdentifier(), "Simple id");
		
		assertEquals(etc.getParentConcepts().get(0).getGuid(), "urn:lsid:afd:taxon:134");
		assertEquals(etc.getParentConcepts().get(0).getNameString(), "Sarcophilus Parent");
		
		assertEquals(etc.getSynonyms().get(0).getGuid(), "urn:lsid:afd:taxon:124");
		assertEquals(etc.getSynonyms().get(0).getNameString(), "Sarcophilus satanius");
		
		assertEquals(etc.getImages().get(0).getGuid(), "urn:lsid:afd:taxon:125");
		assertEquals(etc.getImages().get(0).getTitle(), "Test1");
		assertEquals(etc.getImages().get(0).getIdentifier(), "T ID1");
		
		assertEquals(etc.getTaxonName().getGuid(), "urn:lsid:afd:taxon:125");
		assertEquals(etc.getTaxonName().getNameComplete(), "Test Taxon Name");
		
		assertEquals(etc.getCommonNames().get(0).getGuid(), "urn:lsid:afd:taxon:125");
		assertEquals(etc.getCommonNames().get(0).getNameString(), "Test Common Name");
		
		assertEquals(etc.getConservationStatuses().get(0).getStatus(), "Test status");
		assertEquals(etc.getConservationStatuses().get(0).getSystem(), "Test system");
		assertEquals(etc.getConservationStatuses().get(0).getRegion(), "Test region");
		
		assertEquals(etc.getPestStatuses().get(0).getRegion(), "Test region");
		assertEquals(etc.getPestStatuses().get(0).getStatus(), "Test status");
		
		assertEquals(etc.getExtantStatusus().get(0).getStatus(), "Test status");
		
		assertEquals(etc.getHabitats().get(0).getStatus(), "Test status");
		
		tcDao.delete(TEST_TCDAO_GUID);
	}
		
	public void testFindByScientificName() throws Exception {
		TaxonConceptDao tcDao = initTaxonConceptDao();
		
		List<SearchTaxonConceptDTO> sr = tcDao.findByScientificName("Sarcophilus harrisii", 10);
		
		assertEquals(sr.get(0).getName(), "Sarcophilus harrisii");
		
		tcDao.delete(TEST_TCDAO_GUID);
	}
	
	private TaxonConceptDao initTaxonConceptDao() throws Exception {
		TaxonConceptDao tcDao = SpringUtils.getContext().getBean(TaxonConceptDao.class);

		tcDao.delete(TEST_TCDAO_GUID);
		
		TaxonConcept tc = new TaxonConcept();
		tc.setGuid(TEST_TCDAO_GUID);
		tc.setNameString("Sarcophilus harrisii");
		tcDao.create(tc);
		
		return tcDao;
	}
}
