package org.openxava.xavaprojects.model;

import javax.persistence.*;

import org.openxava.jpa.*;

import lombok.*;

/**
 * 
 * @author Javier Paniza
 */

@Entity @Getter @Setter
public class IssueType extends IconableWithUseAsDefaultValueForMyCalendar {
	
	public static IssueType findById(String id) { 
		return (IssueType) XPersistence.getManager().find(IssueType.class, id);
	}
	
	public static IssueType findTheDefaultOneForMyCalendar() {
		return (IssueType) findTheDefaultOne("IssueType", "useAsDefaultValueForMyCalendar");
	}
	
}
