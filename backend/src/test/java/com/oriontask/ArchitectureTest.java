package com.oriontask;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.oriontask", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  @ArchTest
  static final ArchRule domainDoesNotDependOnFrameworks =
      noClasses()
          .that()
          .resideInAnyPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.springframework..", "jakarta.persistence..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule applicationDoesNotDependOnAdapters =
      noClasses()
          .that()
          .resideInAnyPackage("..application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..adapter..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule inputAdaptersDoNotAccessPersistence =
      noClasses()
          .that()
          .resideInAnyPackage("..adapter.in..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..adapter.out.persistence..", "..repository..", "..entity..")
          .allowEmptyShould(true);
}
