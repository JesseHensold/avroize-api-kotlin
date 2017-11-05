package avroize.api

import avroize.util.Utility

class app {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val json = Utility.getJSONFromFile(args[0]);

            val generator = SchemaGenerator(json)
            generator.generate()
        }
    }
}