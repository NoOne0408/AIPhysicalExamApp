package utils;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

public class Dialog {
    static public void showSampleInformationDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("选择一个选项");

        // 添加三个按钮
        builder.setItems(new CharSequence[]{"选项1", "选项2", "选项3"}, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // 根据用户点击的按钮，改变变量的值

                dialog.dismiss(); // 关闭对话框
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
