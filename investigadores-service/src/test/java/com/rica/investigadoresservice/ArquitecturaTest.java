package com.rica.investigadoresservice;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.rica.investigadoresservice", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaTest {

    @ArchTest
    static final ArchRule el_dominio_no_depende_de_la_infraestructura =
            noClasses().that().resideInAPackage("..investigadores.dominio..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..investigadores.infraestructura..");

    @ArchTest
    static final ArchRule la_aplicacion_no_depende_de_la_infraestructura =
            noClasses().that().resideInAPackage("..investigadores.aplicacion..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..investigadores.infraestructura..");

    @ArchTest
    static final ArchRule los_adaptadores_de_entrada_solo_hablan_con_aplicacion_o_dominio =
            classes().that().resideInAPackage("..infraestructura.entrada..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "..investigadores.aplicacion..",
                            "..investigadores.dominio..",
                            "..investigadores.infraestructura.entrada..",
                            "..compartido..",
                            "java..",
                            "jakarta..",
                            "org.slf4j..",
                            "org.springframework..",
                            "io.grpc..",
                            "tools.jackson..",
                            "com.rica.investigadoresservice.grpc..");
}
