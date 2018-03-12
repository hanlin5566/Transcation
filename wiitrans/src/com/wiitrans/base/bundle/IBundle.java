/*
 * @author	: ECI
 * @date	: 2015-4-7
 */

package com.wiitrans.base.bundle;

import org.json.JSONObject;

public interface IBundle {

	int SetResponse(IResponse res);

	int Start();

	int Stop();

	// This is multi-thread invoke.
	int Request(JSONObject msg);

	int Request(String msg);

	String GetBundleId();
}