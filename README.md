[![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-Ready--to--Code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/herve-brun/swagger-diff-maven-plugin) 

![Java CI](https://github.com/herve-brun/swagger-diff-maven-plugin/workflows/Java%20CI/badge.svg)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=herve-brun_swagger-diff-maven-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=herve-brun_swagger-diff-maven-plugin) [![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=herve-brun_swagger-diff-maven-plugin&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=herve-brun_swagger-diff-maven-plugin) [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=herve-brun_swagger-diff-maven-plugin&metric=security_rating)](https://sonarcloud.io/dashboard?id=herve-brun_swagger-diff-maven-plugin) [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=herve-brun_swagger-diff-maven-plugin&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=herve-brun_swagger-diff-maven-plugin)

# swagger-diff-maven-plugin

Maven plugin which generates a report of the differences between two APIs written in OpenAPI (versions 1.0 and 2.0).

Very handy when you need to generate a changelog readable by humans and deploy it along your maven artifact :)

# Usage

mvn fr.laposte.dsibr.maven.plugins:swagger-diff:help -Ddetail -goal=diff

# Kudos

This plugin uses the [swagger-diff](https://github.com/Sayi/swagger-diff) library by @Sayi.