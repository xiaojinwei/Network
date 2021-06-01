package com.cj.compiler

import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.reflect.KClass

object TypeUtils {
    internal fun doubleErasure(elementType: TypeMirror): String {
        var name = AptContext.types.erasure(elementType).toString()
        val typeParamStart = name.indexOf('<')
        if (typeParamStart != -1) {
            name = name.substring(0, typeParamStart)
        }
        return name
    }

    internal fun getTypeFromClassName(className: String) = AptContext.elements.getTypeElement(className).asType()
}

object AptContext{
    lateinit var types: Types
    lateinit var elements: Elements
    lateinit var messager: Messager
    lateinit var filer: Filer

    fun init(env: ProcessingEnvironment){
        elements = env.elementUtils
        types = env.typeUtils
        messager = env.messager
        filer = env.filer
    }
}

fun TypeElement.packageName(): String {
    var element = this.enclosingElement
    while (element != null && element.kind != ElementKind.PACKAGE) {
        element = element.enclosingElement
    }
    return element?.asType()?.toString() ?: throw IllegalArgumentException("$this does not have an enclosing element of package.")
}

fun Element.simpleName(): String = simpleName.toString()
fun TypeElement.canonicalName(): String = qualifiedName.toString()

//region subType
fun TypeMirror.isSubTypeOf(className: String): Boolean {
    return AptContext.types.isSubtype(this, TypeUtils.getTypeFromClassName(className))
}

fun TypeMirror.isSubTypeOf(cls: Class<*>): Boolean {
    return cls.canonicalName?.let { className ->
        isSubTypeOf(className)
    } ?: false
}

fun TypeMirror.isSubTypeOf(cls: KClass<*>) = isSubTypeOf(cls.java)

fun TypeMirror.isSubTypeOf(typeMirror: TypeMirror): Boolean {
    return AptContext.types.isSubtype(this, typeMirror)
}
//endregion
