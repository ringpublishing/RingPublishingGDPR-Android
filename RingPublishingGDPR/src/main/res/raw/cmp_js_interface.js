(
	function() {
		var cmpInterval;
        var cmpIntervalCount = 0;

		function addCMPListeners() {
		    console.log("CMP addCMPListeners");

			if (window.__tcfapi !== undefined) {
				try {
                        console.log("CMP addEventListener");

                        window.__tcfapi('addEventListener', 2, function(tcData, success) {

                           console.log("CMP new event success: "  + success);

                            if (success && tcData !== undefined) {
                     		    Android.onTcfEvent(JSON.stringify(tcData));
                     	    } else {
                     		    Android.onError("CMP Error Tc data undefined");
                     	    }
                        });

                    clearInterval(cmpInterval);
				} catch (error) {
				    console.log("CMP window.__tcfapi add listener error: " + error);
					Android.onError(error);
				}
			}
			else
			{
				console.log("CMP window.__tcfapi undefined. Waiting");
				cmpIntervalCount = cmpIntervalCount + 1;
				if(cmpIntervalCount > 10) {
				    clearInterval(cmpInterval);
					Android.onError("Error. Timeout attach event listener");
				}
			}
		}
		cmpInterval = setInterval(addCMPListeners, 1000);
	}
)
()