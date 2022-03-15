package com.voyah.compiler;

import com.google.auto.service.AutoService;
import com.google.common.io.MoreFiles;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Copyright (c) 2021-.
 * All Rights Reserved by Software.
 * --
 * You may not use, copy, distribute, modify, transmit in any form this file.
 * except in compliance with szLanyou in writing by applicable law.
 * --
 * brief   brief function description.
 * 主要功能.
 * --
 * date last_modified_date.
 * 时间.
 * --
 * version 1.0.
 * 版本信息。
 * --
 * details detailed function description
 * 功能描述。
 * --
 * DESCRIPTION.
 * Create it.
 * --
 * Edit History.
 * DATE.
 * 2022/3/11.
 * --
 * NAME.
 * anyq.
 * --
 */

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(EventConstance.annotationPackage)
@SupportedOptions(EventConstance.optionName)
public class SubscribeProcess extends AbstractProcessor {
    private Messager messager;
    private Types typeUtils;
    private Filer filer;
    private Elements elementUtils;
    private Map<String, String> options;
    private String moduleName;
    private HashMap<String, List<ExecutableElement>> methodMap = new HashMap<>();

    @Override

    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        messager = processingEnvironment.getMessager();
        typeUtils = processingEnvironment.getTypeUtils();
        filer = processingEnvironment.getFiler();
        elementUtils = processingEnvironment.getElementUtils();
        options = processingEnvironment.getOptions();

        moduleName = options.get(EventConstance.optionName);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set != null) {
            Set<? extends Element> elementsAnnotatedWith =
                    roundEnvironment.getElementsAnnotatedWith(Subscribe.class);
            if (elementsAnnotatedWith != null && elementsAnnotatedWith.size() > 0) {

                parserElements(elementsAnnotatedWith);

                return true;
            }
        }

        try {
            makeFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }


    private void parserElements(Set<? extends Element> elementsAnnotatedWith) {
        for (Element element : elementsAnnotatedWith) {
            if (element.getKind() != ElementKind.METHOD) {
                messager.printMessage(Diagnostic.Kind.NOTE, "@subscibe注解只能注释在方法上");
                return;
            }
            // 强转方法元素
            ExecutableElement method = (ExecutableElement) element;
            //检测方法合法
            if (checkMethod(method)) {
                Element enclosingElement = method.getEnclosingElement();
                String className = enclosingElement.getSimpleName().toString();
                List<ExecutableElement> elements = null;
                if (methodMap.containsKey(className)) {
                    elements = methodMap.get(className);
                } else {
                    elements = new ArrayList<>();
                }
                elements.add(method);
                methodMap.put(className, elements);

            }
        }
    }

    private boolean checkMethod(ExecutableElement method) {
        if (method.getModifiers().contains(Modifier.STATIC)) {
            messager.printMessage(Diagnostic.Kind.NOTE, "方法不能是静态的");
            return false;
        }
        if (!method.getModifiers().contains(Modifier.PUBLIC)) {
            messager.printMessage(Diagnostic.Kind.NOTE, "方法必须是public的");
            return false;
        }
        if (method.getParameters().size() != 1) {
            messager.printMessage(Diagnostic.Kind.NOTE, "方法参数必须有且只有一个");
            return false;
        }

        return true;
    }


    private void makeFile() throws IOException {
        if (methodMap.size() == 0) {
            return;
        }
        String packageName = null;

        MethodSpec.Builder loadSubscriptionMethod = MethodSpec.methodBuilder(EventConstance.loadSubscriptionMethod)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(CopyOnWriteArrayList.class);
        for (String className : methodMap.keySet()) {
            List<ExecutableElement> executableElements = methodMap.get(className);
            for (ExecutableElement executableElement : executableElements) {
                packageName = elementUtils.getPackageOf(executableElement).getQualifiedName().toString();
                Subscribe subscribe = executableElement.getAnnotation(Subscribe.class);
                int priority = subscribe.priority();
                ThreadMode threadMode = subscribe.threadMode();
                boolean sticky = subscribe.isSticky();
                String methodName = executableElement.getSimpleName().toString();
                Element classElement = executableElement.getEnclosingElement();
                List<? extends VariableElement> parameters = executableElement.getParameters();
                //因为只有一个参数
                VariableElement variableElement = parameters.get(0);
                TypeName typeName = ClassName.get(variableElement.asType());
                // loadClassMethod(clazz, new SubscribeMethod()
                //                .setPriority(1)
                //                .setSticky(true)
                //                .setThreadMode(ThreadMode.MAINTHREAD)
                //                .setEventType()
                //                .setMethod(Mainactivity.class.getDeclaredMethod()))
                loadSubscriptionMethod
                        .addCode("loadClassMethod($T.class,new $T() \n",
                                ClassName.get(classElement.asType()), SubscribeMethod.class)
                        .addCode(CodeBlock.of(".setPriority(" + priority + ")\n"))
                        .addCode(CodeBlock.of(".setThreadMode($T.$L)\n", ThreadMode.class, threadMode))
                        .addCode(CodeBlock.of(" .setSticky($L)\n", sticky))
                        .addCode(CodeBlock.of(" .setEventType($T.class)\n", typeName))
                        .addStatement(".setMethod($T.class,\"$L\",$T.class))",
                                ClassName.get(classElement.asType()), methodName, typeName);

            }
        }
        loadSubscriptionMethod.addException(ClassName.get(Exception.class))
                .addStatement("return $N", EventConstance.fieldName);

        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(CopyOnWriteArrayList.class),
                ClassName.get(Scription.class));

        FieldSpec.Builder fieldSpec = FieldSpec.builder(parameterizedTypeName, EventConstance.fieldName,
                Modifier.PRIVATE)
                .initializer("new $T()", CopyOnWriteArrayList.class);


        MethodSpec.Builder method = MethodSpec.methodBuilder(EventConstance.methodName)
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ClassName.get(Class.class), EventConstance.parameterName)
                .addParameter(ClassName.get(SubscribeMethod.class), EventConstance.parameterName1)
                //  Scription scription = new Scription()
                .addCode(" $T scription = new $T()\n", Scription.class, Scription.class)
                //   .setClazz(clazz)
                .addCode("   .setClazz(clazz)")
                //.setSubscribeMethod(method);
                .addStatement("        .setSubscribeMethod(method)")
                //scriptionHashMap.put(clazz, scription);
                .addStatement("$N.add(scription)", EventConstance.fieldName);

        TypeSpec.Builder typeSpec = TypeSpec.classBuilder(EventConstance.className)
                .addField(fieldSpec.build())
                .addSuperinterface(SubscriptionIndexesInterfaces.class)
                .addMethod(loadSubscriptionMethod.build())
                .addMethod(method.build())
                .addModifiers(Modifier.PUBLIC);

        JavaFile.builder(packageName, typeSpec.build())
                .build()
                .writeTo(filer);
    }
}
