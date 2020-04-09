package com.phy.bcs.service.ifs.ftp.camel.custom;

import org.apache.camel.CamelContext;
import org.apache.camel.Expression;
import org.apache.camel.LoggingLevel;
import org.apache.camel.component.file.GenericFileExclusiveReadLockStrategy;
import org.apache.camel.component.file.GenericFileProcessStrategy;
import org.apache.camel.component.file.remote.strategy.FtpChangedExclusiveReadLockStrategy;
import org.apache.camel.component.file.strategy.*;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.camel.spi.Language;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.net.ftp.FTPFile;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

public final class CustomFtpProcessStrategyFactory {

    private CustomFtpProcessStrategyFactory() {
    }

    public static GenericFileProcessStrategy<FTPFile> createGenericFileProcessStrategy(CamelContext context, Map<String, Object> params) {
        Expression moveExpression = (Expression)params.get("move");
        Expression moveFailedExpression = (Expression)params.get("moveFailed");
        Expression preMoveExpression = (Expression)params.get("preMove");
        boolean isNoop = params.get("noop") != null;
        boolean isDelete = params.get("delete") != null;
        boolean isMove = moveExpression != null || preMoveExpression != null || moveFailedExpression != null;
        GenericFileExpressionRenamer renamer;
        if (isDelete) {
            GenericFileDeleteProcessStrategy<FTPFile> strategy = new GenericFileDeleteProcessStrategy();
            strategy.setExclusiveReadLockStrategy(getExclusiveReadLockStrategy(params));
            if (preMoveExpression != null) {
                renamer = new GenericFileExpressionRenamer();
                renamer.setExpression(preMoveExpression);
                strategy.setBeginRenamer(renamer);
            }

            if (moveFailedExpression != null) {
                renamer = new GenericFileExpressionRenamer();
                renamer.setExpression(moveFailedExpression);
                strategy.setFailureRenamer(renamer);
            }

            return strategy;
        } else if (!isMove && !isNoop) {
            GenericFileNoOpProcessStrategy<FTPFile> strategy = new GenericFileNoOpProcessStrategy();
            strategy.setExclusiveReadLockStrategy(getExclusiveReadLockStrategy(params));
            return strategy;
        } else {
            GenericFileRenameProcessStrategy<FTPFile> strategy = new GenericFileRenameProcessStrategy();
            strategy.setExclusiveReadLockStrategy(getExclusiveReadLockStrategy(params));
            if (!isNoop && moveExpression != null) {
                renamer = new GenericFileExpressionRenamer();
                renamer.setExpression(moveExpression);
                strategy.setCommitRenamer(renamer);
            }

            if (moveFailedExpression != null) {
                renamer = new GenericFileExpressionRenamer();
                renamer.setExpression(moveFailedExpression);
                strategy.setFailureRenamer(renamer);
            }

            if (preMoveExpression != null) {
                renamer = new GenericFileExpressionRenamer();
                renamer.setExpression(preMoveExpression);
                strategy.setBeginRenamer(renamer);
            }

            return strategy;
        }
    }

    private static GenericFileExpressionRenamer<FTPFile> getDefaultCommitRenamer(CamelContext context) {
        // use context to lookup language to let it be loose coupled
        Language language = context.resolveLanguage("file");
        Expression expression = language.createExpression("${file:parent}/.camel/${file:onlyname}");
        return new GenericFileExpressionRenamer<>(expression);
    }

    @SuppressWarnings("unchecked")
    private static GenericFileExclusiveReadLockStrategy<FTPFile> getExclusiveReadLockStrategy(Map<String, Object> params) {
        GenericFileExclusiveReadLockStrategy<FTPFile> strategy = (GenericFileExclusiveReadLockStrategy<FTPFile>) params.get("exclusiveReadLockStrategy");
        if (strategy != null) {
            return strategy;
        }

        // no explicit strategy set then fallback to readLock option
        String readLock = (String) params.get("readLock");
        if (ObjectHelper.isNotEmpty(readLock)) {
            Long timeout;
            Long checkInterval;
            if ("none".equals(readLock) || "false".equals(readLock)) {
                return null;
            }else if ("rename".equals(readLock)) {
                GenericFileRenameExclusiveReadLockStrategy<FTPFile> readLockStrategy = new GenericFileRenameExclusiveReadLockStrategy();
                timeout = (Long)params.get("readLockTimeout");
                if (timeout != null) {
                    readLockStrategy.setTimeout(timeout);
                }

                checkInterval = (Long)params.get("readLockCheckInterval");
                if (checkInterval != null) {
                    readLockStrategy.setCheckInterval(checkInterval);
                }

                Boolean readLockMarkerFile = (Boolean)params.get("readLockMarkerFile");
                if (readLockMarkerFile != null) {
                    readLockStrategy.setMarkerFiler(readLockMarkerFile);
                }

                LoggingLevel readLockLoggingLevel = (LoggingLevel)params.get("readLockLoggingLevel");
                if (readLockLoggingLevel != null) {
                    readLockStrategy.setReadLockLoggingLevel(readLockLoggingLevel);
                }

                return readLockStrategy;
            } else if ("changed".equals(readLock)) {
                FtpChangedExclusiveReadLockStrategy readLockStrategy = new FtpChangedExclusiveReadLockStrategy();
                timeout = (Long)params.get("readLockTimeout");
                if (timeout != null) {
                    readLockStrategy.setTimeout(timeout);
                }

                checkInterval = (Long)params.get("readLockCheckInterval");
                if (checkInterval != null) {
                    readLockStrategy.setCheckInterval(checkInterval);
                }

                Long minLength = (Long)params.get("readLockMinLength");
                if (minLength != null) {
                    readLockStrategy.setMinLength(minLength);
                }

                Long minAge = (Long)params.get("readLockMinAge");
                if (null != minAge) {
                    readLockStrategy.setMinAge(minAge);
                }

                Boolean fastExistsCheck = (Boolean)params.get("fastExistsCheck");
                if (fastExistsCheck != null) {
                    readLockStrategy.setFastExistsCheck(fastExistsCheck);
                }

                Boolean readLockMarkerFile = (Boolean)params.get("readLockMarkerFile");
                if (readLockMarkerFile != null) {
                    readLockStrategy.setMarkerFiler(readLockMarkerFile);
                }

                LoggingLevel readLockLoggingLevel = (LoggingLevel)params.get("readLockLoggingLevel");
                if (readLockLoggingLevel != null) {
                    readLockStrategy.setReadLockLoggingLevel(readLockLoggingLevel);
                }

                return readLockStrategy;
            } else if ("idempotent".equals(readLock)) {
                CustomFtpIdempotentRepositoryReadLockStrategy readLockStrategy = new CustomFtpIdempotentRepositoryReadLockStrategy();
                Boolean readLockRemoveOnRollback = (Boolean) params.get("readLockRemoveOnRollback");
                if (readLockRemoveOnRollback != null) {
                    readLockStrategy.setRemoveOnRollback(readLockRemoveOnRollback);
                }
                Boolean readLockRemoveOnCommit = (Boolean) params.get("readLockRemoveOnCommit");
                if (readLockRemoveOnCommit != null) {
                    readLockStrategy.setRemoveOnCommit(readLockRemoveOnCommit);
                }
                IdempotentRepository repo = (IdempotentRepository) params.get("readLockIdempotentRepository");
                if (repo != null) {
                    readLockStrategy.setIdempotentRepository(repo);
                }
                Integer readLockIdempotentReleaseDelay = (Integer) params.get("readLockIdempotentReleaseDelay");
                if (readLockIdempotentReleaseDelay != null) {
                    readLockStrategy.setReadLockIdempotentReleaseDelay(readLockIdempotentReleaseDelay);
                }
                Boolean readLockIdempotentReleaseAsync = (Boolean) params.get("readLockIdempotentReleaseAsync");
                if (readLockIdempotentReleaseAsync != null) {
                    readLockStrategy.setReadLockIdempotentReleaseAsync(readLockIdempotentReleaseAsync);
                }
                Integer readLockIdempotentReleaseAsyncPoolSize = (Integer) params.get("readLockIdempotentReleaseAsyncPoolSize");
                if (readLockIdempotentReleaseAsyncPoolSize != null) {
                    readLockStrategy.setReadLockIdempotentReleaseAsyncPoolSize(readLockIdempotentReleaseAsyncPoolSize);
                }
                ScheduledExecutorService readLockIdempotentReleaseExecutorService = (ScheduledExecutorService) params.get("readLockIdempotentReleaseExecutorService");
                if (readLockIdempotentReleaseExecutorService != null) {
                    readLockStrategy.setReadLockIdempotentReleaseExecutorService(readLockIdempotentReleaseExecutorService);
                }
                strategy = readLockStrategy;
            }

            if (strategy != null) {
                timeout = (Long) params.get("readLockTimeout");
                if (timeout != null) {
                    strategy.setTimeout(timeout);
                }
                checkInterval = (Long) params.get("readLockCheckInterval");
                if (checkInterval != null) {
                    strategy.setCheckInterval(checkInterval);
                }
                LoggingLevel readLockLoggingLevel = (LoggingLevel) params.get("readLockLoggingLevel");
                if (readLockLoggingLevel != null) {
                    strategy.setReadLockLoggingLevel(readLockLoggingLevel);
                }
                Boolean readLockMarkerFile = (Boolean) params.get("readLockMarkerFile");
                if (readLockMarkerFile != null) {
                    strategy.setMarkerFiler(readLockMarkerFile);
                }
                Boolean readLockDeleteOrphanLockFiles = (Boolean) params.get("readLockDeleteOrphanLockFiles");
                if (readLockDeleteOrphanLockFiles != null) {
                    strategy.setDeleteOrphanLockFiles(readLockDeleteOrphanLockFiles);
                }
            }
        }

        return strategy;
    }
}
