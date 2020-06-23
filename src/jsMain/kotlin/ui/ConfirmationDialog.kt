package ui

import org.w3c.dom.HTMLButtonElement
import ui.UiControl.Companion.doWhenButtonClicked
import ui.UiControl.Companion.hideElements
import kotlin.browser.document
import kotlin.dom.addClass
import kotlin.dom.removeClass

class ConfirmationDialog private constructor(
    val title: String,
    val message: String,
    val hasYesNoOption: Boolean,
    val runOnConfirm: (() -> Unit)? = null
) {

    companion object {
        fun showMessageDialog(title: String, message: String) {
            ConfirmationDialog(title, message, false).show()
        }

        fun showConfirmationDialog(title: String, message: String, runOnConfirm: (() -> Unit)) {
            ConfirmationDialog(title, message, true, runOnConfirm).show()
        }
    }

    private fun show() {
        val titleElement = document.getElementById("confirmationDialogTitle")
        val messageElement = document.getElementById("confirmationDialogMessage")

        titleElement?.innerHTML = title
        messageElement?.innerHTML = message

        if (hasYesNoOption) {
            doWhenButtonClicked("confirmationDialogYesButton") {
                runOnConfirm?.let { it() }
                hide()
            }
            hideElements("confirmationDialogOkButton")
        } else {
            hideElements( "confirmationDialogYesButton", "confirmationDialogNoButton")
        }

        doWhenButtonClicked("confirmationDialogOkButton") {
            hide()
        }
        doWhenButtonClicked("confirmationDialogNoButton") {
            hide()
        }

        document.getElementById("confirmationDialog")?.addClass("is-active")
    }

    private fun hide() {
        document.getElementById("confirmationDialog")?.removeClass("is-active")
    }
}