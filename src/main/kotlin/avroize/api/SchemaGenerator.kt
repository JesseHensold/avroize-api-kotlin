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
        var fileBuilder = FileSpec.builder(namespace, name)

        val fields = json.array<JsonObject>("fields")

        if (fields != null) {
            val classBuilder = TypeSpec.classBuilder(name)
            val constructorBuilder = FunSpec.constructorBuilder()
            fields.forEach {
                val fieldName = it.string("name")!!
                val fieldType = it.string("type")!!
                addConstructorParameter(classBuilder, constructorBuilder, fieldName, fieldType)
            }
            classBuilder.addModifiers(KModifier.DATA)
                    .primaryConstructor(constructorBuilder.build())
                    .addFunction(FunSpec.builder("toJson")
                            .addStatement("val gson = %T()", Gson::class)
                            .addStatement("return gson.toJson(this)")
                            .returns(String::class)
                            .build())
            fileBuilder = fileBuilder
                    .addType(classBuilder.build())
        }

        val record = fileBuilder.build()
        record.writeTo(Paths.get("/home/ed/dev/avroize-api-kotlin/generated/main/kotlin"))
    }

    fun generateNonRecordRoot() {

    }

    fun addConstructorParameter(classBuilder: TypeSpec.Builder, constructorBuilder: FunSpec.Builder,
                                name: String, type: String) {
        when (type) {
            "string" -> {
                constructorBuilder.addParameter(name, String::class)
                classBuilder.addProperty(PropertySpec.builder(name, String::class)
                        .initializer(name)
                        .build())
            }
            "int" -> {
                constructorBuilder.addParameter(name, Int::class)
                classBuilder.addProperty(PropertySpec.builder(name, Int::class)
                        .initializer(name)
                        .build())
            }
        }
    }
}