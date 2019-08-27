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