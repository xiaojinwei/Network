package com.cj.compiler

import com.cj.annotation.NetConfig
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class NetworkProcessor : AbstractProcessor() {

    private val supportedAnnotations = setOf(NetConfig::class.java)

    override fun getSupportedAnnotationTypes() = supportedAnnotations.mapTo(HashSet<String>(), Class<*>::getCanonicalName)

    override fun getSupportedSourceVersion() = SourceVersion.RELEASE_7

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        AptContext.init(processingEnv)
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        val configClasses = HashMap<Element, NetConfigClass>()
        val configClassNames = mutableListOf<String>()
        roundEnv.getElementsAnnotatedWith(NetConfig::class.java)
            .filter { it.kind.isClass }
            .forEach { element: Element ->
                configClasses[element] = NetConfigClass(element as TypeElement)
            }
        configClasses.values.forEach {
            if(it.checkValidClass()){
                configClassNames.add(it.qualifiedName)
            }
        }
        if(configClassNames.size > 0){
            InitializerClass().build("com.cj.runtime",AptContext.filer,*configClassNames.toTypedArray())
        }
        return true
    }

}