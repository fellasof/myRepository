package com.lchclearnet.cds.services.connector;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.jdom2.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lchclearnet.cds.common.exception.CdsTechnicalException;
import com.lchclearnet.cds.common.utils.DateUtils;
import com.lchclearnet.cds.common.utils.ResourcesHelper;
import com.lchclearnet.cds.common.utils.StringUtils;
import com.lchclearnet.cds.common.xml.XmlToBoParser;
import com.lchclearnet.cds.domain.shared.das.CdsMarkitDataFlowDas;
import com.lchclearnet.cds.domain.shared.entity.CdsMarkitDataFlow;
import com.lchclearnet.cds.domain.shared.entity.enums.TradingSystemType;
import com.lchclearnet.cds.domain.trade.entity.enums.FamilyType;
import com.lchclearnet.cds.services.calendar.CdsFinancialCalendarService;
import com.lchclearnet.cds.services.connector.enums.ConnectorMessageTypeEnum;
import com.lchclearnet.cds.services.connector.exception.ConnectorParsingException;
import com.lchclearnet.cds.services.connector.jaxb.markitserv.SCML;

/**
 * <pre>
 * Implementation of <b>ConnectorToMarkitservParsorService</b> :  
 * Parse all messages incoming or sent between cds-markitserv-connector and MarkitServ
 * </pre>
 */
@Service
public class ConnectorToMarkitservParserServiceImpl implements ConnectorToMarkitservParserService {

	@Autowired
	private CdsMarkitDataFlowDas cdsMarkitDataFlowDas;

	@Autowired
	private CdsFinancialCalendarService calendarService;

	private static final String CLEARING_XML_MAPPING = "mapping/clearingXMLMapping.xml";

	public static final String IDX_FAMILY_TYPE = "IDX_FAMILY_TYPE";
	public static final String SN_FAMILY_TYPE = "SN_FAMILY_TYPE";
	public static final String PARTY_TRADE_IDENTIFIER = "PARTY_TRADE_IDENTIFIER";
	public static final String CB_PARTY_REFERENCE = "CB_PARTY_REFERENCE";
	public static final String CB_PARTY_ID = "CB_PARTY_ID";
	public static final String TMF_BUYER = "TMF_BUYER";
	public static final String TMF_SELLER = "TMF_SELLER";
	public static final String INDEX_RED_CODE = "INDEX_RED_CODE";
	public static final String REF_ENTITY_NAME = "REF_ENTITY_NAME";
	public static final String REF_OBLIG_CODE = "REF_OBLIG_CODE";
	public static final String NOTIONAL = "NOTIONAL";
	public static final String PARTY_ID_SWAPSWIRE = "PARTY_ID_SWAPSWIRE";
	public static final String PARTY_CLIENT = "PARTY_CLIENT";

	/**
	 * Parse and persist xmlContent in function of message type
	 */
	@Override
	public SCML parseScml(String xmlContent) {

		JAXBContext jc;
		SCML message = null;
		try {
			jc = JAXBContext.newInstance("com.lchclearnet.cds.services.connector.jaxb.markitserv");
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			message = (SCML) unmarshaller.unmarshal(new StreamSource(new StringReader(xmlContent)));

			ConnectorMessageTypeEnum connectorMessageTypeEnum = identifyMessageType(message);

			if (connectorMessageTypeEnum != null) {
				persist(message, connectorMessageTypeEnum);
			}

		} catch (JAXBException e) {
			throw new ConnectorParsingException(e.getMessage());
		}
		return message;
	}

	/**
	 * Identify RECEIVED/REJECT/ACCEPT Message
	 * 
	 * @param message
	 * @return
	 */
	private ConnectorMessageTypeEnum identifyMessageType(SCML message) {

		if (message != null && message.getStatusUpdate() != null && message.getStatusUpdate().getClearingServicePartyAStatusUpdate() != null
				&& message.getStatusUpdate().getClearingServicePartyBStatusUpdate() != null) {
			if (message.getStatusUpdate().getClearingServicePartyAStatusUpdate().equalsIgnoreCase(ConnectorMessageTypeEnum.Received.name())
					&& message.getStatusUpdate().getClearingServicePartyBStatusUpdate().equalsIgnoreCase(ConnectorMessageTypeEnum.Received.name())) {
				return ConnectorMessageTypeEnum.Received;
			}
			if (message.getStatusUpdate().getClearingServicePartyAStatusUpdate().equalsIgnoreCase(ConnectorMessageTypeEnum.Parked.name())
					&& message.getStatusUpdate().getClearingServicePartyBStatusUpdate().equalsIgnoreCase(ConnectorMessageTypeEnum.Parked.name())) {
				return ConnectorMessageTypeEnum.Parked;
			}

		}

		if (message != null && message.getStatusUpdate() != null && message.getStatusUpdate().getClearingServicePartyAStatusUpdate() != null
				&& message.getStatusUpdate().getClearingServicePartyBStatusUpdate() != null) {
			if (message.getStatusUpdate().getClearingServicePartyAStatusUpdate().equalsIgnoreCase(ConnectorMessageTypeEnum.AwaitingAcceptance.name())
					&& message.getStatusUpdate().getClearingServicePartyBStatusUpdate().equalsIgnoreCase(ConnectorMessageTypeEnum.AwaitingAcceptance.name())) {
				return ConnectorMessageTypeEnum.AwaitingAcceptance;
			}
		}

		if (message != null && message.getReject() != null && message.getReject().getRejectReason() != null && StringUtils.isNotEmpty(message.getReject().getRejectReason().getReasonCode())) {
			return ConnectorMessageTypeEnum.Reject;
		}

		if (message != null && message.getAccept() != null && StringUtils.isNotEmpty(message.getAccept().getAcceptText())) {
			return ConnectorMessageTypeEnum.Accept;
		}

		return null;
	}

	/**
	 * Persist a CdsMarkitDataFlow in function of SCML message
	 * 
	 * @param message
	 * @param connectorMessageTypeEnum
	 */
	private void persist(SCML message, ConnectorMessageTypeEnum connectorMessageTypeEnum) {

		CdsMarkitDataFlow cdsMarkitDataFlow = new CdsMarkitDataFlow();

		cdsMarkitDataFlow.setMessaegeSender(TradingSystemType.Connector);
		cdsMarkitDataFlow.setMessageReceiver(TradingSystemType.Markit);
		cdsMarkitDataFlow.setMessageReception(new Date());
		cdsMarkitDataFlow.setTradeid(message.getMatchingServiceTradeReference());
		cdsMarkitDataFlow.setBusinessDate(calendarService.getCurrentBusinessDate());
		cdsMarkitDataFlow.setMessageType(connectorMessageTypeEnum);

		cdsMarkitDataFlowDas.persist(cdsMarkitDataFlow);
	}

	@Override
	public CdsMarkitDataFlow parseClearingXml(String xmlContent) {
		try {
			final String mapping = ResourcesHelper.getResourceAsString(CLEARING_XML_MAPPING);
			final Map<String, Object> mappedColumns = XmlToBoParser.parse(xmlContent, new StringReader(mapping));
			CdsMarkitDataFlow markitDataFlow = createMarkitDataFlowFromMapping(mappedColumns);
			return cdsMarkitDataFlowDas.createOrUpdate(markitDataFlow);
		} catch (final JDOMException e) {
			throw new CdsTechnicalException("error during Xml parsing of a Clearing XML", e);
		} catch (final IOException e) {
			throw new CdsTechnicalException("error during Xml parsing of a Clearing XML", e);
		}
	}

	/**
	 * Create the entity CdsMarkitDataFlow from the map mappedColumns. this latter is generated by parssing the message XML
	 * 
	 * @param mappedColumns
	 * @return the entity CdsMarkitDataFlow
	 */
	@SuppressWarnings("unchecked")
	private CdsMarkitDataFlow createMarkitDataFlowFromMapping(Map<String, Object> mappedColumns) {
		Map<String, String> mapParties = new HashMap<String, String>();
		if ((Map<String, String>) mappedColumns.get(PARTY_CLIENT) != null) {
			mapParties = (Map<String, String>) mappedColumns.get(PARTY_CLIENT);
		}
		Map<String, String> mapPartyTradeIdentifier = new HashMap<String, String>();
		if ((Map<String, String>) mappedColumns.get(PARTY_TRADE_IDENTIFIER) != null) {
			mapPartyTradeIdentifier = (Map<String, String>) mappedColumns.get(PARTY_TRADE_IDENTIFIER);
		}
		String PartyIdSwapwire = (String) mappedColumns.get(PARTY_ID_SWAPSWIRE);
		CdsMarkitDataFlow markitDataFlow = new CdsMarkitDataFlow();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		markitDataFlow.setMessageReception(calendar.getTime());
		markitDataFlow.setBusinessDate(DateUtils.removeTime(calendar.getTime()));
		if (PartyIdSwapwire != null) {
			markitDataFlow.setTradeid(mapPartyTradeIdentifier.get(PartyIdSwapwire));
		}
		if ((String) mappedColumns.get(TMF_BUYER) != null) {
			markitDataFlow.setCodeTmfBuyer(mapParties.get((String) mappedColumns.get(TMF_BUYER)));
		}
		if ((String) mappedColumns.get(TMF_SELLER) != null) {
			markitDataFlow.setCodeTmfSeller(mapParties.get((String) mappedColumns.get(TMF_SELLER)));
		}
		if ((String) mappedColumns.get(CB_PARTY_REFERENCE) != null && (String) mappedColumns.get(CB_PARTY_ID) != null
				&& ((String) mappedColumns.get(CB_PARTY_REFERENCE)).equals((String) mappedColumns.get(TMF_BUYER))) {
			markitDataFlow.setCodeCmfBuyer((String) mappedColumns.get(CB_PARTY_ID));
		} else {
			markitDataFlow.setCodeCmfBuyer(markitDataFlow.getCodeTmfBuyer());
		}
		if ((String) mappedColumns.get(CB_PARTY_REFERENCE) != null && (String) mappedColumns.get(CB_PARTY_ID) != null
				&& ((String) mappedColumns.get(CB_PARTY_REFERENCE)).equals((String) mappedColumns.get(TMF_SELLER))) {
			markitDataFlow.setCodeCmfSller((String) mappedColumns.get((String) mappedColumns.get(CB_PARTY_ID)));
		} else {
			markitDataFlow.setCodeCmfSller(markitDataFlow.getCodeTmfSeller());
		}

		markitDataFlow.setIndexRedCode((String) mappedColumns.get(INDEX_RED_CODE));
		markitDataFlow.setNotional((BigDecimal) mappedColumns.get(NOTIONAL));
		if ((String) mappedColumns.get(IDX_FAMILY_TYPE) != null) {
			markitDataFlow.setMessageFamilyType(FamilyType.INDEX);
		} else if ((String) mappedColumns.get(SN_FAMILY_TYPE) != null) {
			markitDataFlow.setMessageFamilyType(FamilyType.SINGLENAME);
			markitDataFlow.setReferenceObligationCode((String) mappedColumns.get(REF_OBLIG_CODE));
			markitDataFlow.setReferenceEntityName((String) mappedColumns.get(REF_ENTITY_NAME));
		}

		markitDataFlow.setMessageType(ConnectorMessageTypeEnum.Trade);

		markitDataFlow.setMessaegeSender(TradingSystemType.Markit);
		markitDataFlow.setMessageReceiver(TradingSystemType.Connector);

		return markitDataFlow;

	}
}