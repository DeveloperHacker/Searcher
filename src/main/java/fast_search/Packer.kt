package fast_search

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.io.FileWriter
import java.io.IOException


private val JAVA_DOC_TAG_NAME = "java-doc"
private val HEAD_TAG_NAME = "@head"
private val PARAM_TAG_NAME = "@param"
private val RETURN_TAG_NAME = "@return"
private val SEE_TAG_NAME = "@see"
private val THROWS_TAG_NAME = "@throw"

private val DESCRIPTION_TAG_NAME = "description"
private val CONTRACT_TAG_NAME = "contract"

private val STATIC = "static"
private val FLAT = "flat"
private val NAME = "name"
private val TYPE = "type"
private val OWNER = "owner"
private val PARAMETERS = "parameters"

@Throws(IOException::class)
fun pack(path: String, methods: Collection<Method>) {
    val raw_methods = methods.filter { it.comment != null }.mapTo(JSONArray(), ::method)
//    FileWriter(path).use { raw_methods.writeJSONString(it) }
    val parser = JsonParser()
    val element = parser.parse(raw_methods.toString())
    val builder = GsonBuilder().setPrettyPrinting().create()
    val string = builder.toJson(element)
    FileWriter(path).use { it.write(string) }
}

private fun method(method: Method): JSONObject {
    val raw_method = JSONObject()
    raw_method[JAVA_DOC_TAG_NAME] = java_doc(method.comment!!)
    raw_method[DESCRIPTION_TAG_NAME] = description(method.description)
    raw_method[CONTRACT_TAG_NAME] = JSONArray()
    return raw_method
}

private fun java_doc(comment: String): JSONObject {
    val tags = hashSetOf(HEAD_TAG_NAME, PARAM_TAG_NAME, RETURN_TAG_NAME, SEE_TAG_NAME, THROWS_TAG_NAME)
    var tag = HEAD_TAG_NAME

    return comment.split("\n[\\s\t]*/?\\*".toRegex())
            .map(String::trim)
            .filter(String::isNotEmpty)
            .map { line ->
                tags.firstOrNull { line.startsWith(it) }?.let { tag = it }
                tag to line
            }
            .groupBy { it.first }
            .mapValuesTo(JSONObject()) { it.value.mapTo(JSONArray()) { it.second } }
}

private fun description(description: Description): JSONObject {
    val raw_description = JSONObject()
    raw_description[STATIC] = description.modifiers.static
    raw_description[FLAT] = description.flat
    raw_description[NAME] = description.name
    raw_description[TYPE] = type(description.type)
    raw_description[OWNER] = type(description.owner)
    raw_description[PARAMETERS] = description.parameters.mapTo(JSONArray(), ::parameter)
    return raw_description
}

private fun parameter(parameter: Parameter): JSONObject {
    val raw_description = JSONObject()
    raw_description[NAME] = parameter.name
    raw_description[TYPE] = type(parameter.type)
    return raw_description
}


private fun type(type: String) = type.split("$", ".").last()

