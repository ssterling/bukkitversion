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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;

/**
 * The {@code BukkitVersion} class derives and contains the values
 * extrapolated from a Bukkit server string.
 *
 * <p>The simplest way to use this from within a Bukkit plugin is
 * by using the value of
 * {@link org.bukkit.Bukkit#getBukkitVersion() getBukkitVersion()}
 * to construct a {@code BukkitVersion} object, like such:
 * <code class="java">new BukkitVersion(Bukkit.getBukkitVersion());</code>.
 *
 * <p>For those who learn better by example, refer to the following
 * example usage from within a Bukkit plugin:
 * <pre><code class="java">package net.ssterling.exampleplugin;
 *
 *import org.bukkit.Bukkit;
 *import org.bukkit.JavaPlugin;
 *import net.ssterling.bukkitversion.BukkitVersion;
 *
 *public class ExamplePlugin extends JavaPlugin
 *{
 *	&#64;Override
 *	public void onEnable()
 *	{
 *		// Grabs the server version automatically
 *		BukkitVersion version = new BukkitVersion();
 *		
 *		if (version.compareTo(new BukkitVersion("1.12.2-R0.1-SNAPSHOT")) == 0) {
 *			getLogger().info("Running on Minecraft 1.12.2-R0.1-SNAPSHOT");
 *		}
 *
 *		// More typical format: specify just major.minor.patch
 *		if (version.compareTo(new BukkitVersion("1.2.5", false)) &gt;= 0) {
 *			getLogger().info("Running on 1.2.5 or above"); // (not a typo)
 *		}
 *
 *		// Comparison will stop at a certain point, in this case: minor version
 *		BukkitVersion ver = new BukkitVersion("1.8.8R1.0-SNAPSHOT", false);
 *		if (version.compareTo(ver, BukkitVersion.Component.MINOR) &lt; 0) {
 *			getLogger().info("Running below Minecraft 1.8");
 *		}
 *	}
 *}</code></pre>
 *
 * @author	Seth Price
 * @since	0.1.0
 */
public final class BukkitVersion implements Comparable<BukkitVersion>
{
	/**
	 * Whether the version is a beta release (e.g. {@code b1.7.3}).
	 */
	protected boolean beta;

	/**
	 * The major version number.
	 * Assumed to always be present, and of value {@code 1}
	 * for the forseeable future.
	 */
	protected Integer major;

	/**
	 * The minor version number.
	 * Assumed to always be present.
	 */
	protected Integer minor;

	/**
	 * The patch version number.
	 * Null if not present in version string.
	 */
	protected Integer patch;

	/**
	 * The prerelease number of the version.
	 * Null if not present in version string.
	 */
	protected Integer prerelease;

	/**
	 * The release candidate number of the version.
	 * Null if not present in version string.
	 */
	protected Integer release_candidate;

	/**
	 * The major revision number of the version.
	 * Assumed to always be present.
	 */
	protected Integer revision_major;

	/**
	 * The minor revision number of the version.
	 */
	protected Integer revision_minor;

	/**
	 * The number to add to {@link release_candidate} when returning its value
	 * from {@link getPrereleaseOrReleaseCandidate}.
	 */
	private static final int RC_OFFSET = 10000; // FIXME: arbitrary magic number

	/**
	 * The number to return from {@link getPrereleaseOrReleaseCandidate}
	 * when both {@link prerelease} and {@link release_candidate}
	 * are null.
	 */
	private static final int PRERC_NULL_VALUE = RC_OFFSET * 2;

	/**
	 * The regex pattern used to dissect a version string.
	 *
	 * <p>Both vanilla Minecraft versions (such as {@code 1.6.4},
	 * {@code 1.12.2-pre2} or {@code 1.18-rc4}) and Bukkit API versions
	 * (vanilla versions suffixed usually with a revision number and always
	 * with a snapshot qualifier, such as {@code 1.2.5-R5.2-SNAPSHOT} or
	 * {@code 1.14-pre5-SNAPSHOT}) will match this pattern.
	 *
	 * <p>For those unfamiliar with
	 * <a href="https://en.wikipedia.org/wiki/Regex">regular expressions</a>
	 * but still curious what constitutes a valid version string per this
	 * pattern, the author recommends
	 * <a href="https://regex101.com/r/0SppWa">experimenting with this pattern</a>
	 * on Regex101.
	 *
	 * @author Seth Price
	 */
	private static final Pattern PATTERN = Pattern.compile("^(?<beta>b)?(?<major>\\d+)(?:\\.(?<minor>\\d+)(?:(?:\\.(?<patch>[0-9]+))?(?:(?:-pre(?<pre>\\d))|(?:-rc(?<rc>\\d+))?)?(?:-R(?<revisionmajor>\\d)(?:.(?<revisionminor>\\d))?)?(?<snapshot>-SNAPSHOT)?)?)?$");

	/**
	 * Creates an instance of {@code BukkitVersion} by parsing the value
	 * of {@link org.bukkit.Bukkit#getBukkitVersion()}.
	 * May only be used when the Bukkit API is available, such as
	 * from within a Bukkit plugin.
	 *
	 * @throws	NoSuchMethodError if Bukkit API cannot be found
	 * @throws	IllegalArgumentException if {@link org.bukkit.Bukkit#getBukkitVersion()}
	 *		does not return a valid Bukkit API version string
	 * @since 0.1.0
	 */
	public BukkitVersion()
	{
		try {
			fromString(Bukkit.getBukkitVersion(), true);
		} catch (NoSuchMethodError ex) {
			// Not Minecraft 1.0 and above, at least
			try {
				Pattern pattern = Pattern.compile("^.*\\(MC: 1\\.(?<minor>\\d)(?:\\.(?<patch>\\d))?\\)$");
				String version = Bukkit.getVersion();
				Matcher matcher = pattern.matcher(version);

				if (!matcher.matches()) {
					throw new IllegalArgumentException("unhandled beta version");
				}

				beta = true;
				major = 1;
				minor = Integer.parseInt(matcher.group("minor"));
				if (matcher.group("patch") != null) {
					patch = Integer.parseInt(matcher.group("patch"));
				}
			} catch (NoSuchMethodError exx) {
				// This would be highly unusual
				throw new NoSuchMethodError("cannot find Bukkit API");
			}
		}
	}

	/**
	 * Creates a {@code BukkitVersion} object by parsing a Bukkit API
	 * version string.
	 *
	 * @param	version full Bukkit API version string
	 *
	 * @throws	NullPointerException if {@code version} is null
	 * @throws	IllegalArgumentException if {@code version} is not a
	 *		valid Bukkit API version string
	 * @since 0.1.0
	 */
	public BukkitVersion(final String version)
	{
		fromString(version, true);
	}

	/**
	 * Creates a {@code BukkitVersion} object by parsing a version string,
	 * such as the value of {@link org.bukkit.Bukkit#getBukkitVersion()},
	 * or something simpler, such as {@code 1.19.1-pre3}.
	 *
	 * @param	version version string
	 * @param	strict {@code true} to require the string be a valid
	 *		Bukkit API version string, {@code false} to allow vanilla
	 *		Minecraft version strings
	 *
	 * @throws	NullPointerException if {@code version} is null
	 * @throws	IllegalArgumentException if {@code strict} is {@code true}
	 *		and {@code version} is not a valid Bukkit API version string
	 * @since 0.1.0
	 */
	public BukkitVersion(final String version, final boolean strict)
	{
		fromString(version, strict);
	}

	/**
	 * Creates a {@code BukkitVersion} object by inputting the individual
	 * components of a Bukkit API version.
	 *
	 * @param	major major version number
	 * @param	minor minor version number
	 * @param	patch patch version number ({@code null} if none)
	 * @param	prerelease prerelease number ({@code null} if none)
	 * @param	release_candidate release candidate number ({@code null} if none)
	 * @param	revision_major revision number major component ({@code null} if none)
	 * @param	revision_minor revision number minor component ({@code null} if none)
	 * @param	beta true if version is beta, false otherwise
	 * @throws	NullPointerException if any of the following parameters
	 *		are null: {@code major}, {@code minor}
	 * @throws	IllegalArgumentException if both {@code prerelease} and
	 *		{@code release_candidate} parameters are not null
	 * @since 0.1.0
	 */
	public BukkitVersion(final Integer major, final Integer minor, final Integer patch,
			final Integer prerelease, final Integer release_candidate,
			final Integer revision_major, final Integer revision_minor,
			final boolean beta)
	{
		if (major == null || minor == null) {
			throw new IllegalArgumentException("major and minor version numbers are required parameters");
		} else if (prerelease != null && release_candidate != null) {
			throw new IllegalArgumentException("pre-releases and release candidates are mutually exclusive");
		}

		this.beta = beta;
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.prerelease = prerelease;
		this.release_candidate = release_candidate;
		this.revision_major = revision_major;
		this.revision_minor = revision_minor;
	}

	/**
	 * A list of Bukkit version components as used in the class.
	 * @since 0.1.0
	 */
	public enum Component {
		/** The major version number. */
		MAJOR,
		/** The minor version number. */
		MINOR,
		/** The patch number. */
		PATCH,
		/** Either the pre-release or release candidate numbers. */
		PRE_RC,
		/** The major revision number. */
		REVISION_MAJOR,
		/** The minor revision number. */
		REVISION_MINOR;
	}

	/**
	 * Creates a {@code BukkitVersion} object by parsing a version string,
	 * such as the value of {@link org.bukkit.Bukkit#getBukkitVersion()},
	 * or something simpler, such as {@code 1.19.1-pre3}.
	 *
	 * @param	version version string
	 * @param	strict {@code true} to require the string be a valid
	 *		Bukkit API version string, {@code false} to allow vanilla
	 *		Minecraft version strings
	 *
	 * @throws	NullPointerException if {@version} is null
	 * @throws	IllegalArgumentException if {@code strict} is {@code true}
	 *		and {@code version} is not a valid Bukkit API version string
	 * @since 0.1.0
	 */
	private void fromString(final String version, final boolean strict)
	{
		if (version == null) {
			throw new NullPointerException("null version string");
		}

		Matcher matcher = PATTERN.matcher(version);

		if (!matcher.matches()) {
			throw new IllegalArgumentException("invalid vanilla version");
		} else if (matcher.group("snapshot") == null && strict == true) {
			throw new IllegalArgumentException("invalid Bukkit API version");
		}

		if (matcher.group("beta") != null) {
			beta = true;
		}
		major = Integer.parseInt(matcher.group("major"));
		if (matcher.group("minor") != null) {
			minor = Integer.parseInt(matcher.group("minor"));
		}
		if (matcher.group("patch") != null) {
			patch = Integer.parseInt(matcher.group("patch"));
		}
		if (matcher.group("pre") != null) {
			prerelease = Integer.parseInt(matcher.group("pre"));
		}
		if (matcher.group("rc") != null) {
			release_candidate = Integer.parseInt(matcher.group("rc"));
		}
		if (matcher.group("revisionmajor") != null) {
			revision_major = Integer.parseInt(matcher.group("revisionmajor"));
		}
		if (matcher.group("revisionminor") != null) {
			revision_minor = Integer.parseInt(matcher.group("revisionminor"));
		}
	}

	/**
	 * Gets the beta status.
	 *
	 * @return	true if version is beta, false otherwise
	 * @since 0.3.0
	 */
	public boolean isBeta()
	{
		return beta;
	}

	/**
	 * Gets the major version number.
	 *
	 * @return	major component of the version contained in the object
	 * @since 0.1.0
	 */
	public Integer getMajor()
	{
		return major;
	}

	/**
	 * Gets the minor version number.
	 *
	 * @return	minor component of the version contained in the object
	 * @since 0.1.0
	 */
	public Integer getMinor()
	{
		return minor;
	}

	/**
	 * Gets the patch number.
	 *
	 * @return	patch number of the version contained in the object,
	 *		or {@code null} if not present
	 * @since 0.1.0
	 */
	public Integer getPatch()
	{
		return patch;
	}

	/**
	 * Gets the pre-release version number.
	 *
	 * @return	prerelease number of the version contained in the object,
	 *		or {@code null} if not present
	 * @since 0.1.0
	 */
	public Integer getPrerelease()
	{
		return prerelease;
	}

	/**
	 * Gets the release candidate number.
	 *
	 * @return	release candidate number of the version contained in the object,
	 *		or {@code null} if not present
	 * @since 0.1.0
	 */
	public Integer getReleaseCandidate()
	{
		return release_candidate;
	}

	/**
	 * Gets the release candidate number, or, if not present,
	 * the value of {@link Integer#MAX_VALUE}.
	 *
	 * <p>This is a workaround that allows easier comparison of pre-releases
	 * and release candidates to their corresponding releases and amongst
	 * each other.
	 *
	 * @return	pre-release number of the version contained in the object,
	 *		if present; or, the sum of the release candidate number
	 *		of the version contained in the object and the constant
	 *		{@link RC_OFFSET}, if release candidate number
	 *		is present; or, {@link PRERC_NULL_VALUE} if none of the
	 *		aforementioned criteria are met
	 * @since 0.1.0
	 */
	protected Integer getPrereleaseOrReleaseCandidate()
	{
		if (prerelease != null) {
			return prerelease;
		} else if (release_candidate != null) {
			return release_candidate + RC_OFFSET;
		}
		return PRERC_NULL_VALUE;
	}

	/**
	 * Gets the major version number of the revision number.
	 *
	 * @return	major component of the revision number of the version
	 *		contained in the object
	 * @since 0.1.0
	 */
	public Integer getRevisionMajor()
	{
		return revision_major;
	}

	/**
	 * Gets the minor version number of the revision number.
	 *
	 * @return	minor component of the revision number of the version
	 *		contained in the object, or {@code null} if not present
	 * @since 0.1.0
	 */
	public Integer getRevisionMinor()
	{
		return revision_minor;
	}

	/**
	 * Builds a vanilla Minecraft version string.
	 *
	 * @return	Minecraft version string congruent with the version
	 *		contained within the object
	 * @since 0.1.0
	 */
	public String toVanillaString()
	{
		return (beta == true ? "b" : "")
			+ major + "." + minor
			+ (patch == null ? "" : "." + patch)
			+ (prerelease == null ? "" : "-pre" + prerelease)
			+ (release_candidate == null ? "" : "-rc" + release_candidate);
	}

	/**
	 * Builds a Bukkit API version string.
	 *
	 * @return	Bukkit API version string congruent with the version
	 *		contained within the object
	 * @since 0.1.0
	 */
	@Override
	public String toString()
	{
		return toVanillaString()
			+ (revision_major == null ? "" : "-R" + revision_major)
			+ (revision_minor == null ? "" : "." + revision_minor)
			+ "-SNAPSHOT";
	}

	/**
	 * Compares a given Bukkit version to that contained in the object.
	 *
	 * @param	compare the object to which a comparison will be made
	 *
	 * @return	{@code -1} if the version provided is newer than that
	 *		contained in the object, {@code 0} if the version
	 *		provided is the same as that contained in the object,
	 *		or {@code 1} if the version provided is older than
	 *		that contained in the object
	 * @since 0.1.0
	 */
	@Override
	public int compareTo(final BukkitVersion compare)
	{
		return compareTo(compare, Component.REVISION_MINOR);
	}

	/**
	 * Compares a given Bukkit version to that contained in the object,
	 * ignoring any quantifiers past that specified.
	 *
	 * @param	compare the object to which a comparison will be made
	 * @param	granularity the least significant component of the
	 *		version to be compared
	 *
	 * @return	ignoring any quantifiers less significant than that
	 *		specified in the {@code granularity} parameter:
	 *		{@code -1} if the version provided is newer than that
	 *		contained in the object, {@code 0} if the version
	 *		provided is the same as that contained in the object,
	 *		or {@code 1} if the version provided is older than
	 *		that contained in the object
	 * @since 0.1.0
	 */
	public int compareTo(final BukkitVersion compare, final Component granularity)
	{
		/* This is the only exception to a release candidate
		 * coming *before* a pre-release, and, unless Microsoft
		 * screw something up as massively as they did with
		 * 1.19.1 for a *second* time, this should be just fine
		 * hardcoded (as opposed to some kind of online lookup) */
		int offset = 0;
		int compare_offset = 0;
		if (compare.toString().matches("^1.19.1(?:-.+)?$")) {
			// -pre1, -rc1, -pre2, -pre3, -pre4, -rc2 …
			if (prerelease > 1) {
				/* Offsets pre-releases other than 1.19.1-pre1
				 * to come after 1.19.1-rc1 */
				offset = RC_OFFSET + 1;
			} else if (release_candidate != null) {
				/* Offsets release candidates other than
				 * 1.19.1-rc1 to come after any practically
				 * possible pre-release numbers */
				// FIXME: doubly arbitrary magic number
				offset = RC_OFFSET + 100;
			}
		}
		if (compare.toString().matches("^1.19.1(?:-.+)?$")) {
			if (compare.getPrerelease() > 1) {
				// Same as above
				compare_offset = RC_OFFSET + 1;
			} else if (compare.getReleaseCandidate() != null) {
				// Same as above
				// FIXME: doubly arbitrary magic number
				compare_offset = RC_OFFSET + 100;
			}
		}

		// XXX: technically correct but extremely nonintuitive
		if (!beta && compare.isBeta()) {
			return 1;
		} else if (beta && !compare.isBeta()) {
			return -1;
		} else if (granularity.compareTo(Component.MAJOR) > 1) {
			/* Same up to this point, and granularity dictates
			 * we not compare past this, so return 0 (same) */
			return 0;
		} else if (major > compare.getMajor()) {
			return 1;
		} else if (major < compare.getMajor()) {
			return -1;
		} else if (granularity.compareTo(Component.MINOR) > 1) {
			/* …and so on */
			return 0;
		} else if (minor > compare.getMinor()) {
			return 1;
		} else if (minor < compare.getMinor()) {
			return -1;
		} else if (granularity.compareTo(Component.PATCH) > 1) {
			return 0;
		} else if (patch > compare.getPatch()) {
			return 1;
		} else if (patch < compare.getPatch()) {
			return -1;
		} else if (granularity.compareTo(Component.PRE_RC) > 1) {
			return 0;
		} else if (getPrereleaseOrReleaseCandidate() + offset >
				compare.getPrereleaseOrReleaseCandidate()
				+ compare_offset) {
			return 1;
		} else if (getPrereleaseOrReleaseCandidate() + offset <
				compare.getPrereleaseOrReleaseCandidate()
				+ compare_offset) {
			return -1;
		} else if (granularity.compareTo(Component.REVISION_MAJOR) > 1) {
			return 0;
		} else if (revision_major > compare.getRevisionMajor()) {
			return 1;
		} else if (revision_major < compare.getRevisionMajor()) {
			return -1;
		} else if (granularity.compareTo(Component.REVISION_MINOR) > 1) {
			return 0;
		} else if (revision_minor > compare.getRevisionMinor()) {
			return 1;
		} else if (revision_minor < compare.getRevisionMinor()) {
			return -1;
		}

		// If none of the above matched, well, they’re the same
		return 0;
	}
}
