/*
 * Copyright 2021 Delft University of Technology
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.f4sten.pomanalyzer.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.jboss.shrinkwrap.resolver.api.NoResolvedResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.f4sten.pomanalyzer.data.ResolutionResult;
import eu.fasten.core.utils.TestUtils;

// The artifact source resolution breaks caching mechanisms by deleting packages from the
// local .m2 folder. This exact functionality is tested here, so the test suite will download
// dependencies over-and-over again on every build. Enable this test only for local tests.
@Disabled
public class ResolverTest {

	@Test
	public void reminderToReenableDisabledAnnotationOnClass() {
		fail("Suite is expensive and should only be run locally. Re-enable @Disabled annotation.");
	}

	private Set<String> db;
	private Resolver sut;

	@BeforeEach
	public void setup() {
		db = new HashSet<>();
		sut = new Resolver();
		sut.setExistenceCheck(dep -> db.contains(dep));
	}

	@Test
	public void resolveDirectDependencies() {
		var actual = resolveTestPom("basic.pom");
		var expected = new HashSet<ResolutionResult>();
		expected.add(JSR305);
		expected.add(COMMONS_LANG3);
		expected.add(REMLA);

		assertEquals(expected, actual);
	}

	@Test
	public void defaultConfigAddsEverything() {
		sut = new Resolver();
		resolveDirectDependencies();
	}

	@Test
	public void ignoresExistingPackages() {
		db.add(COMMONS_LANG3.coordinate);
		var actual = resolveTestPom("basic.pom");
		var expected = new HashSet<ResolutionResult>();
		expected.add(JSR305);
		expected.add(REMLA);

		assertEquals(expected, actual);
	}

	@Test
	public void resolveTransitiveDependencies() {
		var actual = resolveTestPom("transitive.pom");
		var expected = new HashSet<ResolutionResult>();
		expected.add(COMMONS_TEXT);
		expected.add(COMMONS_LANG3);

		assertEquals(expected, actual);
	}

	@Test
	public void noDependencies() {
		var actual = resolveTestPom("no-dependencies.pom");
		var expected = new HashSet<ResolutionResult>();
		assertEquals(expected, actual);
	}

	@Test
	public void unresolvableDependencies() {
		assertThrows(NoResolvedResultException.class, () -> {
			resolveTestPom("unresolvable.pom");
		});
	}

	private static final ResolutionResult JSR305 = new ResolutionResult(//
			"com.google.code.findbugs:jsr305:jar:3.0.2", //
			"https://repo.maven.apache.org/maven2", //
			new File("/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.pom"));

	private static final ResolutionResult COMMONS_LANG3 = new ResolutionResult(//
			"org.apache.commons:commons-lang3:jar:3.9", //
			"https://repo.maven.apache.org/maven2", //
			new File("/org/apache/commons/commons-lang3/3.9/commons-lang3-3.9.pom"));

	private static final ResolutionResult COMMONS_TEXT = new ResolutionResult(//
			"org.apache.commons:commons-text:jar:1.8", //
			"https://repo.maven.apache.org/maven2", //
			new File("/org/apache/commons/commons-text/1.8/commons-text-1.8.pom"));

	private static final ResolutionResult REMLA = new ResolutionResult(//
			"remla:mylib:jar:0.0.5", //
			"https://gitlab.com/api/v4/projects/26117144/packages/maven", //
			new File("/remla/mylib/0.0.5/mylib-0.0.5.pom"));

	private Set<ResolutionResult> resolveTestPom(String pathToPom) {
		var fullPath = Path.of(ResolverTest.class.getSimpleName(), pathToPom);
		File pom = TestUtils.getTestResource(fullPath.toString());
		Set<ResolutionResult> actual = sut.resolveDependenciesFromPom(pom);
		actual.forEach(rr -> {
			rr.localPomFile = stripLocalBasePath(rr.localPomFile);
		});
		return actual;
	}

	private static File stripLocalBasePath(File f) {
		assertNotNull(f);
		assertTrue(f.exists());
		assertTrue(f.isFile());

		String p = f.getAbsolutePath();
		String marker = File.separator + ".m2" + File.separator + "repository" + File.separator;
		int idx = p.indexOf(marker);
		assertTrue(idx > 0);

		return new File(p.substring(idx + marker.length() - 1));
	}
}