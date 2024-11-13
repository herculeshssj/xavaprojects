package org.openxava.xavaprojects.model;

import java.math.*;
import java.time.*;
import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.util.*;
import org.openxava.web.editors.*;
import org.openxava.xavaprojects.calculators.*;
import org.openxava.xavaprojects.jobs.*;
import org.quartz.*;
import org.quartz.impl.*;

import lombok.*;

/**
 * 
 * @author Javier Paniza
 */

@Entity @Getter @Setter
@Tab(properties="title, type.name, description, project.name, version.name, createdBy, createdOn, status.name")
@Tab(name="MyCalendar", editors="Calendar", 
	properties="title", 
	baseCondition = "${assignedTo.worker.userName} = ?", 
	filter=org.openxava.filters.UserFilter.class)
public class Issue extends Identifiable {

	@Column(length=100) @Required
	String title;
	
	@DescriptionsList
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	IssueType type;
		
	@Stereotype("SIMPLE_HTML_TEXT") @Column(columnDefinition="MEDIUMTEXT")
	String description;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@DescriptionsList
	@DefaultValueCalculator(DefaultProjectCalculator.class)
	Project project; 
	
	@Column(length=30) @ReadOnly
	@DefaultValueCalculator(CurrentUserCalculator.class)
	String createdBy;
	
	LocalDate plannedFor;
	// tmr ini
	public void setPlannedFor(LocalDate plannedFor) {
		if (Is.equal(this.plannedFor, plannedFor)) return;
		if (this.plannedFor != null) unplanReminder();
		this.plannedFor = plannedFor;
		planReminder();		
	}
	
	private void planReminder() {
		try {
			if (plannedFor == null) return;
			if (getId() == null) return;
			JobDataMap jobDataMap = new JobDataMap();
			jobDataMap.put("issue.id", getId());
	        JobDetail job = JobBuilder.newJob(PlannedIssueReminderJob.class)
	            .withIdentity(getId(), "issueReminders")
	            .usingJobData(jobDataMap)	
	            .build();
	
			// tmr LocalDateTime localDateTime = plannedFor.atStartOfDay();	    
	        // TMR ME QUEDÉ POR AQUÍ. HICE LAS PRIMERAS PRUEBAS Y PARECE QUE FUNCIONÓ, USANDO EL TRUCO DE LOS MINUTOS
	        LocalDateTime localDateTime = LocalDateTime.of(2024, 11, 13, 21, plannedFor.getDayOfMonth()); // tmr Solo para testear
	        System.out.println("[Issue.planReminder] Planificando " + getId()  +" para " + localDateTime); // tmr
			Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());	        
			Trigger trigger = TriggerBuilder.newTrigger()
	            .withIdentity(getId(), "issueReminders")
	            .startAt(date)  
	            .build();
	
			StdSchedulerFactory.getDefaultScheduler().scheduleJob(job, trigger);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void unplanReminder() {
		try {
			System.out.println("[Issue.unplanReminder] Quitando del plan " + getId()); // tmr
			StdSchedulerFactory.getDefaultScheduler().deleteJob(new JobKey(getId(), "issueReminders"));
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	@PostPersist
	private void planReminderIfNeeded() {
		if (plannedFor != null) planReminder();
	}
	// tmr fin
	
	
	@ReadOnly 
	@DefaultValueCalculator(CurrentLocalDateCalculator.class) 
	LocalDate createdOn;
		
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@DescriptionsList(order="${level} desc")
	@DefaultValueCalculator(value=IntegerCalculator.class, 
		properties = @PropertyValue(name="value", value="5") )
	Priority priority; 
		
	@DescriptionsList(condition="project.id = ?", depends="this.project", order="${name} desc") 
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	Version version;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@DescriptionsList(descriptionProperties="worker.name, period.name")
	Plan assignedTo; // tmr cuando cambie replanear
	
	@DescriptionsList
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@DefaultValueCalculator(DefaultIssueStatusCalculator.class) 
	IssueStatus status; // tmr cuando cambie replanear
	
	@DescriptionsList
	@ManyToOne(fetch=FetchType.LAZY)
	Customer customer; 

	@Max(99999)
	int minutes; 
	
	@ReadOnly
	@Calculation("minutes / 60")
	@Column(length=6, scale=2)
	BigDecimal hours; 
	
	@Stereotype("FILES") @Column(length=32)
	String attachments;
	
	@Stereotype("DISCUSSION")
	@Column(length=32)
	private String discussion;
	
	@PreRemove
	void removeDiscussion() {
	    DiscussionComment.removeForDiscussion(discussion);
	}

	public static Issue findByTitle(String title) { 
		TypedQuery<Issue> query = XPersistence.getManager().createQuery(
			"from Issue i where i.title = :title", Issue.class);
		query.setParameter("title", title);
		return query.getSingleResult();
	}
	
	public static Issue findById(String id) { // tmr 
		return XPersistence.getManager().find(Issue.class, id);
	}	

}
