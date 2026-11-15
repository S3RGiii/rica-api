package com.rica.publicacionesservice;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.rica.publicacionesservice", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaTest {

    @ArchTest
    static final ArchRule el_caso_de_uso_no_conoce_al_adaptador =
            noClasses().that().haveSimpleName("PublicacionService")
                    .should().dependOnClassesThat()
                    .haveSimpleName("VerificadorInvestigadorGrpc");

    @ArchTest
    static final ArchRule solo_el_adaptador_habla_grpc =
            noClasses().that().resideOutsideOfPackages(
                            "..publicacionesservice.publicaciones..",
                            "com.rica.publicacionesservice.grpc..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("io.grpc..");

    @ArchTest
    static final ArchRule solo_el_adaptador_usa_resilience4j =
            noClasses().that().haveSimpleNameNotEndingWith("VerificadorInvestigadorGrpc")
                    .should().dependOnClassesThat()
                    .resideInAPackage("io.github.resilience4j..");
}
