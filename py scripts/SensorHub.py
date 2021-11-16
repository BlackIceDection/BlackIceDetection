import smbus
import sys
import paho.mqtt.client as mqtt
import paho.mqtt.publish as publish
import time
from pathlib import Path

DEVICE_BUS = 1
DEVICE_ADDR = 0x17
TEMP_REG = 0x01
LIGHT_REG_L = 0x02
LIGHT_REG_H = 0x03
STATUS_REG = 0x04
ON_BOARD_TEMP_REG = 0x05
ON_BOARD_HUMIDITY_REG = 0x06
ON_BOARD_SENSOR_ERROR = 0x07
BMP280_TEMP_REG = 0x08
BMP280_PRESSURE_REG_L = 0x09
BMP280_PRESSURE_REG_M = 0x0A
BMP280_PRESSURE_REG_H = 0x0B
BMP280_STATUS = 0x0C
HUMAN_DETECT = 0x0D

def readData():
    sys.stdout = open('output.txt', 'w')
    bus = smbus.SMBus(DEVICE_BUS)
    aReceiveBuf = []
    aReceiveBuf.append(0x00)

    for i in range(TEMP_REG, HUMAN_DETECT + 1):
        aReceiveBuf.append(bus.read_byte_data(DEVICE_ADDR, i))
    if aReceiveBuf[STATUS_REG] & 0x01:
        print("Off-chip temperature sensor overrange!")
    elif aReceiveBuf[STATUS_REG] & 0x02:
        print("No external temperature sensor!")
    else:
        print("Current off-chip sensor temperature = %d Celsius" %
              aReceiveBuf[TEMP_REG])
    if aReceiveBuf[STATUS_REG] & 0x04:
        print("Onboard brightness sensor overrange!")
    elif aReceiveBuf[STATUS_REG] & 0x08:
        print("Onboard brightness sensor failure!")
    else:
        print("Current onboard sensor brightness = %d Lux" %
              (aReceiveBuf[LIGHT_REG_H] << 8 | aReceiveBuf[LIGHT_REG_L]))
    print("Current onboard sensor temperature = %d Celsius" %
          aReceiveBuf[ON_BOARD_TEMP_REG])
    print("Current onboard sensor humidity = %d %%" %
          aReceiveBuf[ON_BOARD_HUMIDITY_REG])
    if aReceiveBuf[ON_BOARD_SENSOR_ERROR] != 0:
        print("Onboard temperature and humidity sensor data may not be up to date!")
    if aReceiveBuf[BMP280_STATUS] == 0:
        print("Current barometer temperature = %d Celsius" %
              aReceiveBuf[BMP280_TEMP_REG])
        print("Current barometer pressure = %d pascal" % (
            aReceiveBuf[BMP280_PRESSURE_REG_L] | aReceiveBuf[BMP280_PRESSURE_REG_M] << 8 | aReceiveBuf[BMP280_PRESSURE_REG_H] << 16))
    else:
        print("Onboard barometer works abnormally!")
    if aReceiveBuf[HUMAN_DETECT] == 1:
        print("Live body detected within 5 seconds!")
    else:
        print("No humans detected!")
    sys.stdout = sys.__stdout__

# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))
    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe("$SYS/#")


# Publishes every 10 second the Data read from the sensors
def on_message(client, userdata, msg):
    #print(msg.topic+" "+str(msg.payload))
    readData()
    txt = open('output.txt').read()
    print(txt)
    publish.single("temperature", txt, hostname="192.168.43.4")
    time.sleep(10)

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message
client.connect("192.168.43.4", 1883, 60)

# Blocking call that processes network traffic, dispatches callbacks and
# handles reconnecting.
# Other loop*() functions are available that give a threaded interface and a
# manual interface.
client.loop_forever()
