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

/**
 * The {@code VersionUtil} class is a group of utilities useful for parsing
 * Minecraft version strings.
 * 
 * @author	Seth Price
 * @since	0.1.0
 */
public final class VersionUtil
{
	// disabled
	private VersionUtil() {}

	/**
	 * Converts a Bukkit API version string to a vanilla Minecraft
	 * version string.
	 *
	 * @param	version full Bukkit API version string
	 *
	 * @throws	NullPointerException if {@code version} is null
	 * @throws	IllegalArgumentException if {@code version} is not a
	 *		valid Bukkit API version string
	 * @return	vanilla Minecraft version string
	 * @since 0.1.0
	 */
	public static String convertBukkitToVanilla(final String version)
	{
		return new BukkitVersion(version, true).toVanillaString();
	}
}
