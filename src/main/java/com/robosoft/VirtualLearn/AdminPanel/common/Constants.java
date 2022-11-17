package com.robosoft.VirtualLearn.AdminPanel.common;

import org.springframework.stereotype.Service;

@Service
public class Constants
{
    public static final String FIREBASE_SDK_JSON ="src/drivelistfilesapplication-firebase-adminsdk-ny679-8a0c60eb7d.json";//copy the sdk-config file root address, if its in root ,filename is enough
    public static final String FIREBASE_BUCKET = "drivelistfilesapplication.appspot.com";//enter your bucket name
    public static final String FIREBASE_PROJECT_ID ="drivelistfilesapplication";//enter your project id
    public static final String DOWNLOAD_URL = "https://firebasestorage.googleapis.com/v0/b/drivelistfilesapplication.appspot.com/o/%s?alt=media";
}
