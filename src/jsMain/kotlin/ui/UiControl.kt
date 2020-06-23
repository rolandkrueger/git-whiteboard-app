package ui

import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import kotlin.browser.document
import kotlin.dom.addClass
import kotlin.dom.removeClass

class UiControl {

    companion object {
        fun hideElements(vararg elementIds: String) {
            elementIds.forEach {
                document.getElementById(it)?.addClass("hidden")
            }
        }

        fun showElements(vararg elementIds: String) {
            elementIds.forEach {
                document.getElementById(it)?.removeClass("hidden")
            }
        }

        fun doWhenButtonClicked(buttonId: String, clickHandler: () -> Unit) {
            val buttonElement = document.getElementById(buttonId) as HTMLButtonElement
            buttonElement.onclick = { clickHandler() }
        }

        fun doWhenLinkClicked(linkId: String, clickHandler: () -> Unit) {
            val buttonElement = document.getElementById(linkId) as HTMLAnchorElement
            buttonElement.onclick = { clickHandler() }
        }

        fun activateTab(activeTabId: String, vararg otherTabIds: String) {
            showElements(activeTabId)
            hideElements(*otherTabIds)
            document.getElementById("${activeTabId}Control")?.parentElement?.addClass("is-active")
            otherTabIds.forEach {
                document.getElementById("${it}Control")?.parentElement?.removeClass("is-active")
            }
        }

        fun getUserInput(inputFieldId: String): String {
            val htmlInputElement = document.getElementById(inputFieldId) as HTMLInputElement?
            return htmlInputElement?.value ?: ""
        }
    }
}