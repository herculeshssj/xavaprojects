package org.openxava.xavaprojects.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.model.*;

/**
 * tmp
 * 
 * @author Javier Paniza
 */
@Entity
public class Period extends Identifiable {

	@Column(length=40) @Required
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
