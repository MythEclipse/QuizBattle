package com.mytheclipse.quizbattle.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.mytheclipse.quizbattle.R

/**
 * Helper class for showing beautiful result dialogs.
 * Replaces Toast messages with modal dialogs for better UX.
 */
object ResultDialogHelper {

    /**
     * Shows a success dialog with green checkmark icon.
     * 
     * @param context Context to show dialog in
     * @param title Dialog title (e.g., "Berhasil!")
     * @param message Dialog message body
     * @param buttonText Primary button text (default: "OK")
     * @param onDismiss Optional callback when dialog is dismissed
     */
    fun showSuccess(
        context: Context,
        title: String,
        message: String,
        buttonText: String = "OK",
        onDismiss: (() -> Unit)? = null
    ) {
        showDialog(
            context = context,
            iconRes = R.drawable.ic_success,
            title = title,
            message = message,
            buttonText = buttonText,
            buttonColorRes = R.color.primary_green,
            onDismiss = onDismiss
        )
    }

    /**
     * Shows an error dialog with red X icon.
     * 
     * @param context Context to show dialog in
     * @param title Dialog title (e.g., "Gagal")
     * @param message Dialog message body
     * @param buttonText Primary button text (default: "OK")
     * @param onDismiss Optional callback when dialog is dismissed
     */
    fun showError(
        context: Context,
        title: String,
        message: String,
        buttonText: String = "OK",
        onDismiss: (() -> Unit)? = null
    ) {
        showDialog(
            context = context,
            iconRes = R.drawable.ic_error,
            title = title,
            message = message,
            buttonText = buttonText,
            buttonColorRes = R.color.primary_red,
            onDismiss = onDismiss
        )
    }

    /**
     * Shows an info dialog with primary blue button.
     * 
     * @param context Context to show dialog in
     * @param title Dialog title
     * @param message Dialog message body
     * @param buttonText Primary button text (default: "OK")
     * @param onDismiss Optional callback when dialog is dismissed
     */
    fun showInfo(
        context: Context,
        title: String,
        message: String,
        buttonText: String = "OK",
        onDismiss: (() -> Unit)? = null
    ) {
        showDialog(
            context = context,
            iconRes = R.drawable.ic_success, // Reuse success icon with different button color
            title = title,
            message = message,
            buttonText = buttonText,
            buttonColorRes = R.color.primary_blue,
            onDismiss = onDismiss
        )
    }

    private fun showDialog(
        context: Context,
        iconRes: Int,
        title: String,
        message: String,
        buttonText: String,
        buttonColorRes: Int,
        onDismiss: (() -> Unit)?
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_result, null)
        dialog.setContentView(view)
        
        // Make dialog background transparent so our rounded corners show
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        // Set dialog width to 85% of screen width
        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.85).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        // Set icon
        val iconView = view.findViewById<ImageView>(R.id.dialogIcon)
        iconView.setImageResource(iconRes)
        
        // Set title
        val titleView = view.findViewById<TextView>(R.id.dialogTitle)
        titleView.text = title
        
        // Set message
        val messageView = view.findViewById<TextView>(R.id.dialogMessage)
        messageView.text = message
        
        // Set button
        val primaryButton = view.findViewById<MaterialButton>(R.id.dialogPrimaryButton)
        primaryButton.text = buttonText
        primaryButton.backgroundTintList = ContextCompat.getColorStateList(context, buttonColorRes)
        primaryButton.setOnClickListener {
            dialog.dismiss()
        }
        
        // Handle dismiss callback
        dialog.setOnDismissListener {
            onDismiss?.invoke()
        }
        
        dialog.show()
    }

    /**
     * Shows a confirmation dialog with two buttons.
     * 
     * @param context Context to show dialog in
     * @param title Dialog title
     * @param message Dialog message body
     * @param confirmText Confirm button text
     * @param cancelText Cancel button text
     * @param onConfirm Callback when confirm is pressed
     * @param onCancel Optional callback when cancel is pressed
     */
    fun showConfirmation(
        context: Context,
        title: String,
        message: String,
        confirmText: String = "Ya",
        cancelText: String = "Batal",
        onConfirm: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_result, null)
        dialog.setContentView(view)
        
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.85).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        // Hide icon for confirmation dialogs
        view.findViewById<ImageView>(R.id.dialogIcon).visibility = android.view.View.GONE
        
        view.findViewById<TextView>(R.id.dialogTitle).text = title
        view.findViewById<TextView>(R.id.dialogMessage).text = message
        
        val primaryButton = view.findViewById<MaterialButton>(R.id.dialogPrimaryButton)
        primaryButton.text = confirmText
        primaryButton.setOnClickListener {
            dialog.dismiss()
            onConfirm()
        }
        
        val secondaryButton = view.findViewById<MaterialButton>(R.id.dialogSecondaryButton)
        secondaryButton.visibility = android.view.View.VISIBLE
        secondaryButton.text = cancelText
        secondaryButton.setOnClickListener {
            dialog.dismiss()
            onCancel?.invoke()
        }
        
        dialog.show()
    }
}
