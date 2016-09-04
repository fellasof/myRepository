package com.lchclearnet.cds.services.psd;

import java.util.List;

import com.lchclearnet.cds.domain.psd.entity.PsdContract;
import com.lchclearnet.cds.domain.psd.entity.PsdProduct;
import com.lchclearnet.cds.domain.psd.entity.PsdReferenceEntity;
import com.lchclearnet.cds.domain.psd.entity.PsdReferenceObligationPair;
import com.lchclearnet.cds.domain.psd.entity.PsdSroAssocOblig;
import com.lchclearnet.cds.ref.service.marketdata.CDSContract;
import com.lchclearnet.cds.ref.service.marketdata.CDSIndexConstituent;
import com.lchclearnet.cds.ref.service.marketdata.CDSProduct;
import com.lchclearnet.cds.ref.service.marketdata.CDSReferenceEntity;
import com.lchclearnet.cds.ref.service.marketdata.CDSReferenceObligationPairs;

/** 
 * [SN-VaR] [2156] [22 Aout 2013] 
 */
/**
 * Integrate Reference data coming from MDM
 * 
 * @author Toscane.dev.team
 * 
 */
public interface ReferenceDataMessagingWrapperService {

	/**
	 * Transform CDSReferenceEntity to PsdReferenceEntity
	 * 
	 * @param xmlReferEntitiesList
	 *            List of Reference Entities from XML file
	 * @return a list of PsdReferenceEntity
	 */
	List<PsdReferenceEntity> wrapReferenceEntity(List<CDSReferenceEntity> xmlReferEntitiesList);

	/**
	 * Transform CDSReferenceObligationPairs object to list of PsdReferenceObligationPair
	 * 
	 * @param refObPairsXmlObject
	 *            Object containing all Reference Obligation Pairs from XML file
	 * @param listReferenceEntity
	 * @return a list of PsdReferenceObligationPair
	 */
	List<PsdReferenceObligationPair> wrapReferenceObligPair(CDSReferenceObligationPairs refObPairsXmlObject, List<PsdReferenceEntity> listReferenceEntity);

	/**
	 * Complete the PsdReferenceObligationPair with the associated PsdEligibleReferenceObligation
	 * 
	 * @param obligationDtosList
	 *            List of transformed Reference Obligation Pairs from XML file
	 * @param cdsReferenceObligationPairs
	 *            object containing list of Reference Obligation Pairs from XML file
	 * @return a list of PsdReferenceObligationPair
	 */
	List<PsdReferenceObligationPair> addEligibleToReferenceObligation(List<PsdReferenceObligationPair> obligationDtosList, CDSReferenceObligationPairs cdsReferenceObligationPairs);

	/**
	 * Transform CDSProduct to PsdProduct
	 * 
	 * @param xmlProductsList
	 *            List of Products from XML file
	 * @param obligationDtosList
	 *            List of Reference Obligation Pairs Entities
	 * @param xmlConstituentsList
	 *            List of Index Constituents from XML file
	 * @return a list of PsdProduct
	 */
	List<PsdProduct> wrapProducts(List<CDSProduct> xmlProductsList, List<PsdReferenceObligationPair> obligationDtosList, List<CDSIndexConstituent> xmlConstituentsList);

	/**
	 * Transform CDSIndexConstituent to PsdIdxComposition and PsdIdxConstituent
	 * 
	 * @param productDtosList
	 *            List of Products Entities
	 * @param xmlConstituentsList
	 *            List of Index Constituents from XML file
	 * @return a list of PsdProduct
	 */
	List<PsdProduct> wrapConstituents(List<PsdProduct> productDtosList, List<CDSIndexConstituent> xmlConstituentsList);

	/**
	 * Transform CDSContract to PsdContract
	 * 
	 * @param xmlContractsList
	 *            List of CDSContract from XML file
	 * @param productDtosList
	 *            List of Products Entities
	 * 
	 * @return List of Contracts Entities
	 */
	List<PsdContract> wrapContracts(List<CDSContract> xmlContractsList, List<PsdProduct> productDtosList);

	/**
	 * Create or Update Reference Entities
	 * 
	 * @param psdReferEntities
	 */
	void createOrUpdateReferenceEntity(List<PsdReferenceEntity> psdReferEntities);

	/**
	 * Create or Update Reference Obligation Pairs
	 * 
	 * @param psdReferObligPairs
	 */
	void createOrUpdateReferenceObligationPairs(List<PsdReferenceObligationPair> psdReferObligPairs);

	/**
	 * Create or Update Products
	 * 
	 * @param psdProducts
	 */
	void createOrUpdateProducts(List<PsdProduct> psdProducts);

	/**
	 * Create or Update Contracts
	 * 
	 * @param wrapContracts
	 */
	void createOrUpdateContracts(List<PsdContract> wrapContracts);

	/**
	 * Check Index with definition of products
	 * 
	 * @param productDtosList
	 */
	void checkIndexFactorWithWeightRate(List<PsdProduct> productDtosList);

	/**
	 * Extract/update list of new SRO Associated Reference Obligation Pairs
	 * 
	 * @param cdsReferenceObligationPairs
	 * @param listReferenceObligationPair
	 * @return
	 */
	List<PsdSroAssocOblig> processSroAssocOblig(CDSReferenceObligationPairs cdsReferenceObligationPairs, List<PsdReferenceObligationPair> listReferenceObligationPair);

	/**
	 * Create or Update SRO Associated Obligations.
	 * 
	 * @param listPsdSroAssocOblig
	 */
	void createOrUpdateSroAssociatedObligations(List<PsdSroAssocOblig> listPsdSroAssocOblig);

}
