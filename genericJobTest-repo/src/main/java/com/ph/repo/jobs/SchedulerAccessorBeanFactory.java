package com.ph.repo.jobs;

import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.scheduling.quartz.SchedulerAccessorBean;

import java.lang.reflect.Method;
import java.util.List;

public class SchedulerAccessorBeanFactory extends AbstractFactoryBean<SchedulerAccessorBean>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerAccessorBeanFactory.class);

    private static final String ALFRESCO_SCHEDULER_ACCESSOR_BEAN = "org.alfresco.schedule.AlfrescoSchedulerAccessorBean";
    private static final String SPRING_SCHEDULER_ACCESSOR_BEAN = "org.springframework.scheduling.quartz.SchedulerAccessorBean";

    private static Class<?> schedulerBean;

    private Scheduler scheduler;
    private List<Trigger> triggers;
    private boolean enabled;

    public SchedulerAccessorBeanFactory()
    {
        setSingleton(true);
        
        try
        {
            schedulerBean = Class.forName(ALFRESCO_SCHEDULER_ACCESSOR_BEAN);
            LOGGER.debug("Found class {}", schedulerBean);
        }
        catch(ClassNotFoundException cnfe)
        {
            LOGGER.warn("Cannot find Class {}", ALFRESCO_SCHEDULER_ACCESSOR_BEAN);
        }

        if(schedulerBean == null)
        {
            try
            {
                schedulerBean = Class.forName(SPRING_SCHEDULER_ACCESSOR_BEAN);
                LOGGER.debug("Found class {}", schedulerBean);
            }
            catch(ClassNotFoundException cnfe)
            {
                LOGGER.error("Cannot find Class {}", SPRING_SCHEDULER_ACCESSOR_BEAN, cnfe);
            }
        }
    }

    @Override
    protected SchedulerAccessorBean createInstance() throws Exception
    {
        LOGGER.debug("Creating bean of class {}", schedulerBean);

        Object bean = schedulerBean.newInstance();
        Method setSchedulerMethod = schedulerBean.getMethod("setScheduler", Scheduler.class);
        setSchedulerMethod.invoke(bean, scheduler);

        Method setTriggers = schedulerBean.getMethod("setTriggers", Trigger[].class);
        setTriggers.invoke(bean, new Object[] { triggers.toArray(new Trigger[0]) });

        Method afterPropertiesSet = schedulerBean.getMethod("afterPropertiesSet");
        try
        {
            Method setEnabled = schedulerBean.getMethod("setEnabled", boolean.class);
            setEnabled.invoke(bean, enabled);

            afterPropertiesSet.invoke(bean);
        }
        catch(NoSuchMethodException ignore)
        {
            if(enabled)
                afterPropertiesSet.invoke(bean);
        }

        return (SchedulerAccessorBean) bean;
    }

    @Override
    public Class<?> getObjectType()
    {
        return schedulerBean;
    }

    public Scheduler getScheduler()
    {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public List<Trigger> getTriggers()
    {
        return triggers;
    }

    public void setTriggers(List<Trigger> triggers)
    {
        this.triggers = triggers;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
}