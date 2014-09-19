package com.ac.tdl.model;

public class HashtagBuilder {
	private long hashtagId = -1;
	private String label;
	private long dateCreated;
	private long taskId = -1;
	private boolean archived;

	public HashtagBuilder withHashtagId(long hashtagId) {
		this.hashtagId = hashtagId;
		return this;
	}

	public HashtagBuilder withLabel(String label) {
		this.label = label;
		return this;
	}

	public HashtagBuilder withDateCreated(long dateCreated) {
		this.dateCreated = dateCreated;
		return this;
	}

	public HashtagBuilder withTaskId(long taskId) {
		this.taskId = taskId;
		return this;
	}

	public HashtagBuilder withArchived(boolean archived) {
		this.archived = archived;
		return this;
	}

	public Hashtag build() {
		return new Hashtag(hashtagId, label, dateCreated, taskId, archived);
	}
}
