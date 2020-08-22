package ui

import org.w3c.dom.*
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

        fun doWhenCheckboxClicked(checkboxId: String, clickHandler: (Boolean) -> Unit) {
            val checkboxElement = document.getElementById(checkboxId) as HTMLInputElement
            checkboxElement.onclick = { clickHandler(checkboxElement.checked)}
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
            val htmlInputElement = document.getElementById(inputFieldId) as HTMLInputElement
            return htmlInputElement.value
        }

        fun getSelectedOption(selectId: String): String {
            val htmlSelectElement = document.getElementById(selectId) as HTMLSelectElement
            return htmlSelectElement[htmlSelectElement.selectedIndex]?.innerHTML ?: ""
        }

        fun setSelectOptions(selectId: String, options: List<String>) {
            val htmlSelectElement = document.getElementById(selectId) as HTMLSelectElement
            htmlSelectElement.disabled = options.isEmpty()

            for (i in 0..htmlSelectElement.childElementCount) {
                htmlSelectElement.remove(0)
            }

            options.forEach {
                val element = document.createElement("option") as HTMLOptionElement
                element.innerHTML = it
                htmlSelectElement.add(element)
            }
        }

        fun isCheckboxChecked(checkboxId: String): Boolean {
            val checkbox = document.getElementById(checkboxId) as HTMLInputElement
            return checkbox.checked
        }
    }
}