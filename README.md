# example-spark-app


## Scala Streaming Example
Demonstrates reading RawMetricMessage protobuf records from kafka, parsing and writing to stdout.

### Build Instructions (requires sbt tool to be installed)

<code>

cd scala-example \
sbt assembly 
</code>

### Deploy Instructions
Copy the jar file, example-streaming-assembly-0.1.jar onto a SPA VM and then into minio.

<code>
s3cmd put /tmp/example-streaming-assembly-0.1.jar s3://my-bucket/
</code>

Submit the app to the cluster. From inside sparkworker or sparkmaster container, it can be launched this way:

<code>

bin/spark-submit \
--master spark://sparkmaster:7077 \
--deploy-mode cluster \
--driver-memory 1G \
--executor-memory 2G \
--executor-cores 4 \
--total-executor-cores 4 \
--class com.accedian.spa.ExampleStreaming \
--verbose \
--conf spark.ui.reverseProxy=true \
s3a://my-bucket/example-streaming-assembly-0.1.jar test_topic
</code>

Visit the Spark Cluster UI at https://\<deployment-dns>/sparkui to view the status and output of the spark application

# Deploying the rest of the SPA stack

## Prerequisites for GCE
	INSERT GCE PREREQS HERE

## Deploying
- Fill out the env_template.sh file, and rename it to env.sh
- Deploy the solution

	`./deploy.sh <DEPLOYMENT_NAME> pvx create-site`

## Create Your Tenant
	INSERT TENANT CREATION INSTRUCTIONS HERE
	
## [TODO PEYO to complete] Running the connector
- Instructions on how to create connector config since there is a new type which the UI doesn't support yet. They have to create one with an existing type, and then update it through pouch.


- fill in the correct values for:
	- dhHost
	- tenantHost
	- tenantId 
	- zone (obtained whern you created the connector config in the UI) 
