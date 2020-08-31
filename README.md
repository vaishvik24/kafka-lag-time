# kafka-lag-time

# Our Code logic : 
I have also written code in java to find Kafka - lag time. just connecting with influx DB with URL , username , and password. And running query for particular G, T, P. And make point class having offset, timestamp , lag offset, and lag time. Lag Offset can be achieved by diff of legend offset and offset. For a lag time, I m using scala written function estimate and estimating lag time using interpolation logic.



# Kafka Lag Exporter Code Logic : 
 https://github.com/lightbend/kafka-lag-exporter 
 
Main Thread 

1. Creating kafkaClient (consumer + adminClient) – Getting information about Groups, Topics, Partitions, Offsets

2. Creating endPointCreators which are list of kafkaClusterManager – To get lag in time and offset

3. Creating Prometheus instance – To send metrics to prometheus
Metrics that are being collected -

LatestOffsetMetric,
EarliestOffsetMetric,
MaxGroupOffsetLagMetric,
MaxGroupTimeLagMetric,
LastGroupOffsetMetric,
OffsetLagMetric,
TimeLagMetric,
SumGroupOffsetLagMetric,
SumGroupTopicOffsetLagMetric,
PollTimeMetric

4. Initialising KafkaCluserManager by passing parameters kafkaClient, endPointCreators and appConfig. (ActorSystem)
→  Starting kafka lag exporter and calling manger

→  manager has behaviours – clusterAdded, clusterRemoved, Stop, Done.

→  Manager uses consumerGroupCollector which uses collector and collectorState (creates lookup table)

→ collector has diff. Behaviours

Collect offset : 
Collecting current time offset snapshot, and invoking behaviour (2) and (3).

Receiving offset snapshot :
→  Update lookup table (By adding all points (offset,time) from snapshot offset in lookup table )
→  Reports : earlierOffsetMetrics, lastOffsetMetrics, evictedMetrics, consumerGroupMetrics : (lag in time estimate is done here)

Receiving metadata :  reports pollTimeMetrics

Stop : gracefully stopped polling and Kafka client for cluster

Stop with error : throws exception






Kafka Lag Exporter estimates time lag by either interpolation or extrapolation of the timestamp of when the last consumed offset was first produced. We begin by retrieving the source data from Kafka. We poll the last produced offset for all partitions in all consumer groups and store the offset (x) and current time (y) as a coordinate in a table (the interpolation table) for each partition. This information is retrieved as a metadata call using the KafkaConsumer endOffsets call and does not require us to actually poll for messages. The Kafka Consumer Group coordinator will return the last produced offsets for all the partitions we are subscribed to (the set of all partitions of all consumer groups). Similarly, we use the Kafka AdminClient’s listConsumerGroupOffsets API to poll for consumer group metadata from all consumer groups to get the last consumed offset for each partition in a consumer group.
Once we’ve built up an interpolation table of at least two values we can begin estimating time lag by performing the following operations (some edge cases are omitted for clarity) for each last consumed offset of each partition.
Lookup interpolation table for a consumer group partition
Find two points within the table that contain the last consumed offset
If there are no two points that contain the last consumed offset then use the first and last points as input to the interpolation formula. This is the extrapolation use case.
Interpolate inside (or extrapolate outside) the two points from the table we picked to predict a timestamp for when the last consumed message was first produced.
Take the difference of the time of the last consumed offset (~ the current time) and the predicted timestamp to find the time lag.

