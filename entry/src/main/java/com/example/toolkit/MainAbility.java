package com.example.toolkit;

import com.example.toolkit.slice.MainAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;

import java.util.ArrayList;
import java.util.List;

public class MainAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(MainAbilitySlice.class.getName());
        requestPermission();
    }

    private void requestPermission(){
        String[] permissions = {
                "ohos.permission.INTERNET",
                "ohos.permission.READ_USER_STORAGE",
                "ohos.permission.WRITE_USER_STORAGE",
                "ohos.permission.MANAGE_USER_STORAGE"
        };
        List<String> permissionToProcess = new ArrayList<>();
        for (String permission: permissions){
            permissionToProcess.add(permission);
        }
        requestPermissionsFromUser(permissionToProcess.toArray(new String[0]),0);
    }
}
