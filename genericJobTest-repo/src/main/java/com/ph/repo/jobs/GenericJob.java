package com.ph.repo.jobs;

import de.acosix.alfresco.utility.repo.job.JobUtilities;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericJob implements de.acosix.alfresco.utility.repo.job.GenericJob
{
    private static final String SERVICE_NAMESPACE = "genericJob";

    static final Logger LOGGER = LoggerFactory.getLogger(GenericJob.class);
    
    @Override
    public void execute(Object jobExecutionContext)
    {
        try
        {
            AuthenticationUtil.runAsSystem(() -> {
                JobUtilities.runWithJobLock(jobExecutionContext, QName.createQName(SERVICE_NAMESPACE, getLockName(jobExecutionContext)), (lockReleaseCheck) -> {
                    final TransactionService transactionService = JobUtilities.getJobDataValue(jobExecutionContext, "transactionService", TransactionService.class);
                    final RetryingTransactionHelper retryingTransactionHelper = transactionService.getRetryingTransactionHelper();
                    retryingTransactionHelper.doInTransaction(() -> {
                        doExecute(jobExecutionContext);
                        return null;
                    });
                });
                return null;
            });
        }
        catch (final RuntimeException e)
        {
            if (!(e instanceof LockAcquisitionException))
            {
                LOGGER.warn("Execution failed ...", e);
            }
        }
        catch (final Exception e)
        {
            LOGGER.error("Execution failed ...", e);
        }
    
    }

    public void doExecute(Object jobExecutionContext)
    {
        LOGGER.debug("Executing Generic Job Test");
    }

    public String getLockName(Object jobDataMap)
    {
        return getClass().getSimpleName() + "_" + SERVICE_NAMESPACE;
    }
}
