package fast_search

import com.github.javaparser.ast.body.ModifierSet

data class Parameter(val name: String, val type: String)


data class Modifiers(val modifiers: Int) {
    val static
        get() = ModifierSet.isStatic(modifiers)
}

data class Description(val modifiers: Modifiers, val flat: String, val name: String, val type: String, val owner: String, val parameters: List<Parameter>)


data class Method(val description: Description, val comment: String?, val body: String)
