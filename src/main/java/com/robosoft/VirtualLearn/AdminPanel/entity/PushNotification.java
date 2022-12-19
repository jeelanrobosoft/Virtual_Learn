
package com.robosoft.VirtualLearn.AdminPanel.entity;


import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class PushNotification {

    public static PushNotificationResponse sendPushNotification(String to, String body, String title){
        Logger logger = LoggerFactory.getLogger(PushNotification.class);
        RestTemplate restTemplate = new RestTemplate();
        final String Base_URL = "https://fcm.googleapis.com/fcm/send";
//         String s = "{\n" +
//                "\t\"to\": "+ "\"" + fcmToken + "\",\n" +
//                "\t\"notification\": {\n" +
//                "\t\t\"body\": \"" + body +"\",\n" +
//                "\t\t\"title\": \""+ title +"\",\n" +
//                "\t\t\"subtitle\": \"Firebase Cloud Message Subtitle\"\n" +
//                "\t}\n" +
//                "}";
        Notification notification = new Notification(body,title);
        PushNotificationBody pushNotificationBody = new PushNotificationBody(to,notification);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization","key=AAAAufkt9Rw:APA91bFHo9emy365YwdS5kCnvSDwoK9tVx7jfA9VLD3hoUO0INUmbpyO5b7Mf984C4qWulvzkWm153C4e4Ff8hNH3l5-tzPomZHpa-45TrW9T-p3MX3dwJh6HyRTlwsoWw7xWhPYzTbb");
        HttpEntity<Object> entity = new HttpEntity<>(pushNotificationBody,headers);

        try{
        ResponseEntity<PushNotificationResponse> responseEntity = restTemplate.exchange(Base_URL, HttpMethod.POST,entity, PushNotificationResponse.class);
            PushNotificationResponse notificationResponse = responseEntity.getBody();
            return notificationResponse;
        } catch (Exception e){
            logger.info("Cannot send push notification to Ios device");
            return null;
        }

        //https://fcm.googleapis.com/fcm/send

//        HttpStatus response = responseEntity.getStatusCode();
//        HttpHeaders httpHeaders = responseEntity.getHeaders();
//        PushNotificationResponse notificationResponse = responseEntity.getBody();
//        return notificationResponse;

    }

}
