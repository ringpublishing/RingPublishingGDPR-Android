package com.ringpublishing.gdpr.internal.cmp;

import android.text.TextUtils;
import android.webkit.CookieManager;

import com.ringpublishing.gdpr.RingPublishingGDPRError;
import com.ringpublishing.gdpr.RingPublishingGDPRListener;
import com.ringpublishing.gdpr.internal.cmp.CmpAction.ActionType;
import com.ringpublishing.gdpr.internal.storage.Storage;
import com.ringpublishing.gdpr.internal.view.FormViewImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CmpWebViewActionTest
{

    private Storage storage;

    private FormViewImpl formView;

    private RingPublishingGDPRListener listener;

    private CmpWebViewAction action;

    private MockedStatic<CookieManager> cookieManagerStatic;

    private MockedStatic<TextUtils> textUtilsStatic;

    @Before
    public void setUp()
    {
        storage = Mockito.mock(Storage.class);
        formView = Mockito.mock(FormViewImpl.class);
        listener = Mockito.mock(RingPublishingGDPRListener.class);
        when(formView.isOnline()).thenReturn(true);
        cookieManagerStatic = Mockito.mockStatic(CookieManager.class);
        cookieManagerStatic.when(CookieManager::getInstance).thenReturn(Mockito.mock(CookieManager.class));
        textUtilsStatic = Mockito.mockStatic(TextUtils.class);
        textUtilsStatic.when(() -> TextUtils.isEmpty(Mockito.any())).thenAnswer(i -> {
            final CharSequence value = i.getArgument(0);
            return value == null || value.length() == 0;
        });
        action = new CmpWebViewAction(storage, listener, formView);
    }

    @After
    public void tearDown()
    {
        cookieManagerStatic.close();
        textUtilsStatic.close();
    }

    @Test
    public void testOnActionError_WhenOnline_ThenClosesFormWithoutTouchingConsentOutdated()
    {
        action.onActionError("x");

        verify(storage, never()).setConsentOutdated(anyBoolean());
        verify(storage, never()).saveLastAPIConsentsCheckStatus(anyString());
        verify(formView).hideForm();
        verify(listener).onError(eq(RingPublishingGDPRError.CLOSE_FORM_WITH_ERROR), anyString());
    }

    @Test
    public void testOnActionError_WhenOffline_ThenShowsErrorAndKeepsFormOpen()
    {
        when(formView.isOnline()).thenReturn(false);

        action.onActionError("x");

        verify(formView).showError();
        verify(formView, never()).hideForm();
        verify(storage, never()).setConsentOutdated(anyBoolean());
        verify(listener).onError(eq(RingPublishingGDPRError.WEBVIEW_LOADING_FAIL), anyString());
    }

    @Test
    public void testOnSubmitTimeout_WhenOffline_ThenClearsWaitingAndClosesByErrorWithoutErrorView()
    {
        when(formView.isOnline()).thenReturn(false);

        action.onSubmitTimeout();

        verify(formView).clearWaitingActions();
        verify(listener).onError(eq(RingPublishingGDPRError.CLOSE_FORM_WITH_ERROR), anyString());
        verify(formView, times(1)).hideForm();
        verify(formView, never()).showError();
        verify(listener, never()).onConsentsUpdated();
        verify(storage, never()).setConsentOutdated(anyBoolean());
    }

    @Test
    public void testGetCompleteConsentData_WhenSavedAndWaitingFinished_ThenClearsOutdatedAndClosesForm() throws Exception
    {
        when(formView.isWaitingFor(ActionType.GET_COMPLETE_CONSENT_DATA)).thenReturn(true);
        when(formView.waitingActionFinish(ActionType.GET_COMPLETE_CONSENT_DATA)).thenReturn(true);

        action.getCompleteConsentData("", "{}");

        verify(storage).saveConsentData("{}");
        verify(storage).setConsentOutdated(false);
        verify(storage).saveLastAPIConsentsCheckStatus(null);
        verify(formView).hideForm();
        verify(listener).onConsentsUpdated();
    }

    @Test
    public void testGetCompleteConsentData_WhenError_ThenKeepsConsentOutdated()
    {
        when(formView.isWaitingFor(ActionType.GET_COMPLETE_CONSENT_DATA)).thenReturn(true);

        action.getCompleteConsentData("boom", "{}");

        verify(storage).clearAllConsentData();
        verify(storage, never()).setConsentOutdated(anyBoolean());
        verify(storage, never()).saveLastAPIConsentsCheckStatus(anyString());
        verify(formView).hideForm();
        verify(formView).clearWaitingActions();
    }

    @Test
    public void testOnActionInAppTCData_WhenErrorThenLateCompleteConsentData_ThenLateCallbackIgnored() throws Exception
    {
        stubWaitingState();

        action.onActionInAppTCData("{}", false);
        action.getCompleteConsentData("", "{}");

        verifyClosedOnlyByError();
        verify(storage, never()).saveConsentData(anyString());
    }

    @Test
    public void testGetCompleteConsentData_WhenErrorThenLateTCData_ThenLateCallbackIgnored() throws Exception
    {
        stubWaitingState();

        action.getCompleteConsentData("boom", "{}");
        action.onActionInAppTCData("{}", true);

        verifyClosedOnlyByError();
        verify(storage, never()).saveTCData(anyString());
    }

    @Test
    public void testCallbacks_WhenNotWaitingForAnything_ThenSuccessCallbacksIgnored() throws Exception
    {
        action.onActionInAppTCData("{}", true);
        action.getCompleteConsentData("", "{}");

        verify(storage, never()).saveTCData(anyString());
        verify(storage, never()).saveConsentData(anyString());
        verify(formView, never()).hideForm();
        verify(listener, never()).onConsentsUpdated();
        verify(storage, never()).setConsentOutdated(anyBoolean());
    }

    private void stubWaitingState()
    {
        final Set<ActionType> waiting = new HashSet<>(Arrays.asList(ActionType.GET_TC_DATA, ActionType.GET_COMPLETE_CONSENT_DATA));
        when(formView.isWaitingFor(any())).thenAnswer(i -> waiting.contains(i.<ActionType>getArgument(0)));
        doAnswer(i -> {
            waiting.clear();
            return null;
        }).when(formView).clearWaitingActions();
    }

    private void verifyClosedOnlyByError()
    {
        verify(formView, times(1)).hideForm();
        verify(formView).clearWaitingActions();
        verify(listener, never()).onConsentsUpdated();
        verify(storage, never()).setConsentOutdated(anyBoolean());
    }
}
