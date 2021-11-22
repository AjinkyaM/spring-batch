package org.demo.batch.job;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.Resource;

public class CustomMultiResourcePartitioner implements Partitioner {
	
    private static final String DEFAULT_KEY_NAME = "fileName";

    private static final String PARTITION_KEY = "partition";
    
    private String keyName = DEFAULT_KEY_NAME;
    
	private Resource[] resources;
	
    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }
    
    public void setResources(Resource[] resources) {
        this.resources = resources;
    }

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		Map<String, ExecutionContext> map = new HashMap<>(gridSize);
		
		int i = 0, k = 1;
        for (Resource resource : resources) {
        	ExecutionContext context = new ExecutionContext();
        	
        	context.putString(keyName, resource.getFilename());
        	context.putString("opFileName", "output" + k++ + ".csv");
        	
        	map.put(PARTITION_KEY + i, context);
            i++;
        }
        
		return map;
	}
	
	

}
