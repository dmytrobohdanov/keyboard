

package dev.patrickgold.florisboard.ime.theme

import dev.patrickgold.florisboard.lib.ext.ExtensionComponent
import dev.patrickgold.florisboard.lib.ext.ExtensionComponentName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.florisboard.lib.snygg.SnyggStylesheetEditor

@Suppress("NOTHING_TO_INLINE")
inline fun extCoreTheme(id: String) = ExtensionComponentName(
    extensionId = "org.florisboard.themes",
    componentId = id,
)

@Suppress("NOTHING_TO_INLINE")
inline fun extPreviewTheme(id: String) = ExtensionComponentName(
    extensionId = "local.themes.preview",
    componentId = id,
)

interface ThemeExtensionComponent : ExtensionComponent {
    companion object {
        fun defaultStylesheetPath(id: String): String {
            return "stylesheets/$id.json"
        }
    }

    override val id: String
    override val label: String
    override val authors: List<String>
    val isNightTheme: Boolean
    val stylesheetPath: String?

    fun stylesheetPath(): String = stylesheetPath.takeUnless { it.isNullOrBlank() } ?: defaultStylesheetPath(id)
}

@Serializable
data class ThemeExtensionComponentImpl(
    override val id: String,
    override val label: String,
    override val authors: List<String>,
    @SerialName("isNight")
    override val isNightTheme: Boolean = true,
    @SerialName("stylesheet")
    override val stylesheetPath: String? = null,
) : ThemeExtensionComponent {

    fun edit() = ThemeExtensionComponentEditor(
        id, label, authors, isNightTheme, stylesheetPath ?: "",
    )
}

class ThemeExtensionComponentEditor(
    override var id: String = "",
    override var label: String = "",
    override var authors: List<String> = emptyList(),
    override var isNightTheme: Boolean = true,
    override var stylesheetPath: String = "",
) : ThemeExtensionComponent {

    var stylesheetPathOnLoad: String? = null
    var stylesheetEditor: SnyggStylesheetEditor? = null

    fun build(): ThemeExtensionComponentImpl {
        val component = ThemeExtensionComponentImpl(
            id = id.trim(),
            label = label.trim(),
            authors = authors.filterNot { it.isBlank() },
            isNightTheme = isNightTheme,
            stylesheetPath = stylesheetPath.takeUnless { it.isBlank() },
        )
        check(id.isNotBlank()) { "Theme component ID cannot be blank" }
        check(label.isNotBlank()) { "Theme component label cannot be blank" }
        check(authors.isNotEmpty()) { "Theme component authors must contain at least one non-blank author field" }
        return component
    }
}
