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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.societies.android.box.BoxConstants;
import org.societies.android.platform.entity.Entity;

import android.content.ContentResolver;
import android.util.Log;

import com.box.androidlib.Box;
import com.box.androidlib.BoxSynchronous;
import com.box.androidlib.DAO.BoxFile;
import com.box.androidlib.ResponseListeners.FileUploadListener;
import com.box.androidlib.ResponseListeners.RenameListener;
import com.box.androidlib.ResponseParsers.FileResponseParser;

/**
 * Thread handling Box upload operations.
 *
 * @author Kato
 */
public class BoxUploadOperation extends BoxOperation {
	
	private static final String TAG = "BoxUploadOperation";
	
	/** The content of the temporary files. */
	private static final String TEMP_CONTENT = ".";
	/** The status returned when trying to upload to a deleted file. */
	private static final String STATUS_FILE_DELETED = "e_file_deleted";

	private Entity mEntity;
	private BoxHandler mBoxHandler;
	private BoxSynchronous mBoxInstance;
	private String mAuthToken;
	private ContentResolver mResolver;
	
	/**
	 * Initializes a Box upload operation.
	 * @param entity The entity to be uploaded.
	 * @param authToken The authentication token.
	 * @param boxHandler The box handler instance.
	 * @param resolver The content resolver.
	 */
	public BoxUploadOperation(
			Entity entity,
			String authToken,
			BoxHandler boxHandler,
			ContentResolver resolver) {
		mEntity = entity;
		mBoxHandler = boxHandler;
		mBoxInstance = BoxSynchronous.getInstance(BoxConstants.API_KEY);
		mAuthToken = authToken;
		mResolver = resolver;
	}
	
	@Override
	public void run() {
		try {
			try {
				long fileId = Long.parseLong(mEntity.getGlobalId());
				uploadEntity(fileId, false);
			} catch (NumberFormatException nfe) {
				uploadEntity(getNewFileId(), true);
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}
	
	/**
	 * Uploads the entity to an existing file.
	 * @param fileId The ID of the existing file.
	 * @param updateEntity Whether or not the entity should be updated with a
	 * new global ID.
	 * @throws IOException If an error occurs while uploading.
	 */
	private void uploadEntity(long fileId, boolean updateEntity) throws IOException {
		if (updateEntity)
			mEntity.setGlobalId(String.valueOf(fileId));
		
		FileResponseParser response = upload(
				Box.UPLOAD_ACTION_OVERWRITE,
				mEntity.serialize(),
				String.valueOf(fileId),
				fileId);
		
		if (response.getStatus().equals(FileUploadListener.STATUS_UPLOAD_OK)) {
			if (updateEntity)
				mEntity.update(mResolver);
		} else if (response.getStatus().equals(STATUS_FILE_DELETED)) {
			// TODO: Either delete local or upload new copy
			// mEntity.delete(mResolver);
			uploadEntity(getNewFileId(), true);
		} else {
			throw new IOException("Failed to upload entity: " + response.getStatus());
		}
	}
	
	/**
	 * Creates a temporary file and returns it's ID.
	 * @return The ID of the the temporary file.
	 * @throws IOException If an error occurs while uploading.
	 */
	private long getNewFileId() throws IOException {
		FileResponseParser response = upload(
				Box.UPLOAD_ACTION_UPLOAD,
				TEMP_CONTENT,
				String.valueOf(mEntity.serialize().hashCode()),
				mBoxHandler.getFolderId(mEntity.getClass()));
		
		if (response.getStatus().equals(FileUploadListener.STATUS_UPLOAD_OK)) {
			BoxFile tempFile = response.getFile();
			
			String status = mBoxInstance.rename(
					mAuthToken,
					Box.TYPE_FILE,
					tempFile.getId(),
					String.valueOf(tempFile.getId()));
			
			if (status.equals(RenameListener.STATUS_S_RENAME_NODE))
				return tempFile.getId();
		}
		
		throw new IOException("Failed to create temporary file");
	}
	
	/**
	 * Uploads the specified content to a file.
	 * @param action The action to perform (see Box.upload() doc).
	 * @param content The content to upload.
	 * @param name The name of the file to upload to.
	 * @param destinationId The folder ID or file ID, depending on the action.
	 * @return The response.
	 * @throws IOException If an error occurs while uploading.
	 */
	private FileResponseParser upload(
			String action,
			String content,
			String name,
			long destinationId) throws IOException {
		return mBoxInstance.upload(
				mAuthToken,
				action,
				createInputStream(content),
				name,
				destinationId,
				null,
				null);
	}
	
	/**
	 * Creates an input stream containing the specified string.
	 * @param content The content of the stream.
	 * @return An input stream containing the specified string.
	 */
	private InputStream createInputStream(String content) {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
		
		return inputStream;
	}
}