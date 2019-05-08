package csp;

import java.time.LocalDate;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

/**
 * CSP: Calendar Satisfaction Problem Solver Provides a solution for scheduling
 * some n meetings in a given period of time and according to some unary and
 * binary constraints on the dates of each meeting.
 */
public class CSP {

	/**
	 * Public interface for the CSP solver in which the number of meetings, range of
	 * allowable dates for each meeting, and constraints on meeting times are
	 * specified.
	 * 
	 * @param nMeetings   The number of meetings that must be scheduled, indexed
	 *                    from 0 to n-1
	 * @param rangeStart  The start date (inclusive) of the domains of each of the n
	 *                    meeting-variables
	 * @param rangeEnd    The end date (inclusive) of the domains of each of the n
	 *                    meeting-variables
	 * @param constraints Date constraints on the meeting times (unary and binary
	 *                    for this assignment)
	 * @return A list of dates that satisfies each of the constraints for each of
	 *         the n meetings, indexed by the variable they satisfy, or null if no
	 *         solution exists.
	 */
	public static List<LocalDate> solve(int nMeetings, LocalDate rangeStart, LocalDate rangeEnd,
			Set<DateConstraint> constraints) {
		//Initializes a list of meetings, and an initial assignment
		ArrayList<Meeting> meetings = new ArrayList<Meeting>();
		List<LocalDate> initialAssignment = new ArrayList<LocalDate>();
		//Fills the meeting list and initial assignment with nulls
		for (int i = 0; i < nMeetings; i++) {
			meetings.add(new Meeting(new ArrayList<LocalDate>(), i));
			initialAssignment.add(null);
		}
		//Sorts the constraints into unary/binary, and attaches them to the relevant meetings
		for (DateConstraint d : constraints) {
			if (d.arity() == 1) {
				meetings.get(d.L_VAL).unaryConstraints.add((UnaryDateConstraint) (d));
			} else {
				BinaryDateConstraint constraint = (BinaryDateConstraint) d;
				meetings.get(constraint.L_VAL).binaryConstraints.add(constraint);
				meetings.get(constraint.R_VAL).binaryConstraints.add(constraint);
			}
		}
		//For each meeting, add every value to the domain, and then check for node consistency to
		// remove some values from domains.
		for (Meeting meeting : meetings) {
			for (LocalDate i = rangeStart; i.compareTo(rangeEnd) <= 0; i = i.plusDays(1)) {
				meeting.domain.add(i);
			}
			meeting.domain.removeAll(checkNodeConsistency(rangeStart, rangeEnd, meeting));
		}
		//For each meeting, check arc consistency to remove some values from domains.
		for (Meeting meeting : meetings) {
			meeting.domain.removeAll(checkArcConsistency(meeting, meetings));
			if (meeting.domain.size() == 0) {
				return null;
			}
		}

		return backtrackAndSolve(initialAssignment, meetings);
	}

	/**
	 * Checks whether nodes are consistent with the unary constraints
	 * @param start   Start range for meeting dates
	 * @param end     End range for meeting dates
	 * @param meeting Meeting we are considering consistency of
	 * @return Inconsistent dates for the meeting's domain
	 */
	private static ArrayList<LocalDate> checkNodeConsistency(LocalDate start, LocalDate end, Meeting meeting) {
		ArrayList<LocalDate> inconsistentValues = new ArrayList<LocalDate>();
		for (UnaryDateConstraint constraint : meeting.unaryConstraints) {
			for (int i = 0; i < meeting.domain.size(); i++) {
				if ( ! compareDates(meeting.domain.get(i), constraint.R_VAL, constraint.OP)) {
					inconsistentValues.add(meeting.domain.get(i));
				}
			}
		}
		return inconsistentValues;
	}
	
	/**
	 * Checks arc consistency for binary constraints
	 * 
	 * @param meeting  The current meeting we are considering the consistency of
	 * @param meetings The list of meetings
	 * @return Inconsistent values for the current meeting's domain
	 */
	private static ArrayList<LocalDate> checkArcConsistency(Meeting meeting, ArrayList<Meeting> meetings) {
		ArrayList<LocalDate> inconsistentValues = new ArrayList<LocalDate>();
		for (BinaryDateConstraint constraint : meeting.binaryConstraints) {
			boolean isVariableOnLeft = (constraint.L_VAL == meeting.index);
			for (LocalDate date : meeting.domain) {
				Meeting tail = meetings.get((isVariableOnLeft) ? constraint.R_VAL : constraint.L_VAL);
				boolean isConsistent = false;
				for (int i = 0; i < tail.domain.size(); i++) {
					LocalDate valueInHead = tail.domain.get(i);
					LocalDate leftDateToCompare = date, rightDateToCompare = valueInHead;
					if ( ! isVariableOnLeft) {
						leftDateToCompare = valueInHead;
						rightDateToCompare = date;
					}
					if (compareDates(leftDateToCompare, rightDateToCompare, constraint.OP)) {
						isConsistent = true;
					}
				}
				if ( ! isConsistent) {
					inconsistentValues.add(date);
				}
			}
		}
		return inconsistentValues;
	}
	
	/**
	 * Backtracks and solves the meeting problem by assigning values to variables
	 * and checking if constraints are satisfied
	 * 
	 * @param assignment The assignment of values to variables thus far
	 * @param meetings   The list of meetings
	 * @return An answer, or no answer! If there's no answer, it will be null
	 */
	private static List<LocalDate> backtrackAndSolve(List<LocalDate> assignment, ArrayList<Meeting> meetings) {
		if (checkIfAllAssigned(assignment)) {
			return assignment;
		}
		Meeting unassignedMeeting = findUnassignedMeeting(assignment, meetings);

		for (LocalDate date : unassignedMeeting.domain) {
			if (doesDateSatisfyConstraints(unassignedMeeting, date, assignment)) {
				assignment.set(unassignedMeeting.index, date);

				List<LocalDate> result = backtrackAndSolve(assignment, meetings);
				if (result != null) {
					return result;
				} else {
					assignment.set(unassignedMeeting.index, null);
				}
			}
		}
		return null;
	}
	
	/**
	 * Checks if all variables in the assignment have been assigned
	 * 
	 * @param assignment The assignment thus far
	 * @return Whether all the variables are assigned
	 */
	private static boolean checkIfAllAssigned(List<LocalDate> assignment) {
		for (int i = 0; i < assignment.size(); i++) {
			if (assignment.get(i) == null) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Finds the first unassigned meeting
	 * 
	 * @param assignments The assignment of variables thus far
	 * @param meetings    The list of meetings
	 * @return An unassigned meeting
	 */
	private static Meeting findUnassignedMeeting(List<LocalDate> assignments, ArrayList<Meeting> meetings) {
		for (int i = 0; i < assignments.size(); i++) {
			if (assignments.get(i) == null) {
				return meetings.get(i);
			}
		}
		return null;
	}
	
	/**
	 * Checks if date satisfies constraint
	 * 
	 * @param meeting    The meeting we are checking
	 * @param date       The date we are checking
	 * @param assignment The current assignment of values to variables
	 * @return Whether or not the constraint is satisfied by the date
	 */
	private static boolean doesDateSatisfyConstraints(Meeting meeting, LocalDate date, List<LocalDate> assignment) {
		for (UnaryDateConstraint unaryConstraint : meeting.unaryConstraints) {
			if ( ! compareDates(date, unaryConstraint.R_VAL, unaryConstraint.OP)) {
				return false;
			}
		}
		for (BinaryDateConstraint binaryConstraint : meeting.binaryConstraints) {
			if ( ! isBinaryConstraintSatisfied(meeting, date, binaryConstraint, assignment)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Compares two dates with an operand and returns true if the operand holds
	 * @param firstDate The first date to compare
	 * @param secondDateThe second date to compare
	 * @param operand The operand to compare the dates against
	 * @return Whether or not the operand held true for the two dates
	 */
	private static boolean compareDates(LocalDate firstDate, LocalDate secondDate, String operand) {
		switch (operand) {
		case "==":
			if (firstDate.compareTo(secondDate) == 0) {
				return true;
			}
			break;
		case "!=":
			if (firstDate.compareTo(secondDate) != 0) {
				return true;
			}
			break;
		case "<":
			if (firstDate.compareTo(secondDate) < 0) {
				return true;
			}
			break;
		case ">":
			if (firstDate.compareTo(secondDate) > 0) {
				return true;
			}
			break;
		case "<=":
			if (firstDate.compareTo(secondDate) <= 0) {
				return true;
			}
			break;
		case ">=":
			if (firstDate.compareTo(secondDate) >= 0) {
				return true;
			}
			break;
		}
		return false;
	}
	
	/**
	 * Checks if the binary constraint is satisfied
	 * 
	 * @param meeting    The meeting we are considering
	 * @param date       The date we are considering assigning to the meeting
	 * @param constraint The constraint we are checking the satisfaction of
	 * @param assignment The assignment of values to variables thus far
	 * @return Whether or not the binary constraint is satisfied
	 */
	private static boolean isBinaryConstraintSatisfied(Meeting meeting, LocalDate date, BinaryDateConstraint constraint,
			List<LocalDate> assignment) {
		boolean isVariableOnLeft = (constraint.L_VAL == meeting.index);
		LocalDate leftValue, rightValue;
		if (isVariableOnLeft) {
			if (assignment.get(constraint.R_VAL) == null) {
				return true;
			}
			leftValue = date;
			rightValue = assignment.get(constraint.R_VAL);
		} else {
			if (assignment.get(constraint.L_VAL) == null) {
				return true;
			}
			leftValue = assignment.get(constraint.L_VAL);
			rightValue = date;
		}
		return compareDates(leftValue, rightValue, constraint.OP);
	}

	private static class Meeting {
		int index;
		ArrayList<LocalDate> domain;
		ArrayList<UnaryDateConstraint> unaryConstraints;
		ArrayList<BinaryDateConstraint> binaryConstraints;

		Meeting(ArrayList<LocalDate> domain, int index) {
			this.index = index;
			this.domain = domain;
			unaryConstraints = new ArrayList<UnaryDateConstraint>();
			binaryConstraints = new ArrayList<BinaryDateConstraint>();
		}

	}

}
