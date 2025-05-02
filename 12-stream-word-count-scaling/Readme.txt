Create a input with two partitions:

Start two instances of Kafka stream application.

What will happen?

Running two instances of the program means that each instance will consume from one partition of the input topic. The word counts 
for each partition will be aggregated separately, and the results will be written to the output topic.

How to get the aggregate word count across both partitions?

Add the below code before sending to output topic:

// create a new KTable that aggregates the counts for each word across both partitions
KTable<String, Long> totalWordCounts = wordCounts
  .groupBy((key, value) -> KeyValue.pair("total", value), Serialized.with(Serdes.String(), Serdes.Long()))
  .reduce((aggValue, newValue) -> aggValue + newValue, Materialized.with(Serdes.String(), Serdes.Long()));



We need to create a new KTable totalWordCounts that groups all of the word counts into a single partition, using a dummy key "total". We then use the reduce() 
method to sum the counts for each word across both partitions.



