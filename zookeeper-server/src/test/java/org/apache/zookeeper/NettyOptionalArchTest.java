/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zookeeper;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Architectural test to enforce that Netty is an optional dependency.
 *
 * <p>Only classes that provide Netty-based transport or SSL/TLS functionality
 * are allowed to import {@code io.netty} packages. All other ZooKeeper classes
 * must remain Netty-free so that Netty can be an optional dependency for users
 * who do not need SSL/TLS.
 */
public class NettyOptionalArchTest {

    private static final String NETTY_PACKAGE = "io.netty..";

    /**
     * Regex matching classes that are legitimately Netty-dependent:
     * Netty-based transport classes and SSL/TLS utility classes that build on Netty's SSL API.
     * Test classes are also excluded from this check.
     */
    private static final String NETTY_DEPENDENT_CLASS_REGEX =
        ".*\\.(NettyServerCnxnFactory|NettyServerCnxn|ClientCnxnSocketNetty|NettyUtils"
        + "|UnifiedServerSocket|ClientX509Util).*"
        + "|.*Test.*";  // exclude test classes which may import Netty for integration tests

    @Test
    public void nonNettyClassesShouldNotDependOnNetty() {
        JavaClasses classes = new ClassFileImporter()
            .importPackages("org.apache.zookeeper");

        ArchRule rule = noClasses()
            .that().resideInAPackage("org.apache.zookeeper..")
            .and().haveNameNotMatching(NETTY_DEPENDENT_CLASS_REGEX)
            .should().dependOnClassesThat().resideInAPackage(NETTY_PACKAGE)
            .because("Netty is an optional dependency; only Netty transport and SSL/TLS classes should import io.netty");

        rule.check(classes);
    }
}
