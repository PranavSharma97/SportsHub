package com.example.admin.loginandregistration.app;

/**
 * Created by admin on 4/14/2016.
 */
public class AppConfig {

    // Server user login url
    public static String URL_LOGIN = "http://67.205.184.148/android_login_api/login.php";

    // Server user register url
    public static String URL_REGISTER = "http://67.205.184.148/android_login_api/register.php";

    public static String URL_CHECK_FOR_USERNAME_AND_EMAIL = "http://67.205.184.148/android_login_api/check_for_username_and_email.php";

    public static String URL_GET_USER_PROFILE = "http://67.205.184.148/android_login_api/get_user_profile.php";

    public static String URL_GET_USER_INTERACTION = "http://67.205.184.148/sports_hub_location_api/get_interaction.php";

    public static String URL_UPDATE_USER_PROFILE = "http://67.205.184.148/android_login_api/update_user_profile.php";

    public static String URL_CREATE_LOCATION = "http://67.205.184.148/sports_hub_location_api/create_location.php";

    public static String URL_CREATE_INTERACTION = "http://67.205.184.148/sports_hub_location_api/create_interaction.php";

    public static String URL_GET_ALL_LOCATIONS = "http://67.205.184.148/sports_hub_location_api/get_all_locations.php";

    public static String URL_SEND_SMS = "http://67.205.184.148/sports_hub_location_api/request_sms.php";

    public static String URL_UPDATE_LOCATION = "http://67.205.184.148/sports_hub_location_api/update_location_details.php";

    public static String URL_UPDATE_INTERACTION = "http://67.205.184.148/sports_hub_location_api/update_interaction.php";

    public static String URL_DELETE_LOCATION = "http://67.205.184.148/sports_hub_location_api/delete_location.php";

    public static String URL_GET_USER_LOCATION = "http://67.205.184.148/sports_hub_location_api/get_location.php";

    public static final String URL_REQUEST_SMS = "http://67.205.184.148/android_sms/request_sms.php";
    public static final String URL_VERIFY_OTP = "http://67.205.184.148/android_sms/verify_otp.php";

    public static final String UPLOAD_URL = "http://67.205.184.148/profile_photo_api/upload.php";

    public static final String UPDATE_PROFILE_PHOTO_URL = "http://67.205.184.148/profile_photo_api/update_profile_photo.php";

    public static final String GET_IMAGE_URL = "http://67.205.184.148/profile_photo_api/getImage.php?email=";

    // SMS provider identification
    // It should match with your SMS gateway origin
    // You can use  MSGIND, TESTER and ALERTS as sender ID
    // If you want custom sender Id, approve MSG91 to get one
    public static final String SMS_ORIGIN = "ANHIVE";

    // special character to prefix the otp. Make sure this character appears only once in the sms
    public static final String OTP_DELIMITER = ":";

}

