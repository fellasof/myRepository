package com.lchclearnet.cds.dozer;

import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lchclearnet.cds.domain.psd.entity.PsdContract;
import com.lchclearnet.cds.domain.psd.entity.PsdEligibleReferenceObligation;
import com.lchclearnet.cds.domain.psd.entity.PsdIdxComposition;
import com.lchclearnet.cds.domain.psd.entity.PsdIdxConstituent;
import com.lchclearnet.cds.domain.psd.entity.PsdPrefReferObligPair;
import com.lchclearnet.cds.domain.psd.entity.PsdProduct;
import com.lchclearnet.cds.domain.psd.entity.PsdReferenceEntity;
import com.lchclearnet.cds.domain.psd.entity.PsdReferenceObligationPair;
import com.lchclearnet.cds.domain.psd.entity.PsdSroAssocOblig;
import com.lchclearnet.cds.domain.refdata.entity.enums.RefObligationPairType;
import com.lchclearnet.cds.ref.service.marketdata.CDSContract;
import com.lchclearnet.cds.ref.service.marketdata.CDSIndexConstituent;
import com.lchclearnet.cds.ref.service.marketdata.CDSProduct;
import com.lchclearnet.cds.ref.service.marketdata.CDSReferenceEntity;
import com.lchclearnet.cds.ref.service.marketdata.EligibleReferenceObligationPair;
import com.lchclearnet.cds.ref.service.marketdata.ReferenceObligationPairNonSRO;
import com.lchclearnet.cds.ref.service.marketdata.ReferenceObligationPairSRO;
import com.lchclearnet.cds.ref.service.marketdata.SROAssociatedObligation;

/**
 * Mapping class DTO and entity bean
 * 
 * @author Toscane.Dev.Team
 * 
 */
@Component
public class CdsDozerMapping {

	@Autowired
	private Mapper dozerMapper;

	/**
	 * CDSReferenceEntity to PsdReferenceEntity
	 * 
	 * @param referenceXml
	 * @return
	 */
	public PsdReferenceEntity referenceEntityDtoToEntity(CDSReferenceEntity referenceXml) {
		PsdReferenceEntity referenceEntity = dozerMapper.map(referenceXml, PsdReferenceEntity.class);

		return referenceEntity;
	}

	/**
	 * ReferenceObligationPairNonSRO to PsdReferenceObligationPair
	 * 
	 * @param referenceOblPairXml
	 * @return
	 */
	public PsdReferenceObligationPair referenceObligationPairNonSROToEntity(ReferenceObligationPairNonSRO referenceOblPairXml) {
		PsdReferenceObligationPair referenceObligationPair = dozerMapper.map(referenceOblPairXml, PsdReferenceObligationPair.class);
		referenceObligationPair.setRefObligationPairType(RefObligationPairType.NON_SRO);

		return referenceObligationPair;
	}

	/**
	 * ReferenceObligationPairSRO to PsdReferenceObligationPair
	 * 
	 * @param referenceOblPairXml
	 * @return
	 */
	public PsdReferenceObligationPair referenceObligationPairSROToEntity(ReferenceObligationPairSRO referenceOblPairXml) {
		PsdReferenceObligationPair referenceObligationPair = dozerMapper.map(referenceOblPairXml, PsdReferenceObligationPair.class);
		referenceObligationPair.setRefObligationPairType(RefObligationPairType.SRO);

		return referenceObligationPair;
	}

	/**
	 * EligibleReferenceObligationPair to PsdEligibleReferenceObligation
	 * 
	 * @param referenceOblPairXml
	 * @return
	 */
	public PsdEligibleReferenceObligation referenceEligibleObligationDtoToEntity(EligibleReferenceObligationPair eligibleReferenceObligationPair) {
		PsdEligibleReferenceObligation referenceReferenceObligation = dozerMapper.map(eligibleReferenceObligationPair, PsdEligibleReferenceObligation.class);

		return referenceReferenceObligation;
	}

	/**
	 * CDSProduct to PsdProduct
	 * 
	 * @param productXml
	 * @return
	 */
	public PsdProduct productDtoToEntity(CDSProduct productXml) {
		PsdProduct product = dozerMapper.map(productXml, PsdProduct.class);

		return product;
	}

	/**
	 * CDSProduct to PsdPrefReferObligPair
	 * 
	 * @param productXml
	 * @return
	 */
	public PsdPrefReferObligPair prefReferObligPairDtoToEntity(CDSProduct productXml) {
		PsdPrefReferObligPair prefReferObligPair = dozerMapper.map(productXml, PsdPrefReferObligPair.class);

		return prefReferObligPair;
	}

	/**
	 * CDSContract to PsdContract
	 * 
	 * @param contractXml
	 * @return
	 */
	public PsdContract contractDtoToEntity(CDSContract contractXml) {
		PsdContract contractEntity = dozerMapper.map(contractXml, PsdContract.class);

		return contractEntity;
	}

	/**
	 * CDSIndexConstituent to PsdIdxComposition
	 * 
	 * @param indexConstituentXml
	 * @return
	 */
	public PsdIdxComposition indexCompositionDtoToEntity(CDSIndexConstituent indexConstituentXml) {
		PsdIdxComposition indexComposition = dozerMapper.map(indexConstituentXml, PsdIdxComposition.class);

		return indexComposition;
	}

	/**
	 * CDSIndexConstituent to PsdIdxConstituent
	 * 
	 * @param indexConstituentXml
	 * @return
	 */
	public PsdIdxConstituent indexConstituentDtoToEntity(CDSIndexConstituent indexConstituentXml) {
		PsdIdxConstituent indexConstituent = dozerMapper.map(indexConstituentXml, PsdIdxConstituent.class);

		return indexConstituent;
	}

	public PsdSroAssocOblig sroAssociatedObligationToEntity(SROAssociatedObligation associatedObligation) {
		PsdSroAssocOblig sroAssocOblig = dozerMapper.map(associatedObligation, PsdSroAssocOblig.class);

		return sroAssocOblig;
	}

}
