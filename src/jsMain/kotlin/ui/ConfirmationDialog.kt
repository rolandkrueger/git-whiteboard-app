package ui

import org.w3c.dom.HTMLButtonElement
import kotlinx.browser.document
import ui.UiControl.Companion.doWhenButtonClicked
import ui.UiControl.Companion.hideElements
import ui.UiControl.Companion.showElements

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
            showElements( "confirmationDialogYesButton", "confirmationDialogNoButton")
        } else {
            hideElements( "confirmationDialogYesButton", "confirmationDialogNoButton")
            showElements("confirmationDialogOkButton")

        }

        doWhenButtonClicked("confirmationDialogOkButton") {
            hide()
        }
        doWhenButtonClicked("confirmationDialogNoButton") {
            hide()
        }

        document.getElementById("confirmationDialog")?.classList?.add("is-active")
    }

    private fun hide() {
        document.getElementById("confirmationDialog")?.classList?.remove("is-active")
    }
}
