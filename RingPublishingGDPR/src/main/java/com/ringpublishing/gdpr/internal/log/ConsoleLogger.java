package com.ringpublishing.gdpr.internal.log;

import android.util.Log;
import com.ringpublishing.gdpr.LogListener;

class ConsoleLogger implements LogListener
{
	private final String tag = "RingPublishingGDPR";

	@Override
	public void debug(String message)
	{
		Log.d(tag, message);
	}

	@Override
	public void info(String message)
	{
		Log.i(tag, message);
	}

	@Override
	public void warn(String message)
	{
		Log.w(tag, message);
	}

	@Override
	public void error(String message)
	{
		Log.e(tag, message);
	}
}
