/**
 * Copyright © 2002 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Core.
 *
 * FenixEdu Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Core.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.fenixedu.domain.organizationalStructure;

import java.util.Comparator;

import net.sourceforge.fenixedu.domain.DomainObjectUtil;
import net.sourceforge.fenixedu.domain.ExecutionInterval;
import net.sourceforge.fenixedu.domain.Person;
import net.sourceforge.fenixedu.domain.exceptions.DomainException;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.joda.time.YearMonthDay;

import pt.ist.fenixframework.Atomic;

public class PersonFunction extends PersonFunction_Base {

    public final static Comparator<PersonFunction> COMPARATOR_BY_BEGIN_DATE = new ComparatorChain();

    public final static Comparator<PersonFunction> COMPARATOR_BY_PERSON_NAME = new ComparatorChain();

    static {
        ((ComparatorChain) COMPARATOR_BY_BEGIN_DATE).addComparator(new BeanComparator("beginDate"));
        ((ComparatorChain) COMPARATOR_BY_BEGIN_DATE).addComparator(DomainObjectUtil.COMPARATOR_BY_ID);

        ((ComparatorChain) COMPARATOR_BY_PERSON_NAME).addComparator(new BeanComparator("person.name"));
        ((ComparatorChain) COMPARATOR_BY_PERSON_NAME).addComparator(DomainObjectUtil.COMPARATOR_BY_ID);
    }

    public PersonFunction(Party parentParty, Party childParty, Function function, YearMonthDay begin, YearMonthDay end,
            Double credits) {
        super();
        setParentParty(parentParty);
        setChildParty(childParty);
        setAccountabilityType(function);
        setCredits(credits);
        setOccupationInterval(begin, end);
    }

    public PersonFunction(Party parentParty, Party childParty, Function function, ExecutionInterval executionInterval,
            Double credits) {
        super();
        setParentParty(parentParty);
        setChildParty(childParty);
        setAccountabilityType(function);
        setCredits(credits);
        setOccupationInterval(executionInterval);
    }

    public PersonFunction(Party parentParty, Party childParty, Function function, YearMonthDay begin, YearMonthDay end) {
        this(parentParty, childParty, function, begin, end, 0.0);
    }

    public PersonFunction(Person parentParty, Person childParty, Function function, YearMonthDay begin, YearMonthDay end) {
        super();
        setParentParty(parentParty);
        setChildParty(childParty);
        setAccountabilityType(function);
        setCredits(0.0);
        setOccupationInterval(begin, end);
    }

    protected PersonFunction() {
        super();
    }

    @Override
    public YearMonthDay getBeginDate() {
        return getExecutionInterval() != null ? getExecutionInterval().getBeginDateYearMonthDay() : super.getBeginDate();
    }

    @Override
    public YearMonthDay getEndDate() {
        return getExecutionInterval() != null ? getExecutionInterval().getEndDateYearMonthDay() : super.getEndDate();
    }

    @Override
    protected boolean checkDateInterval() {
        return getExecutionInterval() != null ? true : super.checkDateInterval();
    }

    public void edit(YearMonthDay begin, YearMonthDay end, Double credits) {
        setCredits(credits);
        setOccupationInterval(begin, end);
    }

    @Override
    public void setChildParty(Party childParty) {
        if (childParty == null || !childParty.isPerson()) {
            throw new DomainException("error.invalid.child.party");
        }
        super.setChildParty(childParty);
    }

    @Override
    public void setParentParty(Party parentParty) {
        if (parentParty == null || !parentParty.isUnit()) {
            throw new DomainException("error.invalid.parent.party");
        }
        super.setParentParty(parentParty);
    }

    public void setParentParty(Person parentParty) {
        if (parentParty == null) {
            throw new DomainException("error.invalid.parent.party");
        }
        super.setParentParty(parentParty);
    }

    public void setOccupationInterval(ExecutionInterval executionInterval) {
        checkPersonFunctionDatesIntersection(getPerson(), getUnit(), getFunction(), executionInterval.getBeginDateYearMonthDay(),
                executionInterval.getEndDateYearMonthDay());
        super.setExecutionInterval(executionInterval);
    }

    public void setOccupationInterval(YearMonthDay beginDate, YearMonthDay endDate) {
        checkPersonFunctionDatesIntersection(getPerson(), getUnit(), getFunction(), beginDate, endDate);
        super.setBeginDate(beginDate);
        super.setEndDate(endDate);
    }

    @Override
    public void setBeginDate(YearMonthDay beginDate) {
        throw new DomainException("error.invalid.operation");
    }

    @Override
    public void setEndDate(YearMonthDay endDate) {
        throw new DomainException("error.invalid.operation");
    }

    @Override
    public Double getCredits() {
        if (super.getCredits() == null) {
            return 0d;
        }
        return super.getCredits();
    }

    public Person getPerson() {
        return (Person) getChildParty();
    }

    public Unit getUnit() {
        return getParentParty() instanceof Unit ? (Unit) getParentParty() : null;
    }

    public Function getFunction() {
        return (Function) getAccountabilityType();
    }

    @Override
    public boolean isPersonFunction() {
        return true;
    }

    private void checkPersonFunctionDatesIntersection(Person person, Unit unit, Function function, YearMonthDay begin,
            YearMonthDay end) {
        checkBeginDateAndEndDate(begin, end);
        for (PersonFunction personFunction : person.getPersonFunctions(unit)) {
            if (!personFunction.equals(this) && personFunction.getFunction().equals(function)
                    && personFunction.checkDatesIntersections(begin, end)) {
                throw new DomainException("error.personFunction.dates.intersection.for.same.function");
            }
        }
    }

    private boolean checkDatesIntersections(YearMonthDay begin, YearMonthDay end) {
        return ((end == null || !getBeginDate().isAfter(end)) && (getEndDate() == null || !getEndDate().isBefore(begin)));
    }

    private void checkBeginDateAndEndDate(YearMonthDay begin, YearMonthDay end) {
        if (begin == null) {
            throw new DomainException("error.personFunction.no.beginDate");
        }
        if (end == null) {
            throw new DomainException("error.personFunction.no.endDate");
        }
        if (end != null && begin.isAfter(end)) {
            throw new DomainException("error.personFunction.endDateBeforeBeginDate");
        }
    }

    public boolean hasCredits() {
        return getCredits() > 0d;
    }

    @Override
    @Atomic
    public void delete() {
        DomainException.throwWhenDeleteBlocked(getDeletionBlockers());
        if (getCurricularYear() != null) {
            setCurricularYear(null);
        }
        setExecutionInterval(null);
        super.delete();
    }
}
