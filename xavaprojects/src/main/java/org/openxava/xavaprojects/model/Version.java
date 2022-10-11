package org.openxava.xavaprojects.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.jpa.*;
import org.openxava.model.*;

import lombok.*;

/**
 * 
 * @author Javier Paniza
 */

@Entity @Getter @Setter
@Tab(defaultOrder="${name} desc") 
public class Version extends Identifiable {
	
	public static List<Version> findByName(String name) { 
		return XPersistence.getManager()
			.createQuery("from Version v where v.name = :name")
			.setParameter("name", name)
			.getResultList();
	}

	@DescriptionsList
	@ManyToOne(fetch=FetchType.LAZY)
	Project project;
	
	@Column(length=20)
	String name; 
	
	@ListProperties("status.icon, title, type.icon, createdBy, assignedTo.worker.name") 
	@OneToMany(mappedBy="version")
	@OrderColumn
	@NewAction("VersionIssues.new")
	List<Issue> issues;
	
}
