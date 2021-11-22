package org.demo.batch.job;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.demo.batch.model.TweetDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableBatchProcessing
public class TweetProcessSingleThreadBatchJob {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TweetProcessSingleThreadBatchJob.class);
	
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
	public Job job() {
		return jobBuilderFactory.get("tweet-process-batch-job")
				.start(tweetToCsvStep())
				.build();
	}
	
	@Bean
	public Step tweetToCsvStep() {
		return stepBuilderFactory.get("tweet-process-step")
				.<TweetDto, TweetDto>chunk(10)
				.reader(tweetJsonReader())
				.processor(new PassThroughItemProcessor<TweetDto>())
				.writer(tweetCsvWriter(null))
				.listener((ItemWriteListener<TweetDto>)stepListener())
				.listener((ItemReadListener<TweetDto>)stepListener())
				.listener((ItemProcessListener<TweetDto, TweetDto>)stepListener())
				.build();
	}
       
    @Bean
    @StepScope
    public ItemStreamReader<TweetDto> tweetJsonReader(){
    	LOGGER.info("Input File Name : {}", inputFilePath );
        JsonItemReader<TweetDto> reader = new JsonItemReader<>();
		reader.setResource(new PathResource(inputFilePath));
        reader.setJsonObjectReader(new JacksonJsonObjectReader<>(TweetDto.class));
        return reader;
    }
      
    @Bean(destroyMethod = "")
    @StepScope
    public ItemStreamWriter<TweetDto> tweetCsvWriter(@Value("#{stepExecution.jobExecution.jobId}") Integer jobId){
    	LOGGER.info("Output File Name : {}", outFilePath );
    	FlatFileItemWriter<TweetDto> itemWriter = new FlatFileItemWriter<>();
    	StringBuilder tweetCSVFileName = new StringBuilder(outFilePath).append(File.separator)
    			                            .append("tweet_data_").append(jobId).append(".csv");
    	itemWriter.setResource(new PathResource(tweetCSVFileName.toString()));
    	itemWriter.setAppendAllowed(true);
    	itemWriter.setLineAggregator(new DelimitedLineAggregator<TweetDto>() {
            {
                setDelimiter(",");
                setFieldExtractor(new BeanWrapperFieldExtractor<TweetDto>() {
                    {
                        setNames(new String[] { "id", "text", "lang"});
                    }
                });
            }
        });
    	
    	itemWriter.setHeaderCallback(new TweetCsvHeaderCallback());
    	
    	return itemWriter;
    }
    
	@Bean
	public BatchItemListener stepListener() {
		return new BatchItemListener();
	}
    
	public class TweetCsvHeaderCallback implements FlatFileHeaderCallback {
	    @Override
	    public void writeHeader(Writer writer) throws IOException {
	        writer.write("id, text, lang");
	    }
	}
	
	public class TweetJsonLineMapper implements LineMapper<TweetDto>{
		@Override
		public TweetDto mapLine(String line, int lineNumber) throws Exception {
			return new ObjectMapper().readValue(line, TweetDto.class);
		}
	}
}
