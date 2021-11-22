package org.demo.batch;

import java.io.File;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReadFilePartitionerBatchTest {
	
	@Value("${input-file-path}")
	private String inputFilePath;
	
	@Value("${output-file-path}")
	private String outFilePath;
	
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    
    @BeforeAll
    public void setUp() {
    	Arrays.stream(new File(outFilePath).listFiles()).forEach(File::delete);
    }

    @Test
    public void testMyJob() throws Exception {
       // when
       JobExecution jobExecution = this.jobLauncherTestUtils.launchJob();

       // then
       Assert.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
    }
    
    @Test
    public void testOutPutFile() {
    	
        String FileNameArray[] = new File(outFilePath).list();
        
        Assert.assertNotNull(FileNameArray);
        
        Assert.assertEquals(FileNameArray.length,5);
        
        Assert.assertEquals(FileNameArray[0], "output1.csv");
    }
    
    @AfterAll
    public void cleanUp() {
    	Arrays.stream(new File(outFilePath).listFiles()).forEach(File::delete);
    }
}
