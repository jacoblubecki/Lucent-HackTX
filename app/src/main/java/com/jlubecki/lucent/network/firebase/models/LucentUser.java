package com.jlubecki.lucent.network.firebase.models;

import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * Created by Jacob on 10/22/16.
 */

public class LucentUser {

   private static FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
   private static String userID;
   private static String userName;

   @Nullable
   public static String getUserID() {
       if (user != null) {
           userID = user.getUid();
       } else {
           return null;
       }
       return userID;
   }

    @Nullable
    public static String getUserName(){
        if(user!=null){
            userName = user.getDisplayName();
        }
        else
            return null;

        return userName;
    }
}
