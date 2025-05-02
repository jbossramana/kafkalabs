Scaling the application:
----------------------------

As the input topic has 2 partitions, we can launch upto 2 instances
of our application in parallel without doing any changes in the code.

[Kafka Streams application relies on KafkaConsumer]


practicals:
1. run script/word-count.bat
2. run 2 instances of the application



Window types include:
------------------------

Stateful Kafka Streams operations also support Windowing. This allows you to scope your stream processing pipelines
to a specific time window/range


1). Tumbling time- Fixed-size, non-overlapping. A record will only be part of one window.
2). Hopping time - Fixed-size, overlapping windows
3). Sliding time - Fixed-size, overlapping windows that work on differences between record timestamps [join operations]
4). Session windows are used to aggregate key-based events into so-called sessions, the process of which is referred to as sessionization


Tumbling time window
------------------------

TimeWindowedKStream<String, String> windowed = 
stream.groupByKey()
      .windowedBy(TimeWindows.of(Duration.ofMinutes(5)));

Hopping time windows 
-------------------------

These windows based on time intervals. They model fixed-sized, (possibly) overlapping windows. 
A hopping window is defined by two properties: the window’s size and its advance interval


long windowSizeMs = TimeUnit.MINUTES.toMillis(5); // 5 * 60 * 1000L
long advanceMs =    TimeUnit.MINUTES.toMillis(1); // 1 * 60 * 1000L
TimeWindows.of(windowSizeMs).advanceBy(advanceMs);

Sliding time 
------------

KStream<String, String> leftSource = builder.stream("my-kafka-left-stream-topic");
  KStream<String, String> rightSource = builder.stream("my-kafka-right-stream-topic");
 
  KStream<String, String> joined = leftSource.join(rightSource,
      (leftValue, rightValue) -> "left=" + leftValue + ", right=" + rightValue, /* ValueJoiner */
      JoinWindows.of(Duration.ofMinutes(5)),
      Joined.with(
          Serdes.String(), /* key */
          Serdes.String(),   /* left value */
          Serdes.String())  /* right value */
  );
 
  joined.to("my-kafka-stream-stream-inner-join-out");
  
  
Session windows
------------------

“session” (the period of activity separated by a defined gap of inactivity)

KStream<String, String> stream = builder.stream(INPUT_TOPIC);
stream.groupByKey()
      .windowedBy(SessionWindows.with(Duration.ofMinutes(5)))
      .toStream().to(OUTPUT_TOPIC);
      







