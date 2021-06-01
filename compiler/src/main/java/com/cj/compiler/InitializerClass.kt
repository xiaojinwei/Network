package com.cj.compiler

import com.squareup.javapoet.*
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier

class InitializerClass {

    fun build(packageName:String,filer: Filer,vararg netConfigNames:String){
        val typeBuilder = TypeSpec.classBuilder("NetConfigInitializer")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(INITIALIZER)

        val methodCreate = MethodSpec.methodBuilder("create")
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.OBJECT)
            .addParameter(CONTEXT,"context")
            .addStatement("\$T list = new \$T()", LIST, ARRAYLIST)
            netConfigNames.forEach {
                methodCreate.addStatement("list.add(new \$L())", it)
            }
            methodCreate.addStatement("com.cj.runtime.Network.INSTANCE.init(context, list)")
            .addStatement("return null")


        val methodDependencies = MethodSpec.methodBuilder("dependencies")
            .addModifiers(Modifier.PUBLIC)
            .returns(LIST)
            .addStatement("return new \$L()","java.util.ArrayList")


        typeBuilder.addMethod(methodCreate.build())
            .addMethod(methodDependencies.build())

        writeJavaToFile(packageName,filer,typeBuilder.build())
    }

    private fun writeJavaToFile(packageName:String,filer: Filer, typeSpec: TypeSpec){
        try {
            JavaFile.builder(packageName, typeSpec).build().writeTo(filer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}