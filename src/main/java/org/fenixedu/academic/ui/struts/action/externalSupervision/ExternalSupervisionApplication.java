/**
 * Copyright © 2002 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Academic.
 *
 * FenixEdu Academic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Academic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Academic.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.academic.ui.struts.action.externalSupervision;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.fenixedu.academic.domain.student.RegistrationProtocol;
import org.fenixedu.academic.predicate.AccessControl;
import org.fenixedu.bennu.struts.annotations.Forward;
import org.fenixedu.bennu.struts.annotations.Forwards;
import org.fenixedu.bennu.struts.annotations.Mapping;
import org.fenixedu.bennu.struts.portal.StrutsApplication;

@StrutsApplication(bundle = "ExternalSupervisionResources", path = "external-supervision", titleKey = "externalSupervision",
        hint = "External Supervision", accessGroup = "role(EXTERNAL_SUPERVISOR)")
@Mapping(path = "/welcome", module = "externalSupervision")
@Forwards({ @Forward(name = "welcome", path = "/externalSupervision/externalSupervisionGreetings.jsp"),
        @Forward(name = "welcome_AFA", path = "/externalSupervision/externalSupervisionGreetingsAFA.jsp"),
        @Forward(name = "welcome_MA", path = "/externalSupervision/externalSupervisionGreetingsMA.jsp"),
        @Forward(name = "selectProtocol", path = "/externalSupervision/selectProtocol.jsp") })
public class ExternalSupervisionApplication extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        RegistrationProtocol registrationProtocol = AccessControl.getPerson().getOnlyRegistrationProtocol();
        if (registrationProtocol == null) {
            return mapping.findForward("welcome");
        }

        switch (registrationProtocol.getCode()) {
        case "AFA":
            return mapping.findForward("welcome_AFA");
        case "MA":
            return mapping.findForward("welcome_MA");
        default:
            return mapping.findForward("welcome");
        }
    }

    @StrutsApplication(bundle = "ExternalSupervisionResources", path = "consult", titleKey = "button.consult",
            hint = "External Supervision", accessGroup = "role(EXTERNAL_SUPERVISOR)")
    public static class ExternalSupervisionConsultApp {

    }

}
