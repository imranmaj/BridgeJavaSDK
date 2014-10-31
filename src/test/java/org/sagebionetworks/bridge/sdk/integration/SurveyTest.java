package org.sagebionetworks.bridge.sdk.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.bridge.Tests;
import org.sagebionetworks.bridge.sdk.BridgeServerException;
import org.sagebionetworks.bridge.sdk.ResearcherClient;
import org.sagebionetworks.bridge.sdk.TestSurvey;
import org.sagebionetworks.bridge.sdk.TestUserHelper;
import org.sagebionetworks.bridge.sdk.TestUserHelper.TestUser;
import org.sagebionetworks.bridge.sdk.UserClient;
import org.sagebionetworks.bridge.sdk.models.holders.GuidVersionedOnHolder;
import org.sagebionetworks.bridge.sdk.models.surveys.DataType;
import org.sagebionetworks.bridge.sdk.models.surveys.Survey;
import org.sagebionetworks.bridge.sdk.models.surveys.SurveyQuestion;
import org.sagebionetworks.bridge.sdk.models.surveys.SurveyQuestionOption;
import org.sagebionetworks.bridge.sdk.models.surveys.SurveyRule;

import com.google.common.collect.Lists;

public class SurveyTest {

    private TestUser researcher;
    private TestUser user;
    private List<GuidVersionedOnHolder> keys = Lists.newArrayList();

    @Before
    public void before() {
        researcher = TestUserHelper.createAndSignInUser(SurveyTest.class, true, Tests.RESEARCHER_ROLE);
        user = TestUserHelper.createAndSignInUser(SurveyTest.class, true);
    }

    @After
    public void after() {
        ResearcherClient client = researcher.getSession().getResearcherClient();
        for (GuidVersionedOnHolder key : keys) {
            client.closeSurvey(key.getGuid(), key.getVersionedOn());
            client.deleteSurvey(key.getGuid(), key.getVersionedOn());
        }
        researcher.signOutAndDeleteUser();
        user.signOutAndDeleteUser();
    }

    @Test(expected=BridgeServerException.class)
    public void cannotSubmitAsNormalUser() {
        user.getSession().getResearcherClient().getAllVersionsOfAllSurveys();
    }
    
    
    @Test
    public void saveAndRetrieveSurvey() {
        ResearcherClient client = researcher.getSession().getResearcherClient();
        GuidVersionedOnHolder key = client.createSurvey(new TestSurvey());
        keys.add(key);
        Survey survey = client.getSurvey(key.getGuid(), key.getVersionedOn());

        List<SurveyQuestion> questions = survey.getQuestions();
        String prompt = questions.get(1).getPrompt();
        assertEquals("Prompt is correct.", "When did you last have a medical check-up?", prompt);
    }

    @Test
    public void createVersionPublish() {
        ResearcherClient client = researcher.getSession().getResearcherClient();
        
        GuidVersionedOnHolder key = client.createSurvey(new TestSurvey());
        keys.add(key);
        GuidVersionedOnHolder laterKey = client.versionSurvey(key.getGuid(), key.getVersionedOn());
        keys.add(laterKey);
        assertNotEquals("Version has been updated.", key.getVersionedOn(), laterKey.getVersionedOn());

        Survey survey = client.getSurvey(laterKey.getGuid(), laterKey.getVersionedOn());
        assertFalse("survey is not published.", survey.isPublished());

        client.publishSurvey(survey.getGuid(), survey.getVersionedOn());
        survey = client.getSurvey(survey.getGuid(), survey.getVersionedOn());
        assertTrue("survey is now published.", survey.isPublished());
    }

    @Test
    public void getAllVersionsOfASurvey() {
        ResearcherClient client = researcher.getSession().getResearcherClient();
        
        GuidVersionedOnHolder key = client.createSurvey(new TestSurvey());
        keys.add(key);
        key = client.versionSurvey(key.getGuid(), key.getVersionedOn());
        keys.add(key);
        
        int count = client.getAllVersionsOfASurvey(key.getGuid()).size();
        assertEquals("Two versions for this survey.", 2, count);
        
        client.closeSurvey(key.getGuid(), key.getVersionedOn());
    }

    @Test
    public void canGetMostRecentOrRecentlyPublishedSurvey() {
        ResearcherClient client = researcher.getSession().getResearcherClient();
        
        GuidVersionedOnHolder key = client.createSurvey(new TestSurvey());
        keys.add(key);
        key = client.versionSurvey(key.getGuid(), key.getVersionedOn());
        keys.add(key);
        key = client.versionSurvey(key.getGuid(), key.getVersionedOn());
        keys.add(key);

        GuidVersionedOnHolder key1 = client.createSurvey(new TestSurvey());
        keys.add(key1);
        key1 = client.versionSurvey(key1.getGuid(), key1.getVersionedOn());
        keys.add(key1);
        key1 = client.versionSurvey(key1.getGuid(), key1.getVersionedOn());
        keys.add(key1);

        GuidVersionedOnHolder key2 = client.createSurvey(new TestSurvey());
        keys.add(key2);
        key2 = client.versionSurvey(key2.getGuid(), key2.getVersionedOn());
        keys.add(key2);
        key2 = client.versionSurvey(key2.getGuid(), key2.getVersionedOn());
        keys.add(key2);

        List<Survey> recentSurveys = client.getRecentVersionsOfAllSurveys();
        assertTrue("Recent versions of surveys exist in recentSurveys.", containsAll(recentSurveys, key, key1, key2));

        client.publishSurvey(key.getGuid(), key.getVersionedOn());
        client.publishSurvey(key2.getGuid(), key2.getVersionedOn());
        List<Survey> publishedSurveys = client.getPublishedVersionsOfAllSurveys();
        assertTrue("Published surveys contain recently published.", containsAll(publishedSurveys, key, key2));
    }

    @Test
    public void canUpdateASurveyAndTypesAreCorrect() {
        ResearcherClient client = researcher.getSession().getResearcherClient();
        
        GuidVersionedOnHolder key = client.createSurvey(new TestSurvey());
        keys.add(key);
        Survey survey = client.getSurvey(key.getGuid(), key.getVersionedOn());
        assertEquals("Type is Survey.", survey.getClass(), Survey.class);

        List<SurveyQuestion> questions = survey.getQuestions();
        assertEquals("Type is SurveyQuestion.", questions.get(0).getClass(), SurveyQuestion.class);
        assertEquals("Type is BooleanConstraints.", DataType.BOOLEAN, constraintTypeForQuestion(questions, 0));
        assertEquals("Type is DateConstraints", DataType.DATE, constraintTypeForQuestion(questions, 1));
        assertEquals("Type is DateTimeConstraints", DataType.DATETIME, constraintTypeForQuestion(questions, 2));
        assertEquals("Type is DecimalConstraints", DataType.DECIMAL, constraintTypeForQuestion(questions, 3));
        assertEquals("Type is IntegerConstraints", DataType.INTEGER, constraintTypeForQuestion(questions, 4));
        assertEquals("Type is IntegerConstraints", SurveyRule.class, questions.get(4).getConstraints().getRules()
                .get(0).getClass());
        assertEquals("Type is DurationConstraints", DataType.DURATION, constraintTypeForQuestion(questions, 5));
        assertEquals("Type is TimeConstraints", DataType.TIME, constraintTypeForQuestion(questions, 6));
        assertTrue("Type is MultiValueConstraints", questions.get(7).getConstraints().getAllowMultiple());
        assertEquals("Type is SurveyQuestionOption", SurveyQuestionOption.class, questions.get(7).getConstraints()
                .getEnumeration().get(0).getClass());

        survey.setName("New name");
        client.updateSurvey(survey);
        survey = client.getSurvey(survey.getGuid(), survey.getVersionedOn());
        assertEquals("Name should have changed.", survey.getName(), "New name");
    }

    @Test(expected=BridgeServerException.class)
    public void participantCannotRetrieveUnpublishedSurvey() {
        ResearcherClient client = researcher.getSession().getResearcherClient();
        GuidVersionedOnHolder key = client.createSurvey(new TestSurvey());
        keys.add(key);
        
        UserClient userClient = user.getSession().getUserClient();
        userClient.getSurvey(key.getGuid(), key.getVersionedOn());
        fail("Should not get here.");
    }

    private DataType constraintTypeForQuestion(List<SurveyQuestion> questions, int index) {
        return questions.get(index).getConstraints().getDataType();
    }

    private boolean containsAll(List<Survey> surveys, GuidVersionedOnHolder... keys) {
        int count = 0;
        for (Survey survey : surveys) {
            for (GuidVersionedOnHolder key : keys) {
                if (survey.getGuid().equals(key.getGuid()) && survey.getVersionedOn().equals(key.getVersionedOn())) {
                    count++;
                }
            }
        }
        return count == keys.length;
    }


}
