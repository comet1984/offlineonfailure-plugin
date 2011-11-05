package com.polopoly.jenkins.plugin.offlineonfailure;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import hudson.tasks.Builder;
import hudson.tasks.Shell;

import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.UnstableBuilder;

public class OfflineOnFailureTest
    extends HudsonTestCase
{
    private static final Builder FAILURE_SHELL_BUILDER = new Shell("exit 1");
    private static final Builder SUCCESS_SHELL_BUILDER = new Shell("exit 0");
    private static final Builder UNSTABLE_BUILDER = new UnstableBuilder();

    private static final OfflineOnFailurePublisher OFFLINE_UNSTABLE_PUBLISHER = 
    		new OfflineOnFailurePublisher(true,false);
    private static final OfflineOnFailurePublisher OFFLINE_FAILURE_PUBLISHER = 
    		new OfflineOnFailurePublisher(false,true);
    private static final OfflineOnFailurePublisher OFFLINE_BOTH_PUBLISHER = 
    		new OfflineOnFailurePublisher(true,true);

    
    public void test_node_is_not_taken_offline_on_success()
        throws Exception
    {
        performBuildAndAssertNodeStatus(SUCCESS_SHELL_BUILDER, "not_master", false,OFFLINE_BOTH_PUBLISHER);
    }

    public void test_node_is_not_taken_offline_on_failure_for_unstable_publisher()
        throws Exception
    {
        performBuildAndAssertNodeStatus(FAILURE_SHELL_BUILDER, "not_master", false,OFFLINE_UNSTABLE_PUBLISHER);
    }

    public void test_node_is_taken_offline_on_unstable_for_unstable_publisher()
            throws Exception
    {
        performBuildAndAssertNodeStatus(UNSTABLE_BUILDER, "not_master", true,OFFLINE_UNSTABLE_PUBLISHER);
    }

    public void test_node_is_not_taken_offline_on_unstable_for_failure_publisher()
            throws Exception
    {
        performBuildAndAssertNodeStatus(UNSTABLE_BUILDER, "not_master", false,OFFLINE_FAILURE_PUBLISHER);
    }
    
    public void test_node_is_taken_offline_on_failure_for_failure_publisher()
            throws Exception
    {
        performBuildAndAssertNodeStatus(FAILURE_SHELL_BUILDER, "not_master", true,OFFLINE_FAILURE_PUBLISHER);
    }

    public void test_master_node_is_not_taken_offline_on_failure()
        throws Exception
    {
        performBuildAndAssertNodeStatus(FAILURE_SHELL_BUILDER, "master", false,OFFLINE_BOTH_PUBLISHER);
    }

            
    private void performBuildAndAssertNodeStatus(final Builder builder,
                                                 final String label,
                                                 final boolean nodeIsExpectedToBeTakenOffline,
                                                 OfflineOnFailurePublisher publisher
                                                 )
        throws Exception
    {
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
 
        project.getPublishersList().add(publisher);

        if (label != "master") {
            createOnlineSlave(Label.get("not_master"));
        }
        project.setAssignedLabel(Label.get(label));

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertEquals("Node state after build was not the expected one!",
                     nodeIsExpectedToBeTakenOffline,
                     build.getBuiltOn().toComputer().isOffline());
    }
}
