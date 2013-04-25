package org.societies.android.platform;

import org.societies.android.p2p.activity.P2PActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SocialProviderActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    /**
     * On-click handler of the P2P button.
     * @param view The clicked view.
     */
    public void onP2pButtonClick(View view) {
    	Intent intent = new Intent(this, P2PActivity.class);
    	startActivity(intent);
    }
}