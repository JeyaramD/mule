/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.file.api.command;

import static java.lang.String.format;
import static org.mule.runtime.core.config.i18n.MessageFactory.createStaticMessage;
import org.mule.runtime.api.message.MuleMessage;
import org.mule.runtime.core.api.MuleRuntimeException;
import org.mule.runtime.module.extension.file.api.FileConnectorConfig;
import org.mule.runtime.module.extension.file.api.FileSystem;

import java.nio.file.Path;
import java.util.concurrent.locks.Lock;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for implementations of the Command design pattern which
 * performs operations on a file system
 *
 * @param <C> the generic type of the {@link FileConnectorConfig} which configures the operation
 * @param <F> the generic type of the {@link FileSystem} on which the operation is performed
 * @since 4.0
 */
public abstract class FileCommand<C extends FileConnectorConfig, F extends FileSystem>
{

    private static final Logger LOGGER = LoggerFactory.getLogger(FileCommand.class);

    protected final F fileSystem;
    protected final C config;

    /**
     * Creates a new instance
     *
     * @param fileSystem the {@link FileSystem} on which the operation is performed
     * @param config     the config which configures the operation
     */
    protected FileCommand(F fileSystem, C config)
    {
        this.fileSystem = fileSystem;
        this.config = config;
    }

    /**
     * Returns true if the given {@code path} exists
     *
     * @param path the {@link Path} to test
     * @return whether the {@code path} exists
     */
    protected abstract boolean exists(Path path);

    protected void assureParentFolderExists(Path path, boolean createParentFolder)
    {
        if (exists(path))
        {
            return;
        }

        Lock lock = fileSystem.createMuleLock("file-recursive-mkdir-" + path.toString());
        lock.lock();
        try
        {
            if (exists(path))
            {
                return;
            }

            Path parentFolder = path.getParent();
            if (!exists(parentFolder))
            {
                if (createParentFolder)
                {
                    mkdirs(parentFolder);
                }
                else
                {
                    throw new IllegalArgumentException(format("Cannot write to file '%s' because path to it doesn't exist. Consider setting the 'createParentFolder' attribute to 'true'", path));
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Creates the directory pointed by {@code directoryPath} also creating
     * any missing parent directories
     *
     * @param directoryPath the {@link Path} to the directory you want to create
     */
    protected final void mkdirs(Path directoryPath)
    {
        Lock lock = fileSystem.createMuleLock(String.format("%s-mkdirs-%s", getClass().getName(), directoryPath));
        lock.lock();
        try
        {
            // verify no other thread beat us to it
            if (exists(directoryPath))
            {
                return;
            }
            doMkDirs(directoryPath);
        }
        finally
        {
            lock.unlock();
        }

        LOGGER.debug("Directory '{}' created", directoryPath);
    }

    protected abstract void doMkDirs(Path directoryPath);

    /**
     * Returns an absolute {@link Path} to the given
     * {@code filePath}
     *
     * @param filePath the path to a file or directory
     * @return an absolute {@link Path}
     */
    protected Path resolvePath(String filePath)
    {
        Path path = getBasePath();
        if (filePath != null)
        {
            path = path.resolve(filePath);
        }

        return path.toAbsolutePath();
    }

    /**
     * Returns a {@link Path} to which all non absolute paths are relative to
     *
     * @return a not {@code null} {@link Path}
     */
    protected abstract Path getBasePath();

    /**
     * Similar to {@link #resolvePath(String)} only that it throws a
     * {@link IllegalArgumentException} if the given path doesn't exists.
     * <p>
     * The existence of the obtained path is verified by delegating into
     * {@link #exists(Path)}
     *
     * @param filePath the path to a file or directory
     * @return an absolute {@link Path}
     */
    protected Path resolveExistingPath(String filePath)
    {
        Path path = resolvePath(filePath);
        if (!exists(path))
        {
            throw pathNotFoundException(path);
        }

        return path;
    }

    /**
     * Returns a properly formatted {@link MuleRuntimeException}
     * for the given {@code message} and {@code cause}
     *
     * @param message the exception's message
     * @return a {@link RuntimeException}
     */
    public RuntimeException exception(String message)
    {
        return new MuleRuntimeException(createStaticMessage(message));
    }

    /**
     * Returns a properly formatted {@link MuleRuntimeException}
     * for the given {@code message} and {@code cause}
     *
     * @param message the exception's message
     * @param cause   the exception's cause
     * @return {@link RuntimeException}
     */
    public RuntimeException exception(String message, Exception cause)
    {
        return new MuleRuntimeException(createStaticMessage(message), cause);
    }

    /**
     * @param fileName the name of a file
     * @return {@code true} if {@code fileName} equals to &quot;.&quot; or &quot;..&quot;
     */
    protected boolean isVirtualDirectory(String fileName)
    {
        return ".".equals(fileName) || "..".equals(fileName);
    }


    /**
     * Returns an {@link IllegalArgumentException} explaining that
     * a {@link FileSystem#read(MuleMessage, String, boolean)} operation
     * was attempted on a {@code path} pointing to a directory
     *
     * @param path the {@link Path} on which a read was attempted
     * @return {@link RuntimeException}
     */
    protected RuntimeException cannotReadDirectoryException(Path path)
    {
        return new IllegalArgumentException(format("Cannot read path '%s' since it's a directory", path));
    }

    /**
     * Returns a {@link IllegalArgumentException} explaining that
     * a {@link FileSystem#list(String, boolean, MuleMessage, Predicate)}
     * operation was attempted on a {@code path} pointing to a file.
     *
     * @param path the {@link Path} on which a list was attempted
     * @return {@link RuntimeException}
     */
    protected RuntimeException cannotListFileException(Path path)
    {
        return new IllegalArgumentException(format("Cannot list path '%s' because it's a file. Only directories can be listed", path));
    }

    /**
     * Returns a {@link IllegalArgumentException} explaining that
     * a {@link FileSystem#list(String, boolean, MuleMessage, Predicate)} operation
     * was attempted on a {@code path} pointing to a file.
     *
     * @param path the {@link Path} on which a list was attempted
     * @return {@link RuntimeException}
     */
    protected RuntimeException pathNotFoundException(Path path)
    {
        return new IllegalArgumentException(format("Path '%s' doesn't exists", path));
    }

    /**
     * Returns a {@link IllegalArgumentException} explaining that
     * an operation is trying to write to the given {@code path}
     * but it already exists and no overwrite instruction was provided.
     *
     * @param path the {@link Path} that the operation tried to modify
     * @return {@link RuntimeException}
     */
    public IllegalArgumentException alreadyExistsException(Path path)
    {
        return new IllegalArgumentException(format("'%s' already exists. Set the 'overwrite' parameter to 'true' to perform the operation anyway", path));
    }
}
