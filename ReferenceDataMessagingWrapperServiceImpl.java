package com.lchclearnet.cds.services.psd;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.NoResultException;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.lchclearnet.cds.common.utils.DateUtils;
import com.lchclearnet.cds.common.utils.ListUtils;
import com.lchclearnet.cds.domain.common.enums.ApplicationDomain;
import com.lchclearnet.cds.domain.common.enums.TraceLogStatus;
import com.lchclearnet.cds.domain.psd.das.PsdContractDas;
import com.lchclearnet.cds.domain.psd.das.PsdIdxCompositionDas;
import com.lchclearnet.cds.domain.psd.das.PsdIdxConstituentDas;
import com.lchclearnet.cds.domain.psd.das.PsdLegalTermsCategoryDas;
import com.lchclearnet.cds.domain.psd.das.PsdPrefReferObligPairDas;
import com.lchclearnet.cds.domain.psd.das.PsdProductDas;
import com.lchclearnet.cds.domain.psd.das.PsdRefTransTypeDas;
import com.lchclearnet.cds.domain.psd.das.PsdReferenceEntityDas;
import com.lchclearnet.cds.domain.psd.das.PsdReferenceObligationPairDas;
import com.lchclearnet.cds.domain.psd.das.PsdSroAssocObligDas;
import com.lchclearnet.cds.domain.psd.entity.PsdContract;
import com.lchclearnet.cds.domain.psd.entity.PsdEligibleReferenceObligation;
import com.lchclearnet.cds.domain.psd.entity.PsdIdxComposition;
import com.lchclearnet.cds.domain.psd.entity.PsdIdxCompositionPk;
import com.lchclearnet.cds.domain.psd.entity.PsdIdxConstituent;
import com.lchclearnet.cds.domain.psd.entity.PsdIdxConstituentPk;
import com.lchclearnet.cds.domain.psd.entity.PsdLegalTermsCategory;
import com.lchclearnet.cds.domain.psd.entity.PsdPrefReferObligPair;
import com.lchclearnet.cds.domain.psd.entity.PsdPrefReferObligPairPk;
import com.lchclearnet.cds.domain.psd.entity.PsdProduct;
import com.lchclearnet.cds.domain.psd.entity.PsdRefIdxDsgn;
import com.lchclearnet.cds.domain.psd.entity.PsdRefTransType;
import com.lchclearnet.cds.domain.psd.entity.PsdReferenceEntity;
import com.lchclearnet.cds.domain.psd.entity.PsdReferenceObligationPair;
import com.lchclearnet.cds.domain.psd.entity.PsdSroAssocOblig;
import com.lchclearnet.cds.domain.psd.entity.enums.BaseProduct;
import com.lchclearnet.cds.domain.psd.entity.enums.SeriesRange;
import com.lchclearnet.cds.domain.psd.entity.enums.SroTierCode;
import com.lchclearnet.cds.domain.refdata.das.CdsReferenceDataDas;
import com.lchclearnet.cds.domain.refdata.entity.enums.CdsReferenceDataEnum;
import com.lchclearnet.cds.domain.refdata.entity.enums.RefContractualDefinition;
import com.lchclearnet.cds.domain.refdata.entity.enums.RefObligationPairType;
import com.lchclearnet.cds.domain.refdata.entity.enums.RefRestructuringType;
import com.lchclearnet.cds.dozer.CdsDozerMapping;
import com.lchclearnet.cds.ref.service.marketdata.CDSContract;
import com.lchclearnet.cds.ref.service.marketdata.CDSIndexConstituent;
import com.lchclearnet.cds.ref.service.marketdata.CDSIndexCurrentConstituent;
import com.lchclearnet.cds.ref.service.marketdata.CDSProduct;
import com.lchclearnet.cds.ref.service.marketdata.CDSReferenceEntity;
import com.lchclearnet.cds.ref.service.marketdata.CDSReferenceObligationPairs;
import com.lchclearnet.cds.ref.service.marketdata.EligibleReferenceObligationPair;
import com.lchclearnet.cds.ref.service.marketdata.PreferredReferenceObligationInterval;
import com.lchclearnet.cds.ref.service.marketdata.ReferenceObligationPairNonSRO;
import com.lchclearnet.cds.ref.service.marketdata.ReferenceObligationPairSRO;
import com.lchclearnet.cds.ref.service.marketdata.SROAssociatedObligation;
import com.lchclearnet.cds.services.calendar.CdsFinancialCalendarService;
import com.lchclearnet.cds.services.error.CdsTraceLogService;

/**
 * [SN-VaR] [2156] [22 Aout 2013]
 */
/**
 * Integrate Reference data coming from MDM
 *
 * @author Toscane.dev.team
 *
 */
@Component
@Transactional
public class ReferenceDataMessagingWrapperServiceImpl implements ReferenceDataMessagingWrapperService {

	/**
	 * logger
	 */
	private static Logger logger = LoggerFactory.getLogger(ReferenceDataMessagingWrapperServiceImpl.class);

	@Autowired
	private PsdReferenceEntityDas psdReferenceEntityDas;

	@Autowired
	private PsdIdxCompositionDas psdIdxCompositionDas;

	@Autowired
	private PsdIdxConstituentDas psdIdxConstituentDas;

	@Autowired
	private CdsDozerMapping cdsDozerMapping;

	@Autowired
	private PsdReferenceObligationPairDas psdReferenceObligationPairDas;

	@Autowired
	private PsdPrefReferObligPairDas psdPrefReferObligPairDas;

	@Autowired
	private PsdContractDas psdDContractDas;

	@Autowired
	private PsdProductDas psdProductDas;

	@Autowired
	private CdsFinancialCalendarService cdsCalendarService;

	@Autowired
	private CdsTraceLogService traceService;

	@Autowired
	private PsdLegalTermsCategoryDas psdLegalTermsCategoryDas;

	@Autowired
	private CdsReferenceDataDas cdsReferenceDataDas;

	@Autowired
	private PsdSroAssocObligDas psdSroAssocObligDas;

	@Autowired
	private PsdRefTransTypeDas psdRefTransTypeDas;

	/**
	 * Constand to use when converting Basis Points to Decimal. Divide Fixed Rate by this constant to convert basis points to decimal.
	 */
	private static final BigDecimal DIVISION_VALUE = new BigDecimal(10000);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PsdReferenceEntity> wrapReferenceEntity(List<CDSReferenceEntity> xmlReferEntitiesList) {

		List<PsdReferenceEntity> refEntitieDtosList = new ArrayList<PsdReferenceEntity>();

		if (xmlReferEntitiesList == null) {
			return refEntitieDtosList;
		}

		for (CDSReferenceEntity xmlReferEntity : xmlReferEntitiesList) {
			if (xmlReferEntity != null) {
				PsdReferenceEntity referEntityDto = cdsDozerMapping.referenceEntityDtoToEntity(xmlReferEntity);
				refEntitieDtosList.add(referEntityDto);
			}
		}
		return refEntitieDtosList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PsdReferenceObligationPair> wrapReferenceObligPair(CDSReferenceObligationPairs refObPairsXmlObject, List<PsdReferenceEntity> listReferenceEntity) {

		List<PsdReferenceObligationPair> obligPairDtosList = new ArrayList<PsdReferenceObligationPair>();

		if (refObPairsXmlObject == null) {
			return obligPairDtosList;
		}

		List<Object> allRefOb = new ArrayList<>();

		if (refObPairsXmlObject.getCdsReferenceObligationPairsNonSRO() != null) {
			allRefOb.addAll(refObPairsXmlObject.getCdsReferenceObligationPairsNonSRO().getReferenceObligationPairNonSRO());
		}
		if (refObPairsXmlObject.getCdsReferenceObligationPairsSRO() != null) {
			allRefOb.addAll(refObPairsXmlObject.getCdsReferenceObligationPairsSRO().getReferenceObligationPairSRO());
		}

		ReferenceObligationPairNonSRO xmlObligPairNonSRO;
		ReferenceObligationPairSRO xmlObligPairSRO;

		PsdReferenceObligationPair contructedRefObPair;
		PsdReferenceObligationPair loadedRefObPair = null;
		String idRefOb;

		for (Object obj : allRefOb) {
			// Process CdsReferenceObligationPairsNonSRO
			if (ReferenceObligationPairNonSRO.class.isAssignableFrom(obj.getClass())) {
				xmlObligPairNonSRO = (ReferenceObligationPairNonSRO) obj;
				contructedRefObPair = cdsDozerMapping.referenceObligationPairNonSROToEntity(xmlObligPairNonSRO);
				contructedRefObPair.setRefObligationPairType(RefObligationPairType.fromValue(xmlObligPairNonSRO.getReferenceObligationPairType()));
				idRefOb = xmlObligPairNonSRO.getReferenceObligationPairId();
			} else {
				// Process CdsReferenceObligationPairsSRO
				xmlObligPairSRO = (ReferenceObligationPairSRO) obj;
				contructedRefObPair = cdsDozerMapping.referenceObligationPairSROToEntity(xmlObligPairSRO);
				contructedRefObPair.setRefObligationPairType(RefObligationPairType.fromValue(xmlObligPairSRO.getReferenceObligationPairType()));
				// No ReferenceObligationName for SRO
				contructedRefObPair.setReferenceObligationName(" ");
				contructedRefObPair.setSroTierCode(SroTierCode.fromValue(xmlObligPairSRO.getSROTier().getValue()));
				idRefOb = xmlObligPairSRO.getReferenceObligationPairId();
			}

			try {
				// Check if the ReferenceObligationPair exists
				loadedRefObPair = psdReferenceObligationPairDas.load(idRefOb);
			} catch (NoResultException e) {
				logger.info("Reference Obligation Pair not saved yet : " + idRefOb);
			}
			// if exists get its eligibles
			if (loadedRefObPair != null) {
				contructedRefObPair.setPsdEligibleReferenceObligations(loadedRefObPair.getPsdEligibleReferenceObligations());
			}

			contructedRefObPair.setPsdReferenceEntity(getPsdReferenceEntityInSendedList(contructedRefObPair.getPsdReferenceEntity().getKey(), listReferenceEntity));
			// add to be saved
			obligPairDtosList.add(contructedRefObPair);
		}
		return obligPairDtosList;
	}

	/**
	 * look for the PsdReferenceEntity in xml entities or from DB
	 *
	 * @param idRefEnt
	 * @param listReferenceEntity
	 * @return
	 */
	private PsdReferenceEntity getPsdReferenceEntityInSendedList(String idRefEnt, List<PsdReferenceEntity> listReferenceEntity) {
		PsdReferenceEntity result = new PsdReferenceEntity();

		for (PsdReferenceEntity refEnt : listReferenceEntity) {
			if (refEnt.getKey().equals(idRefEnt)) {
				return refEnt;
			}
		}

		try {
			return psdReferenceEntityDas.load(idRefEnt);
		} catch (NoResultException e) {
			result.setKey(idRefEnt);
			logger.warn("Reference Entity not found (xml a nd DB): " + idRefEnt);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PsdReferenceObligationPair> addEligibleToReferenceObligation(List<PsdReferenceObligationPair> obligationDtosList, CDSReferenceObligationPairs cdsReferenceObligationPairs) {
		// Sort by RefOb
		Map<String, Set<PsdEligibleReferenceObligation>> xmlEligibleByRefObIdsMap = buildAndSortEligibleRefObsByRefOb(cdsReferenceObligationPairs);

		if (MapUtils.isNotEmpty(xmlEligibleByRefObIdsMap)) {
			Set<PsdEligibleReferenceObligation> linkedEligDtos;

			for (PsdReferenceObligationPair refObDto : obligationDtosList) {
				// get liked EligibleReferenceObligationPair linked in the xml to the PsdReferenceObligationPair
				linkedEligDtos = xmlEligibleByRefObIdsMap.get(refObDto.getKey());
				// process set of linked objects
				updateOrAddEligibleRefObsToReferenceObligationPair(refObDto, linkedEligDtos);
			}
		}
		return obligationDtosList;
	}

	/**
	 * Sort EligibleReferenceObligationPair by Reference Obligation Pair
	 *
	 * @param cdsReferenceObligationPairs
	 * @return
	 */
	private Map<String, Set<PsdEligibleReferenceObligation>> buildAndSortEligibleRefObsByRefOb(CDSReferenceObligationPairs cdsReferenceObligationPairs) {

		Map<String, Set<PsdEligibleReferenceObligation>> result = new HashMap<String, Set<PsdEligibleReferenceObligation>>();
		Set<PsdEligibleReferenceObligation> linkedElig;
		PsdEligibleReferenceObligation eligibleDto;

		if (cdsReferenceObligationPairs != null) {
			// list xml objects of Eligible RefOb
			List<EligibleReferenceObligationPair> xmlEligibleList = cdsReferenceObligationPairs.getCdsEligibleReferenceObligationPairs().getEligibleReferenceObligationPair();

			for (EligibleReferenceObligationPair eligXml : xmlEligibleList) {
				// Bypass begin date error
				if (eligXml.getEligibilityBeginDate() == null) {
					eligXml.setEligibilityBeginDate(DateUtils.getXMLGregorianCalendarFromDate(new Date()));
				}

				// wrap xml object into Entity
				eligibleDto = cdsDozerMapping.referenceEligibleObligationDtoToEntity(eligXml);

				// Check existing of linked product and bypass error
				try {
					psdProductDas.load(eligXml.getProductId());
				} catch (NoResultException e) {
					traceService.traceLog("Product (" + eligXml.getProductId() + ") not found ", this.getClass().getName(), ApplicationDomain.CDS, TraceLogStatus.TRACELOG_ERR);
				}
				// Bypass error
				if (eligibleDto.getKey().getReferenceObligationPair() == null || com.lchclearnet.cds.common.utils.StringUtils.isEmpty(eligibleDto.getKey().getReferenceObligationPair().getKey())) {
					eligibleDto.getKey().setReferenceObligationPair(new PsdReferenceObligationPair());
					traceService.traceLog("No ReferenceObligationPair found for " + eligXml.getReferenceObligationPairId(), this.getClass().getName(), ApplicationDomain.CDS,
							TraceLogStatus.TRACELOG_ERR);
				}

				// Check if ReferenceObligationPairId already found
				linkedElig = result.get(eligXml.getReferenceObligationPairId());
				// if not add it
				if (linkedElig == null) {
					linkedElig = new HashSet<PsdEligibleReferenceObligation>();
					result.put(eligXml.getReferenceObligationPairId(), linkedElig);
				}
				// add the EligibleReferenceObligationPair builded tothe linked EligRefOb set
				linkedElig.add(eligibleDto);
			}
		}
		return result;
	}

	/**
	 * Update end date of existing Eligible Reference Obligations Or add it if don't exist to list of Eligible of the current Reference Obligation
	 *
	 * @param obligationDto
	 *            current Reference Obligation
	 * @param linkedEligDtos
	 *            Eligible RefOb builded from xml and linked to current Reference Obligation @see ReferenceDataMessagingWrapperServiceImpl#sortEligibleRefObsByRefObMap(CDSReferenceObligationPairs)
	 */
	private void updateOrAddEligibleRefObsToReferenceObligationPair(PsdReferenceObligationPair obligationDto, Set<PsdEligibleReferenceObligation> linkedEligDtos) {
		if (linkedEligDtos != null && !linkedEligDtos.isEmpty()) {
			boolean itemFound = false;
			// reprise de l'ancien code tel quel peut etre pour detecter les Eligibility qui se terminent?!
			for (PsdEligibleReferenceObligation xmlEligibleDto : linkedEligDtos) {
				for (PsdEligibleReferenceObligation dbEligibleDto : obligationDto.getPsdEligibleReferenceObligations()) {
					if (dbEligibleDto.equals(xmlEligibleDto)) {
						itemFound = true;
						// Termination of an old Eligibility?
						dbEligibleDto.setEligibilityEndDate(xmlEligibleDto.getEligibilityEndDate());
					}
				}
				if (!itemFound) {
					// Deja fait par Dozer!!
					obligationDto.getPsdEligibleReferenceObligations().add(xmlEligibleDto);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PsdProduct> wrapProducts(List<CDSProduct> xmlProductsList, List<PsdReferenceObligationPair> obligationDtosList, List<CDSIndexConstituent> xmlConstituentsList) {
		List<PsdProduct> productDtosList = new ArrayList<PsdProduct>();

		if (xmlProductsList == null) {
			return productDtosList;
		}

		PsdProduct productDto = null;
		PsdIdxComposition idxCompositionDto = null;
		for (CDSProduct xmlProduct : xmlProductsList) {
			if (xmlProduct != null) {
				// 1st pass : Product
				productDto = preparePsdProductWithoutDependencies(xmlProduct);
				psdProductDas.createOrUpdate(productDto);
				// 2nd pass : Prefered Reference Obligation Pair
				productDto = addPreferedReferObligPairToProduct(productDto, xmlProductsList);
				// 3rd pass : Index Composition
				idxCompositionDto = addIndexCompositionToProduct(productDto, xmlConstituentsList);
				// 4th pass : Index Constituents
				idxCompositionDto = addIndexConstituentsToProduct(idxCompositionDto, xmlConstituentsList);
				productDtosList.add(productDto);
			}
		}
		return productDtosList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PsdProduct> wrapConstituents(List<PsdProduct> productDtosList, List<CDSIndexConstituent> xmlConstituentsList) {

		if (xmlConstituentsList != null && xmlConstituentsList.size() > 0) {
			boolean itemFound;
			PsdIdxComposition idxCompositionDto = null;
			for (CDSIndexConstituent xmlConstituent : xmlConstituentsList) {
				itemFound = false;
				for (PsdProduct productDto : productDtosList) {
					if (xmlConstituent.getProductId().equals(productDto.getProductId())) {
						itemFound = true;
					}
				}
				if (!itemFound) {
					PsdProduct productDto = null;
					try {
						productDto = psdProductDas.load(xmlConstituent.getProductId());
					} catch (NoResultException e) {
						traceService.traceLog("Product (" + xmlConstituent.getProductId() + ") not found ", this.getClass().getName(), ApplicationDomain.CDS, TraceLogStatus.TRACELOG_ERR);
					}

					idxCompositionDto = addIndexCompositionToProduct(productDto, xmlConstituentsList);
					idxCompositionDto = addIndexConstituentsToProduct(idxCompositionDto, xmlConstituentsList);
					productDtosList.add(productDto);
				}
			}
		}
		return productDtosList;
	}

	/**
	 * Enwrap CDSProduct into a PsdProduct without the dependent objects.
	 *
	 * @param xmlProduct
	 * @return PsdProduct wrapper
	 */
	protected PsdProduct preparePsdProductWithoutDependencies(CDSProduct xmlProduct) {
		PsdProduct productDto = new PsdProduct();
		SeriesRange serieRange = null;

		productDto.setProductId(xmlProduct.getProductId());
		productDto.setBaseProduct(BaseProduct.fromValue(xmlProduct.getBaseProductCode()));
		if (xmlProduct.getTransactionTypeCode() != null) {
			if (productDto.getTransactionTypeCode() == null) {
				productDto.setTransactionTypeCode(new PsdRefTransType());
			}
			productDto.getTransactionTypeCode().setKey(xmlProduct.getTransactionTypeCode());
		}

		productDto.setProductName(xmlProduct.getCdsProductName());
		productDto.setMarkitProductCode(xmlProduct.getIndexProductCode());
		if (xmlProduct.getIndexDesignation() != null) {
			if (productDto.getIdxDesignation() == null) {
				productDto.setIdxDesignation(new PsdRefIdxDsgn());
			}
			productDto.getIdxDesignation().setIdxDesignationCode(xmlProduct.getIndexDesignation());
		}

		if (xmlProduct.getIndexSeries() != null) {
			productDto.setIdxSeries(Long.valueOf(xmlProduct.getIndexSeries()));
			serieRange = productDto.getIdxSeries().compareTo(Long.valueOf(21)) <= 0 ? SeriesRange.A : SeriesRange.B;
		}
		if (xmlProduct.getIndexVersion() != null) {
			productDto.setIdxVersion(Long.valueOf(xmlProduct.getIndexVersion()));
		}

		productDto.setIdxFactor(xmlProduct.getIndexFactor());
		productDto.setIdxEffectiveDate(DateUtils.getDateFromXmlGregorianCalendar(xmlProduct.getIndexEffectiveDate()));
		productDto.setAnnexeDate(DateUtils.getDateFromXmlGregorianCalendar(xmlProduct.getAnnexDate()));
		if (productDto.getReferenceEntity() == null && xmlProduct.getSingleNameReferenceEntityId() != null) {
			productDto.setReferenceEntity(new PsdReferenceEntity());
		}
		if (xmlProduct.getSingleNameReferenceEntityId() != null) {
			productDto.getReferenceEntity().setKey(xmlProduct.getSingleNameReferenceEntityId());
		}
		if (xmlProduct.getSingleNameProductChildId() != null) {
			productDto.setSnProductChildId(xmlProduct.getSingleNameProductChildId());
		}
		if (xmlProduct.getPaymentFrequency() != null) {
			StringBuilder payementFreq = new StringBuilder();
			payementFreq.append(xmlProduct.getPaymentFrequency().getPeriodMultiplier());
			payementFreq.append(xmlProduct.getPaymentFrequency().getPeriod());
			productDto.setPayementFrequency(payementFreq.toString());
		}
		if (xmlProduct.getUPI() != null && xmlProduct.getUPI().getValue() != null) {
			productDto.setUniqueProductId(xmlProduct.getUPI().getValue());
		}
		if (xmlProduct.getCurrency() != null) {
			productDto.setCurrencyCode(xmlProduct.getCurrency().getValue());
		}

		productDto.setValidityBeginDate(DateUtils.getDateFromXmlGregorianCalendar(xmlProduct.getValidityBeginDate()));
		productDto.setValidityEndDate(DateUtils.getDateFromXmlGregorianCalendar(xmlProduct.getValidityEndDate()));
		productDto.setDocClause(xmlProduct.getMarkitDocClause());

		for (RefContractualDefinition ref : RefContractualDefinition.values()) {
			if (cdsReferenceDataDas.getLabel(CdsReferenceDataEnum.CONTRACTUAL_DEFINITION, ref).equals(xmlProduct.getContractualDefinitions())) {
				productDto.setContractualDefinition(ref);
			}
		}

		if (xmlProduct.getRestructuringType() != null) {
			for (RefRestructuringType ref : RefRestructuringType.values()) {
				if (cdsReferenceDataDas.getLabel(CdsReferenceDataEnum.RESTRUCTURING_TYPE, ref).equals(xmlProduct.getRestructuringType())) {
					productDto.setRestructuringType(ref);
				}
			}
		}

		// Find the Legal Terms Category associated to this product
		PsdLegalTermsCategory category = psdLegalTermsCategoryDas.getCategoriesByProduct(productDto, serieRange);
		productDto.setLegalTermsCategory(category);

		// do not update begin date if the product already exists in db : we set the begin date with the value from db
		if (psdProductDas.exist(productDto.getKey())) {
			final PsdProduct entityInDb = psdProductDas.load(productDto);
			productDto.setValidityBeginDate(entityInDb.getValidityBeginDate());
		}

		return productDto;
	}

	/**
	 * Complete PsdProduct with PsdPrefReferObligPair
	 *
	 * @param productDto
	 * @param xmlProductsList
	 * @param obligationDtosList
	 * @return
	 */
	protected PsdProduct addPreferedReferObligPairToProduct(PsdProduct productDto, List<CDSProduct> xmlProductsList) {
		List<PreferredReferenceObligationInterval> prefereds;
		PreferredReferenceObligationInterval firstPrefered;
		PreferredReferenceObligationInterval secondPrefered;

		for (CDSProduct cdsProduct : xmlProductsList) {

			if (cdsProduct != null && productDto != null && ListUtils.isNotEmpty(cdsProduct.getPreferredReferenceObligationPair())) {

				prefereds = cdsProduct.getPreferredReferenceObligationPair();

				if (prefereds.size() == 1) {
					buildAndSavePreferredRefOblig(prefereds.get(0), cdsProduct, productDto);
				} else {
					if (prefereds.size() == 2) {
						firstPrefered = prefereds.get(0);
						secondPrefered = prefereds.get(1);
						if (firstPrefered != null && secondPrefered != null) {

							// one and only one of the two Preferred RefOb should have null as validity end date (XOR validity end date null)
							if ((firstPrefered.getEffectiveEndDate() == null && secondPrefered.getEffectiveEndDate() != null)
									|| (firstPrefered.getEffectiveEndDate() != null && secondPrefered.getEffectiveEndDate() == null)) {

								buildAndSavePreferredRefOblig(firstPrefered, cdsProduct, productDto);
								buildAndSavePreferredRefOblig(secondPrefered, cdsProduct, productDto);

							} else {
								traceService.traceLog(
										"Product (" + cdsProduct.getProductId() + ") has 2 Preferred RefOb with invalid value of Validity End Dates : " + firstPrefered.getEffectiveEndDate()
												+ " or/and " + secondPrefered.getEffectiveEndDate(), this.getClass().getName(), ApplicationDomain.CDS, TraceLogStatus.TRACELOG_ERR);
							}
						} else {
							traceService.traceLog("Product " + cdsProduct.getProductId() + " has at least one of its Preferred RefOb as null", this.getClass().getName(), ApplicationDomain.CDS,
									TraceLogStatus.TRACELOG_ERR);
						}
					} else {
						if (prefereds.size() > 2) {
							traceService.traceLog("Product " + cdsProduct.getProductId() + " has more than 2 Preferred RefOb", this.getClass().getName(), ApplicationDomain.CDS,
									TraceLogStatus.TRACELOG_ERR);
						} else {
							logger.info("Product " + cdsProduct.getProductId() + " has no Preferred RefOb");
						}
					}
				}
			}
		}
		return productDto;
	}

	/**
	 * Build and save a Preferred RefOb
	 *
	 * @param preferedRefOb
	 * @param xmlProduct
	 * @param productDto
	 */
	private void buildAndSavePreferredRefOblig(PreferredReferenceObligationInterval preferedRefOb, CDSProduct xmlProduct, PsdProduct productDto) {
		PsdPrefReferObligPair preferedDto;
		PsdPrefReferObligPairPk preferedDtoKey;
		PsdReferenceObligationPair obligationDto = null;
		if (xmlProduct.getProductId().equals(productDto.getKey()) && preferedRefOb.getEffectiveBeginDate() != null) {
			preferedDto = new PsdPrefReferObligPair();
			if (preferedRefOb.getEffectiveBeginDate() != null) {
				preferedDto.setEndDate(DateUtils.getDateFromXmlGregorianCalendar(preferedRefOb.getEffectiveEndDate()));
			}
			preferedDtoKey = new PsdPrefReferObligPairPk();
			preferedDtoKey.setBeginDate(DateUtils.getDateFromXmlGregorianCalendar(preferedRefOb.getEffectiveBeginDate()));
			preferedDtoKey.setProduct(productDto);
			preferedDto.setKey(preferedDtoKey);

			try {
				obligationDto = psdReferenceObligationPairDas.load(preferedRefOb.getPreferredReferenceObligationPairId());
			} catch (NoResultException e) {
				traceService.traceLog("ReferenceObligationPair (" + preferedRefOb.getPreferredReferenceObligationPairId() + ") not found ", this.getClass().getName(), ApplicationDomain.CDS,
						TraceLogStatus.TRACELOG_ERR);
			}

			if (obligationDto != null) {
				preferedDto.setReferenceObligationPair(obligationDto);
			}
			psdPrefReferObligPairDas.createOrUpdate(preferedDto);
		}
	}

	/**
	 * Complete Product with Index Composition
	 *
	 * @param productDto
	 *            Current PsdProduct
	 * @param xmlConstituentsList
	 *            List of Index Constituents from XML file
	 * @return compositionDto
	 */
	protected PsdIdxComposition addIndexCompositionToProduct(PsdProduct productDto, List<CDSIndexConstituent> xmlConstituentsList) {

		PsdIdxComposition compositionHistoDto = null;
		PsdIdxComposition compositionDto = null;
		PsdIdxCompositionPk compositionDtoKey;

		for (CDSIndexConstituent xmlConstituent : xmlConstituentsList) {
			if (xmlConstituent != null && productDto != null && xmlConstituent.getCompositionDate() != null && xmlConstituent.getProductId().equals(productDto.getKey())) {

				compositionHistoDto = psdIdxCompositionDas.loadLatestIdxComposition(productDto);

				if (compositionHistoDto != null) {
					if (compositionHistoDto.getKey().getBeginDate() != null
							&& compositionHistoDto.getKey().getBeginDate().equals(DateUtils.getDateFromXmlGregorianCalendar(xmlConstituent.getCompositionDate()))) {
						// nothing to be done : no change requested
						// CDS-CLEAR 4930
						continue;

					} else if (compositionHistoDto.getKey().getBeginDate() != null
							&& !compositionHistoDto.getKey().getBeginDate().equals(DateUtils.getDateFromXmlGregorianCalendar(xmlConstituent.getCompositionDate()))) {

						// close the current composition (updates this instance of Index Composition:
						// Validity End Date = Max{Composition Date; BD + 1}-1 calendar day)
						// and creates a new instance of Index Composition with all its Index Constituents

						final Date currentDateAddOneBD = cdsCalendarService.getShiftedBusinessDate(cdsCalendarService.getCurrentBusinessDate(), 1);
						final Date compositionDate = DateUtils.getDateFromXmlGregorianCalendar(xmlConstituent.getCompositionDate());
						final Date baseDate = DateUtils.beforeOrEqual(currentDateAddOneBD, compositionDate) ? compositionDate : currentDateAddOneBD;

						final Date endDate = DateUtils.addDays(baseDate, -1);

						compositionHistoDto.setEndDate(endDate);
						psdIdxCompositionDas.createOrUpdate(compositionHistoDto);

					}
				}
				// there is a new index composition
				compositionDto = new PsdIdxComposition();
				compositionDtoKey = new PsdIdxCompositionPk();
				compositionDtoKey.setBeginDate(DateUtils.getDateFromXmlGregorianCalendar(xmlConstituent.getCompositionDate()));
				compositionDtoKey.setProduct(productDto);
				compositionDto.setKey(compositionDtoKey);

				if (xmlConstituent.getCurrentConstituent() != null) {
					compositionDto.setNbrIndexComposition(xmlConstituent.getCurrentConstituent().size());
				}
				psdIdxCompositionDas.createOrUpdate(compositionDto);

			}
		}

		return compositionDto;
	}

	/**
	 * @param idxCompositionDto
	 * @param xmlConstituentsList
	 * @return idxCompositionDto
	 */
	protected PsdIdxComposition addIndexConstituentsToProduct(PsdIdxComposition idxCompositionDto, List<CDSIndexConstituent> xmlConstituentsList) {

		List<CDSIndexCurrentConstituent> xmlCurrentConstituentsList;
		PsdIdxConstituent constituentDto;

		for (CDSIndexConstituent xmlContituent : xmlConstituentsList) {

			if (idxCompositionDto != null && idxCompositionDto.getKey().getProduct().getProductId().equals(xmlContituent.getProductId())
					&& idxCompositionDto.getKey().getBeginDate().equals(DateUtils.getDateFromXmlGregorianCalendar(xmlContituent.getCompositionDate()))) {

				xmlCurrentConstituentsList = xmlContituent.getCurrentConstituent();

				for (CDSIndexCurrentConstituent xmlCurrentConstituent : xmlCurrentConstituentsList) {
					PsdIdxConstituentPk constituentDtoPk = new PsdIdxConstituentPk();

					constituentDto = new PsdIdxConstituent();
					constituentDtoPk.setIndexComposition(idxCompositionDto);
					if (constituentDtoPk.getReferenceEntity() == null) {
						constituentDtoPk.setReferenceEntity(new PsdReferenceEntity());
					}
					constituentDtoPk.getReferenceEntity().setReferenceEntityId(xmlCurrentConstituent.getCurrentReferenceEntityId());

					constituentDto.setKey(constituentDtoPk);

					constituentDto.setConstituentWeight(xmlCurrentConstituent.getCurrentConstituentWeight());
					if (StringUtils.isNotBlank(xmlCurrentConstituent.getCurrentReferenceObligationPairId())) {
						constituentDto.setReferenceObligationPair(psdReferenceObligationPairDas.findLinkedConstituentRefObligPair(xmlCurrentConstituent.getCurrentReferenceObligationPairId()));
					} else {
						constituentDto.setReferenceObligationPair(null);
					}

					constituentDto.setDocClause(xmlCurrentConstituent.getMarkitDocClause());

					for (RefRestructuringType ref : RefRestructuringType.values()) {
						if (cdsReferenceDataDas.getLabel(CdsReferenceDataEnum.RESTRUCTURING_TYPE, ref).equals(xmlCurrentConstituent.getRestructuringType())) {
							constituentDto.setRestructuringType(ref);
						}
					}
					for (RefContractualDefinition ref : RefContractualDefinition.values()) {
						if (cdsReferenceDataDas.getLabel(CdsReferenceDataEnum.CONTRACTUAL_DEFINITION, ref).equals(xmlCurrentConstituent.getContractualDefinitions())) {
							constituentDto.setContractualDefinition(ref);
						}
					}

					if (StringUtils.isNotBlank(xmlCurrentConstituent.getIsdaTransactionType())) {
						PsdRefTransType transType = psdRefTransTypeDas.load(xmlCurrentConstituent.getIsdaTransactionType());
						constituentDto.setTransactionTypeCode(transType);
					} else {
						constituentDto.setTransactionTypeCode(null);
					}

					psdIdxConstituentDas.createOrUpdate(constituentDto);
				}
			}
		}
		return idxCompositionDto;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PsdContract> wrapContracts(List<CDSContract> xmlContractsList, List<PsdProduct> productDtosList) {
		List<PsdContract> listPsdContracts = new ArrayList<PsdContract>();

		if (xmlContractsList == null) {
			return listPsdContracts;
		}

		PsdContract psdContract;
		boolean itemFound;

		for (CDSContract cdsContract : xmlContractsList) {
			if (cdsContract != null) {
				psdContract = cdsDozerMapping.contractDtoToEntity(cdsContract);

				if (!StringUtils.isNotBlank(psdContract.getContractCode())) {
					psdContract.setContractCode(psdContract.getContractId());
				}

				itemFound = false;
				for (PsdProduct psdProduct : productDtosList) {
					if (cdsContract.getProductId().equals(psdProduct.getKey())) {
						itemFound = true;
						psdContract.setProduct(psdProduct);
					}
				}
				if (!itemFound) {
					PsdProduct productDto = null;
					try {
						productDto = psdProductDas.load(cdsContract.getProductId());
					} catch (NoResultException e) {
						traceService.traceLog("Product (" + cdsContract.getProductId() + ") not found ", this.getClass().getName(), ApplicationDomain.CDS, TraceLogStatus.TRACELOG_ERR);
					}
					psdContract.setProduct(productDto);
				}
				listPsdContracts.add(psdContract);
			}
		}
		return listPsdContracts;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createOrUpdateProducts(List<PsdProduct> psdProducts) {
		for (PsdProduct psdProduct : psdProducts) {
			psdProductDas.createOrUpdate(psdProduct);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createOrUpdateReferenceEntity(List<PsdReferenceEntity> psdReferEntities) {
		for (PsdReferenceEntity psdReferEntity : psdReferEntities) {
			// JIRA 4451
			// do not update begin date if the reference entity already exists in db : we set the begin date with the value from db
			if (psdReferenceEntityDas.exist(psdReferEntity.getKey())) {
				final PsdReferenceEntity entityInDb = psdReferenceEntityDas.load(psdReferEntity);
				psdReferEntity.setDescriptionBeginDate(entityInDb.getDescriptionBeginDate());
			}
			psdReferenceEntityDas.createOrUpdate(psdReferEntity);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createOrUpdateReferenceObligationPairs(List<PsdReferenceObligationPair> psdReferObligPairs) {

		for (PsdReferenceObligationPair psdRefObligPair : psdReferObligPairs) {
			psdReferenceObligationPairDas.createOrUpdate(psdRefObligPair);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createOrUpdateContracts(List<PsdContract> wrapContracts) {
		for (PsdContract psdContract : wrapContracts) {
			// Divide Fixed rate by 10000 to convert basis points to decimal.
			// Refer to JIRA 2805
			psdContract.setFixRate(psdContract.getFixRate().divide(DIVISION_VALUE));
			psdDContractDas.createOrUpdate(psdContract);
			// do not update begin date if the contract already exists in db : we set the begin date with the value from db
			if (psdDContractDas.exist(psdContract.getKey())) {
				final PsdContract entityInDb = psdDContractDas.load(psdContract);
				psdContract.setValidBeginDate(entityInDb.getValidBeginDate());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void checkIndexFactorWithWeightRate(List<PsdProduct> productDtosList) {
		BigDecimal sumWeightRate;
		PsdIdxComposition compositionDto;
		List<PsdIdxConstituent> constituentsList = new ArrayList<PsdIdxConstituent>();

		for (PsdProduct productDto : productDtosList) {
			if (productDto.getBaseProduct().equals(BaseProduct.IDX)) {
				sumWeightRate = BigDecimal.ZERO;

				try {
					compositionDto = psdIdxCompositionDas.loadLatestIdxComposition(productDto);
					constituentsList = psdIdxConstituentDas.get(compositionDto);
				} catch (NoResultException e) {
					logger.warn("No composition found of the product " + productDto.getProductId());
					traceService.traceLog("No composition found of the product " + productDto.getProductId(), this.getClass().getName(), ApplicationDomain.CDS, TraceLogStatus.TRACELOG_WARN);
				}

				for (PsdIdxConstituent constituentDto : constituentsList) {
					sumWeightRate = sumWeightRate.add(constituentDto.getConstituentWeight());
				}
				if (productDto.getIdxFactor().compareTo(sumWeightRate) != 0) {
					logger.warn("Check is performed on the Index Factor of the product " + productDto.getProductId() + " : " + "it must be equal to the sum of constituent Weight Rate");
					traceService.traceLog("Check is performed on the Index Factor of the product " + productDto.getProductId() + " : " + "it must be equal to the sum of constituent Weight Rate", this
							.getClass().getName(), ApplicationDomain.CDS, TraceLogStatus.TRACELOG_WARN);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PsdSroAssocOblig> processSroAssocOblig(CDSReferenceObligationPairs cdsReferenceObligationPairs, List<PsdReferenceObligationPair> listReferenceObligationPair) {

		List<PsdSroAssocOblig> sroAssocObligList = new ArrayList<PsdSroAssocOblig>();

		if (cdsReferenceObligationPairs == null) {
			return sroAssocObligList;
		}

		List<SROAssociatedObligation> listSROAssociatedObligations;

		SROAssociatedObligation firstXmlAssociatedObligation;
		PsdReferenceObligationPair firstRefObNonSRO;

		SROAssociatedObligation secondXmlAssociatedObligation;
		PsdReferenceObligationPair secondRefObNonSRO;

		PsdReferenceObligationPair currentRefObSRO;

		if (cdsReferenceObligationPairs.getCdsReferenceObligationPairsSRO() != null
				&& ListUtils.isNotEmpty(cdsReferenceObligationPairs.getCdsReferenceObligationPairsSRO().getReferenceObligationPairSRO())) {

			for (ReferenceObligationPairSRO xmlObligPairSRO : cdsReferenceObligationPairs.getCdsReferenceObligationPairsSRO().getReferenceObligationPairSRO()) {

				if (xmlObligPairSRO.getSroAssociatedObligations() != null) {

					// Process list of associated Obligation of the current RefObSRO
					listSROAssociatedObligations = xmlObligPairSRO.getSroAssociatedObligations().getSroAssociatedObligation();

					if (ListUtils.isNotEmpty(listSROAssociatedObligations)) {

						if (listSROAssociatedObligations.size() == 1) {
							// Case 1 : Create new SRO Associated Oblig
							firstXmlAssociatedObligation = listSROAssociatedObligations.get(0);
							firstRefObNonSRO = loadPsdReferenceObligationPairIfExists(firstXmlAssociatedObligation.getReferenceObligationPairNonSROId(), listReferenceObligationPair);
							currentRefObSRO = loadPsdReferenceObligationPairIfExists(xmlObligPairSRO.getReferenceObligationPairId(), listReferenceObligationPair);

							if (firstRefObNonSRO != null && currentRefObSRO != null) {
								sroAssocObligList.add(buildSroAssociatedOblig(firstXmlAssociatedObligation, currentRefObSRO, firstRefObNonSRO));
							} else {
								logger.warn("Current RefOb SRO : " + xmlObligPairSRO.getReferenceObligationPairId() + " not found, or the RefOb Non SRO "
										+ firstXmlAssociatedObligation.getReferenceObligationPairNonSROId() + "in the SRO Associated Obligation not found");
							}

						} else {
							if (listSROAssociatedObligations.size() == 2) {
								// Case 2: Create a new SRO Associated Oblig and Update the old one

								firstXmlAssociatedObligation = listSROAssociatedObligations.get(0);
								firstRefObNonSRO = loadPsdReferenceObligationPairIfExists(firstXmlAssociatedObligation.getReferenceObligationPairNonSROId(), listReferenceObligationPair);

								secondXmlAssociatedObligation = listSROAssociatedObligations.get(1);
								secondRefObNonSRO = loadPsdReferenceObligationPairIfExists(secondXmlAssociatedObligation.getReferenceObligationPairNonSROId(), listReferenceObligationPair);

								currentRefObSRO = loadPsdReferenceObligationPairIfExists(xmlObligPairSRO.getReferenceObligationPairId(), listReferenceObligationPair);

								// Check uptade conditions
								if (firstRefObNonSRO != null && secondRefObNonSRO != null && currentRefObSRO != null) {

									// one and only one of the two RefOb Non SRO should have null as validity end date (XOR validity end date null)
									if ((firstXmlAssociatedObligation.getSroEndDate() == null && secondXmlAssociatedObligation.getSroEndDate() != null)
											|| (firstXmlAssociatedObligation.getSroEndDate() != null && secondXmlAssociatedObligation.getSroEndDate() == null)) {

										sroAssocObligList.add(buildSroAssociatedOblig(secondXmlAssociatedObligation, currentRefObSRO, secondRefObNonSRO));
										sroAssocObligList.add(buildSroAssociatedOblig(firstXmlAssociatedObligation, currentRefObSRO, firstRefObNonSRO));

									} else {
										logger.warn("Current RefOb SRO : " + xmlObligPairSRO.getReferenceObligationPairId()
												+ " has 2 SRO Associated Obligations with invalid value of Validity End Dates : " + firstRefObNonSRO.getValidityEndDate() + " or/and "
												+ secondRefObNonSRO.getValidityEndDate());
									}
								} else {
									logger.warn("SRO Associated Obligation rejected cause one or more elements not found in DB : Reference Obligation Pair SRO : "
											+ xmlObligPairSRO.getReferenceObligationPairId() + " , Reference Obligation Pair Non SRO 1 : "
											+ firstXmlAssociatedObligation.getReferenceObligationPairNonSROId() + " or Reference Obligation Pair Non SRO 2 : "
											+ secondXmlAssociatedObligation.getReferenceObligationPairNonSROId());
								}
							} else {
								if (listSROAssociatedObligations.size() > 2) {
									logger.error("Reference Obligation Pair SRO has more than 2 SRO Associated Obligations");
								} else {
									logger.error("Reference Obligation Pair SRO has no SRO Associated Obligation");
								}
							}
						}
					}
				}
			}
		}
		return sroAssocObligList;
	}

	/**
	 * Build a SRO Associated Obligation
	 *
	 * @param xmlAssociatedObligation
	 * @param refObSRO
	 * @param refObNonSRO
	 * @return
	 */
	private PsdSroAssocOblig buildSroAssociatedOblig(SROAssociatedObligation xmlAssociatedObligation, PsdReferenceObligationPair refObSRO, PsdReferenceObligationPair refObNonSRO) {
		PsdSroAssocOblig associatedBuilded = cdsDozerMapping.sroAssociatedObligationToEntity(xmlAssociatedObligation);
		associatedBuilded.getKey().setReferenceObligationPairSRO(refObSRO);
		associatedBuilded.getKey().setReferenceObligationPairNonSRO(refObNonSRO);
		return associatedBuilded;
	}

	/**
	 * Load and Check existing of a Reference Obligation Pair
	 *
	 * @param xmlObligPairId
	 * @return
	 */
	private PsdReferenceObligationPair loadPsdReferenceObligationPairIfExists(String xmlObligPairId, List<PsdReferenceObligationPair> listReferenceObligationPair) {
		PsdReferenceObligationPair result = new PsdReferenceObligationPair();

		for (PsdReferenceObligationPair refOb : listReferenceObligationPair) {
			if (refOb.getKey().equals(xmlObligPairId)) {
				return refOb;
			}
		}

		try {
			// Check if the ReferenceObligationPair exists
			return psdReferenceObligationPairDas.load(xmlObligPairId);
		} catch (NoResultException e) {
			result.setKey(xmlObligPairId);
			logger.warn("No Reference Obligation Pair found : " + xmlObligPairId);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public void createOrUpdateSroAssociatedObligations(List<PsdSroAssocOblig> listPsdSroAssocOblig) {
		for (PsdSroAssocOblig assoc : listPsdSroAssocOblig) {
			psdSroAssocObligDas.createOrUpdate(assoc);
		}
	}

}
