package com.ringpublishing.gdpr;

public interface ConsentFormListener
{

    /**
     * Method will be called when consent form has never been displayed before
     * or the consent is out of date and you need to show form again.
     */
    void onReadyToShowForm();

    /**
     * Invoked when consents are valid. Also, when the gdpr_applies flag is set to false in the remote configuration.
     * This method will also call in case of timeout or other errors that the SDK cannot handle, so as not to stop the user on the screen.
     */
    void onConsentsUpToDate();
}
