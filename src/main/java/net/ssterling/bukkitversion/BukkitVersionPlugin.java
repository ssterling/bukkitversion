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

import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;

/**
 * The {@code BukkitVersionPlugin} class initialises a
 * {@link org.bukkit.plugin.java.JavaPlugin} to expose the BukkitVersion API
 * to other plugins running on a Bukkit server.
 * This is a common alternative to including an API directly in a project
 * in typical fashion.
 *
 * <p>For Maven users: change the dependency scope for BukkitVersion in your
 * plugin’s {@code pom.xml}:
 * <pre><code class="xml">&lt;scope&gt;provided&lt;/scope&gt;</code></pre>
 * Also, add the following to your plugin’s {@code plugin.yml}:
 * <pre><code class="yaml">depend: [BukkitVersion]</code></pre>
 * You can now use the BukkitVersion API without including the entire package
 * within your plugin JAR; however, you must instruct users of your plugin
 * to download the BukkitVersion jar from SpigotMC.
 *
 * @author	Seth Price
 * @since	0.2.0
 */
public class BukkitVersionPlugin extends JavaPlugin
{
	/** bStats plugin ID (for metrics). */
	private static final int BSTATS_ID = 15810;

	/** Spigot project ID (for update checker). */
	private static final String PROJECT_ID = "55295";

	/** Metrics object for bStats. */
	private static Metrics metrics;

	/**
	 * Loads bStats metrics.
	 * No, really, that’s it.
	 */
	@Override
	public void onEnable()
	{
		try {
			metrics = new Metrics(this, BSTATS_ID);
		} catch (UnsupportedClassVersionError ex) {
			// Bizarre edge case, but possible on old (nostalgia) servers
			getLogger().warning("Cannot load bStats metrics class due to outdated JRE: " + System.getProperty("java.version"));
		}
	}
}
