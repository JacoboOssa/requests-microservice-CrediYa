package co.com.crediya.usecase.petition.util;

import co.com.crediya.model.user.User;

public class UserUtil {

    public static User user(){
        return User.builder()
                .email("jq@gmail.com")
                .build();
    }
}
