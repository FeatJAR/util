/*
 * Copyright (C) 2022 Sebastian Krieter, Elias Kuiter
 *
 * This file is part of util.
 *
 * util is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * util is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with util. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-util> for further information.
 */
package de.featjar.base.bin;

import java.util.Optional;

/**
 * Utilities for host-specific operations and information.
 *
 * @author Elias Kuiter
 */
public class HostEnvironment {
    /**
     * Operating systems distinguished by FeatJAR.
     */
    public enum OperatingSystem {
        WINDOWS, MAC_OS, LINUX, UNKNOWN
    }

    /**
     * The operating system running FeatJAR.
     */
    public static final OperatingSystem OPERATING_SYSTEM;

    static {
        final String osName = System.getProperty("os.name").toLowerCase();
        if (osName.matches(".*(win).*"))
            OPERATING_SYSTEM = OperatingSystem.WINDOWS;
        else if (osName.matches(".*(mac).*"))
            OPERATING_SYSTEM = OperatingSystem.MAC_OS;
        else if (osName.matches(".*(nix|nux|aix).*"))
            OPERATING_SYSTEM = OperatingSystem.LINUX;
        else
            OPERATING_SYSTEM = OperatingSystem.UNKNOWN;
    }

    /**
     * The current user's home directory.
     */
    public static final String HOME_DIRECTORY = System.getProperty("user.home");

    /**
     * FeatJAR's default verbosity.
     *
     */
    public static final String FEATJAR_VERBOSITY = getEnvironmentVariable("FEATJAR_VERBOSITY").orElse("info");

    /**
     * {@return the given environment variable}
     *
     * @param environmentVariable the environment variable
     */
    @SuppressWarnings("SameParameterValue")
    private static Optional<String> getEnvironmentVariable(String environmentVariable) {
        return Optional.ofNullable(System.getenv(environmentVariable));
    }

    /**
     * {@return whether FeatJAR is currently running on Windows}
     */
    public static boolean isWindows() {
        return OPERATING_SYSTEM == OperatingSystem.WINDOWS;
    }

    /**
     * {@return whether FeatJAR is currently running on macOS}
     */
    public static boolean isMacOS() {
        return OPERATING_SYSTEM == OperatingSystem.MAC_OS;
    }

    /**
     * {@return whether FeatJAR is currently running on Linux}
     */
    public static boolean isLinux() {
        return OPERATING_SYSTEM == OperatingSystem.LINUX;
    }
}