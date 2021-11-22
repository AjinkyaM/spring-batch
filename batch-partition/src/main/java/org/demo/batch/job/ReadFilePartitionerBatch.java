package org.demo.batch.job;

import java.io.File;
import java.io.IOException;

import org.demo.batch.model.User;
import org.demo.batch.service.RecordFieldSetMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

@Configuration
@EnableBatchProcessing
public class ReadFilePartitionerBatch {
	
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    
    @Autowired
    ResourcePatternResolver resoursePatternResolver;
    
	@Value("${input-file-path}")
	private String inputFilePath;
	
	@Value("${output-file-path}")
	private String outFilePath;
    
    @Bean
    public Job PartitionerJob() {
    	return jobBuilderFactory
    			.get("partitioningJob")
    			.start(partitionStep())
    			.build();
    }
    
    @Bean
    public Step partitionStep() {
    	return stepBuilderFactory
    			.get("partitionStep")
    			.partitioner("leaderStep", partitioner())
    			.step(followerStep())
    			.build();
    }
    
    @Bean
    public Step followerStep() {
    	return stepBuilderFactory
    			.get("followerStep")
    			.<User,User>chunk(1)
    			.reader(itemReader(null))
    			.writer(itemWriter(null))
    			.build();
    }
    
    @Bean
    @StepScope
    public FlatFileItemReader<User> itemReader(@Value("#{stepExecutionContext[fileName]}") String filename){
        FlatFileItemReader<User> reader = new FlatFileItemReader<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        String[] tokens = {"username", "userid", "transactiondate", "amount"};
        tokenizer.setNames(tokens);
        reader.setResource(new ClassPathResource("input/partitioner/" + filename));
        DefaultLineMapper<User> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new RecordFieldSetMapper());
        reader.setLinesToSkip(1);
        reader.setLineMapper(lineMapper);
        return reader;
    }
    
    @Bean(destroyMethod = "")
    @StepScope
    public FlatFileItemWriter<User> itemWriter(@Value("#{stepExecutionContext[opFileName]}") String filename){
    	FlatFileItemWriter<User> itemWriter = new FlatFileItemWriter<>();
    	StringBuilder tweetCSVFileName = new StringBuilder(outFilePath).append(File.separator).append(filename);
    	itemWriter.setResource(new PathResource(tweetCSVFileName.toString()));
    	
    	itemWriter.setAppendAllowed(true);
    	itemWriter.setLineAggregator(new DelimitedLineAggregator<User>() {
            {
                setDelimiter(",");
                setFieldExtractor(new BeanWrapperFieldExtractor<User>() {
                    {
                        setNames(new String[] { "username", "userId", "transactionDate", "amount" });
                    }
                });
            }
        });
    	return itemWriter;
    }
    
    @Bean
    public CustomMultiResourcePartitioner partitioner() {
    	 CustomMultiResourcePartitioner partitioner = new CustomMultiResourcePartitioner();
         Resource[] resources;
         try {
        	resources = resoursePatternResolver.getResources("file:"+inputFilePath+"/*.csv");
         } catch (IOException e) {
             throw new RuntimeException("IOException when resolving the input file pattern.", e);
         }
         
         partitioner.setResources(resources);
         return partitioner;
    }

}
