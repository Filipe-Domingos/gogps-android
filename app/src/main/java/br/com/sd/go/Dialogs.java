package br.com.sd.go;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import br.com.sd.go.R;

/**
 * Created by alan on 25/02/14.
 */
public class Dialogs {

    public static AlertDialog.Builder DialogConfirm(Context context, int message, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

//        builder.setPositiveButton(R.string.label_ok, onClickListener)
//                .setNegativeButton(R.string.label_cancelar, onClickListener)
//                .setMessage(message);

        return builder;
    }

}