package com.cj.compiler

import com.cj.annotation.NetConfig
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

class NetConfigClass(val typeElement: TypeElement) {
    val simpleName: String = typeElement.simpleName()
    val packageName: String = typeElement.packageName()
    val qualifiedName: String = typeElement.qualifiedName.toString()

    val isAbstract = typeElement.modifiers.contains(Modifier.ABSTRACT)
    val isPublic = typeElement.modifiers.contains(Modifier.PUBLIC)

    val isKotlin = typeElement.getAnnotation(META_DATA) != null

    val isEnabled : Boolean = typeElement.getAnnotation(NetConfig::class.java).enabled

    fun isArgumentsConstructor(argumentsSize:Int,modifier:Modifier):Boolean{
        typeElement.enclosedElements.forEach {
            if(it.kind == ElementKind.CONSTRUCTOR && it is ExecutableElement){
                if(it.parameters.size == argumentsSize && it.modifiers.contains(modifier)){
                    return true
                }
            }
        }
        return false
    }

    fun checkValidClass():Boolean{
        if(!isEnabled){
            return false
        }
        if(!typeElement.asType().isSubTypeOf(ConfigModuleInterface)){
            val err = "The class ${typeElement.qualifiedName} annotated with ${NetConfig::class.java.name} must implement the interface ${ConfigModuleInterface}"
            Logger.error(typeElement,err)
            //throw ProcessingException(err)
            return false
        }
        if(isAbstract){
            val err = "The class ${typeElement.qualifiedName} is abstract. You can't annotate abstract classes with ${NetConfig::class.java.name}"
            Logger.error(typeElement,err)
            //throw ProcessingException(err)
            return false
        }
        if(!isPublic){
            val err = "The class ${typeElement.qualifiedName} is not public."
            Logger.error(typeElement,err)
            //throw ProcessingException(err)
            return false
        }
        if(!isArgumentsConstructor(0,Modifier.PUBLIC)){
            val err = "The class ${typeElement.qualifiedName} must provide an public empty default constructor"
            Logger.error(typeElement,err)
            //throw ProcessingException(err)
            return false
        }
        return true
    }

    companion object {
        val ConfigModuleInterface = "com.cj.runtime.integration.ConfigModule"
        val META_DATA = Class.forName("kotlin.Metadata") as Class<Annotation>
    }
}