/*
 * Created on 8/Set/2003
 */
package ServidorApresentacao.Action.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import DataBeans.InfoExecutionCourse;
import DataBeans.comparators.ExecutionPeriodComparator;
import ServidorAplicacao.IUserView;
import ServidorAplicacao.Servico.exceptions.FenixServiceException;
import ServidorAplicacao.Servico.exceptions.NonExistingServiceException;
import ServidorApresentacao.Action.base.FenixAction;
import ServidorApresentacao.Action.exceptions.FenixActionException;
import ServidorApresentacao.Action.exceptions.NonExistingActionException;
import ServidorApresentacao.Action.sop.utils.ServiceUtils;
import ServidorApresentacao.Action.sop.utils.SessionConstants;
import ServidorApresentacao.Action.sop.utils.SessionUtils;

/**
 * @author lmac1
 */
public class ReadExecutionPeriodToAssociateExecutionCoursesAction extends FenixAction {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws FenixActionException {

		IUserView userView = SessionUtils.getUserView(request);
		
		Integer curricularCourseId =  new Integer(request.getParameter("curricularCourseId"));
		
		Object args1[] = { curricularCourseId };
		
		
		List executionCoursesList = null; 
		try {
				executionCoursesList = (List) ServiceUtils.executeService(userView, "ReadExecutionCoursesByCurricularCourse", args1);
		
		} catch (NonExistingServiceException e) {
					throw new NonExistingActionException("message.nonExistingCurricularCourse", "", e);
				} catch (FenixServiceException fenixServiceException) {
					throw new FenixActionException(fenixServiceException.getMessage());
				}
				
		List unavailableExecutionPeriodsIds = new ArrayList();
		Iterator iter = executionCoursesList.iterator();
		while(iter.hasNext()){
		InfoExecutionCourse infoExecutionCourse = (InfoExecutionCourse) iter.next();
System.out.println(infoExecutionCourse.getInfoExecutionPeriod().getIdInternal());
		unavailableExecutionPeriodsIds.add((Integer) infoExecutionCourse.getInfoExecutionPeriod().getIdInternal());
		}
				
		Object args2[] = {unavailableExecutionPeriodsIds };
		try {
			List infoExecutionPeriods = (List) ServiceUtils.executeService(userView, "ReadAvailableExecutionPeriods", args2);

			if (infoExecutionPeriods != null && !infoExecutionPeriods.isEmpty()) {

				Collections.sort(infoExecutionPeriods, new ExecutionPeriodComparator());

				if (infoExecutionPeriods != null && !infoExecutionPeriods.isEmpty()) {
					request.setAttribute(SessionConstants.LIST_EXECUTION_PERIODS, infoExecutionPeriods);
				}

			}
		} catch (FenixServiceException ex) {
			throw new FenixActionException("Problemas de comunicação com a base de dados.", ex);
		}

		return mapping.findForward("viewExecutionPeriods");
	}
}
