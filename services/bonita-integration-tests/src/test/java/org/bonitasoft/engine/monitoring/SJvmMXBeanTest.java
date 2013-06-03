package org.bonitasoft.engine.monitoring;

import static org.junit.Assert.assertNotSame;

import java.util.ArrayList;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.events.model.FireEventException;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.events.model.HandlerUnregistrationException;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.monitoring.mbean.MBeanStartException;
import org.bonitasoft.engine.monitoring.mbean.MBeanStopException;
import org.bonitasoft.engine.monitoring.mbean.SJvmMXBean;
import org.bonitasoft.engine.transaction.SBadTransactionStateException;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.bonitasoft.engine.transaction.STransactionCreationException;
import org.junit.Before;
import org.junit.Test;

public class SJvmMXBeanTest extends CommonServiceTest {

    protected static MBeanServer mbserver = null;

    protected static ObjectName entityMB;

    protected static ObjectName serviceMB;

    protected static ObjectName jvmMB;

    @Before
    public void disableMBeans() throws Exception {
        final ArrayList<MBeanServer> mbservers = MBeanServerFactory.findMBeanServer(null);
        if (mbservers.size() > 0) {
            mbserver = mbservers.get(0);
        }
        if (mbserver == null) {
            mbserver = MBeanServerFactory.createMBeanServer();
        }
        // Constructs the mbean names
        entityMB = new ObjectName(TenantMonitoringService.ENTITY_MBEAN_PREFIX);
        serviceMB = new ObjectName(TenantMonitoringService.SERVICE_MBEAN_PREFIX);
        jvmMB = new ObjectName(PlatformMonitoringService.JVM_MBEAN_NAME);

        unregisterMBeans();
    }

    /**
     * Assure that no Bonitasoft MBeans are registered in the MBServer before each test.
     * 
     * @throws MBeanRegistrationException
     * @throws InstanceNotFoundException
     */
    public void unregisterMBeans() throws MBeanRegistrationException, InstanceNotFoundException {
        if (mbserver.isRegistered(entityMB)) {
            mbserver.unregisterMBean(entityMB);
        }
        if (mbserver.isRegistered(serviceMB)) {
            mbserver.unregisterMBean(serviceMB);
        }
        if (mbserver.isRegistered(jvmMB)) {
            mbserver.unregisterMBean(jvmMB);
        }
    }

    public SJvmMXBean getJvmMXBean() {
        return getServicesBuilder().getJvmMXBean();
    }

    @Test
    public void systemLoadAverageTest() throws STransactionCreationException, SIdentityException, HandlerRegistrationException, SBadTransactionStateException,
            FireEventException, STransactionCommitException, MBeanStartException, HandlerUnregistrationException,
            MBeanStopException, AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
        final SJvmMXBean jvmMXB = getJvmMXBean();
        jvmMXB.start();

        final double usageBefore = (Double) mbserver.getAttribute(jvmMB, "SystemLoadAverage");
        for (int i = 0; i < 4200000; i++) {
            Math.sin((i + 42) * 1000);
        }
        final double usageAfter = (Double) mbserver.getAttribute(jvmMB, "SystemLoadAverage");

        assertNotSame(usageBefore, usageAfter);

        jvmMXB.stop();
    }

    @Test
    public void uptimeTest() throws STransactionCreationException, SIdentityException, HandlerRegistrationException, SBadTransactionStateException,
            FireEventException, STransactionCommitException, MBeanStartException, InterruptedException,
            HandlerUnregistrationException, MBeanStopException, AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
        final SJvmMXBean jvmMXB = getJvmMXBean();
        jvmMXB.start();
        final long uptime1 = (Long) mbserver.getAttribute(jvmMB, "UpTime");
        Thread.sleep(500);
        final long uptime2 = (Long) mbserver.getAttribute(jvmMB, "UpTime");
        assertNotSame(uptime1, uptime2);
        jvmMXB.stop();
    }

    @Test
    public void memoryUsageTest() throws STransactionCreationException, SIdentityException, HandlerRegistrationException, SBadTransactionStateException,
            FireEventException, STransactionCommitException, MBeanStartException, HandlerUnregistrationException,
            MBeanStopException, AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
        final SJvmMXBean jvmMXB = getJvmMXBean();
        jvmMXB.start();
        final long normalMemUsage = (Long) mbserver.getAttribute(jvmMB, "MemoryUsage");
        final ArrayList<Double> testArray = new ArrayList<Double>();
        for (int i = 0; i < 100000; i++) {
            final double x = i * 42;
            testArray.add(x);
        }
        final long stressedMemUsage = (Long) mbserver.getAttribute(jvmMB, "MemoryUsage");
        assertNotSame(normalMemUsage, stressedMemUsage);
        jvmMXB.stop();
    }

    @Test
    public void startTimeTest() throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, InterruptedException,
            HandlerUnregistrationException, MBeanStopException, STransactionCreationException, SIdentityException, HandlerRegistrationException,
            SBadTransactionStateException, FireEventException, STransactionCommitException, MBeanStartException {
        final SJvmMXBean jvmMXB = getJvmMXBean();
        jvmMXB.start();
        final long startTime1 = (Long) mbserver.getAttribute(jvmMB, "StartTime");
        Thread.sleep(500);
        final long startTime2 = (Long) mbserver.getAttribute(jvmMB, "StartTime");
        assertNotSame(startTime1, startTime2);
        jvmMXB.stop();
    }

    @Test
    public void totalThreadCpuTimeTest() throws STransactionCreationException, SIdentityException, HandlerRegistrationException, SBadTransactionStateException,
            FireEventException, STransactionCommitException, MBeanStartException, HandlerUnregistrationException,
            MBeanStopException, AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
        final SJvmMXBean jvmMXB = getJvmMXBean();
        jvmMXB.start();
        final long cpuTime1 = (Long) mbserver.getAttribute(jvmMB, "TotalThreadsCpuTime");
        long cpuTime2 = 0;
        for (int i = 0; i < 4200000; i++) {
            Math.sin((i + 42) * 1000);
            if (i == 4000000) {
                cpuTime2 = (Long) mbserver.getAttribute(jvmMB, "TotalThreadsCpuTime");
            }
        }
        assertNotSame(cpuTime1, cpuTime2);
        jvmMXB.stop();
    }

    @Test
    public void threadCountTest() throws STransactionCreationException, SIdentityException, HandlerRegistrationException, SBadTransactionStateException,
            FireEventException, STransactionCommitException, MBeanStartException, HandlerUnregistrationException,
            MBeanStopException, AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, InterruptedException {
        final SJvmMXBean jvmMXB = getJvmMXBean();
        jvmMXB.start();
        final int threadNumber1 = (Integer) mbserver.getAttribute(jvmMB, "ThreadCount");
        // start some threads
        final Thread t1 = new Thread("Thread 1") {

            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        final Thread t2 = new Thread("Thread 2") {

            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        final Thread t3 = new Thread("Thread 3") {

            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t1.start();
        t2.start();
        t3.start();
        Thread.sleep(1000);
        final int threadNumber2 = (Integer) mbserver.getAttribute(jvmMB, "ThreadCount");
        assertNotSame(threadNumber1, threadNumber2);
        jvmMXB.stop();
    }

}
