package com.ac.tdl.model;

import android.database.sqlite.SQLiteDatabase;

public class HashtagBuilder {
	private int hashtagId;
	private String label;
	private long dateCreated;
	private int taskId;
	private boolean archived;
	private SQLiteDatabase db;

	public HashtagBuilder withHashtagId(int hashtagId) {
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

	public HashtagBuilder withTaskId(int taskId) {
		this.taskId = taskId;
		return this;
	}

	public HashtagBuilder withArchived(boolean archived) {
		this.archived = archived;
		return this;
	}

	public HashtagBuilder withDb(SQLiteDatabase db) {
		this.db = db;
		return this;
	}

	public Hashtag build() {
		return new Hashtag(hashtagId, label, dateCreated, taskId, archived, db);
	}
}
