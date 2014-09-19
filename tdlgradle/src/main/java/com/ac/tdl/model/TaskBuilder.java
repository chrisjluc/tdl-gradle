package com.ac.tdl.model;

public class TaskBuilder {
	private long taskId = -1;
	private String taskTitle;
	private String taskDetails;
	private boolean priority;
	private long dateCreated;
	private long dateReminder;
	private long repetitionInMS;
	private long notifyBeforeReminderInMS;
	private boolean isComplete;
	private boolean archived;

	// Additional attributes
	private Hashtag[] hashtagArray;

	public TaskBuilder withTaskId(long taskId) {
		this.taskId = taskId;
		return this;
	}

	public TaskBuilder withTaskTitle(String taskTitle) {
		this.taskTitle = taskTitle;
		return this;
	}

	public TaskBuilder withTaskDetails(String taskDetails) {
		this.taskDetails = taskDetails;
		return this;
	}

	public TaskBuilder withPriority(boolean priority) {
		this.priority = priority;
		return this;
	}

	public TaskBuilder withDateCreated(long dateCreated) {
		this.dateCreated = dateCreated;
		return this;
	}

	public TaskBuilder withDateReminder(long dateReminder) {
		this.dateReminder = dateReminder;
		return this;
	}

	public TaskBuilder withRepetitionInMS(long repetitionInMS) {
		this.repetitionInMS = repetitionInMS;
		return this;
	}

	public TaskBuilder withNotifyBeforeReminderInMS(
			long notifyBeforeReminderInMS) {
		this.notifyBeforeReminderInMS = notifyBeforeReminderInMS;
		return this;
	}

	public TaskBuilder withIsComplete(boolean isComplete) {
		this.isComplete = isComplete;
		return this;
	}

	public TaskBuilder withArchived(boolean archived) {
		this.archived = archived;
		return this;
	}

	public TaskBuilder withHashtagArray(Hashtag[] hashtagArray) {
		this.hashtagArray = hashtagArray;
		return this;
	}

	public Task build() {
		return new Task(taskId, taskTitle, taskDetails, priority, dateCreated,
				dateReminder, repetitionInMS, notifyBeforeReminderInMS,
				isComplete, archived, hashtagArray);
	}
}
