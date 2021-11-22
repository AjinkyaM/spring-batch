package org.demo.batch.job;

import java.util.List;

import org.demo.batch.model.TweetDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.ItemListenerSupport;

public class BatchItemListener extends ItemListenerSupport<TweetDto, TweetDto> {
	private static final Logger LOGGER = LoggerFactory.getLogger(BatchItemListener.class);

	@Override
    public void onReadError(Exception ex) {
    	LOGGER.info("Encountered error on read", ex);
    }
	
	@Override
	public void beforeWrite(List<? extends TweetDto> itemList) {
		LOGGER.info("beforeWrite Thread Name : {} , list size : {} ", Thread.currentThread().getName(), itemList.size());
	}
	
	@Override
	public void afterRead(TweetDto TweetDto) {
		LOGGER.info("afterRead Thread Name : {} ", Thread.currentThread().getName());
	}

    @Override
    public void onWriteError(Exception ex, List<? extends TweetDto> items) {
    	LOGGER.info("onWriteError Thread Name : {} ", Thread.currentThread().getName());
    	
    }

	@Override
	public void afterWrite(List<? extends TweetDto> items) {
		LOGGER.info("afterWrite Thread Name : {} ", Thread.currentThread().getName());
	}

	@Override
	public void beforeProcess(TweetDto item) {
		LOGGER.info("beforeProcess Thread Name : {} ", Thread.currentThread().getName());
	}

	@Override
	public void afterProcess(TweetDto item, TweetDto result) {
		LOGGER.info("afterProcess Thread Name : {} ", Thread.currentThread().getName());
	}

	@Override
	public void onProcessError(TweetDto item, Exception e) {
		LOGGER.info("onProcessError Thread Name : {} ", Thread.currentThread().getName());
	}

	@Override
	public void beforeRead() {
		LOGGER.info("beforeRead Thread Name : {} ", Thread.currentThread().getName());
	}
}
