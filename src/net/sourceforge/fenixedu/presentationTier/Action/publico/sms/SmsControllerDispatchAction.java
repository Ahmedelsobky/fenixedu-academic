/*
 * Created on 11/Jun/2004
 *  
 */
package net.sourceforge.fenixedu.presentationTier.Action.publico.sms;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.fenixedu.applicationTier.Filtro.exception.FenixFilterException;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.FenixServiceException;
import net.sourceforge.fenixedu.applicationTier.utils.SmsUtil;
import net.sourceforge.fenixedu.applicationTier.utils.exceptions.SmsCommandConfigurationException;
import net.sourceforge.fenixedu.applicationTier.utils.smsResponse.SmsCommandManager;
import net.sourceforge.fenixedu.domain.sms.SmsDeliveryType;
import net.sourceforge.fenixedu.presentationTier.Action.base.FenixDispatchAction;
import net.sourceforge.fenixedu.presentationTier.Action.sop.utils.ServiceUtils;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * @author <a href="mailto:sana@ist.utl.pt">Shezad Anavarali </a>
 * @author <a href="mailto:naat@ist.utl.pt">Nadir Tarmahomed </a>
 * 
 */
public class SmsControllerDispatchAction extends FenixDispatchAction {

	public void updateDeliveryReport(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws FenixFilterException {

		if (checkRemoteAddress(request) == false)
			return;

		// check username/password
		String username = getFromRequest("deliveryUsername", request);
		String password = getFromRequest("deliveryPassword", request);
		String deliveryUsername = SmsUtil.getInstance().getDeliveryUsername();
		String deliveryPassword = SmsUtil.getInstance().getDeliveryPassword();

		if ((!username.equals(deliveryUsername))
				|| (!password.equals(deliveryPassword)))
			return;

		// read smsId and deliveryType
		Integer smsId = new Integer(getFromRequest("smsId", request));
		SmsDeliveryType smsDeliveryType = SmsDeliveryType
				.valueOf(getFromRequest("deliveryType", request));

		// update sms delivery report
		Object args[] = { smsId, smsDeliveryType };

		try {
			ServiceUtils.executeService(null, "UpdateSmsDeliveryReport", args);
		} catch (FenixServiceException e1) {
		}

	}

	public void receiveSms(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws FenixFilterException {

		// if(checkRemoteAddress(request) == false)
		// return;

		String sender = getFromRequest("sender", request);
		String text = getFromRequest("text", request);

		try {
			int indexSeparator = text.indexOf(" ");

			if (indexSeparator != -1) {
				String commandText = text.substring(0, indexSeparator);
				text = commandText.toUpperCase()
						+ text.substring(indexSeparator);

			}
			SmsCommandManager.getInstance().handleCommand(sender, text);
		} catch (SmsCommandConfigurationException e1) {
		}

	}

	/**
	 * @param host
	 * @param hostAddress
	 * @return
	 */
	private boolean checkRemoteAddress(HttpServletRequest request) {
		String host = SmsUtil.getInstance().getHost();
		String remoteAddress = request.getRemoteAddr();
		String hostAddress = "";

		// get host ip
		try {
			InetAddress address = InetAddress.getByName(host);
			byte[] ipAddress = address.getAddress();

			// Convert to dot representation
			for (int i = 0; i < ipAddress.length; i++) {
				if (i > 0) {
					hostAddress += ".";
				}
				hostAddress += ipAddress[i] & 0xFF;
			}

		} catch (UnknownHostException e) {
		}

		// check if the host is accepted
		return remoteAddress.equals(hostAddress);
	}

	private String getFromRequest(String parameter, HttpServletRequest request) {
		String parameterString = request.getParameter(parameter);
		if (parameterString == null) {
			parameterString = (String) request.getAttribute(parameter);
		}
		return parameterString;
	}

}