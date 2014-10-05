package com.ac.tdl.managers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ac.tdl.MainActivity;
import com.ac.tdl.SQL.DbContract;
import com.ac.tdl.SQL.DbHelper;
import com.ac.tdl.model.Hashtag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chrisjluc on 2014-09-18.
 */
public class HashtagManager implements IHashtagManager{


    private static HashtagManager instance;
    private static SQLiteDatabase db = DbHelper.getInstance().getWritableDatabase();
    private MainActivity.HashtagManagerListener hashtagManagerListener;

    public static HashtagManager getInstance() {
        if (instance == null)
            instance = new HashtagManager();
        return instance;
    }

    public void setHashtagManagerListener(MainActivity.HashtagManagerListener hashtagManagerListener) {
        this.hashtagManagerListener = hashtagManagerListener;
    }

    public void notifyDistinctHashtagChanged() {
        if(hashtagManagerListener != null)
            hashtagManagerListener.notifyOnDistinctHashtagChanged();
    }

    private HashMap<Long, List<Hashtag>> hashtagListByTaskId;
    private HashMap<Long, Hashtag> hashtagById;
    private HashMap<String, List<Hashtag>> hashtagsByDistinctLabel;

    private HashtagManager() {
        hashtagListByTaskId = new HashMap<Long, List<Hashtag>>();
        hashtagById = new HashMap<Long, Hashtag>();
        hashtagsByDistinctLabel = new HashMap<String, List<Hashtag>>();
        putAllHashtagsInCache();
    }

    /**
     * For testing ONLY
     *
     * @return
     */
    public static void nullifyInstance() {
        instance = null;
    }

    private void putAllHashtagsInCache() {
        Cursor cursor = db.query(DbContract.HashtagTable.TABLE_NAME
                , null
                , DbContract.HashtagTable.COLUMN_NAME_ARCHIVED + "=?"
                , new String[]{"0"}, null, null, null);
        while (cursor.moveToNext()) {
            Hashtag h = createHashtagFromCursor(cursor);
            addHashtagToCache(h);
        }
    }

    private void addHashtagToCache(Hashtag h){
        if(!hashtagById.containsKey(h.getHashtagId()))
            hashtagById.put(h.getHashtagId(),h);

        if(!hashtagListByTaskId.containsKey(h.getTaskId()))
            hashtagListByTaskId.put(h.getTaskId(), new ArrayList<Hashtag>());
        hashtagListByTaskId.get(h.getTaskId()).add(h);

        if(!hashtagsByDistinctLabel.containsKey(h.getLabel()))
            hashtagsByDistinctLabel.put(h.getLabel(), new ArrayList<Hashtag>());
        hashtagsByDistinctLabel.get(h.getLabel()).add(h);
    }

    private void removeHashtagFromCache(Hashtag h){
        if(hashtagById.containsKey(h.getHashtagId()))
            hashtagById.remove(h.getHashtagId());

        if(hashtagListByTaskId.containsKey(h.getTaskId())) {
            List<Hashtag> hashtags = hashtagListByTaskId.get(h.getTaskId());
            if(hashtags.contains(h))
                hashtags.remove(h);
            if(hashtags.size() == 0)
                hashtagListByTaskId.remove(h.getTaskId());
        }

        if(hashtagsByDistinctLabel.containsKey(h.getLabel())) {
            List<Hashtag> hashtags = hashtagsByDistinctLabel.get(h.getLabel());
            if(hashtags.contains(h))
                hashtags.remove(h);
            if(hashtags.size() == 0)
                hashtagsByDistinctLabel.remove(h.getLabel());
        }
    }

    public Hashtag createHashtagFromCursor(Cursor cursor) {
        Hashtag h = new Hashtag();
        try {
            h.setHashtagId(cursor.getLong(cursor
                    .getColumnIndexOrThrow(DbContract.HashtagTable.COLUMN_NAME_ID)));
            h.setLabel(cursor
                    .getString(cursor
                            .getColumnIndexOrThrow(DbContract.HashtagTable.COLUMN_NAME_HASHTAG_LABEL)));
            h.setDateCreated(cursor
                    .getLong(cursor
                            .getColumnIndexOrThrow(DbContract.HashtagTable.COLUMN_NAME_DATE_CREATED)));
            h.setTaskId(cursor.getInt(cursor
                    .getColumnIndexOrThrow(DbContract.HashtagTable.COLUMN_NAME_TASK_ID)));
            h.setArchived(getBoolFromInt(cursor.getInt(cursor
                    .getColumnIndexOrThrow(DbContract.HashtagTable.COLUMN_NAME_ARCHIVED))));
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        }
        return h;

    }

    public Hashtag getHashtagsById(long id) {
        if(!hashtagById.containsKey(id))
            return null;
        return hashtagById.get(id);
    }


    @Override
    public List<Hashtag> getHashtagsByTaskId(long taskId) {
        if(!hashtagListByTaskId.containsKey(taskId))
            return null;
        return hashtagListByTaskId.get(taskId);
    }

    @Override
    public Collection<String> getDistinctHashtags() {
        return hashtagsByDistinctLabel.keySet();
    }

    public void save(Hashtag h) {
        // Remove from cache
        if(h.isArchived()){
            h.archiveHashtag();
            removeHashtagFromCache(h);
        }else{
            h.setModelInDb();
            addHashtagToCache(h);
        }
    }

    /**
     * @param input
     * @return Array of valid hashtags
     */
    public static List<String> getHashtagListFromString(String input) {
        ArrayList<String> hashtagList = new ArrayList<String>();
        if (input == null || input.isEmpty()) return hashtagList;
        Pattern pattern = Pattern
                .compile("(?:\\s|\\A)[##]+([A-Za-z0-9-_]+[A-Za-z0-9-_])");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String match = matcher.group(1);
            //Don't want numbers to be valid hashtags
            if (!isNumber(match))
                hashtagList.add(match);
        }
        return hashtagList;
    }

    public static String createHashtagString(List<String> array) {
        StringBuilder hashtagBuilder = new StringBuilder();

        if (array == null) {
            return "";
        }

        for (String s : array) {
            hashtagBuilder.append(" #" + s);
        }
        return hashtagBuilder.toString();
    }

    public static boolean isNumber(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    protected boolean getBoolFromInt(int isTrue) throws Exception {
        if (isTrue == 1) {
            return true;
        } else if (isTrue == 0) {
            return false;
        }
        throw new Exception();
    }
}
