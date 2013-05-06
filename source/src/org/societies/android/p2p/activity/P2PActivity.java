package org.societies.android.p2p.activity;

import java.util.ArrayList;
import java.util.List;

import org.societies.android.p2p.IP2PChangeListener;
import org.societies.android.p2p.P2PDevice;
import org.societies.android.p2p.P2PSyncManager;
import org.societies.android.p2p.ConnectionType;
import org.societies.android.p2p.P2PSyncManager.P2PInterfaceStatus;
import org.societies.android.p2p.P2PSyncManager.SyncRole;
import org.societies.android.platform.R;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 * Activity for setting up P2P synchronization.
 * 
 * @author Kato
 */
public class P2PActivity extends Activity implements IP2PChangeListener {
	
	public static final String TAG = "P2PActivity";
	
	private P2PSyncManager mSyncManager;
	private ListView mPeerList;
	private TextView mNameLabel;
	private TextView mStatusLabel;
	private Button mMagicButton;
	private ArrayAdapter<P2PDevice> mPeerAdapter;
	private List<P2PDevice> mPeers = new ArrayList<P2PDevice>();
	
	private boolean mConnected = false;
	private boolean mClosing = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_p2p);
		
		mNameLabel = (TextView) findViewById(R.id.label_this_device);
		mStatusLabel = (TextView) findViewById(R.id.label_status);
		mMagicButton = (Button) findViewById(R.id.button_magic);
		
		mPeerList = (ListView) findViewById(R.id.list_peers);
		mPeerAdapter = new ArrayAdapter<P2PDevice>(
				this, android.R.layout.simple_list_item_1, mPeers);
		mPeerList.setAdapter(mPeerAdapter);
		
		initPeerOnClickListener();
		
		mSyncManager = P2PSyncManager.getSyncManager(
				this, this, ConnectionType.WIFI_DIRECT);
	}
	
	/**
	 * Initializes the item on-click listener of the peer list.
	 */
	private void initPeerOnClickListener() {
		mPeerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(
					AdapterView<?> arg0, View view, int pos, long arg3) {
				P2PDevice peer = (P2PDevice) mPeerList.getItemAtPosition(pos);
				
				if (peer != null)
					confirmConnect(peer);
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		mSyncManager.registerBroadcastReceiver();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		mSyncManager.unregisterBroadcastReceiver();
	}
	
	/**
	 * On-Click handler of the magic button.
	 * @param view The clicked view.
	 */
	public void onButtonClick(View view) {
		Log.i(TAG, "Magic Button Clicked");
		
		if (mConnected)
			confirmDisconnect();
		else
			mSyncManager.discoverPeers();
	}
	
	/**
	 * On-click handler of the close button.
	 * @param view The clicked view.
	 */
	public void onCloseButtonClick(View view) {
		Log.i(TAG, "Close Button Clicked");
		
		if (mSyncManager.isSynchronizationActive() || mSyncManager.isConnected()) {
			mClosing = true;
			
			confirmDisconnect();
			setStatus("Waiting for synchronization to stop...");
		} else {
			finish();
		}
	}
	
	/**
	 * Confirms whether or not the user really wants to connect to the specified
	 * peer.
	 * @param peer The peer to connect to.
	 */
	private void confirmConnect(final P2PDevice peer) {
		new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle("Connect")
			.setMessage("Are you sure you want to connect to " + peer.getName() + "?")
			.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mSyncManager.connectTo(peer);
					}
				})
			.setNegativeButton("No", null)
			.show();
	}
	
	/**
	 * Confirms whether or not the user really wants to disconnect.
	 */
	private void confirmDisconnect() {
		new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle("Disconnect")
			.setMessage("Are you sure you want to disconnect?")
			.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						disconnect();
					}
				})
			.setNegativeButton("No",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mClosing = false;
					}
				})
			.show();
	}
	
	/**
	 * Stops the synchronization and disconnects from the P2P group.
	 */
	private void disconnect() {
		mSyncManager.stopSync();
	}
	
	/**
	 * Sets the text of the status label.
	 * @param status The status text.
	 */
	private void setStatus(String status) {
		mStatusLabel.setText(getString(R.string.label_status) + " " + status);
	}
	
	/**
	 * Sets the text of the you status label.
	 * @param name The name of this device.
	 * @param status The status of this device.
	 */
	private void setYouStatus(String name, String status) {
		mNameLabel.setText(
				getString(R.string.label_this_device) + " " + 
				name + " (" + status + ")");
	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.IP2PChangeListener#onPeersAvailable(java.util.List, boolean, java.lang.Object)
	 */
	public void onPeersAvailable(
			List<P2PDevice> peers, boolean completeList, Object sender) {
		Log.i(TAG, "Received Notification: peersAvailable");
		
		if (completeList)
			mPeers.clear();
		
		mPeers.addAll(peers);
		mPeerAdapter.notifyDataSetChanged();
		
		setStatus("Peers found (" + mPeers.size() + ")");
	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.IP2PChangeListener#onP2pInterfaceStatusChange(org.societies.android.p2p.P2PSyncManager.P2PInterfaceStatus, java.lang.Object)
	 */
	public void onP2pInterfaceStatusChange(
			P2PInterfaceStatus status, Object sender) {
		Log.i(TAG, "Received Notification: p2pInterfaceStatusChange");
		
		setStatus("P2P Interface " + status);
	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.IP2PChangeListener#onThisDeviceChange(org.societies.android.p2p.P2PDevice, java.lang.Object)
	 */
	public void onThisDeviceChange(P2PDevice device, Object sender) {
		Log.i(TAG, "Received Notification: thisDeviceChange");
		
		setYouStatus(device.getName(), device.getConnectionStatus());
	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.IP2PChangeListener#onDiscoverPeersFailure(java.lang.String, java.lang.Object)
	 */
	public void onDiscoverPeersFailure(String reason, Object sender) {
		Log.i(TAG, "Received Notification: discoverPeersFailure");
		
		setStatus("Failed to discover peers (" + reason + ")");
	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.IP2PChangeListener#onConnectFailure(java.lang.String, java.lang.Object)
	 */
	public void onConnectionFailure(String reason, Object sender) {
		Log.i(TAG, "Received Notification: connectionFailure");
		
		setStatus("Failed to connect (" + reason + ")");
	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.IP2PChangeListener#onSuccessfulConnection(org.societies.android.p2p.P2PSyncManager.SyncRole, java.lang.Object)
	 */
	public void onConnectionSuccess(SyncRole role, Object sender) {
		Log.i(TAG, "Received Notification: connectionSuccess");
		
		mConnected = true;
		
		mMagicButton.setText(getString(R.string.button_magic_disconnect));
		setStatus("Connected as " + role);
	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.IP2PChangeListener#onDisconnectFailure(java.lang.String, java.lang.Object)
	 */
	public void onDisconnectFailure(String reason, Object sender) {
		Log.i(TAG, "Received Notification: disconnectFailure");
		
		setStatus("Failed to disconnect (" + reason + ")");
		
		if (mClosing)
			finish();
	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.IP2PChangeListener#onDisconnectSuccess(java.lang.Object)
	 */
	public void onDisconnectSuccess(Object sender) {
		Log.i(TAG, "Received Notification: disconnectSuccess");
		
		mConnected = false;
		
		mMagicButton.setText(getString(R.string.button_magic_discover));
		setStatus("Disconnected");
		
		if (mClosing)
			finish();
	}

	/* (non-Javadoc)
	 * @see org.societies.android.p2p.IP2PChangeListener#onSyncStopped(java.lang.Object)
	 */
	public void onSyncStopped(Object sender) {
		Log.i(TAG, "Received Notification: syncStopped");
		
		mSyncManager.disconnect();
	}
}
