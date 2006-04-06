package net.sourceforge.fenixedu.applicationTier.Servico.manager.executionCourseManagement;

import net.sourceforge.fenixedu.applicationTier.Service;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.FenixServiceException;
import net.sourceforge.fenixedu.domain.CurricularCourse;
import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.persistenceTier.ExcepcaoPersistencia;

/**
 * 
 * @author naat
 * 
 */

public class DissociateCurricularCourseByExecutionCourseId extends Service {

    public void run(Integer executionCourseId, Integer curricularCourseId) throws FenixServiceException,
            ExcepcaoPersistencia {
        ExecutionCourse executionCourse = rootDomainObject.readExecutionCourseByOID(executionCourseId);

        CurricularCourse curricularCourse = (CurricularCourse) persistentObject.readByOID(
                CurricularCourse.class, curricularCourseId);

        curricularCourse.removeAssociatedExecutionCourses(executionCourse);

    }
}
