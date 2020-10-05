Kafka Lag Exporter estimates time lag by either interpolation or extrapolation of the timestamp of when the last consumed offset was first produced. 
We begin by retrieving the source data from Kafka. 
We poll the last produced offset for all partitions in all consumer groups and store the offset (x) and current time (y) as a coordinate in a table (the interpolation table) for each partition. 
This information is retrieved as a metadata call using the KafkaConsumer endOffsets call and does not require us to actually poll for messages. 
The Kafka Consumer Group coordinator will return the last produced offsets for all the partitions we are subscribed to (the set of all partitions of all consumer groups). 
Similarly, we use the Kafka AdminClient’s listConsumerGroupOffsets API to poll for consumer group metadata from all consumer groups to get the last consumed offset for each partition in a consumer group.
Once we’ve built up an interpolation table of at least two values we can begin estimating time lag by performing the following operations (some edge cases are omitted for clarity) for each last consumed offset of each partition. 
Lookup interpolation table for a consumer group partition Find two points within the table that contain the last consumed offset If there are no two points that contain the last consumed offset then use the first and last points as input to the interpolation formula.
This is the extrapolation use case. Interpolate inside (or extrapolate outside) the two points from the table we picked to predict a timestamp for when the last consumed message was first produced.
Take the difference of the time of the last consumed offset (~ the current time) and the predicted timestamp to find the time lag.
