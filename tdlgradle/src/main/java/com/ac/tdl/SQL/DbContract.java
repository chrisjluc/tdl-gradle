package com.ac.tdl.SQL;

import android.provider.BaseColumns;

public interface DbContract {

    public static abstract class HashtagTable implements BaseColumns {
        public static final String TABLE_NAME = "hashtag";

        /**
         * Integer Primary Key
         */
        public static final String COLUMN_NAME_ID = "hashtagId";

        /**
         * Actual hashtag String
         * Ex. #happy would saved as happy
         * <p/>
         * Varchar(48) Max Length 47
         */
        public static final String COLUMN_NAME_HASHTAG_LABEL = "hashtagLabel";

        /**
         * Date saved in milliseconds
         * <p/>
         * Timestamp
         */
        public static final String COLUMN_NAME_DATE_CREATED = "dateCreated";

        /**
         * Id of the associated task
         * <p/>
         * Integer
         */
        public static final String COLUMN_NAME_TASK_ID = "taskId";

        /**
         * Determines if the hashtag is archived or not
         * <p/>
         * Tiny int (0 or 1)
         */
        public static final String COLUMN_NAME_ARCHIVED = "archivedBool";
    }

    public static abstract class TaskTable implements BaseColumns {

        public static final String TABLE_NAME = "task";

        /**
         * Integer primary key
         */
        public static final String COLUMN_NAME_ID = "taskId";

        /**
         * Title of the task, what the user initally sees for each item in the list
         * <p/>
         * Varchar(48) Max Length 47
         */
        public static final String COLUMN_NAME_TITLE = "title";

        /**
         * When an item is expanded user can have extra information for their task
         * <p/>
         * Varchar(48) Max Length 47
         */
        public static final String COLUMN_NAME_DETAILS = "details";

        /**
         * Will have a different colour or visual queue to show this item has priority
         * <p/>
         * tiny int(0 = no priority, 1 = priority)
         */
        public static final String COLUMN_NAME_PRIORITY = "priority";

        /**
         * TimeStamp
         */
        public static final String COLUMN_NAME_DATE_CREATED = "dateCreated";

        /**
         * Timestamp
         */
        public static final String COLUMN_NAME_DATE_REMINDER = "dateReminder";

        /**
         * The amount of time between each time the user wants it to be repeated
         * It is 0 if no repetition
         * <p/>
         * Long
         */
        public static final String COLUMN_NAME_REPETITION_MS = "repetitionInMS";

        /**
         * Time before the reminder date to give an alert
         * <p/>
         * Long
         */
        public static final String COLUMN_NAME_NOTIFY_BEFORE_REMINDER_MS = "notifyBeforeReminderInMS";

        /**
         * Is complete still shows the task, but some visual queue to show it is hidden
         * <p/>
         * tiny int (0 or 1)
         */
        public static final String COLUMN_NAME_IS_COMPLETE = "isComplete";

        /**
         * Hides the task
         * <p/>
         * tiny int (0 or 1)
         */
        public static final String COLUMN_NAME_ARCHIVED = "archivedBool";
    }
}
