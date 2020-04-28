This is the backend of the GUI running at https://tillerino.github.io/Tillerinobot/.

The frontend code is in [../docs](../docs)

This module runs in its own Docker container consuming updates through RabbitMQ
while pushing updates to the connected Websockets.

Messages that are passed through RabbitMQ are serialized in JSON.
The schema definition is in [RemoteLiveActivity.java](../tillerinobot-rabbit/src/main/java/org/tillerino/ppaddict/rabbit/RemoteLiveActivity.java).
