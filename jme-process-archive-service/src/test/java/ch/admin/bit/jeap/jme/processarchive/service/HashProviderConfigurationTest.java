package ch.admin.bit.jeap.jme.processarchive.service;

import ch.admin.bit.jeap.processarchive.plugin.api.storage.HashProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The PAS object storage wiring ({@code ObjectStorageConfiguration}) requires a {@link HashProvider} bean, and jEAP
 * ships no default implementation — without one, the application fails on startup. The application's component scan
 * (rooted at {@code ch.admin.bit.jeap.processarchive.service}) does not cover this module's packages, so the bean
 * must be contributed by an auto-configuration, either declared in this module's
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports} or brought in by a
 * library dependency.
 */
class HashProviderConfigurationTest {

    private static final String AUTO_CONFIGURATION_IMPORTS =
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";

    @Test
    void hashProviderBeanIsContributedByAnAutoConfiguration() {
        Class<?>[] contributors = autoConfigurationCandidates().stream()
                .filter(HashProviderConfigurationTest::contributesHashProvider)
                .toArray(Class[]::new);

        assertThat(contributors)
                .as("No auto-configuration on the classpath contributes a HashProvider bean. The PAS requires one " +
                        "(ObjectStorageConfiguration) and jEAP has no default — the application fails on startup " +
                        "without it. Register a HashProvider implementation via " + AUTO_CONFIGURATION_IMPORTS + ".")
                .isNotEmpty();

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(contributors))
                .run(context -> assertThat(context).hasSingleBean(HashProvider.class));
    }

    private static boolean contributesHashProvider(Class<?> candidate) {
        try {
            if (HashProvider.class.isAssignableFrom(candidate)) {
                return true;
            }
            return Stream.of(candidate.getDeclaredMethods())
                    .map(Method::getReturnType)
                    .anyMatch(HashProvider.class::isAssignableFrom);
        } catch (Throwable optionalDependencyMissing) {
            return false;
        }
    }

    private static List<Class<?>> autoConfigurationCandidates() {
        ClassLoader classLoader = HashProviderConfigurationTest.class.getClassLoader();
        List<Class<?>> candidates = new ArrayList<>();
        try {
            for (URL resource : Collections.list(classLoader.getResources(AUTO_CONFIGURATION_IMPORTS))) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))) {
                    reader.lines()
                            .map(String::trim)
                            .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                            .forEach(className -> loadClass(classLoader, className).ifPresent(candidates::add));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return candidates.stream().distinct().toList();
    }

    private static java.util.Optional<Class<?>> loadClass(ClassLoader classLoader, String className) {
        try {
            return java.util.Optional.of(Class.forName(className, false, classLoader));
        } catch (Throwable optionalDependencyMissing) {
            return java.util.Optional.empty();
        }
    }
}
