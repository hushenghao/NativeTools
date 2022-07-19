package com.dede.nativetools.ui

import android.content.Context
import android.content.DialogInterface
import android.text.InputType
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.preference.EditTextPreference
import com.dede.nativetools.R
import com.dede.nativetools.util.isNotEmpty
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 使用MaterialAlertDialog实现的EditTextPreference
 */
class MaterialEditTextPreference(context: Context, attrs: AttributeSet? = null) :
    EditTextPreference(context, attrs), DialogInterface.OnClickListener {

    private lateinit var editText: EditText

    private var inputType: Int = InputType.TYPE_NULL

    init {
        dialogLayoutResource = R.layout.override_preference_dialog_edittext
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.MaterialEditTextPreference)
        inputType =
            typedArray.getInt(R.styleable.MaterialEditTextPreference_android_inputType, inputType)
        typedArray.recycle()
    }

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
        editText.inputType = inputType

        editText.requestFocus()
        editText.setText(text)
        // Place cursor at the end
        editText.setSelection(editText.text.length)

        val dialog = builder.show()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    private var defaultValue: String? = null

    override fun onSetInitialValue(defaultValue: Any?) {
        super.onSetInitialValue(defaultValue)
        this.defaultValue = defaultValue as? String
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        val defaultValue = this.defaultValue
        var value = editText.text.toString()
        if (value.isEmpty() && defaultValue.isNotEmpty()) {
            value = defaultValue
        }
        if (callChangeListener(value)) {
            text = value
        }
    }
}