/*
 * Created on 14/Ago/2003
 */
package ServidorApresentacao.Action.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.util.LabelValueBean;
import org.apache.struts.validator.DynaValidatorForm;

import DataBeans.InfoDegreeCurricularPlan;
import DataBeans.InfoExecutionDegree;
import DataBeans.InfoExecutionYear;
import DataBeans.InfoTeacher;
import ServidorAplicacao.IUserView;
import ServidorAplicacao.Servico.exceptions.ExistingServiceException;
import ServidorAplicacao.Servico.exceptions.FenixServiceException;
import ServidorAplicacao.Servico.exceptions.NonExistingServiceException;
import ServidorApresentacao.Action.base.FenixDispatchAction;
import ServidorApresentacao.Action.exceptions.ExistingActionException;
import ServidorApresentacao.Action.exceptions.FenixActionException;
import ServidorApresentacao.Action.exceptions.NonExistingActionException;
import ServidorApresentacao.Action.sop.utils.ServiceUtils;
import ServidorApresentacao.Action.sop.utils.SessionUtils;

/**
 * @author lmac1
 */

public class InsertExecutionDegreeDispatchAction extends FenixDispatchAction {


	public ActionForward prepareInsert(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response)
			throws FenixActionException {

				IUserView userView = SessionUtils.getUserView(request);						
				
				String label, value;
				List result = null;
				
		/*   Needed service and creation of bean of InfoTeachers for use in jsp   */    
				try {
						result = (List) ServiceUtils.executeService(userView, "ReadAllTeachers", null);
				} catch (FenixServiceException e) {
					throw new FenixActionException(e);
				}
							
				ArrayList infoTeachersList = new ArrayList();
				if(result != null) {
					InfoTeacher infoTeacher;
					Iterator iter = result.iterator();
					while(iter.hasNext()) {
						infoTeacher = (InfoTeacher) iter.next();
						value = infoTeacher.getIdInternal().toString();
						label = infoTeacher.getTeacherNumber() + " - " + infoTeacher.getInfoPerson().getNome();
						infoTeachersList.add(new LabelValueBean(label, value));
					}
					request.setAttribute("infoTeachersList", infoTeachersList);
				}
				
		/*   Needed service and creation of bean of InfoExecutionYears for use in jsp   */
				try {
						result = (List) ServiceUtils.executeService(userView, "ReadAllExecutionYears", null);
				} catch (FenixServiceException e) {
					throw new FenixActionException(e);
				}
							
				ArrayList infoExecutionYearsList = new ArrayList();
				if(result != null) {
					InfoExecutionYear infoExecutionYear;
					Iterator iter = result.iterator();
					while(iter.hasNext()) {
						infoExecutionYear = (InfoExecutionYear) iter.next();
						value = infoExecutionYear.getIdInternal().toString();
						label = infoExecutionYear.getYear();
						infoExecutionYearsList.add(new LabelValueBean(label, value));
					}
					request.setAttribute("infoExecutionYearsList", infoExecutionYearsList);
				}
				DynaActionForm dynaForm = (DynaActionForm) form;
				dynaForm.set("tempExamMap", "true");
				return mapping.findForward("insertExecutionDegree");
	}


	public ActionForward insert(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response)
		throws FenixActionException {

			IUserView userView = SessionUtils.getUserView(request);
		
		Integer degreeCurricularPlanId = new Integer(request.getParameter("degreeCurricularPlanId"));
    	
		DynaActionForm dynaForm = (DynaValidatorForm) form;
		
		InfoExecutionYear infoExecutionYear = new InfoExecutionYear();
		infoExecutionYear.setIdInternal(new Integer((String) dynaForm.get("executionYearId")));
		InfoTeacher infoTeacher = new InfoTeacher();
		infoTeacher.setIdInternal(new Integer((String) dynaForm.get("coordinatorId")));
		InfoDegreeCurricularPlan infoDegreeCurricularPlan = new InfoDegreeCurricularPlan();
		infoDegreeCurricularPlan.setIdInternal(degreeCurricularPlanId);
		
		InfoExecutionDegree infoExecutionDegree = new InfoExecutionDegree();
		infoExecutionDegree.setInfoExecutionYear(infoExecutionYear);
		infoExecutionDegree.setInfoCoordinator(infoTeacher);
		infoExecutionDegree.setInfoDegreeCurricularPlan(infoDegreeCurricularPlan);

		infoExecutionDegree.setTemporaryExamMap(new Boolean((String) dynaForm.get("tempExamMap")));
		
		Object args[] = { infoExecutionDegree };
		
				
		try {
			ServiceUtils.executeService(userView, "InsertExecutionDegreeAtDegreeCurricularPlan", args);
				 
		} catch (ExistingServiceException ex) {
			throw new ExistingActionException(ex.getMessage(), ex);
		} catch (NonExistingServiceException exception) {
			throw new NonExistingActionException(exception.getMessage(), mapping.findForward("readDegreeCurricularPlan"));
		} catch (FenixServiceException e) {
			throw new FenixActionException(e);
		}
		
		return mapping.findForward("readDegreeCurricularPlan");
	}			
}