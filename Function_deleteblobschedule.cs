using Microsoft.WindowsAzure.Storage;
using Microsoft.WindowsAzure.Storage.Blob;
using System;

public static void Run(TimerInfo myTimer, TraceWriter log)
{
    string storageConnectionString = "AZURE_STORAGE_CONNECTION_STRING";

    CloudStorageAccount storageAccount = CloudStorageAccount.Parse(storageConnectionString);
    CloudBlobClient cloudBlobClient = storageAccount.CreateCloudBlobClient();

    //get valid timestamp
    DateTime dateTime = DateTime.UtcNow.Date;
    dateTime = dateTime.AddDays(-2);
    long compTime = 1000 * (Int64)(dateTime.Subtract(new DateTime(1970, 1, 1))).TotalSeconds;
    log.Info("valid date:" + compTime.ToString());

    //list containers
    IEnumerable<CloudBlobContainer> containers = cloudBlobClient.ListContainers();
    foreach (CloudBlobContainer cloudBlobContainer in containers)
    {
        log.Info("find container:" + cloudBlobContainer.Name);
        //list blob in container
        foreach (IListBlobItem item in cloudBlobContainer.ListBlobs(null, false))
        {
            //get block blob
            if (item.GetType() == typeof (CloudBlockBlob))
            {
                CloudBlockBlob blob = (CloudBlockBlob) item;
                string fileName = blob.Name;
                log.Info("Fine blob: " + blob.Name);
            
                //get document prefix timestamp
                int startIndex = 0;
                int endIndex = fileName.IndexOf("_");
                if (endIndex > startIndex)
                {
                    string subString = fileName.Substring(startIndex, endIndex);
                    log.Info("prefix:" + subString);
                    long prefixTime = Convert.ToInt64(subString);

                    //delete the valid blob
                    if (prefixTime<compTime)
                    { 
                        log.Info("Delete blob: " + blob.Name);
                        blob.DeleteIfExistsAsync();             
                    }
                }
            }
        }
    }

    log.Info($"C# Timer trigger function executed at: {DateTime.Now}");
}

