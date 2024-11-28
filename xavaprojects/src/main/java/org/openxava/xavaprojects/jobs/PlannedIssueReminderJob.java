package org.openxava.xavaprojects.jobs;

import java.time.format.*;

import org.apache.commons.logging.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.xavaprojects.model.*;
import org.quartz.*;

/**
 * 
 * @author Javier Paniza
 */
public class PlannedIssueReminderJob implements Job {
	
	private static final Log log = LogFactory.getLog(PlannedIssueReminderJob.class);
    
    public void execute(JobExecutionContext context) {
    	String issueId = "Unknow";
    	String workerEmail = "Unknow";
    	try {
    		issueId = context.getJobDetail().getJobDataMap().getString("issue.id");
    		Issue issue = Issue.findById(issueId);
    		if (issue == null) return;
    		Plan plan = issue.getAssignedTo();
    		if (plan == null) return;
    		Worker worker = plan.getWorker();
    		if (worker == null) return;
    		workerEmail = worker.getEmail();
    		if (Is.emptyString(workerEmail)) return;
    		if (issue.getPlannedFor() == null) return;
    		
    		DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
    		String formattedDate = issue.getPlannedFor().format(formatter);
    		
    		String subject = XavaResources.getString("planned_issue_reminder_subject", issue.getTitle(), formattedDate);
    		String content = XavaResources.getString("planned_issue_reminder_content", issue.getTitle(), issue.getDescription(), formattedDate);
    		
    		Emails.send(workerEmail, subject, content);
    	}
    	catch (Exception ex) {
    		log.error(XavaResources.getString("planned_issue_reminder_error", issueId, workerEmail), ex);
    	}
    	finally {
    		XPersistence.commit();
    	}    	
    }
    
}