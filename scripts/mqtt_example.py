import paho.mqtt.client as mqtt
import paho.mqtt.publish as publish
import time, random

# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))
    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe("$SYS/#")

# Publishes every 5 second a random generated temperature between [-20; 40] to the MQTT Broker
def on_message(client, userdata, msg):
    #print(msg.topic+" "+str(msg.payload))
    temp = random.randint(-20, 40)
    publish.single("temperature", temp, hostname="localhost")
    time.sleep(5)

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

client.connect("localhost", 1883, 60)

# Blocking call that processes network traffic, dispatches callbacks and
# handles reconnecting.
# Other loop*() functions are available that give a threaded interface and a
# manual interface.
client.loop_forever()