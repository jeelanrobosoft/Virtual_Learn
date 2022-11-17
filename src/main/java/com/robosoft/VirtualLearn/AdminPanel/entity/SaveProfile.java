package com.robosoft.VirtualLearn.AdminPanel.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveProfile {

    private MultipartFile profilePhoto;
    private String  userName;
    private Integer  occupation;
    private String  gender;
    private String  dateOfBirth;
    private String  twitterLink;
    private String  faceBookLink;


}
