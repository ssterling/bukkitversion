/*
 * BukkitVersion - utilities for Minecraft server version info
 *
 * Copyright 2022 Seth Price
 * All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package net.ssterling.bukkitversion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.bukkit.Bukkit;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;

/**
 * The {@code KnownValuesTest} test ensures that valid Bukkit API 
 * version strings are correctly parsed, and that invalid strings are rejected.
 *
 * @author Seth Price
 */
class KnownValuesTest
{
	private ServerMock server;

	/**
	 * The {@code VersionUnit} class contains information which will be
	 * used to determine whether the behaviour of a {@link BukkitVersion}
	 * instance is as expected.
	 *
	 * @author Seth Price
	 */
	private static class VersionUnit
	{
		/** The version string to which tests will be applied. */
		public String version_string;

		/** Whether the construction of {@link BukkitVersion} should fail. */
		public boolean should_fail;

		/** The expected value of {@link BukkitVersion#beta}. */
		public boolean beta;

		/** The expected value of {@link BukkitVersion#major}. */
		public Integer major;

		/** The expected value of {@link BukkitVersion#minor}. */
		public Integer minor;

		/** The expected value of {@link BukkitVersion#patch}. */
		public Integer patch;

		/** The expected value of {@link BukkitVersion#prerelease}. */
		public Integer prerelease;

		/** The expected value of {@link BukkitVersion#release_candidate}. */
		public Integer release_candidate;

		/** The expected value of {@link BukkitVersion#revision_major}. */
		public Integer revision_major;

		/** The expected value of {@link BukkitVersion#revision_minor}. */
		public Integer revision_minor;

		/** The expected value of {@link BukkitVersion#toVanillaString}. */
		public String vanilla_string;

		/**
		 * Creates an instance of {@code VersionUnit} whose components,
		 * if correctly parsed, will be compared against those provided.
		 *
		 * @param	version_string the version string with which to
		 *		construct a {@code BukkitVersion} instance
		 * @param	beta true if version is beta, false otherwise
		 * @param	major expected major version number
		 * @param	minor expected minor version number
		 * @param	patch expected patch version number ({@code null} if none)
		 * @param	prerelease expected prerelease number ({@code null} if none)
		 * @param	release_candidate expected release candidate number ({@code null} if none)
		 * @param	revision_major expected revision number major component ({@code null} if none)
		 * @param	revision_minor expected revision number minor component ({@code null} if none)
		 * @param	vanilla_string expected vanilla version string
		 */
		public VersionUnit(final String version_string,
				final boolean beta,
				final Integer major, final Integer minor,
				final Integer patch, final Integer prerelease,
				final Integer release_candidate,
				final Integer revision_major,
				final Integer revision_minor,
				final String vanilla_string)
		{
			this.version_string = version_string;
			this.should_fail = false;
			this.beta = beta;
			this.major = major;
			this.minor = minor;
			this.patch = patch;
			this.prerelease = prerelease;
			this.release_candidate = release_candidate;
			this.revision_major = revision_major;
			this.revision_minor = revision_minor;
			this.vanilla_string = vanilla_string;
		}

		/**
		 * Creates an instance of {@code VersionUnit} which, if correctly
		 * parsed, will throw an exception.
		 *
		 * @param	version_string the version string out of which
		 *		a {@link BukkitVersion} instance will be
		 *		constructed
		 */
		public VersionUnit(final String version_string)
		{
			this.version_string = version_string;
			this.should_fail = true;
		}
	}

	private final VersionUnit[] versions = {
		// Arbitrarily chosen
		new VersionUnit("1.8-R0.1-SNAPSHOT", false, 1, 8, null, null, null, 0, 1, "1.8"),
		new VersionUnit("1.9.4-R0.1-SNAPSHOT", false, 1, 9, 4, null, null, 0, 1, "1.9.4"),
		new VersionUnit("1.12-pre3-SNAPSHOT", false, 1, 12, null, 3, null, null, null, "1.12-pre3"),
		new VersionUnit("1.13-pre7-R0.1-SNAPSHOT", false, 1, 13, null, 7, null, 0, 1, "1.13-pre7"),
		new VersionUnit("1.14.3-SNAPSHOT", false, 1, 14, 3, null, null, null, null, "1.14.3"),
		new VersionUnit("1.18-rc3-R0.1-SNAPSHOT", false, 1, 18, null, null, 3, 0, 1, "1.18-rc3"),
	};

	/**
	 * Asserts that expected component value for a given version
	 * is correctly deduced by {@link BukkitVersion}.
	 *
	 * @param	expected expected value
	 * @param	actual actual value
	 * @param	type string representation of component type
	 * @param	version_string version string used to get results
	 */
	private static void assertComponentMatch(final Integer expected,
			final Integer actual, final String type,
			final String version_string)
	{
		final String message = type +
			" components do not match using version string "
			+ version_string;
		assertEquals(expected, actual, message);
	}

	/**
	 * Asserts that expected component value for a given version
	 * is correctly deduced by {@link BukkitVersion}.
	 *
	 * @param	expected expected value
	 * @param	actual actual value
	 * @param	type string representation of component type
	 * @param	version_string version string used to get results
	 */
	private static void assertComponentMatch(final boolean expected,
			final boolean actual, final String type,
			final String version_string)
	{
		final String message = type +
			" components do not match using version string "
			+ version_string;
		assertEquals(expected, actual, message);
	}

	@BeforeEach
	public void setUp()
	{
		server = MockBukkit.mock();
	}

	@AfterEach
	public void tearDown()
	{
		MockBukkit.unmock();
	}

	@Test
	void testKnownValues()
	{
		for (VersionUnit unit : versions) {
			BukkitVersion version = null;
			try {
				version = new BukkitVersion(unit.version_string);
			} catch (Throwable ex) {
				if (unit.should_fail) {
					continue;
				}
				fail("failed to construct object using version string "
						+ unit.version_string);
			}
			if (unit.should_fail) {
				fail("failed to fail upon constructing object using version string "
						+ unit.version_string);
			}

			// TODO: better source formatting
			assertComponentMatch(unit.beta, version.isBeta(), "beta", unit.version_string);
			assertComponentMatch(unit.major, version.getMajor(), "major", unit.version_string);
			assertComponentMatch(unit.minor, version.getMinor(), "minor", unit.version_string);
			assertComponentMatch(unit.patch, version.getPatch(), "patch", unit.version_string);
			assertComponentMatch(unit.prerelease, version.getPrerelease(), "pre-release", unit.version_string);
			assertComponentMatch(unit.release_candidate, version.getReleaseCandidate(), "release candidate", unit.version_string);
			assertComponentMatch(unit.revision_major, version.getRevisionMajor(), "revision major", unit.version_string);
			assertComponentMatch(unit.revision_minor, version.getRevisionMinor(), "revision minor", unit.version_string);

			assertEquals(unit.version_string, version.toString(),
					"generated version string does not match actual");
		}
	}
}
