package avroize.api

import com.beust.klaxon.JsonObject
import com.beust.klaxon.array
import com.beust.klaxon.string
import com.google.gson.Gson
import com.squareup.kotlinpoet.*
import java.nio.file.Paths

class SchemaGenerator(val json: JsonObject) {
    fun generate() {
        val type = json.string("type")
        if (type == "record") {
            generateRecordRoot()
        } else {
            generateNonRecordRoot()
        }
    }

    fun generateRecordRoot() {
        val name = json.string("name")!!
        val namespace = json.string("namespace")!!
        var classBuilder = FileSpec.builder(namespace, name)

        val fields = json.array<JsonObject>("fields")

        if (fields != null) {
            val constructorBuilder = FunSpec.constructorBuilder()
            fields.forEach {
                val fieldName = it.string("name")!!
                val fieldType = it.string("type")!!
                addConstructorParameter(constructorBuilder, fieldName, fieldType)
            }

            classBuilder = classBuilder
                    .addType(TypeSpec.classBuilder(name)
                            .primaryConstructor(constructorBuilder.build())
                            .addFunction(FunSpec.builder("toJson")
                                    .addStatement("val gson = %T()", Gson::class)
                                    .addStatement("return gson.toJson(this)")
                                    .returns(String::class)
                                    .build())
                            .build())
        }

        val record = classBuilder.build()
        record.writeTo(Paths.get("/home/ed/dev/avroize-api-kotlin/generated/main/kotlin"))
    }

    fun generateNonRecordRoot() {

    }

    fun addConstructorParameter(constructorBuilder: FunSpec.Builder, name: String, type: String) {
        when (type) {
            "string" -> constructorBuilder.addParameter(name, String::class)
            "int" -> constructorBuilder.addParameter(name, Int::class)
        }
    }
}