package dao;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

/**
 * This class handles file uploads to Azure Blob Storage.
 */
public class StorageUploader {

    /**
     * The client for interacting with the blob container
     */
    private BlobContainerClient containerClient;

    /**
     * Constructor that initializes the connection to Azure Blob Storage.
     * It sets up the container client with the provided connection string and container name.
     */
    public StorageUploader() {
        this.containerClient = new BlobContainerClientBuilder()
                .connectionString("DefaultEndpointsProtocol=https;AccountName=csc311pjtorage;AccountKey=SDbJQX9KT0faiz+pW92OfW73sePMTldigqFCsEYIJiQmmfke6jMT5DgkKgffhhBkf9QhS8CosTd9+AStOxMP1Q==;EndpointSuffix=core.windows.net")
                .containerName("media-files")
                .buildClient();
    }

    /**
     * Uploads a file to the Azure Blob Storage container.
     *
     * @param filePath The local path of the file to be uploaded
     * @param blobName The name to be given to the blob in the container
     */
    public void uploadFile(String filePath, String blobName) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.uploadFromFile(filePath);
    }

    /**
     * Retrieves the BlobContainerClient instance.
     *
     * @return The BlobContainerClient used for interacting with the container
     */
    public BlobContainerClient getContainerClient() {
        return containerClient;
    }
}