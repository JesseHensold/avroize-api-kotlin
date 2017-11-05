package avroize.api

import com.beust.klaxon.JsonObject
import com.beust.klaxon.array
import com.beust.klaxon.string
import com.squareup.kotlinpoet.*
import java.nio.file.Paths

class SchemaGenerator(val json: JsonObject) {
    fun generate() {
        val type = json.string("type")
        if (type == "record") {
            generateRecordRoot()
        }
    }

    fun capitalizeFirstLetter(word: String): String {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    fun generateRecordRoot() {
        val name = capitalizeFirstLetter(json.string("name")!!)
        val namespace = json.string("namespace")!!
        var fileBuilder = FileSpec.builder(namespace, name)

        val fields = json.array<JsonObject>("fields")

        if (fields != null) {
            val classBuilder = TypeSpec.classBuilder(name)
            val constructorBuilder = FunSpec.constructorBuilder()
            fields.forEach {
                val fieldName = it.string("name")!!
                val type = it.get("type")
                if (type is JsonObject) {
                    generateRootNode(fileBuilder, fieldName, type)
                    addConstructorParameterForClass(classBuilder, constructorBuilder, namespace, fieldName)
                } else if (type is String) {
                    addConstructorParameterForPrimitive(classBuilder, constructorBuilder, fieldName, type)
                }
            }
            classBuilder.primaryConstructor(constructorBuilder.build())
            fileBuilder = fileBuilder
                    .addType(classBuilder.build())
        }

        val record = fileBuilder.build()

        val currentPath = System.getProperty("user.dir")
        record.writeTo(Paths.get(currentPath + "/generated/main/kotlin"))
    }

    fun generateRootNode(fileBuilder: FileSpec.Builder, name: String, json: JsonObject) {
        val fields = json.array<JsonObject>("fields")

        if (fields != null) {
            val classBuilder = TypeSpec.classBuilder(capitalizeFirstLetter(name))
            val constructorBuilder = FunSpec.constructorBuilder()
            fields.forEach {
                val fieldName = it.string("name")!!
                val type = it.get("type")
                if (type is String) {
                    addConstructorParameterForPrimitive(classBuilder, constructorBuilder, fieldName, type)
                }
            }
            classBuilder.addModifiers(KModifier.DATA).primaryConstructor(constructorBuilder.build())
            fileBuilder.addType(classBuilder.build())
        }
    }

    fun addConstructorParameterForClass(classBuilder: TypeSpec.Builder, constructorBuilder: FunSpec.Builder,
                                        namespace: String, name: String) {
        val fullyQualifiedClassName = namespace + "." + capitalizeFirstLetter(name)
        val className = ClassName.bestGuess(fullyQualifiedClassName)
        constructorBuilder.addParameter(name, className)
        classBuilder.addProperty(PropertySpec.builder(name, className)
                .initializer(name)
                .build())
    }

    fun addConstructorParameterForPrimitive(classBuilder: TypeSpec.Builder, constructorBuilder: FunSpec.Builder,
                                            name: String, type: String) {
        when (type) {
            "boolean" -> {
                val parameter = ParameterSpec.builder(name, Boolean::class).defaultValue("false").build()
                constructorBuilder.addParameter(parameter)
                classBuilder.addProperty(PropertySpec.builder(name, Boolean::class)
                        .initializer(name)
                        .build())
            }
            "int" -> {
                val parameter = ParameterSpec.builder(name, Int::class).defaultValue("0").build()
                constructorBuilder.addParameter(parameter)
                classBuilder.addProperty(PropertySpec.builder(name, Int::class)
                        .initializer(name)
                        .build())
            }
            "string" -> {
                val parameter = ParameterSpec.builder(name, String::class).defaultValue("\"\"").build()
                constructorBuilder.addParameter(parameter)
                classBuilder.addProperty(PropertySpec.builder(name, String::class)
                        .initializer(name)
                        .build())
            }
        }
    }
}