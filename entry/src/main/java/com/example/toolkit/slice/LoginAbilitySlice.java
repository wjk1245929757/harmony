package com.example.toolkit.slice;

import com.example.toolkit.ResourceTable;
import com.example.toolkit.util.PreferenceUtils;
import com.example.toolkit.util.Utils;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.TextField;
import ohos.app.Context;

public class LoginAbilitySlice extends AbilitySlice {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_login);
        Button button = (Button) findComponentById(ResourceTable.Id_login_btn1);
        TextField textField1 = (TextField) findComponentById(ResourceTable.Id_login_textfield_username);
        TextField textField2 = (TextField) findComponentById(ResourceTable.Id_login_textfield_password);
        button.setClickedListener((Component c) -> {
            String username = textField1.getText();
            String password = textField2.getText();
            if (username.equalsIgnoreCase("")){
                Utils.showToast(LoginAbilitySlice.this, "用户名不为空");
            }else if (password.equalsIgnoreCase("")){
                Utils.showToast(LoginAbilitySlice.this, "密码不为空");
            }else {
                PreferenceUtils.getInstance().putString(getApplicationContext(),"username", username);
                PreferenceUtils.getInstance().putString(getApplicationContext(),"password", password);
                Intent intent1 = new Intent();
                intent1.setParam("username", username);
                setResult(intent1);
                terminate();
            }
        });
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
