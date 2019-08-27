package com.accedian.spa

import fedexgrpc.fedex_grpc.{RawMetricMessage, SDMMeta}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Milliseconds, StreamingContext}

object ExampleStreaming extends Logging {

  // For rate-limiting on the consumer
  val maxRatePerPartitionPerSec = 10
  val sparkBatchInterval = Milliseconds(15000)
  val kafkaServer = "kafka:9092"
  val consumerGroupId = "example-streaming"
  var ssc: StreamingContext = _
  val defaultInputTopic = "spark_raw_in"

  def main(args: Array[String]) {
    val spark = SparkSession
      .builder()
      .config("spark.cleaner.referenceTracking.cleanCheckpoints", "true")
      .config("spark.streaming.kafka.maxRatePerPartition", maxRatePerPartitionPerSec)
      .config("spark.streaming.kafka.consumer.cache.enabled", "false")
      .getOrCreate()

    logInfo("Spark Session created")
    ssc = new StreamingContext(spark.sparkContext, sparkBatchInterval)
    sys.addShutdownHook(doShutdown())


    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> kafkaServer,
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[ByteArrayDeserializer],
      "group.id" -> consumerGroupId
    )

    val inputTopic = if(args.isEmpty) {
      defaultInputTopic
    } else {
      args(0)
    }

    logInfo(s"Streaming from topic $inputTopic")

    val inputStream: DStream[ConsumerRecord[String, Array[Byte]]] = KafkaUtils.createDirectStream[String, Array[Byte]](
      ssc,
      PreferConsistent,
      Subscribe[String, Array[Byte]](Set(inputTopic), kafkaParams))

    inputStream
      // Parse the protobuf, extracting the header and data
      .map(
      r => ExampleStreaming.CSVRecord.parse(r.value())
    )
      // Print to stdout
      .foreachRDD(
      rdd => rdd.foreach(printContents)
    )

    logInfo("Starting streaming")
    ssc.start()

    ssc.awaitTermination

    logInfo("Stopped spark streaming context")

  }

  private def printContents(record: CSVRecord): Unit = {
    record.header match {
      case Some(h) => println(h.mkString(","))
      case _ =>
    }
    record.lines.split('\n').foreach(println)
  }


  private def doShutdown(): Unit = {
    try {
      if (ssc != null) {
        ssc.stop(stopSparkContext = true, stopGracefully = true)
      }
    }
    catch {
      case ex: Throwable => logError(s"Got unexpected error while stopping streaming context $ex")
    }
    logInfo("Application stopped")
  }


  case class CSVRecord(header: Option[Seq[String]], lines: String)

  object CSVRecord {
    def parse(bytes: Array[Byte]): CSVRecord = {
      val rawMsg = RawMetricMessage.parseFrom(bytes)
      val headers = rawMsg.metadata match {
        case Some(m) if rawMsg.metadata.get.is(SDMMeta) =>
          // Extract the ordered list of header names
          Some(m.unpack(SDMMeta).headers)
        case _ =>
          // No header info
          None
      }
      CSVRecord(headers, rawMsg.payload.toStringUtf8)
    }
  }

}
