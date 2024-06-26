package com.github.unldenis.javalinfly.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.unldenis.javalinfly.*;
import com.github.unldenis.javalinfly.processor.round.ControllersRound;
import com.github.unldenis.javalinfly.processor.round.GeneratorRound;
import com.github.unldenis.javalinfly.processor.round.JavalinFlyInjectorRound;
import com.github.unldenis.javalinfly.processor.round.MessagerRound;
import com.github.unldenis.javalinfly.processor.utils.EnumUtils;
import com.github.unldenis.javalinfly.processor.utils.ProcessorUtil;
import com.google.auto.service.AutoService;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;

@SupportedAnnotationTypes({"com.github.unldenis.javalinfly.JavalinFlyInjector",
    "com.github.unldenis.javalinfly.Controller"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class JavalinFlyProcessor extends AbstractProcessor {


  private MessagerRound messagerRound;
  private JavalinFlyInjectorRound javalinFlyInjectorRound;
  private ControllersRound controllersRound;
  private GeneratorRound generatorRound;

  private Types typeUtils;
  private Elements elementUtils;
  private Filer filer;


  @Override
  public synchronized void init(ProcessingEnvironment env) {
    super.init(env);
    typeUtils = env.getTypeUtils();
    elementUtils = env.getElementUtils();
    filer = env.getFiler();
    messagerRound = new MessagerRound(env.getMessager());
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//        if (roundEnv.processingOver()) {
//            return true;
//        }
    // check injector
    messagerRound.print("** Hello");

    // javalinflyinjector
    if(javalinFlyInjectorRound == null) {
      javalinFlyInjectorRound = new JavalinFlyInjectorRound(messagerRound, roundEnv, processingEnv);
    }
    javalinFlyInjectorRound.execute();

    if(javalinFlyInjectorRound.rolesTypeMirror == null) {
      return true;
    }

    if(controllersRound == null) {
      controllersRound = new ControllersRound(messagerRound, roundEnv,
          javalinFlyInjectorRound.rolesTypeMirror);
    }
    controllersRound.execute();


    if(controllersRound.endpoints.isEmpty()) {
//      messagerRound.error("Missing a class annotated with @%s", Controller.class.getSimpleName());
      return true;
    }

    if(!javalinFlyInjectorRound.executed() && controllersRound.executed()) {
      messagerRound.error("Missing a class annotated with @%s", JavalinFlyInjector.class.getSimpleName());
      return true;
    }

    if(generatorRound == null) {
      generatorRound = new GeneratorRound(filer, messagerRound, javalinFlyInjectorRound, controllersRound);
    }

    generatorRound.execute();




    return false;
  }







}
