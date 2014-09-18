package com.ac.tdl.test.models;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.ac.tdl.SQL.DbHelper;
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


    @Override
    protected void setUp() throws Exception {
        RenamingDelegatingContext context
                = new RenamingDelegatingContext(getContext(), "test_");
        DbHelper.getInstance(context).create();
    }

    public void testCreateHashtagStringEmpty() {
        List<String> s = Arrays.asList();
        assertTrue(Hashtag.createHashtagString(s).isEmpty());
    }

    public void testCreateHashtagStringList() {
        List<String> s = Arrays.asList(FIRST_HASHTAG, SECOND_HASHTAG);
        assertNotNull(Hashtag.createHashtagString(s));
    }

    public void testSaveHashtag() {
        Hashtag hashtag = new HashtagBuilder().withLabel(FIRST_HASHTAG).withTaskId(0).build();
        hashtag.setModelInDb();
        Hashtag testHashtag = new HashtagBuilder().withHashtagId(hashtag.getHashtagId()).build();
        testHashtag.getModelFromDb();
        assertEquals(FIRST_HASHTAG, testHashtag.getLabel());
    }

    @Override
    protected void tearDown() throws Exception{
        DbHelper.getInstance().clear();
        super.tearDown();
    }
}
