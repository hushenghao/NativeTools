package com.dede.nativetools.ui

import android.content.Context
import android.content.DialogInterface
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.preference.EditTextPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MaterialEditTextPreference(context: Context, attrs: AttributeSet? = null) :
    EditTextPreference(context, attrs), DialogInterface.OnClickListener {

    private lateinit var editText: EditText

    override fun onClick() {
        val view = LayoutInflater.from(context).inflate(dialogLayoutResource, null)

        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(dialogTitle)
            .setView(view)
            .setPositiveButton(positiveButtonText, this)
            .setNegativeButton(negativeButtonText, null)

        val dialogMessageView = view.findViewById<View>(android.R.id.message)
        if (dialogMessageView != null) {
            val message = dialogMessage
            var newVisibility = View.GONE
            if (!TextUtils.isEmpty(message)) {
                if (dialogMessageView is TextView) {
                    dialogMessageView.text = message
                }
                newVisibility = View.VISIBLE
            }
            if (dialogMessageView.visibility != newVisibility) {
                dialogMessageView.visibility = newVisibility
            }
        }

        editText = view.findViewById(android.R.id.edit)

        editText.requestFocus()
        editText.setText(text)
        // Place cursor at the end
        editText.setSelection(editText.text.length)

        val dialog = builder.show()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        val value = editText.text.toString()
        if (callChangeListener(value)) {
            text = value
        }
    }
}