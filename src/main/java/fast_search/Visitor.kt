package fast_search

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import java.util.*


class Visitor : VoidVisitorAdapter<Any?>() {

    val methods = HashSet<Method>()

    override fun visit(declaration: ConstructorDeclaration, arg: Any?) {
        val content = declaration.javaDoc?.content
        val body = declaration.block?.toString() ?: ""
        val modifiers = Modifiers(declaration.modifiers)
        val flat = declaration.declarationAsString
        val name = declaration.name
        val type = "void*"
        val owner = owner(declaration)
        val parameters = declaration.parameters.map { Parameter(it.name, it.type.toString()) }.toList()
        val description = Description(modifiers, flat, name, type, owner, parameters)
        val method = Method(description, content, body)
        methods.add(method)
        return super.visit(declaration, arg)
    }

    override fun visit(declaration: MethodDeclaration, arg: Any?) {
        val content = declaration.javaDoc?.content
        val body = declaration.body?.toString() ?: ""
        val modifiers = Modifiers(declaration.modifiers)
        val flat = declaration.declarationAsString
        val name = declaration.name
        val type = declaration.type.toString()
        val owner = owner(declaration)
        val parameters = declaration.parameters.map { Parameter(it.name, it.type.toString()) }.toList()
        val description = Description(modifiers, flat, name, type, owner, parameters)
        val method = Method(description, content, body)
        methods.add(method)
        return super.visit(declaration, arg)
    }

    private fun owner(node: Node): String {
        val parent = node.parentNode
        return if (parent is TypeDeclaration) parent.name else owner(parent)
    }
}