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
package de.featjar.util.cli;

import de.featjar.util.data.Result;
import de.featjar.util.extension.Extensions;
import de.featjar.util.io.IO;
import de.featjar.util.io.format.Format;
import de.featjar.util.io.format.FormatSupplier;
import de.featjar.util.log.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Command line interface for several functions of FeatureIDE.
 *
 * @author Sebastian Krieter
 */
public class CLI {
    public static final String DEFAULT_VERBOSITY = "info";
    public static final String SYSTEM_INPUT = "system:in.xml"; //todo
    public static final String SYSTEM_OUTPUT = "system:out";
    public static final String SYSTEM_ERROR = "system:err";
    private static final Pattern SYSTEM_INPUT_PATTERN = Pattern.compile("system:in\\.(.+)");

    public static void main(String[] args) {
        Extensions.install();
        if (args.length == 0) {
            printError("No function specified. Please specify a function as the first argument.");
            return;
        }
        final String functionName = args[0];

        CLIFunctions.getInstance().getExtensions().stream()
                .filter(e -> Objects.equals(functionName, e.getName()))
                .findFirst()
                .ifPresentOrElse(
                        function -> {
                            runFunction(args, function);
                        },
                        () -> {
                            printError("The function " + functionName + " could not be found.");
                        });
    }

    public static void installLogger(String verbosity) {
        String[] verbosities = new String[] {"none", "error", "info", "debug", "progress"};
        if (!Arrays.asList(verbosities).contains(verbosity))
            throw new IllegalArgumentException("invalid verbosity " + verbosity);
        Logger.install(cfg -> {
            if (!verbosity.equals("none")) {
                cfg.logToSystemErr(Logger.MessageType.ERROR);
            }
            switch (verbosity) {
                case "progress":
                    cfg.logToSystemOut(Logger.MessageType.INFO, Logger.MessageType.DEBUG, Logger.MessageType.PROGRESS);
                    break;
                case "debug":
                    cfg.logToSystemOut(Logger.MessageType.INFO, Logger.MessageType.DEBUG);
                    break;
                case "info":
                    cfg.logToSystemOut(Logger.MessageType.INFO);
                    break;
                case "error":
                    cfg.logToSystemOut();
                    break;
            }
        });
    }

    private static void printError(String errorMessage) {
        System.err.println(errorMessage);
        printHelp(System.err);
    }

    private static void runFunction(String[] args, CLIFunction function) {
        try {
            function.run(Arrays.asList(args).subList(1, args.length));
        } catch (final IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println(function.getHelp());
        }
    }

    private static void printHelp(PrintStream printStream) {
        printStream.println("The following functions are available:");
        for (final CLIFunction availableFunction :
                CLIFunctions.getInstance().getExtensions()) {
            printStream.printf("\t%-20s %s\n", availableFunction.getName(), availableFunction.getDescription());
        }
    }

    public static String getArgValue(final Iterator<String> iterator, final String arg) {
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            throw new IllegalArgumentException("No value specified for " + arg);
        }
    }

    public static <T> Optional<T> runInThread(Callable<T> method, long timeout) {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<T> future = executor.submit(method);
        try {
            return timeout == 0 ? future.get() : future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (final TimeoutException e) {
            System.exit(0);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            System.exit(0);
        } finally {
            executor.shutdownNow();
        }
        return null;
    }

    public static boolean isValidInput(String pathOrStdin) {
        return SYSTEM_INPUT_PATTERN.matcher(pathOrStdin).matches() || Files.exists(Paths.get(pathOrStdin));
    }

    public static <T> Result<T> loadFile(String pathOrStdin, FormatSupplier<T> formatSupplier) {
        Matcher matcher = SYSTEM_INPUT_PATTERN.matcher(pathOrStdin);
        if (matcher.matches()) {
            Path path = Paths.get("stdin." + matcher.group(1));
            String content = new BufferedReader(new InputStreamReader(System.in, IO.DEFAULT_CHARSET))
                    .lines()
                    .collect(Collectors.joining("\n"));
            return IO.load(content, path, formatSupplier);
        } else {
            return IO.load(Paths.get(pathOrStdin), formatSupplier);
        }
    }

    public static <T> void saveFile(T object, String pathOrStdout, Format<T> format) {
        try {
            if (pathOrStdout.equals(SYSTEM_OUTPUT)) {
                IO.save(object, System.out, format);
            } else {
                IO.save(object, Paths.get(pathOrStdout), format);
            }
        } catch (final IOException e) {
            Logger.logError(e);
        }
    }
}
