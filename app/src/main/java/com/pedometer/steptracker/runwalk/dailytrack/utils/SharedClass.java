package com.pedometer.steptracker.runwalk.dailytrack.utils;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

//import com.mockingjay.blood.bloodpressureapp.bloodsugar.datamodel.CertificationsModel;
//import com.mockingjay.blood.bloodpressureapp.bloodsugar.datamodel.PublicationsModel;
//import com.mockingjay.blood.bloodpressureapp.bloodsugar.datamodel.TestScoreModel;

public class SharedClass {

    public static int template_no = 0;
    public static int flag, page = 0;
    public static int status = 0; // 1 for create 2 for manage
    public static String email;
    public static List<String> interestsData = new ArrayList<>();
    public static List<String> languageData = new ArrayList<>();
    public static List<String> HobbiesData = new ArrayList<>();
//    public static List<CertificationsModel> CertificationsData = new ArrayList<>();
//    public static List<PublicationsModel> PublicationsData = new ArrayList<>();
//    public static List<TestScoreModel> TestScoreData = new ArrayList<>();
    public static Uri mImageUri = null;
    public static String imgDecodableString = "";
    public static StringBuilder htmlContent = new StringBuilder();
    public static Bitmap bitmapAvatar=null;
    public static int template = 0;


    public static Boolean openInterests = true;
    public static Boolean openAchievements = true;
    public static Boolean openActivitis = true;
    public static Boolean openPublication = true;
    public static Boolean openLanguages = true;
    public static Boolean openAdditional = true;
    public static Boolean openProject = true;
    public static Boolean openReference = true;
    public static Boolean openSignature = false;



    public static  Boolean Page_break_default = true;
    public static  Boolean Page_break_size = true;
    public static  int PageMarginPosition = 0;

    public static String filePath = null;

}
