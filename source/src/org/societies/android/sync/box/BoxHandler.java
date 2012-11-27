/**
 * Copyright 2012 UbiCollab
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.societies.android.sync.box;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.societies.android.box.BoxConstants;
import org.societies.android.platform.entity.Community;
import org.societies.android.platform.entity.CommunityActivity;
import org.societies.android.platform.entity.Entity;
import org.societies.android.platform.entity.Membership;
import org.societies.android.platform.entity.Person;
import org.societies.android.platform.entity.PersonActivity;
import org.societies.android.platform.entity.Relationship;
import org.societies.android.platform.entity.Service;
import org.societies.android.platform.entity.ServiceActivity;
import org.societies.android.platform.entity.Sharing;
import org.societies.android.util.ThreadPool;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.util.Log;

import com.box.androidlib.Box;
import com.box.androidlib.BoxSynchronous;
import com.box.androidlib.DAO.BoxFile;
import com.box.androidlib.DAO.BoxFolder;
import com.box.androidlib.DAO.DAO;
import com.box.androidlib.DAO.SearchResult;
import com.box.androidlib.DAO.Update;
import com.box.androidlib.ResponseListeners.CreateFolderListener;
import com.box.androidlib.ResponseListeners.GetUpdatesListener;
import com.box.androidlib.ResponseParsers.AccountTreeResponseParser;
import com.box.androidlib.ResponseParsers.FolderResponseParser;
import com.box.androidlib.ResponseParsers.SearchResponseParser;
import com.box.androidlib.ResponseParsers.UpdatesResponseParser;

/**
 * Handles the interaction with Box.com
 * 
 * @author Kato
 */
public class BoxHandler {
	
	private static final String TAG = "BoxHandler";
	
	/* TEMPORARY CONSTANTS */
	private static final String BOX_FOLDER_ROOT = "UbiShare";
	/* END TEMPORARY CONSTANTS */

	/** The character used as separator in entity file names. */
	public static final String ENTITY_FILE_NAME_SEPARATOR = "_";
	/** The file name format of uploaded entities. */
	public static final String ENTITY_FILE_NAME_FORMAT =
			"%s" + ENTITY_FILE_NAME_SEPARATOR + "%s";
	/** The extension of deleted entity files. */
	public static final String ENTITY_DELETED_EXTENSION = ".del";
	
	private ThreadPool mThreadPool;
	private Map<Class<? extends Entity>, String> mFolderMap;
	private BoxFolder mRootFolder;
	private String mAuthToken;
	private boolean mInitialized;
	private ContentResolver mResolver;
	private SharedPreferences mPreferences;
	
	/**
	 * Initializes a new BoxHandler.
	 * @param preferences The shared preferences.
	 * @param resolver The content resolver.
	 */
	public BoxHandler(SharedPreferences preferences, ContentResolver resolver) {
		mThreadPool = new ThreadPool(1);
		mFolderMap = new HashMap<Class<? extends Entity>, String>();
		mInitialized = false;
		mResolver = resolver;
		mPreferences = preferences;
	}
	
	/**
	 * Initializes the Box handler. A call to this function is mandatory before
	 * any other interaction with box.
	 * @param authToken The token used to authenticate.
	 */
	public void initialize(String authToken) {
		mAuthToken = authToken;
		
		mThreadPool.startExecution();
		
		initFolderMappings();
		loadPreferences();
		
		if (mRootFolder == null)
			fetchDirectoryTree();
		
		if (mRootFolder.getFoldersInFolder().size() < 9) {
			createFolderStructure();
			fetchDirectoryTree();
		}
		
		mInitialized = true;
	}
	
	/**
	 * Cancels all running operations.
	 */
	public void cancelRunningOperations() {
		try {
			mThreadPool.stopExecution(true);
		} catch (InterruptedException e) { /* IGNORED */ }
	}
	
	
	/**
	 * Uploads a database entity to Box.
	 * @param entity The entity to upload.
	 */
	public void uploadEntity(Entity entity) {
		if (!mInitialized)
			throw new IllegalStateException("Not initialized.");
		else {
			/*BoxUploadOperation operation =
					new BoxUploadOperation(entity, mAuthToken, this, mResolver);
			
			mThreadPool.add(operation);*/
		}
	}
	
	/**
	 * Uploads a community to Box.
	 * @param community The community to upload.
	 * @throws IOException If an error occurs while uploading.
	 */
	public void uploadCommunity(Community community) throws IOException {
		if (!mInitialized)
			throw new IllegalStateException("Not initialized.");
		else {
			long targetId;
			try {
				targetId = Long.parseLong(community.getGlobalId());
			} catch (NumberFormatException e) {
				long creationDate = new Date().getTime() / 1000;
				BoxFolder folder = createFolder(
						community.getName() + ENTITY_FILE_NAME_SEPARATOR + creationDate, 0);
				
				community.setGlobalId(String.valueOf(folder.getId()));
				
				targetId = folder.getId();
			}
			
			addUploadOperation(community, "meta", targetId);
		}
	}
	
	/**
	 * Uploads a community activity to Box.
	 * @param activity The activity to upload.
	 */
	public void uploadCommunityActivity(CommunityActivity activity) {
		if (!mInitialized)
			throw new IllegalStateException("Not initialized.");
		else {
			long targetId = Long.parseLong(activity.getGlobalIdFeedOwner());
			addUploadOperation(activity, null, targetId);
		}
	}
	
	/**
	 * Processes the specified Box updates.
	 * @param updates The Box updates to process, or <code>null</code> to
	 * retrieve all the data from Box.
	 */
	public void processUpdates(List<Update> updates) {
		if (updates == null)
			fetchAllEntities();
		else {
			for (Update update : updates) {
				if (update.getUpdateType().equals(Update.UPDATE_FILE_ADDED) ||
					update.getUpdateType().equals(Update.UPDATE_FILE_UPDATED)) {
					for (BoxFile file : update.getFiles()) {
						downloadEntity(file, update.getFolderId());
					}
				}
			}
		}
	}
	
	/**
	 * Gets a list of updates since the specified timestamp.
	 * @param timestamp The Unix time (in seconds) of the oldest updates to get.
	 * @return A list of updates since the specified timestamp.
	 * @throws IOException If an error occurs while fetching updates.
	 */
	public List<Update> getUpdatesSince(long timestamp) throws IOException {
		long unixTimeNow = new Date().getTime() / 1000;
		
		UpdatesResponseParser response =
				BoxSynchronous.getInstance(BoxConstants.API_KEY).getUpdates(
						mAuthToken, timestamp, unixTimeNow, null);
		
		if (response.getStatus().equals(GetUpdatesListener.STATUS_S_GET_UPDATES))
			return response.getUpdates();
		else
			throw new IOException("Failed to get updates: " + response.getStatus());
	}
	
	/**
	 * Gets the folder ID of the specified entity class.
	 * @param entityClass The entity class.
	 * @return The ID of the folder related to the specified entity class.
	 */
	public long getFolderId(Class<? extends Entity> entityClass) {
		for (BoxFolder subFolder : mRootFolder.getFoldersInFolder()) {
			if (subFolder.getFolderName().equals(mFolderMap.get(entityClass)))
				return subFolder.getId();
		}
		
		throw new IllegalArgumentException(
				"Folder of entity type " + entityClass.getName() + " not found");
	}
	
	/**
	 * Waits for running operations to complete.
	 * @throws InterruptedException If the thread is interrupted while waiting.
	 */
	public void waitForRunningOperationsToComplete() throws InterruptedException {
		mThreadPool.stopExecution(false);
	}
	
	/**
	 * Adds an upload operation to the thread pool.
	 * @param entity The entity to upload.
	 * @param fileName The name of the file to upload to, or <code>null</code> if the
	 * name should be automatically generated.
	 * @param targetId The ID of the folder to upload to.
	 */
	private void addUploadOperation(Entity entity, String fileName, long targetId) {
		BoxUploadOperation operation =
				new BoxUploadOperation(
						entity, fileName, targetId, mAuthToken, mResolver);
		
		mThreadPool.add(operation);
	}
	
	/**
	 * Initializes the mapping of entities and box folders.
	 */
	private void initFolderMappings() {
		mFolderMap.put(Community.class, "communities");
		mFolderMap.put(CommunityActivity.class, "communities_activity");
		mFolderMap.put(Membership.class, "memberships");
		mFolderMap.put(Person.class, "people");
		mFolderMap.put(PersonActivity.class, "people_activity");
		mFolderMap.put(Relationship.class, "relationships");
		mFolderMap.put(Service.class, "services");
		mFolderMap.put(ServiceActivity.class, "services_activity");
		mFolderMap.put(Sharing.class, "sharings");
	}
	
	/**
	 * Loads the preferences.
	 */
	private void loadPreferences() {
		String rootJson = mPreferences.getString(
				BoxConstants.PREFERENCE_DIRECTORY_TREE, null);
		
		if (rootJson != null)
			mRootFolder = DAO.fromJSON(rootJson, BoxFolder.class);
	}
	
	/**
	 * Fetches the directory three of the root folder and stores it in 
	 * the shared preferences.
	 */
	private void fetchDirectoryTree() {
		try {
			// TODO: Add root folder to shared preferences (Setup app)
			SearchResponseParser searchParser =
					BoxSynchronous.getInstance(BoxConstants.API_KEY).search(
							mAuthToken,
							BOX_FOLDER_ROOT,
							Box.SORT_RELEVANCE,
							1,
							1,
							Box.DIRECTION_DESC,
							new String[0]);
			
			SearchResult searchResult = searchParser.getSearchResult();
			BoxFolder root = searchResult.getFolders().get(0);
			
			AccountTreeResponseParser treeParser =
					BoxSynchronous.getInstance(BoxConstants.API_KEY).getAccountTree(
							mAuthToken,
							root.getId(),
							new String[] {
								Box.PARAM_SIMPLE,
								Box.PARAM_NOFILES
							});
			
			mRootFolder = treeParser.getFolder();
			
			mPreferences.edit().putString(
					BoxConstants.PREFERENCE_DIRECTORY_TREE,
					DAO.toJSON(mRootFolder)
			).commit();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}
	
	/**
	 * Gets the ID of the root folder.
	 * @return The ID of the root folder.
	 */
	private long getRootFolderId() {
		if (mRootFolder != null)
			return mRootFolder.getId();
		else
			throw new IllegalStateException("Root folder not initialized");
	}
	
	/**
	 * Creates a folder in Box.
	 * @param name The name of the folder.
	 * @param parentId The ID of the parent folder. The ID 0 (zero) is the
	 * root folder.
	 * @return The newly created folder, or <code>null</code> on error.
	 * @throws IOException If an error occurs while creating folder.
	 */
	private BoxFolder createFolder(String name, long parentId) throws IOException {
		FolderResponseParser response =
				BoxSynchronous.getInstance(BoxConstants.API_KEY).createFolder(
						mAuthToken,
						parentId,
						name,
						false);
		
		if (response.getStatus().equals(CreateFolderListener.STATUS_CREATE_OK))
			return response.getFolder();
		else
			return null;
	}
	
	/**
	 * Creates the folder structure.
	 */
	private void createFolderStructure() {
		try {
			for (Class<? extends Entity> entityClass : mFolderMap.keySet())
				createFolder(mFolderMap.get(entityClass), getRootFolderId());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}
	
	/**
	 * Downloads the specified entity.
	 * @param file The file to download.
	 * @param folderId The ID of the folder containing the entity.
	 */
	private void downloadEntity(BoxFile file, long folderId) {
		BoxDownloadOperation operation = new BoxDownloadOperation(
				file,
				mAuthToken,
				mResolver);
		
		mThreadPool.add(operation);
	}
	
	/**
	 * Downloads all the entities in the directory tree under the
	 * specified root folder.
	 * @param rootFolder The root folder.
	 */
	private void downloadAllEntities(BoxFolder rootFolder) {
		for (BoxFile file : rootFolder.getFilesInFolder())
			downloadEntity(file, rootFolder.getId());
		
		for (BoxFolder subFolder : rootFolder.getFoldersInFolder())
			downloadAllEntities(subFolder);
	}
	
	/**
	 * Fetches all the entities from Box.
	 */
	private void fetchAllEntities() {
		try {
			AccountTreeResponseParser treeParser =
					BoxSynchronous.getInstance(BoxConstants.API_KEY).getAccountTree(
							mAuthToken,
							mRootFolder.getId(),
							new String[] { Box.PARAM_SIMPLE });
			
			downloadAllEntities(treeParser.getFolder());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}
}
