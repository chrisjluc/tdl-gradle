package com.ac.tdl.tests.models;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.ac.tdl.SQL.DbHelper;
import com.ac.tdl.managers.HashtagManager;
import com.ac.tdl.managers.TaskManager;
import com.ac.tdl.model.Hashtag;
import com.ac.tdl.model.HashtagBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * Created by chrisjluc on 2014-09-18.
 */
public class HashtagTests extends AndroidTestCase{

    private static final String FIRST_HASHTAG = "1helloyolo";
    private static final String SECOND_HASHTAG = "okay";
    private static final String THIRD_HASHTAG = "okay123";
    private HashtagManager hashtagManager;

    @Override
    protected void setUp() throws Exception {
        RenamingDelegatingContext context
                = new RenamingDelegatingContext(getContext(), "test_");
        DbHelper.getInstance(context).create();
        hashtagManager= HashtagManager.getInstance();
    }

    public void testCreateHashtagStringEmpty() {
        List<String> s = Arrays.asList();
        assertTrue(HashtagManager.createHashtagString(s).isEmpty());
    }

    public void testCreateHashtagStringList() {
        List<String> s = Arrays.asList(FIRST_HASHTAG, SECOND_HASHTAG);
        assertNotNull(HashtagManager.createHashtagString(s));
    }

    public void testSaveHashtag() {
        Hashtag hashtag = new HashtagBuilder().withLabel(FIRST_HASHTAG).withTaskId(0).build();
        hashtagManager.save(hashtag);
        List<Hashtag> testHashtag = hashtagManager.getHashtagsByTaskId(0);
        assertEquals(FIRST_HASHTAG, testHashtag.get(0).getLabel());
    }

    @Override
    protected void tearDown() throws Exception{
        DbHelper.getInstance().clear();
        hashtagManager.nullifyInstance();
        super.tearDown();
    }
}
