package com.lchclearnet.cds.services.connector;

import java.io.StringReader;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.lchclearnet.cds.common.exception.CdsBusinessException;
import com.lchclearnet.cds.common.exception.CdsTechnicalException;
import com.lchclearnet.cds.common.utils.ResourcesHelper;
import com.lchclearnet.cds.common.utils.StringUtils;
import com.lchclearnet.cds.domain.common.enums.ApplicationDomain;
import com.lchclearnet.cds.domain.common.enums.TraceLogStatus;
import com.lchclearnet.cds.domain.shared.das.CdsMarkitDataFlowDas;
import com.lchclearnet.cds.domain.shared.entity.CdsMarkitDataFlow;
import com.lchclearnet.cds.domain.shared.entity.enums.TradingSystemType;
import com.lchclearnet.cds.domain.trade.entity.enums.FamilyType;
import com.lchclearnet.cds.services.calendar.CdsFinancialCalendarService;
import com.lchclearnet.cds.services.connector.enums.ConnectorMessageTypeEnum;
import com.lchclearnet.cds.services.connector.exception.ConnectorParsingException;
import com.lchclearnet.cds.services.connector.jaxb.clearlink.ClientClearingTradeStatusSCML;
import com.lchclearnet.cds.services.connector.jaxb.clearlink.SCML;
import com.lchclearnet.cds.services.connector.jaxb.clearlink.StatusUpdate;
import com.lchclearnet.cds.services.connector.jaxb.connector.ClientClearingTradeFpml;
import com.lchclearnet.cds.services.connector.jaxb.connector.CreditDefaultSwap;
import com.lchclearnet.cds.services.connector.jaxb.connector.FpMLSection;
import com.lchclearnet.cds.services.connector.jaxb.connector.GeneralTerms;
import com.lchclearnet.cds.services.connector.jaxb.connector.IndexReferenceInformation;
import com.lchclearnet.cds.services.connector.jaxb.connector.Party;
import com.lchclearnet.cds.services.connector.jaxb.connector.PartyReference;
import com.lchclearnet.cds.services.connector.jaxb.connector.PartyTradeIdentifier;
import com.lchclearnet.cds.services.connector.jaxb.connector.ReferenceInformation;
import com.lchclearnet.cds.services.connector.jaxb.connector.ReferenceObligation;
import com.lchclearnet.cds.services.connector.jaxb.connector.Trade;
import com.lchclearnet.cds.services.error.CdsTraceLogService;

/**
 * <pre>
 * Implementation of <b>ConnectorToClearlinkParserService</b> :  
 * Parse all messages incoming or sent between cds-markitserv-connector and Clearlink Adapter
 * </pre>
 */
@Service
public class ConnectorToClearlinkParserServiceImpl implements ConnectorToClearlinkParserService {

	private static final Logger logger = LoggerFactory.getLogger(ConnectorToClearlinkParserServiceImpl.class);

	public static final String MESSAGE_NOVATED = "Novated";

	public static final String MESSAGE_REJECT = "REJECT";

	public static final String MESSAGE_RECEIVED = "Received";

	public static final String MESSAGE_PARKED = "Parked";

	public static final String MESSAGE_AWAITING_ACCEPTANCE = "AwaitingAcceptance";

	public static final String PARTY_C = "partyC";

	public static final String XSD_CLEARNET_CONNECTOR = "xsd/clientClearingTradeFpml.xsd";

	@Autowired
	private CdsFinancialCalendarService calendarService;

	@Autowired
	private CdsTraceLogService traceService;

	@Autowired
	private CdsMarkitDataFlowDas cdsMarkitDataFlowDas;

	@Override
	public ClientClearingTradeStatusSCML parseClientClearingTradeStatusScml(String xmlContent) {

		JAXBContext jc;
		ClientClearingTradeStatusSCML message = null;
		try {
			jc = JAXBContext.newInstance("com.lchclearnet.cds.services.connector.jaxb.clearlink");
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			message = (ClientClearingTradeStatusSCML) unmarshaller.unmarshal(new StreamSource(new StringReader(xmlContent)));

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
	 * Identify Novated/REJECT/ACCEPT Message
	 * 
	 * @param message
	 * @return
	 */
	private ConnectorMessageTypeEnum identifyMessageType(ClientClearingTradeStatusSCML message) {

		SCML scml = null;
		if (message != null) {
			scml = message.getScml();
			if (scml != null) {
				StatusUpdate statusUpdate = scml.getStatusUpdate();
				if (statusUpdate != null) {
					if (StringUtils.isNotEmpty(statusUpdate.getClearingServiceStatus())) {
						if (MESSAGE_NOVATED.equalsIgnoreCase(statusUpdate.getClearingServiceStatus())) {
							return ConnectorMessageTypeEnum.Novated;
						}
						if (MESSAGE_REJECT.equalsIgnoreCase(statusUpdate.getClearingServiceStatus())) {
							return ConnectorMessageTypeEnum.Reject;
						}
						if (MESSAGE_RECEIVED.equalsIgnoreCase(statusUpdate.getClearingServiceStatus())) {
							return ConnectorMessageTypeEnum.Received;
						}
						if (MESSAGE_PARKED.equalsIgnoreCase(statusUpdate.getClearingServiceStatus())) {
							return ConnectorMessageTypeEnum.Parked;
						}
						if (MESSAGE_AWAITING_ACCEPTANCE.equalsIgnoreCase(statusUpdate.getClearingServiceStatus())) {
							return ConnectorMessageTypeEnum.AwaitingAcceptance;
						}
					}
				}
			}

		}

		return null;
	}

	@Override
	public void parseClientClearingTradeStatusFpml(String xmlContent) {
		ClientClearingTradeFpml fpmlMessage = null;
		CdsMarkitDataFlow bean = null;
		try {
			final StringReader reader = new StringReader(xmlContent);
			XMLInputFactory xmlif = XMLInputFactory.newInstance();
			XMLStreamReader xmlr = xmlif.createXMLStreamReader(reader);
			final JAXBContext context = JAXBContext.newInstance(ClientClearingTradeFpml.class);
			final Unmarshaller unmarshaller = context.createUnmarshaller();
			URL url = ResourcesHelper.getResource(XSD_CLEARNET_CONNECTOR);
			unmarshaller.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(url));
			JAXBElement<ClientClearingTradeFpml> jaxbElements = unmarshaller.unmarshal(xmlr, ClientClearingTradeFpml.class);
			fpmlMessage = jaxbElements.getValue();
			bean = this.integrateMessageFPML(fpmlMessage);
			cdsMarkitDataFlowDas.persist(bean);

		} catch (final JAXBException | SAXException | XMLStreamException e) {
			handleInvalidXmlMessage(xmlContent, e);
		}
	}

	/**
	 * Persist of Message FPML
	 * 
	 * @param element
	 * @return a CdsMarkitDataFlow Entity
	 */
	private CdsMarkitDataFlow integrateMessageFPML(ClientClearingTradeFpml element) {

		CdsMarkitDataFlow dataFlow = new CdsMarkitDataFlow();
		if (element != null) {

			FpMLSection section = element.getFpML();
			if (section != null) {

				List<Party> listParty = section.getParty();

				Trade trade = section.getTrade();
				if (trade != null) {
					CreditDefaultSwap defaultSwap = trade.getCreditDefaultSwap();
					if (defaultSwap != null) {
						GeneralTerms generalTerms = defaultSwap.getGeneralTerms();
						if (generalTerms != null) {
							// set a Notional value
							dataFlow.setNotional(defaultSwap.getProtectionTerms().getCalculationAmount().getAmount());

							PartyReference seller = generalTerms.getSellerPartyReference();
							PartyReference buyer = generalTerms.getBuyerPartyReference();

							if (seller != null) {
								String str1 = seller.getHref();
								for (Party party : listParty) {
									if (str1.equals(party.getId())) {
										// String string = getPartyName(party, str1);
										dataFlow.setCodeTmfSeller(party.getPartyId());

									}
								}
							}
							if (buyer != null) {
								String str2 = buyer.getHref();
								for (Party party : listParty) {
									if (str2.equals(party.getId())) {
										dataFlow.setCodeTmfBuyer(party.getPartyId());
									}
								}

							}
							IndexReferenceInformation indexReferenceInformation = generalTerms.getIndexReferenceInformation();
							if (indexReferenceInformation != null) {
								dataFlow.setMessageFamilyType(FamilyType.INDEX);
								dataFlow.setIndexRedCode(indexReferenceInformation.getIndexId().getValue());
							}
							ReferenceInformation information = generalTerms.getReferenceInformation();
							if (information != null) {
								dataFlow.setMessageFamilyType(FamilyType.SINGLENAME);
								dataFlow.setReferenceEntityName(information.getReferenceEntity().getEntityName().getValue());

								ReferenceObligation obligation = information.getReferenceObligation();
								if (obligation != null && StringUtils.isNotEmpty(obligation.getValue())) {
									dataFlow.setReferenceObligationCode(obligation.getValue());
								}
							}
							List<PartyTradeIdentifier> listPartyTradeIdentifier = trade.getTradeHeader().getPartyTradeIdentifier();
							for (PartyTradeIdentifier identifier : listPartyTradeIdentifier) {

								if (identifier.getPartyReference().getHref() != null && PARTY_C.equals(identifier.getPartyReference().getHref())) {
									dataFlow.setTradeid(identifier.getTradeId().getValue());
								}

							}
						}
					}
				}
			}
		} else {
			throw new CdsBusinessException();
		}
		dataFlow.setMessageReception(new Date());
		dataFlow.setBusinessDate(calendarService.getCurrentBusinessDate());
		dataFlow.setMessaegeSender(TradingSystemType.Connector);
		dataFlow.setMessageReceiver(TradingSystemType.Clearlink);
		dataFlow.setMessageType(ConnectorMessageTypeEnum.Trade);
		return dataFlow;

	}

	/**
	 * 
	 * @param messageIn
	 * @param e
	 */
	private void handleInvalidXmlMessage(String messageIn, Exception e) {
		logger.error("Unmarshalling error", e);
		traceService.traceLog("Error integrating Fpml Message.  Message does not conform to the XSD schema.  Please refer to the log for more information.", "ConnectorToClearlinkParserService",
				ApplicationDomain.CDS, TraceLogStatus.TRACELOG_ERR);
		throw new CdsTechnicalException("Unmarshalling error", e);

	}

	/**
	 * Persist a CdsMarkitDataFlow in function of ClientClearingTradeStatusSCML message
	 * 
	 * @param message
	 * @param connectorMessageTypeEnum
	 */
	private void persist(ClientClearingTradeStatusSCML message, ConnectorMessageTypeEnum connectorMessageTypeEnum) {

		CdsMarkitDataFlow cdsMarkitDataFlow = new CdsMarkitDataFlow();

		cdsMarkitDataFlow.setMessaegeSender(TradingSystemType.Clearlink);
		cdsMarkitDataFlow.setMessageReceiver(TradingSystemType.Connector);
		cdsMarkitDataFlow.setMessageReception(new Date());

		// Rule for Trade Identifier of the ClearingXML message.
		// If filled: <matchingServiceTradeReference> value
		// Else: <correlationId> value
		if (message != null && message.getScml() != null) {
			if (StringUtils.isNotEmpty(message.getScml().getMatchingServiceTradeReference())) {
				cdsMarkitDataFlow.setTradeid(message.getScml().getMatchingServiceTradeReference());
			} else {
				cdsMarkitDataFlow.setTradeid(message.getScml().getCorrelationId());
			}
		}

		cdsMarkitDataFlow.setBusinessDate(calendarService.getCurrentBusinessDate());
		cdsMarkitDataFlow.setMessageType(connectorMessageTypeEnum);

		cdsMarkitDataFlowDas.persist(cdsMarkitDataFlow);
	}
}