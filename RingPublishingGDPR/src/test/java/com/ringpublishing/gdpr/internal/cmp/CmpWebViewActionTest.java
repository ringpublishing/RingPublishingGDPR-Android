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

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
    public void testGetCompleteConsentData_WhenSavedAndWaitingFinished_ThenClearsOutdatedAndClosesForm() throws Exception
    {
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
        action.getCompleteConsentData("boom", "{}");

        verify(storage).clearAllConsentData();
        verify(storage, never()).setConsentOutdated(anyBoolean());
        verify(storage, never()).saveLastAPIConsentsCheckStatus(anyString());
        verify(formView).hideForm();
    }
}
